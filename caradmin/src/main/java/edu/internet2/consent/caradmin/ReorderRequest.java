package edu.internet2.consent.caradmin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReorderRequest {

	String policyToChange;
	String operation;
	String policy;
	public String getPolicyToChange() {
		return policyToChange;
	}
	public void setPolicyToChange(String policyToChange) {
		this.policyToChange = policyToChange;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getPolicy() {
		return policy;
	}
	public void setPolicy(String policy) {
		this.policy = policy;
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		String retval = om.writeValueAsString(this);
		return retval;
	}
}
