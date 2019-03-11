package edu.internet2.consent.copsu.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class ListableUserId {
	
	@JsonIgnore
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long ListableUserIdentifier;
	
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
		return (((ListableUserId) o).getUserType().equals(this.getUserType()) && ((ListableUserId) o).getUserValue().equals(this.getUserValue()));
	}
	
	public int hashCode() {
		return Objects.hash(userType,userValue);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ListableUserId {\n");
		sb.append("  userType: ").append(userType).append("\n");
		sb.append("  userValue: ").append(userValue).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
