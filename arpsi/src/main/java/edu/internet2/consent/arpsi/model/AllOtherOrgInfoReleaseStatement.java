package edu.internet2.consent.arpsi.model;

import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class AllOtherOrgInfoReleaseStatement {

	@Embedded
	@JsonProperty("allOtherInfoId")
	AllOtherInfoId allOtherInfoId;
	
	@Embedded
	@JsonProperty("directiveAllOtherValues")
	OrgDirectiveAllOtherValues orgDirectiveAllOtherValues;

	@JsonProperty("allOtherInfoId")
	public AllOtherInfoId getAllOtherInfoId() {
		return allOtherInfoId;
	}

	@JsonProperty("allOtherInfoId")
	public void setAllOtherInfoId(AllOtherInfoId allOtherInfoId) {
		this.allOtherInfoId = allOtherInfoId;
	}

	@JsonProperty("directiveAllOtherValues")
	public OrgDirectiveAllOtherValues getOrgDirectiveAllOtherValues() {
		return orgDirectiveAllOtherValues;
	}

	@JsonProperty("directiveAllOtherValues")
	public void setOrgDirectiveAllOtherValues(OrgDirectiveAllOtherValues orgDirectiveAllOtherValues) {
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
		AllOtherOrgInfoReleaseStatement a = (AllOtherOrgInfoReleaseStatement) o;
		return (a.getOrgDirectiveAllOtherValues().equals(this.getOrgDirectiveAllOtherValues()));
	}
	@Override
	public int hashCode() {
		return Objects.hash(orgDirectiveAllOtherValues);
	}
	@Override
	public String toString() {
		return orgDirectiveAllOtherValues.getOrgReleaseDirective().toString();
	}
}
