package edu.internet2.consent.icm.model;

public class ResolvedAnyDecision {
	private IcmReleaseDirective directive;
	private PolicyId policyId;
	public IcmReleaseDirective getDirective() {
		return directive;
	}
	public void setDirective(IcmReleaseDirective directive) {
		this.directive = directive;
	}
	public PolicyId getPolicyId() {
		return policyId;
	}
	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}
	
	
}
