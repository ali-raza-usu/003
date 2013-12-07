package utilities;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	public Message(){}
	
	public Message(Object b)
	{
		
	}

	public String toString() {
		return this.getClass().toString();
	}
}
