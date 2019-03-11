package edu.internet2.consent.icm.model;

import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class UserAllOtherInfoReleaseStatement {
	
	@Embedded
	@JsonProperty("allOtherInfoId")
	private AllOtherInfoId allOtherInfoId;
	@Embedded
	@JsonProperty("directiveAllOtherValues")
	private UserDirectiveAllOtherValues userDirectiveAllOtherValues;
	public AllOtherInfoId getAllOtherInfoId() {
		return allOtherInfoId;
	}
	public void setAllOtherInfoId(AllOtherInfoId allOtherInfoId) {
		this.allOtherInfoId = allOtherInfoId;
	}
	public UserDirectiveAllOtherValues getUserDirectiveAllOtherValues() {
		return userDirectiveAllOtherValues;
	}
	public void setUserDirectiveAllOtherValues(UserDirectiveAllOtherValues userDirectiveAllOtherValues) {
		this.userDirectiveAllOtherValues = userDirectiveAllOtherValues;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		UserAllOtherInfoReleaseStatement aoir = (UserAllOtherInfoReleaseStatement) o;
		return (this.getUserDirectiveAllOtherValues().equals(aoir.getUserDirectiveAllOtherValues()) && this.getAllOtherInfoId().equals(aoir.getAllOtherInfoId()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(userDirectiveAllOtherValues,allOtherInfoId);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class UserAllOtherInfoReleaseStatement {\n");
		sb.append("  allOtherInfoId: ").append(allOtherInfoId.toString()).append("\n");
		if (userDirectiveAllOtherValues != null) {
			sb.append("  userDirectiveAllOtherValues: ").append(userDirectiveAllOtherValues.toString()).append("\n");
		} else {
			sb.append("  ERROR - userDirectiveAllOtherValues is null!\n");
		}
		sb.append("}\n");
		return sb.toString();
	}
}
