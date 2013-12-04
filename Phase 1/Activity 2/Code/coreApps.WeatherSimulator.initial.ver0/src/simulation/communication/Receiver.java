package simulation.communication;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import utilities.Encoder;
import utilities.Message;
import utilities.messages.WeatherDataReading;
import utilities.messages.WeatherDataRequest;
import utilities.messages.WeatherDataVector;

public class Receiver extends Thread {

	private Logger logger = Logger.getLogger(Receiver.class);

	private ByteBuffer readBuf = ByteBuffer.allocateDirect(2048);
	ByteBuffer buffer = null;
	private Timer timer = new Timer();

	private FileWriter fstream = null;
	private BufferedWriter out = null;
	private boolean isPktRcvd = false;

	private DatagramChannel dc = null;
	private SocketAddress srcAddr = null;

	private int port;


	private boolean keepRunning = true;

	private void initalizeFile() {
		try {

			fstream = new FileWriter("SensorData.txt");
			out = new BufferedWriter(fstream);
		} catch (Exception e) {
		}
	}

	public Receiver(int portOne) {
		initalizeFile();
		this.port = portOne;
	}

	public void coreReceiver(int port) throws IOException {
		try {

			dc = DatagramChannel.open();
			dc.configureBlocking(false);
			srcAddr = new InetSocketAddress("localhost", port);

			Message _message = new WeatherDataRequest();
			buffer = ByteBuffer.wrap(Encoder.encode(_message));
			dc.send(buffer, srcAddr);
			// logger.debug("Sending request for Transmitter");
			while (keepRunning) {
				readBuf.clear();
				SocketAddress addr = dc.receive(readBuf);
				readBuf.flip();
				if (readBuf.remaining() > 0) {
					_message = convertBufferToMessage(readBuf);
					if (_message != null) {
						logger.debug(" Socket Address " + addr);
						if (addr.toString().contains(port + "")) {
							isPktRcvd = true;
							logger.debug("received data from Transmitter");
						}
						
						if(_message.getClass().getSimpleName().equals(WeatherDataVector.class.getSimpleName())){
							WeatherDataVector _data = (WeatherDataVector) _message;
	
							for (WeatherDataReading _reading : _data.getReadings())
								logger.debug("Rcvd from " + addr.toString()+ " : Data Speed : " + _reading.getSpeed()+ " Temperature "+ _reading.getTemperature());
							writeDataToAFile(_data, "Rcvd from " + addr.toString());
						}
					}
					if (addr.toString().contains(port + "")) {
						rescheduleTimer();
					} 
				}
			}
			// logger.debug("Out of while loop");
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			// logger.debug("finally called ");
			dc.close();
			srcAddr = null;
		}
		out.close();
		// System.exit(0);
	}
//It is rescheduled when data is received and than it never stops
//It is going to be rescheduled again & again, every time a packet is received
	
	private void rescheduleTimer() {
		isPktRcvd = false;
		if (isPktRcvd == false) {
			logger.debug(" going to schedule PacketLapse again....");
			timer.schedule(new PacketLapse(isPktRcvd,srcAddr), generateRand(300, 500));
			//isTimerDead[index] = true;
		}
	}

	private long generateRand(int min, int max) {
		Random rand = new Random();
		long time = rand.nextInt(max - min + 1) + min;
		return time;
	}

	private void writeDataToAFile(WeatherDataVector _data, String source) {
		try {
			for (WeatherDataReading _reading : _data.getReadings()) {
				out.write("\n" + source + " : Wind Speed "+ _reading.getSpeed() + " Wind Temperature "+ _reading.getTemperature());
				out.flush();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}

	}

	public void run() {
		try {
			coreReceiver(port);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	class PacketLapse extends TimerTask {
		boolean isPktRcvd;
		SocketAddress addr;

		public PacketLapse(boolean isPktRcvd, SocketAddress addr) {
			this.isPktRcvd = isPktRcvd;
			this.addr = addr;
		}

		@Override
		public void run() {
			if (isPktRcvd == false)
				try {
					if (dc != null && addr != null) {
						logger.debug("PacketLapse " + addr+ " is sending a packet ... to address "+ addr.toString() );
						WeatherDataRequest req = new WeatherDataRequest();
						buffer.clear();
						buffer = ByteBuffer.wrap(Encoder.encode(req));
						dc.send(buffer, addr);
						buffer.clear();
						rescheduleTimer();
					}
				} catch (IOException e) {
				}
		}
	}

	public static void main(String args[]) {
		int portOne = 8815;
		Receiver _receiver = new Receiver(portOne);
		_receiver.start();
	}

	private Message convertBufferToMessage(ByteBuffer buffer) {
		Message message = null;
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		if (bytes.length > 0) {
			message = Encoder.decode(bytes);
			buffer.clear();
			buffer = ByteBuffer.wrap(Encoder.encode(message));
		}
		return message;
	}
}
