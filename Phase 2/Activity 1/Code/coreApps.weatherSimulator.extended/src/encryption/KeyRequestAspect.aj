package encryption;

import org.apache.commons.lang3.exception.ExceptionUtils;

import Interactive.Receiver;
import Interactive.Transmitter_8815;
import Interactive.Transmitter_8816;

public aspect KeyRequestAspect {
	
	
	public  static SharedKey Receiver.key=null;
	public  static SharedKey Transmitter_8815._key=null;
	public  static SharedKey Transmitter_8816._key=null;
	
	private KMClient _KMClient = new KMClient();
	
	
	private pointcut GenerateReceiverKey(int portOne, int portTwo):
		call (void *.coreReceiver(int , int)) && args(portOne, portTwo);
	

	private pointcut GenerateTransmitter115Key(int portOne):
		call (void *.coreTransmitter(int)) && args(portOne);
	
	private pointcut GenerateTransmitter116Key(int portOne):
		call (void *.coreTransmitter(int)) && args(portOne);
	
	before (int portOne, int portTwo): GenerateReceiverKey(portOne, portTwo )
	{
		try
		{
		_KMClient.getSharedKey("Client", "abcdef");
		Receiver.key = _KMClient.getKey();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			
	
			
		}
	}
	
	
	before (int portOne): GenerateTransmitter115Key(portOne )
	{
		try
		{
		_KMClient.getSharedKey("Server", "abcde");
		Transmitter_8815._key = _KMClient.getKey();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			
	
			
		}
	}
	
	before (int portOne): GenerateTransmitter116Key(portOne )
	{
		try
		{
		_KMClient.getSharedKey("Server", "abcde");
		Transmitter_8816._key = _KMClient.getKey();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			
	
			
		}
	}
	
	
}
 