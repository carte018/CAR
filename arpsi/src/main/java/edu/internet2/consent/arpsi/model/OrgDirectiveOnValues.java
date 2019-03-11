package edu.internet2.consent.arpsi.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
@Entity
public class OrgDirectiveOnValues {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long DirectiveOnValuesIdentifier;

	@JsonProperty("releaseDirective")
	@Enumerated(EnumType.STRING)
	private OrgReleaseDirective orgReleaseDirective;
	
	@JsonProperty("policyBasis")
	private String orgPolicyBasis;
	
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
	@JsonProperty("valueObjectList")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private List<ValueObject> valueObjectList;

	@JsonIgnore
	public Long getDirectiveOnValuesIdentifier() {
		return DirectiveOnValuesIdentifier;
	}

	@JsonIgnore
	public void setDirectiveOnValuesIdentifier(Long directiveOnValuesIdentifier) {
		DirectiveOnValuesIdentifier = directiveOnValuesIdentifier;
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

	@JsonProperty("valueObjectList")
	public List<ValueObject> getValueObjectList() {
		return valueObjectList;
	}

	@JsonProperty("valueObjectList")
	public void setValueObjectList(List<ValueObject> valueObjectList) {
		this.valueObjectList = valueObjectList;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		OrgDirectiveOnValues v = (OrgDirectiveOnValues) o;
		return (v.getValueObjectList().containsAll(this.getValueObjectList()) && this.getValueObjectList().containsAll(v.getValueObjectList()) && v.getOrgReleaseDirective().equals(this.getOrgReleaseDirective()));
	}
	@Override
	public int hashCode() {
		return Objects.hash(valueObjectList,orgReleaseDirective);
	}
	@Override
	public String toString() {
		return "OrgDirectiveOnValues #"+DirectiveOnValuesIdentifier;
	}
}