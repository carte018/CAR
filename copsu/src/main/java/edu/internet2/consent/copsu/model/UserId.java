package edu.internet2.consent.copsu.model;

import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class UserId {
	
	private String userType;
	private String userValue;
	
	@JsonProperty("user-type")
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	
	@JsonProperty("user-value")
	public String getUserValue() {
		return userValue;
	}
	public void setUserValue(String userValue) {
		this.userValue = userValue;
	}
	
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		return (((UserId) o).getUserType().equals(this.getUserType()) && ((UserId) o).getUserValue().equals(this.getUserValue()));
	}
	
	public int hashCode() {
		return Objects.hash(userType,userValue);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class UserId {\n");
		sb.append("  userType: ").append(userType).append("\n");
		sb.append("  userValue: ").append(userValue).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
