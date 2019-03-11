package edu.internet2.consent.icm.model;

import java.util.Objects;

public class DecisionsForInfoDiscriminator {
	private InfoId infoId;
	private String policySource;
	public String getPolicySource() {
		return policySource;
	}
	public void setPolicySource(String policySource) {
		this.policySource = policySource;
	}
	private PolicyId policyId;
	private PolicyId icm_policyId;
	private String directive;
	
	public String getDirective() {
		return directive;
	}
	public void setDirective(String directive) {
		this.directive = directive;
	}
	public PolicyId getIcm_policyId() {
		return icm_policyId;
	}
	public void setIcm_policyId(PolicyId icm_policyId) {
		this.icm_policyId = icm_policyId;
	}
	public InfoId getInfoId() {
		return infoId;
	}
	public void setInfoId(InfoId infoId) {
		this.infoId = infoId;
	}

	public PolicyId getPolicyId() {
		return policyId;
	}
	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}
	
	@Override 
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (this == o) {
			return true;
		}
		if (this.getClass() != o.getClass()) {
			return false;
		}
		DecisionsForInfoDiscriminator t = (DecisionsForInfoDiscriminator) o;
		if (this.getInfoId().equals(t.getInfoId()) && this.getPolicyId().equals(t.getPolicyId()) && this.getInfoId().equals(t.getInfoId()) && this.getDirective().equals(t.getDirective())) {
			return true;
		} else {
			return false;
		}
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getInfoId(),this.getPolicyId(),this.getDirective());
	}
}
