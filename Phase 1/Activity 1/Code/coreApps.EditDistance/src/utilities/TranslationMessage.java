package utilities;

import java.util.UUID;

public class TranslationMessage extends Message {

	private static final long serialVersionUID = 1L;
	private UUID rquestId = UUID.randomUUID();
	private UUID responseId;
	private String data1 = "";
	private String data2 = "";
	private String response = "";

	public TranslationMessage(String _response, TranslationMessage request) {
		super();
		this.response = _response;
		this.responseId = request.rquestId;
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
		return rquestId;
	}

	public void setRquestId(UUID rquestId) {
		this.rquestId = rquestId;
	}

	public UUID getResponseId() {
		return responseId;
	}

	public void setResponseId(UUID responseId) {
		this.responseId = responseId;
	}

}
