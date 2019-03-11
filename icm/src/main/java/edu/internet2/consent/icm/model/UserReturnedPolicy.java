package edu.internet2.consent.icm.model;

import java.util.Objects;

import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@javax.persistence.Entity
public class UserReturnedPolicy {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long PolicyIdentifier;
	
	@JsonProperty("policyMetaData")
	private UserPolicyMetadata policyMetaData;
	@Embedded
	@JsonProperty("userInfoReleasePolicy")
	private UserInfoReleasePolicy userInfoReleasePolicy;
	public UserPolicyMetadata getPolicyMetaData() {
		return policyMetaData;
	}
	
	@JsonIgnore
	public Long getPolicyIdentifier() {
		return PolicyIdentifier;
	}

	@JsonIgnore
	public void setPolicyIdentifier(Long policyIdentifier) {
		PolicyIdentifier = policyIdentifier;
	}

	public void setPolicyMetaData(UserPolicyMetadata policyMetaData) {
		this.policyMetaData = policyMetaData;
	}
	public UserInfoReleasePolicy getUserInfoReleasePolicy() {
		return userInfoReleasePolicy;
	}
	public void setUserInfoReleasePolicy(UserInfoReleasePolicy userInfoReleasePolicy) {
		this.userInfoReleasePolicy = userInfoReleasePolicy;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		UserReturnedPolicy rp = (UserReturnedPolicy) o;
		return (rp.getPolicyMetaData().equals(this.getPolicyMetaData()) && rp.getUserInfoReleasePolicy().equals(this.getUserInfoReleasePolicy())); 
	}
	
	@Override 
	public int hashCode() {
		return Objects.hash(policyMetaData,userInfoReleasePolicy);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class UserReturnedPolicy {\n");
		sb.append("  policyMetaData: ").append(policyMetaData.toString()).append("\n");
		sb.append("  userInfoReleasePolicy: ").append(userInfoReleasePolicy.toString()).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
	
	public String toJSON() throws JsonProcessingException{
		// Return JSON representation of self
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
	
}
