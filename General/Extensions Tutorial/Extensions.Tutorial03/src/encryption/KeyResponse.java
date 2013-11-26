package encryption;

import utilities.Message;

public class KeyResponse extends Message {

	private SharedKey key = null;
	private String name = null;

	public KeyResponse(SharedKey key, String name) {
		super();
		this.key = key;
		this.name = name;
	}

	public SharedKey getKey() {
		return key;
	}

	public void setKey(SharedKey key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
