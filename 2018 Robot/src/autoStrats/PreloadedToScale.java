package autoStrats;

import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team6164.robot.Robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;

public class PreloadedToScale extends Command{
	boolean isDone = false;
	int gameDataAttempt=0;
	int gameDataTimeout = Robot.gameDataTimeout;
	
	public PreloadedToScale(){
	}
	
	protected void initialize(){	
	}
	
	protected void execute() {
		String gameData=null;
		try{
			gameData = DriverStation.getInstance().getGameSpecificMessage();	//game data passed to the driver station from FMS (for 2018 the ownership of switch/scale)
		}
		catch(Exception e){
			if(gameDataAttempt < gameDataTimeout){
				Timer.delay(0.1);
				gameDataAttempt++;
				execute();
			}
			else{
				System.out.println("ERROR: Game data reterival timeout.");
				isDone=true;
				return;
			}
			isDone=true;
			return;
		}
		gameData = gameData.toUpperCase();
		int position = Robot.position;
		int autoDelay = Robot.autoDelay;
		double autoRotSpeed = Robot.autoRotSpeed;
		
		Timer.delay(autoDelay);

		try{
		if(gameData.charAt(1)=='L'){			//our scale is on the left
			switch (position){
				case 1: 						//start left
					System.out.println("pos 1");
					Robot.squeezeBox();
					Robot.breakString();
					Robot.moveBotDist(8.2, 0.7);
					Robot.rotateBot(-90, autoRotSpeed);
					Robot.setLiftHeightScale();
					Robot.moveBotDist(1, 0.25);
					Robot.moveGripper(0.4, 1, false);
					break;
					
				case 2: 						//start middle
					System.out.println("pos 2");
					Robot.squeezeBox();
					Robot.breakString();
					Robot.moveBotDist(0.5, 0.5);
					Robot.rotateBot(90, autoRotSpeed);
					Robot.moveBotDist(2, 0.5);
					Robot.rotateBot(-90, autoRotSpeed);
					Robot.moveBotDist(7.7, .7);
					Robot.rotateBot(-90, autoRotSpeed);
					Robot.setLiftHeightSwitch();
					Robot.moveBotDist(1, .25);
					Robot.moveGripper(.4, 1, false);
					break;
					
				case 3:							//start right
					System.out.println("pos 3");
					Robot.squeezeBox();
					Robot.breakString();
					Robot.moveBotDist(5.7, .7);
					Robot.rotateBot(90, autoRotSpeed);
					Robot.moveBotDist(4.5, .5);
					Robot.rotateBot(-90, autoRotSpeed);
					Robot.setLiftHeightScale();
					Robot.moveBotDist(1, .25);
					Robot.moveGripper(.4, 1, false);
					break;
					
				default: 
					System.out.println("ERROR: invalid position num in auto");
					break;
			}
		}
		else if(gameData.charAt(1)=='R'){		//our scale is on the right		
			switch (position){
				case 1: 						//start left
					System.out.println("pos 1");
					Robot.squeezeBox();
					Robot.breakString();
					Robot.moveBotDist(5.7, .7);
					Robot.rotateBot(-90, autoRotSpeed);
					Robot.moveBotDist(4.5, .5);
					Robot.rotateBot(90, autoRotSpeed);
					Robot.setLiftHeightScale();
					Robot.moveBotDist(1, .25);
					Robot.moveGripper(.4, 1, false);
					break;
					
				case 2: 						//start middle
					System.out.println("pos 2");
					Robot.squeezeBox();
					Robot.breakString();
					Robot.moveBotDist(.5, .5);
					Robot.rotateBot(-90, autoRotSpeed);
					Robot.moveBotDist(1.5, .5);
					Robot.rotateBot(90, autoRotSpeed);
					Robot.moveBotDist(7.7, .7);
					Robot.rotateBot(90, autoRotSpeed);
					Robot.setLiftHeightScale();
					Robot.moveBotDist(1, .25);
					Robot.moveGripper(.4, 1, false);
					break;
					
				case 3:							//start right
					System.out.println("pos 3");
					Robot.squeezeBox();
					Robot.breakString();
					Robot.moveBotDist(8.2, .7);
					Robot.rotateBot(90, autoRotSpeed);
					Robot.setLiftHeightScale();
					Robot.moveBotDist(1, .2);
					Robot.moveGripper(.4, 1, false);
					break;
					
				default: 
					System.out.println("ERROR: invalid position num in auto");
					break;
			}
		}
		else{
			System.out.println("ERROR: Game data not read properly for auto");
			System.out.println("Game data: '"+ gameData+"'");
		}
		}
		catch(StringIndexOutOfBoundsException e){
			System.out.println("ERROR: tried to access character out of game data bounds");
			isDone=true;
			return;
		}
	isDone=true;

	}
	
	protected void end() {
		System.out.println("auto end");
	}
	
	protected boolean isFinished(){
		if(isDone){
			System.out.println("auto finished");
			return true;
			}
		return false;
	}
	
	protected void interrupted(){
		System.out.println("ERROR: Auto was interrupted, another command which requires one or more of the same subsystems was scheduled to run");
	}
}