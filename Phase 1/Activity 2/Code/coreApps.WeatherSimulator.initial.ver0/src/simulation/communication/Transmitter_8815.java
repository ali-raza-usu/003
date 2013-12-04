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

import utilities.Encoder;
import utilities.Message;
import utilities.messages.WeatherDataRequest;
import utilities.messages.WeatherDataVector;

public class Transmitter_8815 extends Thread {

	public Transmitter_8815(int PortNumber) {
		portNumber = PortNumber;
		sensor = WeatherStationSimulator.createInstance(2000, 0);
	}

	private Logger logger = Logger.getLogger(Transmitter_8815.class);
	private Selector sckt_manager = null;
	DatagramChannel dgc = null;
	DatagramChannel client = null;
	private ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
	private SocketAddress destAddr = null;
	private WeatherStationSimulator sensor = null;
	private int portNumber;
	boolean isReading = true;

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
		int port = 8815;
		Transmitter_8815 transmitter = new Transmitter_8815(port);
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

				while (isReading){
					int num = sckt_manager.select();
					if (num < 1) {
						continue;
					}
					for (Iterator<SelectionKey> i = sckt_manager.selectedKeys().iterator(); i.hasNext();) {
						SelectionKey key = i.next();
						i.remove();

						client = (DatagramChannel) key.channel();
						if (key.isReadable()) {
							buffer.clear();
							destAddr = client.receive(buffer);
							logger.debug("Transmitter " + PortNo+ " : received data from addr: " + destAddr);
							buffer.flip();
							WeatherDataRequest _data = null;
							if (buffer.remaining() > 0) {
								_data = (WeatherDataRequest) convertBufferToMessage(buffer);
							}
							if (_data != null && _data.getClass().equals(WeatherDataRequest.class)) {
								sendData(PortNo, client, _data);
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

	private void sendData(int PortNo, DatagramChannel client,WeatherDataRequest _data) throws Exception {
		int index = 0;
		while (!sensor.getList().isEmpty()) {
			WeatherDataVector _dataVector = sensor.getList().poll();
			_dataVector.setResponseId(_data.getRequestId());
//			for (WeatherDataReading _reading : _dataVector.getReadings()) {
//				logger.debug("Resp ID: " + _dataVector.getResponseId()+ " - port: " + portNumber + " : Wind Speed "+ _reading.getSpeed() + " Wind Temperature " + _reading.getTemperature());
//			}
			buffer.clear();
			buffer = ByteBuffer.wrap(Encoder.encode(_dataVector));

			Thread.sleep(50);
			if (client.isOpen()) {// Need to check it again due to the sleeping state of thread
				client.send(buffer, destAddr);
				index++;
			}
		}
		logger.debug("Transmitter " + PortNo + " : sent " + index+ " messages of type " + _data.getClass().getSimpleName()+ " Resp ID: " + _data.getRequestId());
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
