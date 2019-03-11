package edu.internet2.consent.icm.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DecisionOnValues {

	private List<String> valuesList;
	private UserReleaseDirective releaseDecision;
	private PolicyId policyId;
	
	public PolicyId getPolicyId() {
		return policyId;
	}
	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}
	@JsonProperty("valuesList")
	public List<String> getValuesList() {
		return valuesList;
	}
	@JsonProperty("valuesList")
	public void setValuesList(ArrayList<String> returnedValuesList) {
		this.valuesList = returnedValuesList;
	}
	@JsonProperty("returnedValuesList")
	public void setReturnedValuesList(ArrayList<String> returnedValuesList) {
		this.valuesList = returnedValuesList;
	}
	@JsonProperty("releaseDecision")
	public UserReleaseDirective getReleaseDecision() {
		return releaseDecision;
	}
	public void setReleaseDecision(UserReleaseDirective releaseDecision) {
		this.releaseDecision = releaseDecision;
	}
	
}
