package edu.internet2.consent.car;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DecisionResponseHeader {

	private String carInstanceId;
	private String decisionId;
	public String getCarInstanceId() {
		return carInstanceId;
	}
	public void setCarInstanceId(String carInstanceId) {
		this.carInstanceId = carInstanceId;
	}
	public String getDecisionId() {
		return decisionId;
	}
	public void setDecisionId(String decisionId) {
		this.decisionId = decisionId;
	}
	
	public String toJson() throws JsonProcessingException {
		// Return JSON representation of self
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
