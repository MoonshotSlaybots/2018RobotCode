package autoStrats;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team6164.robot.Robot;

public class CrossLineOnly extends Command{
	boolean isDone = false;

	public CrossLineOnly(){		
	}
	
	protected void initialize(){
	}
	
	protected void execute() {
		int position = Robot.position;
		int autoDelay = Robot.autoDelay;
		
		Timer.delay(autoDelay);

		switch (position){
			case 1: 						//start left
				System.out.println("pos 1");
				Robot.squeezeBox();
				Robot.breakString();
				Robot.moveBotDist(4 , 0.5);		
				break;
				
			case 2: 						//start middle
				System.out.println("pos 2");
				Robot.squeezeBox();
				Robot.breakString();
				Robot.moveBotDist(0.5, 0.5);
				Robot.rotateBot(-90, 0.3);
				Robot.moveBotDist(2.5, 0.5);
				Robot.rotateBot(90, 0.3);
				Robot.moveBotDist(3, 0.5);	
				break;
				
			case 3:							//start right
				System.out.println("pos 3");
				Robot.squeezeBox();
				Robot.breakString();
				Robot.moveBotDist(4, .5);
				break;
				
			default: 
				System.out.println("ERROR: invalid position num in auto");
				break;
		}
		isDone=true;
	}
	
	protected void end() {
		System.out.println("auto end");
	}
	
	protected boolean isFinished(){
		if(isDone){
			return true;
			}
		return false;
	}
	
	protected void interrupted(){
		System.out.println("ERROR: Auto was interrupted, another command which requires one or more of the same subsystems was scheduled to run");
	}
}