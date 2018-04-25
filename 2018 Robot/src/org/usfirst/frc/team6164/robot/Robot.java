/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/


// github pull test


package org.usfirst.frc.team6164.robot;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import autoStrats.CrossLineOnly;
import autoStrats.NoMove;
import autoStrats.PreloadedToScale;
import autoStrats.PreloadedToSwitch;


public class Robot extends TimedRobot {
	
	//constants
	public static int position;									//Default start position (middle)
	public static int autoDelay =0;
	public static int gameDataTimeout = 10;							//if gamedata returns null, wait 100ms and retry this many times
	static final double GEAR_RATIO = 10.71;							//to 1
	static final double WHEEL_R= 0.0762; 			 				//Radius of wheels in Meters
	static final float WHEEL_C= (float)(2*(Math.PI)*WHEEL_R);		//Circumference of wheels in meters
	static final double BOT_DIAMETER= 0.594; 						//distance from middle left wheel to middle right wheel in meters.
	String gameData = null;			//game data passed to the driver station from FMS (for 2018 the ownership of switch/scale)
	

	//speed controllers
	static VictorSP backLeft = new VictorSP(3);
	static VictorSP frontLeft = new VictorSP(2);
	static SpeedControllerGroup leftSide  = new SpeedControllerGroup(frontLeft , backLeft);				
	static VictorSP frontRight = new VictorSP(0);
	static VictorSP backRight = new VictorSP(1);
	static SpeedControllerGroup rightSide  = new SpeedControllerGroup(frontRight , backRight); 	
	static DifferentialDrive robotDrive = new DifferentialDrive(leftSide,rightSide);

	static VictorSP gripper = new VictorSP(4);
	static Talon frontWinch = new Talon(5);								//winch for main lift
	static Talon backWinch = new Talon(6);								//winch to lift entire bot

	
	//sensors
	static Encoder leftEncoder = new Encoder(0,1);
	static Encoder rightEncoder = new Encoder(2,3);
	static DigitalInput gripperCloseSwitch = new DigitalInput(4);
	static DigitalInput gripperOpenSwitch = new DigitalInput(5); 
	
	
	//Controllers & Adjustments						1= full sensitivity 0= no movement
	Joystick driveStick = new Joystick(0);
	Joystick liftStick = new Joystick(1);
	
	double liftDownTrim = 0.5;						// adjust speed at which the lift lowers
	double rotationTrim = 0.5;						// adjust speed of rotation during teleOp
	double liftMode = 0.5;							// mode of the lift speed. default to slow (50%, 0.5) can be changed while drive to fast (100%, 1)
	static double autoRotTrim = .02;				// changes the speed of one wheel to make fine adjustments to auto rotation
	public static double autoRotSpeed = 0.3;		// speed at which bot rotates for auto
	static double autoDriveTrim = 0.01;				// subtracts this from the speed of the right wheel to correct drift while driving straight in auto
	
	//LEDs
	DigitalOutput ledStrip1 = new DigitalOutput(7);
	DigitalOutput ledStrip2 = new DigitalOutput(8);
	boolean ledStatus = true;
	static int LedCycleNum = 0;

	//auto Strat selection
	Command autonomousCommand;
	SendableChooser<Command> autoChooser;
	
	//position selection
	public Integer positionCommand;
	public static SendableChooser<Integer> positionChooser;
	
	//autonomous delay selection
	public Integer autoDelayCommand;
	public static SendableChooser<Integer> autoDelayChooser;
	
