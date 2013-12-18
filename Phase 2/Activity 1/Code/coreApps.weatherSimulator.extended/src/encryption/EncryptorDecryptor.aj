package encryption;


import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.apache.log4j.*;

import Interactive.Receiver;
import Interactive.Transmitter_8815;
import Interactive.Transmitter_8816;


import utilities.Encoder;
import utilities.Message;
import utilities.messages.WeatherDataRequest;
import utilities.messages.WeatherDataVector;


public aspect EncryptorDecryptor {
	
		Logger _logger = Logger.getLogger(EncryptorDecryptor.class);
		
	    
	    byte[] wrappedKey;
		Key unwrappedKey;
		SecretKey passwordKey;
		PBEParameterSpec paramSpec;
		Key sharedKey;
		Cipher cipher;
		PBEKeySpec keySpec;

	public static ByteBuffer Receiver.buffer = ByteBuffer.allocateDirect(5048);	
	public static ByteBuffer Receiver.readBuf = ByteBuffer.allocateDirect(5048);
	public static ByteBuffer Transmitter_8815.buffer = ByteBuffer.allocateDirect(5048);
	public static ByteBuffer Transmitter_8816.buffer = ByteBuffer.allocateDirect(5048);

	
	 private pointcut ChannelSend(DatagramChannel _channel, ByteBuffer _buffer, SocketAddress _socketAddress) :
			call(int DatagramChannel+.send(ByteBuffer, SocketAddress)) && target(_channel) && args(_buffer,_socketAddress);
	 
	 
	 private pointcut ChannelReceive(DatagramChannel _channel, ByteBuffer _buffer) :
			call(* DatagramChannel+.receive(ByteBuffer)) && target(_channel) && args(_buffer);
	 
 
	 
	 int around(DatagramChannel _channel,  ByteBuffer _buffer, SocketAddress _socketAddress) : ChannelSend( _channel, _buffer, _socketAddress ) 
	 {
		
	
		Message message = convertBufferToMessage(_buffer); 
			
		Object obj = thisJoinPoint.getThis();
		if (obj instanceof Transmitter_8815)
		{
			WeatherDataVector msg= (WeatherDataVector) message;
			unwrappedKey = ((Transmitter_8815) obj)._key.getSharedKey();
			_logger.debug("Before Send Encryption: Transmitter buffer of length "+ Receiver.buffer.remaining() );
			Transmitter_8815.buffer.clear();
			Transmitter_8815.buffer = ByteBuffer.wrap(Encrypt(msg, unwrappedKey));
			_logger.debug("After Send Encryption: Transmitter_8815 buffer of length "+ Transmitter_8815.buffer.remaining() );
			return proceed(_channel, Transmitter_8815.buffer, _socketAddress);
		}		
		
		else  if (obj instanceof Transmitter_8816)
		{
			WeatherDataVector msg= (WeatherDataVector) message;
			unwrappedKey = ((Transmitter_8816) obj)._key.getSharedKey();
			
			Transmitter_8816.buffer.clear();
			Transmitter_8816.buffer = ByteBuffer.wrap(Encrypt(msg, unwrappedKey));
			
			return proceed(_channel, Transmitter_8816.buffer, _socketAddress);
		}	
		else if (obj instanceof Receiver)
		{
			WeatherDataRequest msg= (WeatherDataRequest) message;
			unwrappedKey = ((Receiver)obj).key.getSharedKey();
			_logger.debug("Before Send Encryption: Reciever buffer of length "+ _buffer.remaining()+"the key is"+ unwrappedKey  );
			Receiver.buffer.clear();
			Receiver.buffer = ByteBuffer.wrap(Encrypt(msg, unwrappedKey));
			_logger.debug("After Send Encryption : Reciever buffer of length "+ _buffer.remaining() +"-----"+msg);
			return proceed(_channel, Receiver.buffer, _socketAddress);
		}
		
		return 0;
		
	 }
	 
	 

	 SocketAddress around (DatagramChannel _channel,  ByteBuffer _buffer): ChannelReceive( _channel, _buffer){
		 	SocketAddress adr  = proceed(_channel, _buffer);
		    if (_buffer.remaining() > 0)
		    {
		    	 
		    }
		    	_buffer.flip();
		    	Object obj = thisJoinPoint.getThis();
		    	if (obj instanceof Receiver){
		    		unwrappedKey = Receiver.key.getSharedKey();
		    		if(unwrappedKey != null && _buffer.remaining() > 0){
		    			
		    			Message temp=Decrypt(convertBufferToBytes(_buffer), unwrappedKey);
		    			WeatherDataVector _data = (WeatherDataVector) temp;
		    			
		    			if(temp != null){
		    				Receiver.buffer.clear();
		    				Receiver.readBuf = ByteBuffer.wrap(Encoder.encode(_data));
		    				_logger.debug("Reciever received  message of type " + _data);
		    			}
		    		}
		    	}
				else if (obj instanceof Transmitter_8815){
					unwrappedKey = ((Transmitter_8815)obj)._key.getSharedKey();
					Message temp=Decrypt(convertBufferToBytes(_buffer), unwrappedKey);
					WeatherDataRequest _data = (WeatherDataRequest) temp;
					if(temp != null){
						Transmitter_8815.buffer.clear();
						Transmitter_8815.buffer = ByteBuffer.wrap(Encoder.encode(_data));
						_logger.debug("Transmitter received  message of type " + _data);
					}
				}
					else if (obj instanceof Transmitter_8816){
						
						unwrappedKey = ((Transmitter_8816)obj)._key.getSharedKey();
						
						Message temp=Decrypt(convertBufferToBytes(_buffer), unwrappedKey);
						System.out.println("message is "+ temp);
						WeatherDataRequest _data = (WeatherDataRequest) temp;
						System.out.println(" data is " + _data);
						if(temp != null){
							Transmitter_8816.buffer.clear();
							Transmitter_8816.buffer = ByteBuffer.wrap(Encoder.encode(_data));
							
							_logger.debug("Transmitter received  message of type " + _data);
						}
				}
		    
		    return adr;
	}
		
	 private byte[] convertBufferToBytes(ByteBuffer buffer) {
			byte[] bytes = new byte[buffer.remaining()];
			
			buffer.get(bytes);
			buffer.clear();
			buffer = ByteBuffer.wrap(bytes);
			 
			return bytes;
		}
	 
	 public void setVar() throws Exception {
			KeyGenerator kg = KeyGenerator.getInstance("DESede");
			sharedKey = kg.generateKey();
			String password = "password";
			byte[] salt = "salt1234".getBytes();
			paramSpec = new PBEParameterSpec(salt, 20); // Parameter based encryption
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
			try{
				byte[] bytes = new byte[buffer.remaining()];
				buffer.get(bytes);
				message = Encoder.decode(bytes);
				buffer.clear();
				buffer = ByteBuffer.wrap(Encoder.encode(message));
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return message;
		}	
	
}
