package edu.internet2.consent.car;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WrappedFullInputRequest {
	@JsonProperty("request")
	private FullInputRequest request;

	public FullInputRequest getRequest() {
		return request;
	}

	public void setRequest(FullInputRequest request) {
		this.request = request;
	}
	
	public String toJson() throws JsonProcessingException {
		// Return JSON representation of self
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