	public static OI m_oi;

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void robotInit() {
		m_oi = new OI();
		
	//user interface setup
		CameraServer.getInstance().startAutomaticCapture("cam0", 0);
		CameraServer.getInstance().startAutomaticCapture("cam1", 1);
		
		autoChooser = new SendableChooser<Command>();
		positionChooser = new SendableChooser<Integer>();
		autoDelayChooser = new SendableChooser<Integer>();
		
		autoChooser.addDefault("(default)Cross Line Only", new CrossLineOnly());
		autoChooser.addObject("(PreLoaded)To Switch", new PreloadedToSwitch());
		autoChooser.addObject("(PreLoaded)To Scale", new PreloadedToScale());
		autoChooser.addObject("Don't move", new NoMove());
		SmartDashboard.putData("Auto mode", autoChooser);
		
		positionChooser.addDefault("middle (2)", 2);
		positionChooser.addObject("left (1)", 1);
		positionChooser.addObject("right (3)", 3);
		SmartDashboard.putData("Position",positionChooser);
		position = positionChooser.getSelected();

		autoDelayChooser.addDefault("0", 0);
		autoDelayChooser.addObject("1", 1);
		autoDelayChooser.addObject("2", 2);
		autoDelayChooser.addObject("3", 3);
		autoDelayChooser.addObject("4", 4);
		autoDelayChooser.addObject("5", 5);
		SmartDashboard.putData("Auto Delay", autoDelayChooser);
		autoDelay = autoDelayChooser.getSelected();
		
	//LEDs
		ledStrip1.set(true);
		ledStrip2.set(true);
		ledStatus = true;
	
	//speed controller setup/reset
		leftSide.setInverted(true);
		rightSide.setInverted(true);
		robotDrive.setSafetyEnabled(false);
		
	//Encoder setup/reset
		leftEncoder.reset();
		rightEncoder.reset();		
		rightEncoder.setReverseDirection(true);
		leftEncoder.setReverseDirection(true);
		leftEncoder.setDistancePerPulse((WHEEL_C/20)/GEAR_RATIO);						//distance per pulse, 20 pulses per rotation of motor
		rightEncoder.setDistancePerPulse((WHEEL_C/20)/GEAR_RATIO);
	}

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void disabledInit() {			
		leftEncoder.reset();
		rightEncoder.reset();
		LedCycleNum=0;
		liftMode=0.5;
		ledStrip1.set(true);
		ledStrip2.set(true);
		ledStatus = true;
		
		System.out.println("ROBOT HAS BEEN DISABLED");
	}

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
	}

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void autonomousInit() {
		autonomousCommand = autoChooser.getSelected();
		position= positionChooser.getSelected();
		autoDelay= autoDelayChooser.getSelected();

		if (autonomousCommand != null) {
			autonomousCommand.start();
		}
	}

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void autonomousPeriodic() {
		Scheduler.getInstance().run();
	}

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void teleopInit() {
	
		LedCycleNum=0;
		leftEncoder.reset();
		rightEncoder.reset();
		
		if (autonomousCommand != null) {
			autonomousCommand.cancel();
		}
	}

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void teleopPeriodic() {
	//create and update buttons
		boolean liftFineBut = liftStick.getRawButton(9);
		boolean liftCourseBut = liftStick.getRawButton(10);
		boolean liftUp = liftStick.getRawButton(4);
		boolean liftDown = liftStick.getRawButton(2);
		boolean gripOpen = liftStick.getRawButton(8);
		boolean gripClose = liftStick.getRawButton(7);
		boolean backWinchBut = driveStick.getRawButton(3);
		boolean openLimit = gripperOpenSwitch.get();
		boolean closeLimit = gripperCloseSwitch.get();
		
		//back winch control
				if (backWinchBut==true){						//back winch spin
					backWinch.set(-.5);
					}
				else{
					backWinch.set(0);   						//back winch idle		
				}
				
		robotDrive.arcadeDrive(driveStick.getRawAxis(1)*.5, (driveStick.getRawAxis(2)*-1)*rotationTrim);		//main stick drive
		
		System.out.print("left encoder: "+ leftEncoder.getDistance());
		System.out.print("Right encoder: "+ rightEncoder.getDistance());
		
		System.out.print("left encoder modified: "+ getDistanceLeft(leftEncoder));
		System.out.print("Right encoder modified: "+ getDistanceRight(rightEncoder));

	//lift mode set
		if (liftFineBut==true){							//fine control of lift
			liftMode=0.5;
			System.out.println("lift fine");}
		if (liftCourseBut==true){						//course control of lift
			liftMode=1;
			System.out.println("lift course");}
		
	//lift movement control
		if (liftUp==true){								//move lift up
			frontWinch.set(liftMode);
			System.out.println("lift up");}
		else if (liftDown==true){						//move lift down
			frontWinch.set(-liftMode*liftDownTrim);
			System.out.println("lift down");}
		else{frontWinch.set(0.1);}						//lift idle
		
		
	//gripper movement control
		if (gripOpen & openLimit){							//open gripper
				gripper.set(1);
				System.out.println("gripper open");
			}
		else if (gripClose & closeLimit){					//close gripper	
				gripper.set(-1);
				System.out.println("gripper close");
			}
		else if(closeLimit){									//hold gripper
			gripper.set(0);
		}
		else{																//stop gripper
			gripper.set(0);
		}
	
	}

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void testPeriodic() {
		
		robotDrive.arcadeDrive(driveStick.getRawAxis(1), (driveStick.getRawAxis(2)*-1)*rotationTrim);
		
		if (driveStick.getRawButton(1)==true){
			setLiftHeightSwitch();
		}
		if (driveStick.getRawButton(2)==true){
			setLiftHeightScale();
		}
		if (driveStick.getRawButton(3)==true){
			moveBotDist(5,0.5);
		}
		if (driveStick.getRawButton(4)==true){
			rotateBot(-90,0.3);
		}
		if (driveStick.getRawButton(5)==true){
			moveLift(.5, .5,true);
		}
		if (driveStick.getRawButton(6)==true){
			moveGripper(.5, 1, true);
		}
		if (driveStick.getRawButton(7)==true){
			breakString();
		}
		
		
	}

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static void moveBotDist(double distance, double speed){ 			//distance in meters speed from 0 to 1
		System.out.println("move bot " + distance + " at speed: " + speed);

		speed = Math.abs(speed);
		boolean rightMoving = true;
		boolean leftMoving = true;
		double startDistLeft = getDistanceLeft(leftEncoder);
		double startDistRight = getDistanceRight(rightEncoder);
		
		double currentDistanceLeft = getDistanceLeft(leftEncoder);
		double currentDistanceRight= getDistanceRight(rightEncoder);
		
		if (distance> 0){ 															//move forward
			while(leftMoving | rightMoving==true){
				if(currentDistanceLeft < startDistLeft+distance){
					leftSide.set(-speed);												//TESTME: negative speed for auto move?
					System.out.println(getDistanceLeft(leftEncoder)+" auto Left" + startDistLeft + "left start");
					currentDistanceLeft=getDistanceLeft(leftEncoder);
					if (currentDistanceLeft >= startDistLeft+distance){leftSide.set(0);leftMoving=false;}
				}
				if(currentDistanceRight< startDistRight+distance){
					rightSide.set(speed-autoDriveTrim);
					System.out.println(getDistanceRight(rightEncoder)+" auto Right"+startDistRight+"right Start");
					currentDistanceRight= getDistanceRight(rightEncoder);
					if(currentDistanceRight >= startDistRight+distance){rightSide.set(0);rightMoving = false;}
				}
			}
		}
		
		if (distance< 0){ 															//move backward
			distance = Math.abs(distance);
			while(leftMoving | rightMoving==true){
				if(currentDistanceLeft > startDistLeft-distance){
					leftSide.set(speed);												//TESTME: positive speed for auto move?
					System.out.println(Math.abs(getDistanceLeft(leftEncoder))+" auto Left");
					currentDistanceLeft= Math.abs(getDistanceLeft(leftEncoder));
					if (currentDistanceLeft <= startDistLeft-distance){leftSide.set(0);leftMoving=false;}
				}
				if(currentDistanceRight> startDistRight-distance){
					rightSide.set(-speed+autoDriveTrim);
					System.out.println(Math.abs(getDistanceRight(rightEncoder))+" auto Right");
					currentDistanceRight= Math.abs(getDistanceRight(rightEncoder));
					if(currentDistanceRight <= startDistRight-distance){rightSide.set(0);rightMoving = false;}
				}
			}
		}
	}
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	public static void rotateBot(double degrees, double speed){					//negative degrees is clockwise, speed between 0 and 1
		System.out.println("rotate bot: " + degrees + " degrees at speed: "+speed);
		
		speed = Math.abs(speed);
		boolean leftMoving = true;
		boolean rightMoving = true;
		double startLeft = getDistanceLeft(leftEncoder);
		double startRight = getDistanceRight(rightEncoder);
		
		double currentLeft = getDistanceLeft(leftEncoder);
		double currentRight = getDistanceRight(rightEncoder);
		
		double arcLength = Math.abs((degrees*2*Math.PI*(BOT_DIAMETER/2))/360);
		
		if (degrees<0){								//clockwise rotation  (negative degrees)
			while(leftMoving | rightMoving == true){
				if (currentLeft < startLeft+arcLength){			//left wheel
				leftSide.set(-speed);							//TESTME: negative speed for clockwise rotation
				currentLeft = getDistanceLeft(leftEncoder);
				System.out.println("left "+currentLeft);
					if (currentLeft >= startLeft+arcLength){leftMoving = false; leftSide.set(0);}
				}
				if (currentRight > startRight-arcLength){			//right wheel
					rightSide.set(-speed+autoRotTrim);				//TESTME: negative speed
					currentRight = getDistanceRight(rightEncoder);
					System.out.println("right"+currentRight+"start right:"+startRight);
					if(currentRight <= startRight-arcLength){rightMoving = false; rightSide.set(0);}
				}
			}
		} 
		else if(degrees>0){							//counter clockwise rotation (positive degrees)
			while (leftMoving | rightMoving == true){
				if (currentLeft > startLeft-arcLength){			//left wheel
					leftSide.set(speed);							//TESTME: positive speed?
					currentLeft = getDistanceLeft(leftEncoder);
					System.out.println("left"+currentLeft);
						if (currentLeft <= startLeft-arcLength){leftMoving = false; leftSide.set(0);}
					}
					if (currentRight < startRight+arcLength){			//right wheel
						rightSide.set(speed-autoRotTrim);
						currentRight = getDistanceRight(rightEncoder);
						System.out.println("right"+currentRight);
						if(currentRight>= startRight+arcLength){rightMoving = false; rightSide.set(0);}
					}
			}
		}
	}
