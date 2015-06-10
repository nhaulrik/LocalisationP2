

//hej v1.2

import java.util.Random;

import lejos.nxt.*;
import lejos.nxt.comm.RConsole;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Pose;
import lejos.util.Delay;

/**
 * Demonstration of the Behavior subsumption classes.
 * 
 * Requires a wheeled vehicle with two independently controlled
 * motors connected to motor ports A and C, and 
 * a touch sensor connected to sensor  port 1 and
 * an ultrasonic sensor connected to port 3;
 * 
 * @author Brian Bagnall and Lawrie Griffiths, modified by Roger Glassey
 *
 * Uses a new version of the Behavior interface and Arbitrator with
 * integer priorities returned by takeCaontrol instead of booleans.
 * 
 * Exit behavior inserted, local distance sampling thread and
 * backward drive added in DetectWall by Ole Caprani, 23-4-2012
 */


public class BumperCar
{
	

	static double wheelDiameter = 3.025, trackWidth = 15;
    static double travelSpeed = 5, rotateSpeed = 90;
    
    static NXTRegulatedMotor left = Motor.B;
    static NXTRegulatedMotor right = Motor.C;
      
    static DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, trackWidth, left, right);
    static OdometryPoseProvider poseProvider = new OdometryPoseProvider(pilot);
      
    static Pose initialPose = new Pose(0,0,0);
    
      

 
	
    public double getWheelDiameter() {    	return wheelDiameter; }
    public double getTrackWidth() 	{    	return trackWidth;    }
    public double getTravelSpeed() {    	return travelSpeed;   }
    public double getRotateSpeed() {    	return rotateSpeed;   }
    
  public static void main(String[] args)
  {
	  RConsole.open();
	  pilot.setTravelSpeed(travelSpeed);
	  pilot.setRotateSpeed(rotateSpeed);
	  poseProvider.setPose(initialPose);
	     
    
    Behavior b1 = new Wander();
    Behavior b2 = new Avoid();
    
    /*
    Behavior b2 = new Charge();
    Behavior b3 = new Survive();
    Behavior b4 = new Exit();
    */

    Behavior[] behaviorList =
    {
      b1 , b2 //, b3, b4
    };
    Arbitrator arbitrator = new Arbitrator(behaviorList);
    LCD.drawString("Bumper Car",0,1);
	
    
    arbitrator.start();
    
    

	 }
  
  public static void show(Pose p)
  {
     LCD.clear();
      LCD.drawString("Pose X " + p.getX(), 0, 2);
      LCD.drawString("Pose Y " + p.getY(), 0, 3);
      LCD.drawString("Pose V " + p.getHeading(), 0, 4);
  }
  
  
  
}

class Wander extends Thread implements Behavior{
	
	private boolean _suppressed = false;

	public double randomNumber;

	
	public Wander(){
		
		this.setDaemon(true);
        this.start();
        
     
	}

	@Override
	public int takeControl() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public void action() {
		
		
   
	    Random random = new Random();

		_suppressed = false;
		
		
	    LCD.drawString("Wander         ",0,1);
	    
	    BumperCar.pilot.travel(20, true);
	    BumperCar.show(BumperCar.poseProvider.getPose());
	    
	    while (!_suppressed && BumperCar.pilot.isMoving())
	    {
	    	Thread.yield(); //don't exit till suppressed
	    }
	    
	    randomNumber = random.nextInt(100);
	  //  Avoid.threadRunning(true);
	    
    	//LCD.drawString("Random     "+randomNumber,0,3);
    	
    	if(!_suppressed && randomNumber >=0 && randomNumber <= 24){
    		Avoid.threadRunning(false);
    		BumperCar.pilot.rotate(90, false);
    		BumperCar.show(BumperCar.poseProvider.getPose());
    		//Delay.msDelay(1000);
    		Avoid.threadRunning(true);
    	}
    	else if(!_suppressed && randomNumber >=25 && randomNumber <= 49){
    		Avoid.threadRunning(false);
    		BumperCar.pilot.rotate(180, false);
    		BumperCar.show(BumperCar.poseProvider.getPose());
    		//Delay.msDelay(1000);

    		Avoid.threadRunning(true);
    	}
    	else if(!_suppressed && randomNumber >=50 && randomNumber <= 74){
    		Avoid.threadRunning(false);
    		BumperCar.pilot.rotate(-90, false);
    		BumperCar.show(BumperCar.poseProvider.getPose());
    		//Delay.msDelay(1000);

    		Avoid.threadRunning(true);
    	}
    	else if(!_suppressed && randomNumber >=75 && randomNumber <= 100){
    		Avoid.threadRunning(false);
    		BumperCar.pilot.rotate(0, false);
    		BumperCar.show(BumperCar.poseProvider.getPose());
    		//Delay.msDelay(1000);
    		Avoid.threadRunning(true);
    	}
    	
	    
	    while (!_suppressed && BumperCar.pilot.isMoving())
	    {
	    	Thread.yield(); //don't exit till suppressed
	    }
	    
	    BumperCar.pilot.stop();
		
	}

