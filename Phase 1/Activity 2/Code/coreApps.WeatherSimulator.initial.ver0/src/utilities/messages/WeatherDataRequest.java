package utilities.messages;

import java.io.Serializable;
import java.util.UUID;

import org.junit.Test;

import utilities.Message;

public class WeatherDataRequest extends Message implements Serializable {

	

	private static final long serialVersionUID = 1L;
	private UUID requestId = UUID.randomUUID();

	public WeatherDataRequest() {
	}


	@Test
	public void testRandomType() {
		int i = 0;
		while (i++ < 20) {
		}
	}

	public UUID getRequestId() {
		return requestId;
	}

	public void setRequestId(UUID requestId) {
		this.requestId = requestId;
	}

}