//----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static void moveLift(double time , double speed, boolean up){
		System.out.println("move lift for: "+time+" seconds and speed: "+speed+" up="+up);
		
		speed = Math.abs(speed);
		
			if (up){
				frontWinch.set(speed);
			}
			else if (!up){
				frontWinch.set(-speed);
			}
				Timer.delay(time);
		frontWinch.set(0.1);				//lift idle 
	}
//----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static void setLiftHeightSwitch(){
		System.out.println("set lift height to switch");
		//needs 3ft 1M
		moveLift(3, 0.5,true);
	}
//----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static void setLiftHeightScale(){
		System.out.println("set lift height to scale");
		//needs 6ft 1.8M
		moveLift(1.7,1,true);
	}
//----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static void moveGripper (double time , double speed , boolean close){
		System.out.println("move gripper for:"+time+" seconds at speed: "+speed+"close ="+close);
		
		boolean openLimit = gripperOpenSwitch.get();
		boolean closeLimit = gripperCloseSwitch.get();
		speed = Math.abs(speed);
		
		
			if (close & closeLimit){
				gripper.set(-speed);	
				
			}
			else if (!close & openLimit){
				gripper.set(speed);
			}
			
		Timer.delay(time);
			gripper.set(0);
	}
//----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static void breakString(){
		System.out.println("break string");
		moveLift(1,0.5,true);
		moveLift(.75,.25,false);
	}
//----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public void ledControl(){
		if (ledStatus){
			System.out.println("lights off");
			ledStrip1.set(false);
			ledStrip2.set(false);
			ledStatus = false;
		}
		else{
			System.out.println("lights on");
			ledStrip1.set(true);
			ledStrip2.set(true);
			ledStatus = true;
		}
	}
//----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public void ledControl(boolean on){
		if (on){
			System.out.println("lights on");
			ledStrip1.set(true);
			ledStrip2.set(true);
			ledStatus = true;
		}
		else{
			System.out.println("lights off");
			ledStrip1.set(false);
			ledStrip2.set(false);
			ledStatus = false;
		}
	}
	public static double getDistanceRight(Encoder encoder){
		double rawData = encoder.getDistance();
		double convertedData = rawData * -1;
		return convertedData;
	}
	public static double getDistanceLeft(Encoder encoder){
		double rawData = encoder.getDistance();
		return rawData;
	}
	public static void squeezeBox(){
		moveGripper(.4,1 ,true );
	}
	
}
