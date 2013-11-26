package aspects;

import interactive.Client;
import interactive.Encoder;
import interactive.Message;
import interactive.TranslationMessage;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public aspect LogTimeOnReceive {

	private static Logger _logger = Logger
			.getLogger(LogTimeOnReceive.class);

	private pointcut ChannelRead(DatagramChannel _channel, ByteBuffer _buffer) :
		call(* DatagramChannel+.read(ByteBuffer)) && target(_channel) && args(_buffer);

	int around(DatagramChannel _channel, ByteBuffer _buffer) : ChannelRead(_channel, _buffer) {
		ByteBuffer tempBuf = _buffer.duplicate();

		int readBytes = proceed(_channel, _buffer);
		if (readBytes > 0) {
			Object obj = thisJoinPoint.getThis();
			if (obj instanceof Client) {
				TranslationMessage msg = (TranslationMessage) convertBufferToMessage(tempBuf);
				_logger.debug("Client received the message "+ msg.getClass()+" at time " + getCurrentTime());
				
			}
		}
		return readBytes;
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
