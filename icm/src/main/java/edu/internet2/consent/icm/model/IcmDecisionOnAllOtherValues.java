package edu.internet2.consent.icm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IcmDecisionOnAllOtherValues {

	private UserReleaseDirective releaseDecision;
	private AllOtherValuesConst allOtherValuesConst;
	private AugmentedPolicyId augmentedPolicyId;
	
	@JsonProperty("releaseDecision")
	public UserReleaseDirective getReleaseDecision() {
		return releaseDecision;
	}
	public void setReleaseDecision(UserReleaseDirective releaseDecision) {
		this.releaseDecision = releaseDecision;
	}
	@JsonProperty("allOtherValuesConst")
	public AllOtherValuesConst getAllOtherValuesConst() {
		return allOtherValuesConst;
	}
	public void setAllOtherValuesConst(AllOtherValuesConst allOtherValuesConst) {
		this.allOtherValuesConst = allOtherValuesConst;
	}
	@JsonProperty("aubmentedPolicyId")
	public AugmentedPolicyId getAugmentedPolicyId() {
		return augmentedPolicyId;
	}
	public void setAugmentedPolicyId(AugmentedPolicyId augmentedPolicyId) {
		this.augmentedPolicyId = augmentedPolicyId;
	}
}
