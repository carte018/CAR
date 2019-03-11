package edu.internet2.consent.arpsi.model;

public class ResolvedOtherDecision {
	private InfoId infoId;
	private OrgReleaseDirective directive;
	private PolicyId policyId;
	public InfoId getInfoId() {
		return infoId;
	}
	public void setInfoId(InfoId infoId) {
		this.infoId = infoId;
	}
	public OrgReleaseDirective getDirective() {
		return directive;
	}
	public void setDirective(OrgReleaseDirective directive) {
		this.directive = directive;
	}
	public PolicyId getPolicyId() {
		return policyId;
	}
	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}
	
	
}
