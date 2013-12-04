package aspects.encyption;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import utilities.Message;

public class KMClient implements Runnable {

	Logger _logger = Logger.getLogger(KMClient.class);
	private Socket clientSocket = null;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	private SharedKey key = null;

	public KMClient() {

	}

	public void closeSocket() {
		try {
			clientSocket.close();
			_logger.debug("KMClient is getting closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connectToServer() {
		try {
			clientSocket = new Socket(InetAddress.getLocalHost(), 4444);
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(clientSocket.getInputStream());

		} catch (Exception e) {
			e.printStackTrace();
			_logger.debug("Don't know about host");
		}
	}

	public void run() {
		connectToServer();
		while (true) {
			try {
				KeyResponse resp = (KeyResponse) receiveMessage();
				if (resp != null) {
					key = resp.getKey();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void sendMessage(Message msg) {
		try {
			out.writeObject(msg);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Message receiveMessage() throws IOException {
		KeyResponse msg = null;
		try {
			msg = (KeyResponse) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return msg;
	}

	void closeConnections() throws IOException {
		out.close();
		in.close();
		clientSocket.close();
	}
}