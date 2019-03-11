package edu.internet2.consent.arpsi.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReturnedPrecedenceObject {
	private String policyBaseId;
	private String policyDescription;
	private long numericRank;
	public String getPolicyBaseId() {
		return policyBaseId;
	}
	public void setPolicyBaseId(String policyBaseId) {
		this.policyBaseId = policyBaseId;
	}
	public String getPolicyDescription() {
		return policyDescription;
	}
	public void setPolicyDescription(String policyDescription) {
		this.policyDescription = policyDescription;
	}
	public long getNumericRank() {
		return numericRank;
	}
	public void setNumericRank(long numericRank) {
		this.numericRank = numericRank;
	}
	
	// This is an output-only class -- no overrides needed

	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
