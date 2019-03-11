package edu.internet2.consent.icm.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ListOfUserReturnedPolicy {
	@JsonProperty("ReturnedPolicies")
	private ArrayList<UserReturnedPolicy> contained;
	
	public ArrayList<UserReturnedPolicy> getContained() {
		return contained;
	}

	public void setContained(ArrayList<UserReturnedPolicy> contained) {
		this.contained = contained;
	}

	public ListOfUserReturnedPolicy() {
		contained = new ArrayList<UserReturnedPolicy>();
	}
	
	public void addPolicy(UserReturnedPolicy a) {
		contained.add(a);
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this.contained);
		return retval;
	}
	
}
