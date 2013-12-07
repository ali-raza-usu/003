package Interactive;

import Interactive.*;
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
import org.apache.log4j.*;


import utilities.Encoder;
import utilities.Message;
import utilities.messages.WeatherDataRequest;

public aspect EncryptorDecryptor {
	
		Logger _logger = Logger.getLogger(EncryptorDecryptor.class);
		
	    
	    byte[] wrappedKey;
		Key unwrappedKey;
		SecretKey passwordKey;
		PBEParameterSpec paramSpec;
		Key sharedKey;
		Cipher cipher;
		PBEKeySpec keySpec;

	public static ByteBuffer Reciever.buffer = ByteBuffer.allocateDirect(5048);	
	public static ByteBuffer Reciever.readBuf = ByteBuffer.allocateDirect(5048);
	public static ByteBuffer Transmitter_8815.buffer = ByteBuffer.allocateDirect(5048);
	public  static SharedKey Reciever.key=null;

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
			//Message message = (WeatherDataVector) convertBufferToMessage(_buffer);
			unwrappedKey = ((Transmitter_8815) obj)._key.getSharedKey();
			_logger.debug("Before Send Encryption: Transmitter buffer of length "+ Reciever.buffer.remaining() );
			Transmitter_8815.buffer.clear();
			Transmitter_8815.buffer = ByteBuffer.wrap(Encrypt(message, unwrappedKey));
			_logger.debug("After Send Encryption: Transmitter_8815 buffer of length "+ Transmitter_8815.buffer.remaining() );
			return proceed(_channel, Transmitter_8815.buffer, _socketAddress);
		}		
		
		else  
		{
			unwrappedKey = Reciever.key.getSharedKey();
			_logger.debug("Before Send Encryption: Reciever buffer of length "+ _buffer.remaining()+"the key is"+ unwrappedKey  );
			Reciever.buffer.clear();
			Reciever.buffer = ByteBuffer.wrap(Encrypt(message, unwrappedKey));
			_logger.debug("After Send Encryption : Reciever buffer of length "+ _buffer.remaining() +"-----"+message);
			return proceed(_channel, Reciever.buffer, _socketAddress);
		}
		
	 }
	 
	 

	 SocketAddress around (DatagramChannel _channel,  ByteBuffer _buffer): ChannelReceive( _channel, _buffer){
		 	SocketAddress adr  = proceed(_channel, _buffer);
		    if (_buffer.remaining() > 0)
		    {
		    	_buffer.flip();
		    	Object obj = thisJoinPoint.getThis();
		    	if (obj instanceof Reciever){
		    		unwrappedKey = ((Reciever) obj).key.getSharedKey();
		    		if(unwrappedKey != null && _buffer.remaining() > 0){
		    			Message temp=Decrypt(convertBufferToBytes(_buffer), unwrappedKey);
		    			if(temp != null){
		    				Reciever.buffer.clear();
		    				Reciever.readBuf = ByteBuffer.wrap(Encoder.encode(temp));
		    				_logger.debug("Reciever received  message of type " + temp);
		    			}
		    		}
		    	}
				else{
					unwrappedKey = Transmitter_8815._key.getSharedKey();
					Message temp=Decrypt(convertBufferToBytes(_buffer), unwrappedKey);
					if(temp != null){
						Transmitter_8815.buffer.clear();
						Transmitter_8815.buffer = ByteBuffer.wrap(Encoder.encode(temp));
						_logger.debug("Transmitter received  message of type " + temp);
					}
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
