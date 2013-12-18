package utilities;

import java.util.UUID;

public class TranslationRequestMessage extends Message {
	private static final long serialVersionUID = 1L;
	private UUID rquestId = UUID.randomUUID();
	private String data1 = "";
	private String data2 = "";
	
	
	public TranslationRequestMessage(String _response, TranslationResponseMessage response) {
		super();
		//this.response = _response;
		this.rquestId = response.getResponseId();
		
	}

	
	public TranslationRequestMessage(String _data1, String _data2) {
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

	

	public UUID getRquestId() {
		return rquestId;
	}

	public void setRquestId(UUID rquestId) {
		this.rquestId = rquestId;
	}

	

	
	

}
