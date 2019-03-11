package edu.internet2.consent.icm.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IcmDecisionOnValues {

	@JsonProperty("valuesList")
	private List<String> returnedValuesList;
	private UserReleaseDirective releaseDecision;
	private AugmentedPolicyId augmentedPolicyId;
	
	public AugmentedPolicyId getAugmentedPolicyId() {
		return augmentedPolicyId;
	}
	public void setAugmentedPolicyId(AugmentedPolicyId policyId) {
		this.augmentedPolicyId = policyId;
	}
	@JsonProperty("valuesList")
	public List<String> getReturnedValuesList() {
		return returnedValuesList;
	}
	public void setReturnedValuesList(ArrayList<String> returnedValuesList) {
		this.returnedValuesList = returnedValuesList;
	}
	@JsonProperty("releaseDecision")
	public UserReleaseDirective getReleaseDecision() {
		return releaseDecision;
	}
	public void setReleaseDecision(UserReleaseDirective releaseDecision) {
		this.releaseDecision = releaseDecision;
	}
	
}
