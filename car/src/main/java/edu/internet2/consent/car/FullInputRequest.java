package edu.internet2.consent.car;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.icm.model.DecisionRequestObject;

public class FullInputRequest {

	@JsonProperty("header")
	private FullInputRequestHeader header;
	@JsonProperty("request")
	private DecisionRequestObject request;  // wrapped ICM decision request
	
	
	public FullInputRequestHeader getHeader() {
		return header;
	}
	public void setHeader(FullInputRequestHeader header) {
		this.header = header;
	}
	
	/**
	 * @return the request
	 */
	public DecisionRequestObject getRequest() {
		return request;
	}
	/**
	 * @param request the request to set
	 */
	public void setRequest(DecisionRequestObject request) {
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
