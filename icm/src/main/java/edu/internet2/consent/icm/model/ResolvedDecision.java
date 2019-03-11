package edu.internet2.consent.icm.model;

public class ResolvedDecision {
	private InfoId infoId;
	private IcmReleaseDirective directive;
	private String value;
	private PolicyId policyId;
	public InfoId getInfoId() {
		return infoId;
	}
	public void setInfoId(InfoId infoId) {
		this.infoId = infoId;
	}
	public IcmReleaseDirective getDirective() {
		return directive;
	}
	public void setDirective(IcmReleaseDirective directive) {
		this.directive = directive;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public PolicyId getPolicyId() {
		return policyId;
	}
	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}
	
	// No overrides at this point
}
