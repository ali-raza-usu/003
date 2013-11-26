package encryption;

import java.security.Key;

import utilities.Message;

public class SharedKey extends Message {

	private Key sharedKey = null;

	public SharedKey(Key sharedKey) {
		super();
		this.sharedKey = sharedKey;
	}

	public Key getSharedKey() {
		return sharedKey;
	}

	public void setSharedKey(Key sharedKey) {
		this.sharedKey = sharedKey;
	}

}
