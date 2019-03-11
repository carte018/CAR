package edu.internet2.consent.icm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DecisionOnAllOtherValues {

	private UserReleaseDirective releaseDecision;
	private AllOtherValuesConst allOtherValuesConst;
	private PolicyId policyId;
	
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
	@JsonProperty("policyId")
	public PolicyId getPolicyId() {
		return policyId;
	}
	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}
}
