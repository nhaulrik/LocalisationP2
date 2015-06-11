import lejos.nxt.*;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Move;

public class PilotRoute
{
    private SlaveIOStreams PC;
    
	private double wheelDiameter = 5.5, trackWidth = 16.0;
	private double travelSpeed = 5, rotateSpeed = 45;
	private NXTRegulatedMotor left = Motor.B;
	private NXTRegulatedMotor right = Motor.C;
	   
	private DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, trackWidth, left, right);

	public PilotRoute(boolean usb) 
	{
    	PC = new SlaveIOStreams(usb);
    	PC.open();
 	   
 	    pilot.setTravelSpeed(travelSpeed);
 	    pilot.setRotateSpeed(rotateSpeed);
	}
	
	private void sendMove(Move move)
	{	
		PC.output((move.getMoveType() == Move.MoveType.TRAVEL? 0:1 ));
		PC.output(move.getDistanceTraveled());
		PC.output(move.getAngleTurned());
	}
	
	private void travel(double distance)
	{	
		pilot.travel(distance);
		sendMove(pilot.getMovement());
	}
	
	private void rotate(double angle)
	{	
		pilot.rotate(angle);
		sendMove(pilot.getMovement());
	}
	
	public void go()
	{				
		Sound.beep();
		while ( ! Button.ENTER.isDown()) Thread.yield();
		Sound.twoBeeps();
		
		travel(50);
		rotate(90);	
		travel(20);
		rotate(-90);
		travel(50);
		
		while ( ! Button.ENTER.isDown()) Thread.yield();
		
        LCD.clear();	
        LCD.drawString("Closing",0,0);
    	if ( PC.close() ) LCD.drawString("Closed",0,0); 
        try {Thread.sleep(2000);} catch (Exception e){}
	}
	
	
	public static void main(String[] args) 
	{
		PilotRoute route = new PilotRoute(false);
		
		LCD.clear();
		LCD.drawString("Pilot route", 0, 0);
		route.go();		
	}
}