	@Override
	public void suppress() {
		// TODO Auto-generated method stub
		_suppressed = true;// standard practice for suppress methods
		
	}
	
}

class Avoid extends Thread implements Behavior{
	
	private boolean _suppressed = false;
	private TouchSensor bumper;
	private LightSensor lightFront;
	private LightSensor lightBack;
	private int lightValFront;
	private int lightValBack;

	
	private boolean isFrontBlack;
	private boolean isBackBlack;
	private boolean isFrontWhite;
	private boolean isBackWhite;
	
	private boolean flag = false;
	private static boolean isRunning = true;

	public Avoid(){
		
		lightFront = new LightSensor(SensorPort.S2, true);
		lightBack = new LightSensor(SensorPort.S3, true);
		bumper = new TouchSensor(SensorPort.S1);
		
		this.setDaemon(true);
        this.start();
    
	}
	
	public static void threadRunning(boolean b){
		
		isRunning = b;
		//LCD.drawString("isRunning? "+isRunning,0,3);
	}
	
	public void run()
    {
     Thread t1 = new Thread(new Runnable(){

          @Override
          public void run() {
              while ( true ){
            	  
            	lightValFront = lightFront.readValue();
            	lightValBack = lightBack.readValue();
            	Delay.msDelay(50);
            	BumperCar.show(BumperCar.poseProvider.getPose());
           }
          }  
        });
        t1.start();
    }

	@Override
	public int takeControl() {
		// TODO Auto-generated method stub
			
		if ( isRunning == true) {
		
			if(isBackWhite == true && isFrontBlack == true && lightValFront > 40 ){
	    		Sound.twoBeeps();
	    		flag = true;
	    	}
	  		
	  		// Front sensor
	  		if(lightValFront < 40 && lightValFront < 254){ isFrontBlack = true; isFrontWhite = false; }	
	  		if(lightValFront > 40) {isFrontWhite = true; isFrontBlack = false;}		
	  		
	  		// Back sensor
	  		if(lightValBack < 40 && lightValBack < 254 ){ isBackBlack = true; isBackWhite = false; }	
	  		if(lightValBack > 40){ isBackWhite = true; isBackBlack = false; }
	  		
	  		/*
	  		LCD.drawString("backBlack? "+isBackBlack,0,3);
	  		LCD.drawString("backWhite? "+isBackWhite,0,4);
	  		
	  		LCD.drawString("frontBlack? "+isFrontBlack,0,5);
	  		LCD.drawString("frontWhite? "+isFrontWhite,0,6);
			*/
	  		
		}
  		
		if(flag == true || bumper.isPressed()) {
    		return 100;
    	} 
    	else { 
    		return 0;
    	}
    	
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		_suppressed = false;

	    
	    LCD.drawString("Avoid          ",0,1);
	    
	    BumperCar.pilot.travel(-10, false );
	    BumperCar.show(BumperCar.poseProvider.getPose());
	    
	    while (!_suppressed && BumperCar.pilot.isMoving())
	    {
	    	Thread.yield(); //don't exit till suppressed
	    }
	    
    	BumperCar.pilot.rotate(180, false);
    	BumperCar.show(BumperCar.poseProvider.getPose());
    	flag = false;

    	
	    while (!_suppressed && BumperCar.pilot.isMoving())
	    {
	    	Thread.yield(); //don't exit till suppressed
	    }
		
	    BumperCar.pilot.stop();
	}

