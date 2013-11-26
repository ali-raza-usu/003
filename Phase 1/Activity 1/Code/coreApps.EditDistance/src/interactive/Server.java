package interactive;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import utilities.Encoder;
import utilities.TranslationMessage;

public class Server extends Thread {

	Logger _logger = Logger.getLogger(Server.class);
	SelectionKey selkey = null;
	Selector sckt_manager = null;
	ByteBuffer buffer = ByteBuffer.allocateDirect(2048);

	public Server() {
	}

	public void run() {
		try {
			coreServer();
		} catch (Exception e) {
			_logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	public static void main(String args[]) {
		Server _server = new Server();
		_server.start();
	}

	private void coreServer() {
		try {
			ServerSocketChannel ssc = ServerSocketChannel.open();

			try {
				// Establishing New Channel
				ssc.socket().bind(new InetSocketAddress(8897));
				sckt_manager = SelectorProvider.provider().openSelector();
				ssc.configureBlocking(false);
				SocketChannel client = null;
				ssc.register(sckt_manager, SelectionKey.OP_ACCEPT);
				_logger.debug("Channel Establishd");

				TranslationMessage msg = null;

				while (true) {
					sckt_manager.select();
					for (Iterator<SelectionKey> i = sckt_manager.selectedKeys()
							.iterator(); i.hasNext();) {
						SelectionKey key = i.next();
						i.remove();
						if (key.isConnectable()) {
							((SocketChannel) key.channel()).finishConnect();
						}
						// Accepting a new Client
						if (key.isAcceptable()) {
							client = ssc.accept();
							client.configureBlocking(false);
							client.socket().setTcpNoDelay(true);
							client.register(sckt_manager, SelectionKey.OP_READ);
							_logger.debug("A new client established");
						}
						// reading and writing data
						if (key.isReadable()) {
							buffer = ByteBuffer.allocateDirect(2048);
							buffer.clear();
							while (client.read(buffer) <= 0)
								;
							{

								buffer.flip();
								// New Input Message
								msg = (TranslationMessage) convertBufferToMessage(buffer);
								_logger.debug("Received " + msg.getData1());
								if (msg.getData1().equals("quit")) {
									_logger.debug("Now disconnecting the client");
									client.close();
									return;
								}
							}
							int num = 1 + (int) (Math.random() * ((1 - 4) + 1));
							Thread.sleep(num * 200);
							int result = LevenshteinDistance(msg.getData1(),
									msg.getData2());
							msg = new TranslationMessage(
									"Levenshtein Distance between string : "
											+ msg.getData1() + " and string "
											+ msg.getData2() + " is " + result);
							buffer.clear();
							buffer = ByteBuffer.wrap(Encoder.encode(msg));
							client.write(buffer);
							_logger.debug("Sending " + msg.getResponse());
							if (msg.getData1().equals("quit")
									|| msg.getData2().equals("quit")) {
								client.close();
								return;
							}
						}
					}
				}
			} catch (IOException e) {
				_logger.error(ExceptionUtils.getStackTrace(e));
			} finally {
				try {
					if (ssc != null) {
						ssc.close();
					}
				} catch (IOException e) {
					_logger.error(ExceptionUtils.getStackTrace(e));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			_logger.error(ExceptionUtils.getStackTrace(e));

		}
	}

	private TranslationMessage convertBufferToMessage(ByteBuffer buffer) {
		TranslationMessage message = null;
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		message = (TranslationMessage) Encoder.decode(bytes);
		buffer.clear();
		buffer = ByteBuffer.wrap(Encoder.encode(message));
		return message;
	}

	int LevenshteinDistance(CharSequence str1, CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));

		return distance[str1.length()][str2.length()];
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	@Test
	public void testStringDifference() {
		String s = "kitten";
		String l = "sittin";
		int distance = LevenshteinDistance(s, l);
		Assert.assertEquals(2, distance);

		s = "kitten";
		l = "";
		distance = LevenshteinDistance(s, l);
		Assert.assertEquals(6, distance);

		s = "";
		l = "";
		distance = LevenshteinDistance(s, l);
		Assert.assertEquals(0, distance);

		s = "kitten";
		l = "sittin";
		distance = LevenshteinDistance(l, s);
		Assert.assertEquals(2, distance);

		s = "kittn";
		l = "sittn";
		distance = LevenshteinDistance(l, s);
		Assert.assertEquals(1, distance);

		s = "kitten";
		l = "sittn";
		distance = LevenshteinDistance(l, s);
		Assert.assertEquals(2, distance);
	}
}
