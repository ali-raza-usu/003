package Interactive;

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

import utilities.Encoder;
import utilities.Message;
import utilities.messages.RequestType;
import utilities.messages.WeatherDataReading;
import utilities.messages.WeatherDataRequest;
import utilities.messages.WeatherDataVector;

public class Transmitter_8815 extends Thread {

	
	
	
	
	public Transmitter_8815(int PortNumber) {
		portNumber = PortNumber;
		sensor = WeatherStationSimulator.createInstance(500, 0);
	}

	private Logger logger = Logger.getLogger(Transmitter_8815.class);
	private Selector sckt_manager = null;
	DatagramChannel dgc = null;
	DatagramChannel client = null;
	//private ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
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

	@Override
	public void run() {
		try {
			
			coreTransmitter(portNumber);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	public static void main(String args[]) {
		Transmitter_8815 transmitter = new Transmitter_8815(8815);
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
							
							//buffer.flip();
							WeatherDataRequest _data = (WeatherDataRequest) convertBufferToMessage(buffer);
							if (_data.getClass().equals(
									WeatherDataRequest.class)) {
								

								if (_data.getReqType() == RequestType.SEND) {
									
									new Thread(new Sender(_data)).start();
								} else if (_data.getReqType() == RequestType.PAUSE) {
									keepSending = false;
									
								} else if (_data.getReqType() == RequestType.STOP) {
									isReading = false;
									keepSending = false;
									
								}
							}
						}
					}
				}
				
			} catch (IOException e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			} finally {
				
				if (dgc != null) {
					try {
						dgc.close();
					} catch (IOException e) {
						logger.error(ExceptionUtils.getStackTrace(e));
					}
				}
			}
			
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
			buffer = ByteBuffer.wrap(Encoder.encode(_dataVector));
			
			Thread.sleep(500);
			if (keepSending) {
								
				client.send(buffer, destAddr);
				index++;
			}
		}
		if (!sensor.getList().isEmpty() && keepSending == false)
			logger.debug("Thread is interrupted by the PAUSE request from the receiver ");
		
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
				
			} catch (Exception e) {
				logger.debug(ExceptionUtils.getStackTrace(e));
			}
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
