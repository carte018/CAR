package edu.internet2.consent.arpsi.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
@Entity
public class OrgInfoReleaseStatement {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long OIRKey;
	
	@Embedded
	@JsonProperty("infoId")
	private InfoId infoId;
	
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	@JsonProperty("arrayOfDirectiveOnValues")
	List<OrgDirectiveOnValues> arrayOfOrgDirectiveOnValues;
	
	@Embedded
	@JsonProperty("directiveAllOtherValues")
	OrgDirectiveAllOtherValues orgDirectiveAllOtherValues;

	@JsonIgnore
	public Long getOIRKey() {
		return OIRKey;
	}

	@JsonIgnore
	public void setOIRKey(Long oIRKey) {
		OIRKey = oIRKey;
	}

	@JsonProperty("infoId")
	public InfoId getInfoId() {
		return infoId;
	}

	@JsonProperty("infoId")
	public void setInfoId(InfoId infoId) {
		this.infoId = infoId;
	}

	@JsonProperty("arrayOfDirectiveOnValues")
	public List<OrgDirectiveOnValues> getArrayOfOrgDirectiveOnValues() {
		return arrayOfOrgDirectiveOnValues;
	}
	
	@JsonProperty("arrayOfDirectiveOnValues")
	public void setArrayOfOrgDirectiveOnValues(List<OrgDirectiveOnValues> arrayOfOrgDirectiveOnValues) {
		this.arrayOfOrgDirectiveOnValues = arrayOfOrgDirectiveOnValues;
	}

	// also masquerade for use in the ICM
	@JsonProperty("arrayOfOrgDirectiveOnValues")
	public void setAODOV(List<OrgDirectiveOnValues> arrayOfOrgDirectiveOnValues) {
		this.arrayOfOrgDirectiveOnValues = arrayOfOrgDirectiveOnValues;
	}
	
	@JsonProperty("directiveAllOtherValues")
	public OrgDirectiveAllOtherValues getOrgDirectiveAllOtherValues() {
		return orgDirectiveAllOtherValues;
	}

	@JsonProperty("directiveAllOtherValues")
	public void setOrgDirectiveAllOtherValues(OrgDirectiveAllOtherValues orgDirectiveAllOtherValues) {
		this.orgDirectiveAllOtherValues = orgDirectiveAllOtherValues;
	}
	// Masquerade for the ICM
	@JsonProperty("orgDirectiveAllOtherValues")
	public void setODAOV(OrgDirectiveAllOtherValues orgDirectiveAllOtherValues) {
		this.orgDirectiveAllOtherValues = orgDirectiveAllOtherValues;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		OrgInfoReleaseStatement i = (OrgInfoReleaseStatement) o;
		return i.getInfoId().equals(this.getInfoId());  // not very deep comparison
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(infoId); // weak
	}
	
	@Override
	public String toString() {
		return "OrgInfoReleaseStatement #" + OIRKey;
	}
}
