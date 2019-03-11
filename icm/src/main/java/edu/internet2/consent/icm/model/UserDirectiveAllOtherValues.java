package edu.internet2.consent.icm.model;

import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class UserDirectiveAllOtherValues {
	
	@Enumerated(EnumType.STRING)
	
	UserReleaseDirective userReleaseDirective;
	@Enumerated(EnumType.STRING)
	AllOtherValuesConst allOtherValues;
	
	@JsonProperty("releaseDirective")
	public UserReleaseDirective getUserReleaseDirective() {
		return userReleaseDirective;
	}
	public void setUserReleaseDirective(UserReleaseDirective userReleaseDirective) {
		this.userReleaseDirective = userReleaseDirective;
	}
	
	@JsonProperty("allOtherValuesConst")
	public AllOtherValuesConst getAllOtherValues() {
		return allOtherValues;
	}
	public void setAllOtherValues(AllOtherValuesConst allOtherValues) {
		this.allOtherValues = allOtherValues;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		
		// Value of allOtherValuesConst is constant
		return (((UserDirectiveAllOtherValues) o).getUserReleaseDirective().equals(this.getUserReleaseDirective()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(userReleaseDirective,allOtherValues);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class DirectiveAllOtherValue {\n");
		sb.append("  userReleaseDirective: ").append(userReleaseDirective).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