	@Override
	public void suppress() {
		// TODO Auto-generated method stub
		_suppressed = true;
		
	}
	
}



//!--               Old Code                     --!//
/*
class Survive extends Thread implements Behavior
{
    private boolean _suppressed = false;
    private int lightValue;


    public Survive() {
        
        light = new LightSensor(SensorPort.S2);
        light.setFloodlight(true);
        
         this.setDaemon(true);
         this.start();
    }
    
    
    public void run()
      {
        Thread t1 = new Thread(new Runnable(){

            @Override
            public void run() {
                while ( true ) lightValue = light.readValue();
            }  
          });
          t1.start();
      }
     
    
    public int takeControl() {
        // TODO Auto-generated method stub
        
    	if(lightValue > 48) {
    		return 200;
    	} 
    	else { 
    		return 0;
    	}
    }

    
    public void action() {
        // TODO Auto-generated method stub
        _suppressed = false;
        LCD.drawString("Survive:       ",0,2);
        LCD.drawInt(lightValue,0,3);
        LCD.refresh();
       
        int now = (int)System.currentTimeMillis();
         while (!_suppressed && ((int)System.currentTimeMillis()< now + 1000))
            {
        	 //
        	    //Sound.beepSequenceUp();
                
              	Motor.A.rotate(-720, true);// start Motor.A rotating backward
            	Motor.C.rotate(-720, true);  // start Motor.C rotating backward
                
            
   
             
        	 Thread.yield(); //don't exit till suppressed
            }
        
     

        
    }

    @Override
    public void suppress() {
        // TODO Auto-generated method stub
        _suppressed = true;// standard practice for suppress methods

    }
    private LightSensor light;

}








class LookForTarget implements Behavior
{
	
	private boolean _suppressed = false;
	
	@Override
	public int takeControl() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		_suppressed = false;
		
		Motor.A.forward();
	    Motor.C.backward();
	    LCD.drawString("Look for target",0,2);
	    while (!_suppressed)
	    {
	      Thread.yield(); //don't exit till suppressed
	    }
	  
		
	}

	@Override
	public void suppress() {
		// TODO Auto-generated method stub
		_suppressed = true;// standard practice for suppress methods
	}
	
}

class Charge extends Thread implements Behavior
{
	private boolean _suppressed = false;
	private boolean active = false;
	private int leftDistance = 255;
	private int rightDistance = 255;
	
	private UltrasonicSensor leftSonar;
	private UltrasonicSensor rightSonar;
	
	public Charge()
	{
		leftSonar = new UltrasonicSensor(SensorPort.S1);
		rightSonar = new UltrasonicSensor(SensorPort.S4);
	    
		this.setDaemon(true);
	    this.start();
		
	}
	
	  public void run()
	  {
		Thread t1 = new Thread(new Runnable(){
	
			@Override
			public void run() {
				while ( true ){
					leftDistance = leftSonar.getDistance();
					rightDistance = rightSonar.getDistance();
				}
			}  
		  });
		  t1.start();
	  }

	@Override
	public int takeControl() {
		// TODO Auto-generated method stub
		if (leftDistance < 60 ||  rightDistance < 60 )  {
		       return 100;
		} 
		else {
	       return 0;
		}
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
	    _suppressed = false;
	   // active = true;
	    
	    //Do charge behavior
	    
	   
	    Motor.A.setSpeed(400*2);
	    Motor.C.setSpeed(400*2);

	    Motor.A.forward();
	    Motor.C.forward();
	   
	    
	    LCD.drawString("Charge         ",0,2);
	    LCD.drawString("Distance: " + leftDistance, 0, 3);

	   int now = (int)System.currentTimeMillis();
	    while (!_suppressed && ((int)System.currentTimeMillis()< now + 200) )
	    {
		
	      Thread.yield(); //don't exit till suppressed
	    }
	   // Motor.A.stop(); // not strictly necessary, but good programming practice
	   // Motor.C.stop();
	    LCD.drawString("Charge stopped ",0,2);
	    
	    
	    
	    //End with active = false;
	   Motor.A.setSpeed(400);
	   Motor.C.setSpeed(400);
	  //  _suppressed = true;
	    active = false;
		
		
	}

	@Override
	public void suppress() {
		// TODO Auto-generated method stub
		 _suppressed = true;// standard practice for suppress methods
	}
	
}







class DriveForward implements Behavior
{

  private boolean _suppressed = false;
  

  public int takeControl()
  {
    return 10;  // this behavior always wants control.
  }

  public void suppress()
  {
    _suppressed = true;// standard practice for suppress methods
  }

  public void action()
  {
    _suppressed = false;
    Motor.A.forward();
    Motor.C.forward();
    LCD.drawString("Drive forward",0,2);
    while (!_suppressed)
    {
      Thread.yield(); //don't exit till suppressed
    }
    Motor.A.stop(); // not strictly necessary, but good programming practice
    Motor.C.stop();
    LCD.drawString("Drive stopped",0,2);
  }
}





class DetectWall extends Thread implements Behavior
{
  private boolean _suppressed = false;
  private boolean active = false;
  private int distance = 255;

  public DetectWall()
  {
    touch = new TouchSensor(SensorPort.S1);
    sonar = new UltrasonicSensor(SensorPort.S3);
    this.setDaemon(true);
    this.start();
  }
  
  public void run()
  {
	Thread t1 = new Thread(new Runnable(){

		@Override
		public void run() {
			while ( true ) distance = sonar.getDistance();
		}  
	  });
	  t1.start();
  }

  public int takeControl()
  {
    if (touch.isPressed() || distance < 25)
       return 100;
    if ( active )
       return 50;
    return 0;
  }

  public void suppress()
  {
    _suppressed = true;// standard practice for suppress methods  
  }

  public void action()
  {
    _suppressed = false;
    active = true;
   // Sound.beepSequenceUp();
	
    // Backward for 1000 msec
    LCD.drawString("Drive backward",0,3);
    Motor.A.backward();
    Motor.C.backward();
    int now = (int)System.currentTimeMillis();
    while (!_suppressed && ((int)System.currentTimeMillis()< now + 1000) )
    {
       Thread.yield(); //don't exit till suppressed
    }
    
    // Stop for 1000 msec 
    LCD.drawString("Stopped       ",0,3);
    Motor.A.stop(); 
    Motor.C.stop();
    now = (int)System.currentTimeMillis();
    while (!_suppressed && ((int)System.currentTimeMillis()< now + 1000) )
    {
      Thread.yield(); //don't exit till suppressed
    }
    
    // Turn
    LCD.drawString("Turn          ",0,3);
    Motor.A.rotate(-180, true);// start Motor.A rotating backward
    Motor.C.rotate(-360, true);  // rotate C farther to make the turn
    while (!_suppressed && Motor.C.isMoving())
    {
      Thread.yield(); //don't exit till suppressed
    }
    Motor.A.stop(); 
    Motor.C.stop();
    LCD.drawString("Stopped       ",0,3);
   // Sound.beepSequence();
    active = false;
    
  }
  private TouchSensor touch;
  private UltrasonicSensor sonar;
}

class Exit implements Behavior
{
  private boolean _suppressed = false;

public int takeControl()
  {
    if ( Button.ESCAPE.isPressed()) {
    	return 200; } else {
    return 0;
    }
  }

  public void suppress()
  {
    _suppressed = true;// standard practice for suppress methods  
  }

  public void action()
  {
    System.exit(0);
  }
}
*/


