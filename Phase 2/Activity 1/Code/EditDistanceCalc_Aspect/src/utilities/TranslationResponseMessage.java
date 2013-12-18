package utilities;

import java.util.UUID;

public class TranslationResponseMessage extends Message
{
	private static final long serialVersionUID = 1L;
	private UUID responseId;
	private String response = "";
	
	public TranslationResponseMessage(String _response, TranslationRequestMessage request) {
		super();
		this.response = _response;
		this.responseId = request.getRquestId();
	}
	
	
	public TranslationResponseMessage(String _response) {
		super();
		this.response = _response;
	}
	
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
	
	
	public UUID getResponseId() {
		return responseId;
	}

	public void setResponseId(UUID responseId) {
		this.responseId = responseId;
	}
	
	
}
