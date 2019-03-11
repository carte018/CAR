package edu.internet2.consent.car;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DecisionResponse {

	@JsonProperty("header")
	private DecisionResponseHeader header;
	@JsonProperty("decisions")
	private ArrayList<NameValueDecision> decisions;
	public DecisionResponseHeader getHeader() {
		return header;
	}
	public void setHeader(DecisionResponseHeader header) {
		this.header = header;
	}
	public ArrayList<NameValueDecision> getDecisions() {
		return decisions;
	}
	public void setDecisions(ArrayList<NameValueDecision> decisions) {
		this.decisions = decisions;
	}
	
	public String toJson() throws JsonProcessingException {
		// Return JSON representation of self
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
