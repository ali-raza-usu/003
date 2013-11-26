package interactive;

import java.io.Serializable;
import java.util.UUID;

public class TranslationMessage extends Message {

	private static final long serialVersionUID = 1L;
	private UUID messageId = UUID.randomUUID();
	private String data1 = "";
	private String data2 = "";
	private String response = "";

	public TranslationMessage(String _response, UUID _messageId) {
		super();
		this.response = _response;
		this.messageId = _messageId;
	}

	public TranslationMessage(String _response) {
		super();
		this.response = _response;
	}

	public TranslationMessage(String _data1, String _data2) {
		super();
		this.data1 = _data1;
		this.data2 = _data2;
	}

	public String getData1() {
		return data1;
	}

	public void setData1(String data) {
		this.data1 = data;
	}

	@Override
	public String toString() {
		
		return data1;
	}

	public String getData2() {
		return data2;
	}

	public void setData2(String data2) {
		this.data2 = data2;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public UUID getRquestId() {
		return messageId;
	}

	public void setRquestId(UUID rquestId) {
		this.messageId = rquestId;
	}
}
