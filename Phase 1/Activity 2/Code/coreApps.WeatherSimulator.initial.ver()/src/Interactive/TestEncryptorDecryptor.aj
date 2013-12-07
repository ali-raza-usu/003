package Interactive;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public aspect TestEncryptorDecryptor {
	
	 private pointcut ChannelSend(DatagramChannel _channel, ByteBuffer _buffer, SocketAddress _socketAddress) :
			call(int DatagramChannel+.send(ByteBuffer, SocketAddress)) && target(_channel) && args(_buffer,_socketAddress);
	 
	 
	 private pointcut ChannelReceive(DatagramChannel _channel, ByteBuffer _buffer) :
			call(* DatagramChannel+.receive(ByteBuffer)) && target(_channel) && args(_buffer);
	 
	 
	 
	 int around(DatagramChannel _channel,  ByteBuffer _buffer, SocketAddress _socketAddress) : ChannelSend( _channel, _buffer, _socketAddress ) 
	 {
		 System.out.println("int around channel sent pointcut gets fired ");
		return proceed(_channel, _buffer, _socketAddress);
	 }
	 
	 

	 SocketAddress around (DatagramChannel _channel,  ByteBuffer _buffer): ChannelReceive( _channel, _buffer)
		 {
		 	SocketAddress adr  = proceed(_channel, _buffer);
		 	//System.out.println("int around channel received pointcut gets fired ");
		    return adr;
	}
		

}
