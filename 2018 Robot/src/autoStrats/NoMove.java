package autoStrats;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;

public class NoMove extends Command{
	boolean isDone = false;

	public NoMove(){		
	}
	
	protected void initialize(){
	}
	
	protected void execute() {
		Timer.delay(5);
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