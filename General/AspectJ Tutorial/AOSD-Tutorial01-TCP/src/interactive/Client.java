package interactive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class Client extends Thread {
	Logger _logger = Logger.getLogger(Client.class);
	SelectionKey selkey = null;
	Selector sckt_manager = null;
	ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
	ByteBuffer readBuf = ByteBuffer.allocateDirect(1024);
	BufferedReader bufReader = null;

	public Client() {
	}

	public void coreClient() {

		String _data1 = null;
		String _data2 = null;
		SocketChannel sc = null;
		try {
			// Connecting to Server

			sc = SocketChannel.open();
			sc.configureBlocking(false);
			sc.connect(new InetSocketAddress(8897));
			while (!sc.finishConnect())
				; // wait until the connection gets established
			_logger.debug("Connection is accepted by server");

			while (true) {

				if (sc.isConnected()) {
					try {
						if (sc != null) {
							System.out
									.println("===============Levenshtein Translator======================");
							System.out.print("Enter String 1 : ");
							bufReader = new BufferedReader(
									new InputStreamReader(System.in));
							_data1 = bufReader.readLine();

							System.out.print("Enter String 2 : ");
							bufReader = new BufferedReader(
									new InputStreamReader(System.in));
							_data2 = bufReader.readLine();

							TranslationMessage msg = null;
							if (_data1 != null && _data2 != null) {
								msg = new TranslationMessage(_data1, _data2);
								buffer = ByteBuffer.wrap(Encoder.encode(msg));
								sc.write(buffer);
								_logger.debug("Sending strings '"
										+ msg.getData1() + "' and '"
										+ msg.getData2() + "'");
								if (msg.getData1().equals("quit")
										|| msg.getData2().equals("quit")) {
									sc.close();
									return;
								}
							}
							buffer.clear();

							readBuf.clear();

							while (sc.read(readBuf) <= 0)
								;
							readBuf.flip();
							msg = (TranslationMessage) convertBufferToMessage(readBuf);
							System.out.println("Received " + msg.getResponse());
							_logger.debug("Received " + msg.getResponse());
						}
					} catch (Exception e) {
						e.printStackTrace();
						_logger.error(ExceptionUtils.getStackTrace(e));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			_logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			try {
				if (sc.isConnected()) {
					sc.close();
				}
				if (bufReader != null) {
					bufReader.close();
				}
			} catch (IOException e) {
				_logger.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	public void run() {
		try {
			coreClient();
		} catch (Exception e) {
			e.printStackTrace();
			_logger.error(e);
		}
	}

	public static void main(String args[]) {
		Client _client = new Client();
		_client.start();
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
