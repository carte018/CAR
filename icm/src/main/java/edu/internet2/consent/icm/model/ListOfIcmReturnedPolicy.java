package edu.internet2.consent.icm.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ListOfIcmReturnedPolicy {

	@JsonProperty("returnedPolicies")
	private ArrayList<IcmReturnedPolicy> contained;

	@JsonProperty("returnedPolicies")
	public ArrayList<IcmReturnedPolicy> getContained() {
		return contained;
	}

	@JsonProperty("returnedPolicies")
	public void setContained(ArrayList<IcmReturnedPolicy> contained) {
		this.contained = contained;
	}
	
	public ListOfIcmReturnedPolicy() {
		contained = new ArrayList<IcmReturnedPolicy>();
	}
	
	public void addPolicy(IcmReturnedPolicy i) {
		contained.add(i);
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this.contained);
		return retval;
	}
	
	
}
