package edu.internet2.consent.car;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WrappedDecisionResponse {

	@JsonProperty("decisionResponse")
	private DecisionResponse decisionResponse;

	public DecisionResponse getDecisionResponse() {
		return decisionResponse;
	}

	public void setDecisionResponse(DecisionResponse decisionResponse) {
		this.decisionResponse = decisionResponse;
	}
	
	public String toJson() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
