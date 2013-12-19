package Aspect_part;

import Interactive.*;



public aspect KeyGeneratorAspect {
	
	public  static SharedKey Client.key=null;
	public  static SharedKey Server.key=null;
	
	
	private KMClient _KMClient = new KMClient();
	
	
	private pointcut GenerateClientKey():
		call (void *.coreClient()) ;
	

	private pointcut GenerateServerKey():
		call (void *.coreServer());
	
	private pointcut GenerateTransmitter116Key(int portOne):
		call (void *.coreTransmitter(int)) && args(portOne);
	
	before (): GenerateClientKey()
	{
		try
		{
		_KMClient.getSharedKey("Client", "abcdef");
		Client.key = _KMClient.getKey();
		System.out.print("the key for client is "+ Client.key);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			
	
			
		}
	}
	
	
	before (): GenerateServerKey()
	{
		try
		{
		_KMClient.getSharedKey("Server", "abcde");
		Server.key = _KMClient.getKey();
		System.out.print("the key for Server is "+ Server.key);

		}
		catch (Exception e) 
		{
			e.printStackTrace();
			
	
			
		}
	}
	
	
	

}
