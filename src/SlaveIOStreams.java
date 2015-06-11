import java.io.DataInputStream;
import java.io.DataOutputStream;

import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.USB;
import lejos.nxt.comm.NXTConnection;

public class SlaveIOStreams {

	private NXTConnection conn;
	private boolean usb;
	private DataOutputStream dOut;
    private DataInputStream dIn;
    
	public SlaveIOStreams(boolean usb)
	{
		this.usb = usb;
	}
	
	public void open()
	{
		Sound.beep();
		
        if ( usb ){
        	LCD.drawString("Waiting USB", 0, 0);       
        	conn = USB.waitForConnection();
        	LCD.drawString("Connected USB", 0, 0);
        }
        else
        {
        	LCD.drawString("Waiting BT", 0, 0);         
        	conn = Bluetooth.waitForConnection();
        	LCD.drawString("Connected BT", 0, 0);
        }
        
        Sound.twoBeeps();
        
		dIn  = conn.openDataInputStream();
		dOut = conn.openDataOutputStream();
	}
	
	public float input()
	{
		float result;
        try
        {
            result = dIn.readFloat();
        }
        catch (Exception e) 
        {
            result = -1;
        } 
        return result;

	}
	
	public boolean output(float r)
	{ 	
		boolean result;
        try
        {
    		dOut.writeFloat(r);
    		dOut.flush();
    		result = true;
        }
        catch (Exception e) 
        {
        	result = false;
        } 
        return result;
	}
	
	public boolean close()
	{
		boolean result;
        try
        {   
        	dOut.close();
        	dIn.close();
            conn.close();
        	result = true;
        }
        catch (Exception e) 
        {
        	result = false;
        }         
        return result;
	}

}
