package simulation.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import utilities.RequestType;
import utilities.WeatherDataReading;
import utilities.WeatherDataRequest;
import utilities.WeatherDataVector;
import utilities.Encoder;
import utilities.Message;

public class Transmitter_8816 extends Thread {

	public Transmitter_8816(int PortNumber) {
		portNumber = PortNumber;
		sensor = WeatherStationSimulator.createInstance(500, 0);
	}

	private Logger logger = Logger.getLogger(Transmitter_8816.class);
	private Selector sckt_manager = null;
	DatagramChannel dgc = null;
	DatagramChannel client = null;
	private ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
	private SocketAddress destAddr = null;
	private WeatherStationSimulator sensor = null;
	private int portNumber;
	private boolean keepSending = true;

	public WeatherStationSimulator get_sensor() {
		return sensor;
	}

	public void set_sensor(WeatherStationSimulator _sensor) {
		this.sensor = _sensor;
	}

	public void run() {
		try {
			coreTransmitter(portNumber);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	public static void main(String args[]) {
		Transmitter_8816 transmitter = new Transmitter_8816(8816);
		transmitter.start();
	}

	private void coreTransmitter(int PortNo) {
		try {
			dgc = DatagramChannel.open();
			try {
				logger.debug("Binding Server Socket to port " + PortNo);
				dgc.socket().bind(new InetSocketAddress("localhost", PortNo));
				sckt_manager = SelectorProvider.provider().openSelector();
				dgc.configureBlocking(false);

				dgc.register(sckt_manager, dgc.validOps());
				boolean isReading = true;
				while (isReading) {
					int num = sckt_manager.select();
					if (num < 1) {
						continue;
					}
					for (Iterator<SelectionKey> i = sckt_manager.selectedKeys()
							.iterator(); i.hasNext();) {
						SelectionKey key = i.next();
						i.remove();

						client = (DatagramChannel) key.channel();
						if (key.isReadable()) {
							buffer.clear();
							destAddr = client.receive(buffer);
							// logger.debug("Transmitter " + PortNo +
							// " : received data from addr: "+destAddr);
							buffer.flip();
							WeatherDataRequest _data = (WeatherDataRequest) convertBufferToMessage(buffer);
							if (_data.getClass().equals(
									WeatherDataRequest.class)) {
								// logger.debug(portNumber+" : received request of type "+
								// _data.getReqType().name());

								if (_data.getReqType() == RequestType.SEND) {
									// logger.debug(portNumber+" A seperate thread started sending data readings");
									new Thread(new Sender(_data)).start();
								} else if (_data.getReqType() == RequestType.PAUSE) {
									keepSending = false;
									// logger.debug(portNumber+" : Rcvd: PAUSE: Sever paused transmision of messages Transmitter");
								} else if (_data.getReqType() == RequestType.STOP) {
									isReading = false;
									keepSending = false;
									// logger.debug(portNumber+" Rcvd: STOP: Now closing the Transmitter");
								}
							}
						}
					}
				}
				// client.close();
			} catch (IOException e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			} finally {
				// logger.debug("Finally closing the connection! ");
				if (dgc != null) {
					try {
						dgc.close();
					} catch (IOException e) {
						logger.error(ExceptionUtils.getStackTrace(e));
					}
				}
			}
			// System.exit(0);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	private void sendData(int PortNo, DatagramChannel client,
			WeatherDataRequest _data) throws Exception {
		int index = 0;
		while (keepSending && !sensor.getList().isEmpty()) {
			WeatherDataVector _dataVector = sensor.getList().poll();
			_dataVector.setResponseId(_data.getRequestId());
			for (WeatherDataReading _reading : _dataVector.getReadings()) {
				logger.debug(portNumber + " : Wind Speed "
						+ _reading.getSpeed() + " Wind Temperature "
						+ _reading.getTemperature());
			}
			ByteBuffer buffer = ByteBuffer.wrap(Encoder.encode(_dataVector));
			// logger.debug("After sending value " );//+
			// CompressManager.getInstance().getCompression().name());
			Thread.sleep(500);
			if (keepSending) {// Need to check it again due to the sleeping
								// state of thread
				client.send(buffer, destAddr);
				index++;
			}
		}
		if (!sensor.getList().isEmpty() && keepSending == false)
			logger.debug("Thread is interrupted by the PAUSE request from the receiver ");
		// Toggle flag : Other thread may interrupt the thread and now resetting
		// the thread
		keepSending = true;
		logger.debug("Transmitter " + PortNo + " : sent " + index
				+ " messages of type " + _data.getClass().getSimpleName());
	}

	class Sender implements Runnable {

		WeatherDataRequest request = null;

		public Sender(WeatherDataRequest _request) {
			request = _request;
		}

		@Override
		public void run() {
			try {
				logger.debug("Sender is running !");
				if (client != null)
					sendData(portNumber, client, request);
				// logger.debug("Finnished sending all the available data readings ");
			} catch (Exception e) {
				logger.debug(ExceptionUtils.getStackTrace(e));
			}
		}

	}

	private Message convertBufferToMessage(ByteBuffer buffer) {
		Message message = null;
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		// logger.debug("convertBufferToMessage of length "+ bytes.length);
		message = (Message) Encoder.decode(bytes);
		buffer.clear();
		buffer = ByteBuffer.wrap(Encoder.encode(message));
		return message;
	}
}
