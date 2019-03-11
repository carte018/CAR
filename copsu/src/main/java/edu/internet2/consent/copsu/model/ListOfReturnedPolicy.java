package edu.internet2.consent.copsu.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ListOfReturnedPolicy {
	@JsonProperty("ReturnedPolicies")
	private ArrayList<ReturnedPolicy> contained;
	
	public ArrayList<ReturnedPolicy> getContained() {
		return contained;
	}

	public void setContained(ArrayList<ReturnedPolicy> contained) {
		this.contained = contained;
	}

	public ListOfReturnedPolicy() {
		contained = new ArrayList<ReturnedPolicy>();
	}
	
	public void addPolicy(ReturnedPolicy a) {
		contained.add(a);
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this.contained);
		return retval;
	}
	
}
