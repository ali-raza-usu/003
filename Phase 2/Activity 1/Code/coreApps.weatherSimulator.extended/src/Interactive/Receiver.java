package Interactive;

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

import encryption.KMClient;

import utilities.Encoder;
import utilities.Message;

import utilities.messages.RequestType;
import utilities.messages.WeatherDataReading;
import utilities.messages.WeatherDataRequest;
import utilities.messages.WeatherDataVector;

public class Receiver extends Thread {

	
	private Logger logger = Logger.getLogger(Receiver.class);

	//private ByteBuffer readBuf = ByteBuffer.allocateDirect(4096);
	private Timer[] timer = new Timer[] { new Timer(), new Timer() }; // To ask
																		// the
																		// transmitterOne
																		// "Send me more packets"

	private FileWriter fstream = null;
	private BufferedWriter out = null;
	private boolean[] isPktRcvd = new boolean[] { false, false };

	private boolean timerActive[] = new boolean[] { false, false };

	private DatagramChannel dc = null;
	private SocketAddress[] srcAddr = new SocketAddress[2];

	private int portOne;
	private int portTwo;

	private boolean keepRunning = true;
	private KMClient _KMClient=new KMClient();

	
	

	private void initalizeFile() {
		try {

			fstream = new FileWriter("sensorData.txt");
			out = new BufferedWriter(fstream);
			
		} catch (Exception e) {
		}
	}

	public Receiver() {

	}

	public Receiver(int portOne, int portTwo) {
		initalizeFile();
		this.portOne = portOne;
		this.portTwo = portTwo;
		
	}

	public void coreReceiver(int portOne, int portTwo) throws IOException {
		try {
			dc = DatagramChannel.open();
			dc.configureBlocking(false);
			srcAddr[0] = new InetSocketAddress("localhost", portOne);// To send
																		// request
																		// to
																		// Transmitter1
			srcAddr[1] = new InetSocketAddress("localhost", portTwo);// To send
																		// request
																		// to
																		// Transmitter2
			ByteBuffer buffer = ByteBuffer.wrap(Encoder
					.encode(new WeatherDataRequest(RequestType.SEND)));
			dc.send(buffer, srcAddr[0]);
			
			buffer = ByteBuffer.wrap(Encoder.encode(new WeatherDataRequest(
					RequestType.SEND)));
			dc.send(buffer, srcAddr[1]);
			
			while (keepRunning) {
				readBuf.clear();
				SocketAddress addr = dc.receive(readBuf);
				//readBuf.flip();
				if (readBuf.remaining() > 0) {
					Message _message = convertBufferToMessage(readBuf);
					System.out.println("Receive.java : received message "  + _message); 
					if (_message != null) {
						

						if (addr.toString().contains(portOne + "")) {
							isPktRcvd[0] = true;
							
						} else {
							isPktRcvd[1] = true;
							
						}
						WeatherDataVector _data = (WeatherDataVector) _message;
						for (WeatherDataReading _reading : _data.getReadings())
							logger.debug("Rcvd from " + addr.toString()
									+ " : Data Speed : " + _reading.getSpeed()
									+ " Temperature "
									+ _reading.getTemperature()
									+ " Compression Level ");
																
						writeDataToAFile(_data, "Rcvd from " + addr.toString());
						readBuf =ByteBuffer.allocateDirect(5048);
						_message = null;
					
					}
					if (addr.toString().contains(portOne + "")) {
						rescheduleTimer(0);
					} else {
						rescheduleTimer(1);
					}

				}
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			
			dc.close();
			srcAddr[0] = null;
			srcAddr[1] = null;
		}
		out.close();
		
	}

	private void rescheduleTimer(int index) {
		isPktRcvd[index] = false;
		if (isPktRcvd[index] == false && !timerActive[index]) {
			
			timer[index].schedule(new PacketLapse(isPktRcvd[index],
					srcAddr[index], index), generateRand(1000, 2000));
			timerActive[index] = true;
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
				out.write("\n" + source + " : Wind Speed "
						+ _reading.getSpeed() + " Wind Temperature "
						+ _reading.getTemperature());
				out.flush();
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}

	}

	@Override
	public void run() {
		try {
			
			coreReceiver(portOne, portTwo);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	class PacketLapse extends TimerTask {
		boolean isPktRcvd;
		SocketAddress addr;
		int tmrIndex;

		public PacketLapse(boolean isPktRcvd, SocketAddress addr, int tmrIndex) {
			this.isPktRcvd = isPktRcvd;
			this.addr = addr;
			this.tmrIndex = tmrIndex;
		}

		@Override
		public void run() {
			try
			{
			_KMClient.getSharedKey("Client", "abcdef");
			key = _KMClient.getKey();
			}
			catch(IOException e)
			{
			}
			
			if (isPktRcvd == false)
				try {
					if (dc != null && addr != null) {
						RequestType type = randomType();
						logger.debug("PacketLapse " + addr
								+ " is sending a packet of type " + type.name()
								+ "...");
						dc.send(ByteBuffer.wrap(Encoder
								.encode(new WeatherDataRequest(type))), addr);

						timerActive[tmrIndex] = false;
						if (type == RequestType.STOP) {
							timerActive[tmrIndex] = true;
							srcAddr[tmrIndex] = null;
							
							if (srcAddr[0] == null && srcAddr[1] == null) {
								keepRunning = false;
								logger.debug("Now quitting the receiver ....!");
							}
						} else if (type == RequestType.PAUSE) {
							
							rescheduleTimer(tmrIndex);
						}
					}
				} catch (IOException e) {
				}
		}
	}

	private RequestType randomType() {
		RequestType[] _type = RequestType.values();
		int val = 0 + (int) (Math.random() * 4);
		if (val >= 3)
			val = 0;
		return _type[val];
	}

	public static void main(String args[]) {
		Receiver _receiver = new Receiver(8815, 8816);
		
		_receiver.start();
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
