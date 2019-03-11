package edu.internet2.consent.arpsi.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ListOfOrgReturnedPolicy {
	@JsonProperty("returnedPolicies")
	private ArrayList<OrgReturnedPolicy> contained;

	@JsonProperty("returnedPolicies")
	public ArrayList<OrgReturnedPolicy> getContained() {
		return contained;
	}

	@JsonProperty("returnedPolicies")
	public void setContained(ArrayList<OrgReturnedPolicy> contained) {
		this.contained = contained;
	}
	
	public ListOfOrgReturnedPolicy() {
		contained = new ArrayList<OrgReturnedPolicy>();
	}
	
	public void addPolicy(OrgReturnedPolicy o) {
		contained.add(o);
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this.contained);
		return retval;
	}
}