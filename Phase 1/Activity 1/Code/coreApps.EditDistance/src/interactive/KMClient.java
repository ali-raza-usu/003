package Interactive;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import utilities.Message;

public class KMClient {//implements {//Runnable {

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
	
	public void getSharedKey(String name, String password) throws IOException
	{
		//Message key=null;
		KeyRequest _keyRequest = new KeyRequest(name, password);
		System.out.println("getshsred " + key +",,," + _keyRequest.getName());
		connectToServer();
		System.out.println("getshsred " + key +",,," + _keyRequest.getName());
		sendMessage(_keyRequest);
		System.out.println("aftersend " + key); 
		while(true){
		try {
			KeyResponse resp = (KeyResponse) receiveMessage();
			if (resp != null) {
				key = resp.getKey();
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
		System.out.println("after run " + key);
		closeConnections();
		

	}
	
	public SharedKey getKey()
	{
		return key;
	}
	
}