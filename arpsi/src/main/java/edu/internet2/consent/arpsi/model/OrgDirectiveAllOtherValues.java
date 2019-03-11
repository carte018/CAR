package edu.internet2.consent.arpsi.model;

import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class OrgDirectiveAllOtherValues {

	@Enumerated(EnumType.STRING)
	@JsonProperty("releaseDirective")
	OrgReleaseDirective orgReleaseDirective;
	@JsonProperty("policyBasis")
	String orgPolicyBasis;
	@Enumerated(EnumType.STRING)
	AllOtherValuesConst allOtherValuesConst;
	
	public String getOrgPolicyBasis() {
		return orgPolicyBasis;
	}
	public void setOrgPolicyBasis(String orgPolicyBasis) {
		this.orgPolicyBasis = orgPolicyBasis;
	}
	
	@JsonProperty("orgPolicyBasis")
	public void setOPB(String opb) {
		this.orgPolicyBasis = opb;
	}
	@JsonProperty("releaseDirective")
	public OrgReleaseDirective getOrgReleaseDirective() {
		return orgReleaseDirective;
	}
	@JsonProperty("releaseDirective")
	public void setOrgReleaseDirective(OrgReleaseDirective orgReleaseDirective) {
		this.orgReleaseDirective = orgReleaseDirective;
	}
	// Masquerade for the ICM
	@JsonProperty("orgReleaseDirective")
	public void setORD(OrgReleaseDirective orgReleaseDirective) {
		this.orgReleaseDirective = orgReleaseDirective;
	}
	public AllOtherValuesConst getAllOtherValuesConst() {
		return allOtherValuesConst;
	}
	public void setAllOtherValuesConst(AllOtherValuesConst allOtherValuesConst) {
		this.allOtherValuesConst = allOtherValuesConst;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		OrgDirectiveAllOtherValues a = (OrgDirectiveAllOtherValues) o;
		return (a.getOrgReleaseDirective() == this.getOrgReleaseDirective());
	}
	@Override
	public int hashCode() {
		return Objects.hash(orgReleaseDirective);
	}
	@Override
	public String toString() {
		return "orgReleaseDirective=" + orgReleaseDirective.toString();
	}
}
