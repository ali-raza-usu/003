package encryption;

import utilities.Message;

public class KeyRequest extends Message {

	private String name = null;
	private String password = null;

	public String getName() {
		return name;
	}

	public KeyRequest(String name, String password) {
		super();
		this.name = name;
		this.password = password;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassoword(String passoword) {
		this.password = passoword;
	}

}
