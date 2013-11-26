package aspects;

import interactive.Client;
import interactive.Encoder;
import interactive.Message;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public aspect LogTimeOnSend {

	Logger _logger = Logger.getLogger(LogTimeOnSend.class);

	private pointcut ChannelWrite(SocketChannel _channel, ByteBuffer _buffer) :
			call(* SocketChannel+.write(ByteBuffer)) && target(_channel) && args(_buffer);

	Object around(SocketChannel _channel, ByteBuffer _buffer) : ChannelWrite(_channel, _buffer){
		// _logger.debug("Someone is send the data ");
		ByteBuffer tempBuf = _buffer.duplicate();
		 Message msg = convertBufferToMessage(tempBuf);
		_logger.debug("Data in the buffer " + tempBuf.remaining());
		Object obj = thisJoinPoint.getThis();
		if (obj instanceof Client) {
				_logger.debug("Client send the message  "+ msg.getClass()+" at time " + getCurrentTime());
		}

		return proceed(_channel, _buffer);
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
	
	private String getCurrentTime(){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
