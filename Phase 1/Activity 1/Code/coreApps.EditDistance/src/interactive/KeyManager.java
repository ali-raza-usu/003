package Interactive;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class KeyManager implements Runnable {

	private Logger _logger = Logger.getLogger(KeyManager.class);
	Encryption encyption = Encryption.getInstance();
	private HashMap<String, String> processList = new HashMap<String, String>();
	private boolean isAlive = true;

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	private ServerSocket serverSocket = null;

	public KeyManager() {
		processList.put("Server", "abcde");
		processList.put("Client", "abcdef");
	}

	public static void main(String[] args) throws IOException {
		Thread server = new Thread(new KeyManager());
		server.start();
	}

	public void extablishConnection() throws IOException {

		try {
			serverSocket = new ServerSocket(4444);
		} catch (IOException e) {
			_logger.debug("Could not listen on port: 4444.");
			e.printStackTrace();
		}

		// Listener will never end because it would continuously listen for
		// incomming request.
		while (isAlive) {
			_logger.debug("waiting for client !! on port "
					+ serverSocket.getLocalPort());
			new KeyManagerThread(serverSocket.accept(), this).start();
			_logger.debug("connection established");
		}
		_logger.debug("Server is closing the client socket");
		serverSocket.close();
	}

	@Override
	public void run() {
		try {
			extablishConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean authenticate(String _pName, String _password) {
		// Key : Process Name
		// Value : Password
		for (Entry<String, String> entry : processList.entrySet()) {
			if (entry.getKey().equals(_pName)
					&& entry.getValue().equals(_password)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void TestAuthentication01() {
		processList.put("Server", "abcde");
		processList.put("Client", "abcdef");

		Assert.assertTrue(authenticate("Server", "abcde"));
		Assert.assertFalse(authenticate("Client", "abcde"));
	}
}

class KeyManagerThread extends Thread {
	Logger _logger = Logger.getLogger(KeyManagerThread.class);
	private Socket socket = null;
	private KeyManager server = null;
	Encryption encryption = null;

	public KeyManagerThread(Socket socket, KeyManager server) {
		super("KeyManagerThread");
		this.socket = socket;
		this.server = server;
		encryption = server.encyption;
	}

	public void run() {
		try {
			processMessage();
		} catch (Exception e) {
			_logger.debug("Connection gets closed by the server");
			socket = null;
			// e.printStackTrace();
		}
	}

	void processMessage() throws IOException, ClassNotFoundException {
		System.out.println("hello");
		while (true) {
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());

			KeyRequest msg = (KeyRequest) in.readObject();
			if (msg != null) {
				if (server.authenticate(msg.getName(), msg.getPassword())) {
					out.writeObject(getResponse(msg));
					out.flush();
					_logger.debug("Process is autenticated and now Key Manager is now writing the message to "
							+ msg.getName());
				} else {
					out.writeObject(getResponse(null));
					out.flush();
					_logger.debug("Process is not authenticated and Key Manager is now writing the message to "
							+ msg.getName());
				}
			}
		}
	}

	private KeyResponse getResponse(KeyRequest _request) {
		if (_request == null) {
			KeyResponse _keyRes = new KeyResponse(null, null);
			return _keyRes;
		} else {
			SharedKey key = new SharedKey(encryption.getSharedKey());
			KeyResponse _keyRes = new KeyResponse(key, _request.getName());
			return _keyRes;
		}
	}
}
