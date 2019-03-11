package edu.internet2.consent.car;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//formatting
public class WrappedInputRequest {
	@JsonProperty("request")
	private InputRequest request;

	public InputRequest getRequest() {
		return request;
	}

	public void setRequest(InputRequest request) {
		this.request = request;
	}
	
	public String toJson() throws JsonProcessingException {
		// Return JSON representation of self
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
