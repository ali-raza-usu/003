package Aspect_part;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import utilities.Encoder;
import utilities.Message;

import utilities.TranslationRequestMessage;
import utilities.TranslationResponseMessage;

import Interactive.*;
//import org.junit.Test;

public aspect Encryptor {
	
   SharedKey key = null;
  
   byte[] wrappedKey;
	Key unwrappedKey;
	SecretKey passwordKey;
	PBEParameterSpec paramSpec;
	Key sharedKey;
	Cipher cipher;
	PBEKeySpec keySpec;
	ByteBuffer b=null;
	public static ByteBuffer Client.buffer = ByteBuffer.allocateDirect(5048);
	public static ByteBuffer Client.readBuf = ByteBuffer.allocateDirect(5048);

	public static ByteBuffer Server.buffer = ByteBuffer.allocateDirect(5048);
	

	//dc.read(readBuf)
	
   private pointcut ChannelRead(DatagramChannel _channel, ByteBuffer _buffer) :
		call(* DatagramChannel+.read(ByteBuffer)) && target(_channel) && args(_buffer);
   
   private pointcut ChannelReceive(DatagramChannel _channel, ByteBuffer _buffer) :
		call(* DatagramChannel+.receive(ByteBuffer)) && target(_channel) && args(_buffer);
  
  //dc.send(buffer, srcAddr);
 
  private pointcut ChannelWrite(DatagramChannel _channel, ByteBuffer _buffer, SocketAddress _addr) :
		call(* DatagramChannel+.send(ByteBuffer, SocketAddress)) && target(_channel) && args(_buffer, _addr);
  
  
  int around ( DatagramChannel _channel, ByteBuffer _buffer) : ChannelRead(_channel, _buffer)  ///Read -- recieve
  {	  
		int readBytes = proceed(_channel, _buffer);
		if (readBytes > 0) {
				ByteBuffer tempBuf = _buffer.duplicate();
				tempBuf.flip();
		        byte[] bytes = new byte[tempBuf.remaining()];
		        tempBuf.get(bytes);
				Object obj = thisJoinPoint.getThis();
				
				if (obj instanceof Client) // message is read by client, use the client key  
				{
					unwrappedKey = Client.key.getSharedKey();
					Message temp=Decrypt(bytes, unwrappedKey);
					TranslationResponseMessage msg = (TranslationResponseMessage)temp;
					Client.readBuf = ByteBuffer.wrap(Encoder.encode(msg));
				
				//	_logger.debug("Client received the message "+ msg.getClass()+" at time " + getCurrentTime());
					
				}
				else
				{
					unwrappedKey = Server.key.getSharedKey();
					
					Message temp=Decrypt(bytes, unwrappedKey);
					TranslationRequestMessage msg = (TranslationRequestMessage)temp;
					Server.buffer = ByteBuffer.wrap(Encoder.encode(msg));
	
				}
			}		
		return readBytes;
		
  }
  
  Object around ( DatagramChannel _channel, ByteBuffer _buffer) : ChannelReceive(_channel, _buffer)  ///Read -- recieve
  {	  
		Object socket = proceed(_channel, _buffer);
		if (_buffer.remaining() > 0) {
				ByteBuffer tempBuf = _buffer.duplicate();
				tempBuf.flip();
		        byte[] bytes = new byte[tempBuf.remaining()];
		        tempBuf.get(bytes);
				Object obj = thisJoinPoint.getThis();
				
				if (obj instanceof Client) // message is read by client, use the client key  
				{
					unwrappedKey = Client.key.getSharedKey();
					Message temp=Decrypt(bytes, unwrappedKey);
					TranslationResponseMessage msg = (TranslationResponseMessage)temp;
					Client.readBuf = ByteBuffer.wrap(Encoder.encode(msg));
				
				//	_logger.debug("Client received the message "+ msg.getClass()+" at time " + getCurrentTime());
					
				}
				else
				{
					unwrappedKey = Server.key.getSharedKey();
					
					Message temp=Decrypt(bytes, unwrappedKey);
					TranslationRequestMessage msg = (TranslationRequestMessage)temp;
					Server.buffer = ByteBuffer.wrap(Encoder.encode(msg));
	
				}
			}		
		return socket;
		
  }
  


int around( DatagramChannel _channel, ByteBuffer _buffer, SocketAddress _addr) : ChannelWrite(_channel, _buffer, _addr) // write -- send
{
			ByteBuffer tempBuf = _buffer.duplicate();
	 		Message message = null;
	 
		
			Object obj = thisJoinPoint.getThis();
			if (obj instanceof Client) // message is read by client, use the client key  
			{
				message = (TranslationRequestMessage)convertBufferToMessage(tempBuf);
				unwrappedKey = ((Client) obj).key.getSharedKey();
				
				Client.buffer.clear();
				Client.buffer = ByteBuffer.wrap(Encrypt(message, unwrappedKey));
				
				return proceed(_channel, Client.buffer, _addr);
			}
			else
			{
				message = (TranslationResponseMessage)convertBufferToMessage(tempBuf);
				unwrappedKey = ((Server) obj).key.getSharedKey();
				
				Server.buffer.clear();
				Server.buffer = ByteBuffer.wrap(Encrypt(message, unwrappedKey));
				
				return proceed(_channel, Server.buffer, _addr);

			}		

}



public void setVar() throws Exception {
	KeyGenerator kg = KeyGenerator.getInstance("DESede");
	sharedKey = kg.generateKey();
	String password = "password";
	byte[] salt = "salt1234".getBytes();
	paramSpec = new PBEParameterSpec(salt, 20); // Parameter based
												// encryption
	keySpec = new PBEKeySpec(password.toCharArray());

}

public byte[] Encrypt(Message data, Key _sharedKey) {
	try {
		setVar();
		SecretKeyFactory kf = SecretKeyFactory
				.getInstance("PBEWithMD5AndDES");
		passwordKey = kf.generateSecret(keySpec);
		cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.WRAP_MODE, passwordKey, paramSpec);
		wrappedKey = cipher.wrap(sharedKey);
		cipher = Cipher.getInstance("DESede");
		cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.UNWRAP_MODE, passwordKey, paramSpec);
		unwrappedKey = cipher.unwrap(wrappedKey, "DESede",
				Cipher.SECRET_KEY);
		cipher = Cipher.getInstance("DESede");
		cipher.init(Cipher.ENCRYPT_MODE, _sharedKey);
		byte[] input = Encoder.encode(data);
		byte[] encrypted = cipher.doFinal(input);
		return encrypted;
	} catch (Exception e) {
		e.printStackTrace();
		return null;
	}
}

public Message Decrypt(byte[] encrypted, Key _unwrappedKey) 
{
	try {
		setVar();
		SecretKeyFactory kf = SecretKeyFactory
				.getInstance("PBEWithMD5AndDES");
		passwordKey = kf.generateSecret(keySpec);
		cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.WRAP_MODE, passwordKey, paramSpec);
		wrappedKey = cipher.wrap(sharedKey);
		cipher = Cipher.getInstance("DESede");
		cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.UNWRAP_MODE, passwordKey, paramSpec);
		unwrappedKey = cipher.unwrap(wrappedKey, "DESede",
				Cipher.SECRET_KEY);
		cipher = Cipher.getInstance("DESede");
		cipher.init(Cipher.DECRYPT_MODE, _unwrappedKey);
		Message data = (Message) Encoder.decode(cipher.doFinal(encrypted));

		return data;
	} catch (Exception e) {
		e.printStackTrace();
		return null;
	}
}

	
private Message convertBufferToMessage(ByteBuffer buffer) {
	Message message = null;
	byte[] bytes = new byte[buffer.remaining()];
	buffer.get(bytes);
	message = Encoder.decode(bytes);
	buffer.clear();
	buffer = ByteBuffer.wrap(Encoder.encode(message));
	return message;
}

	

}
