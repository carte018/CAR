package edu.internet2.consent.arpsi.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class OrgReturnedPolicy {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long ReturnedPolicyIdentifier;
	
	@JsonIgnore
	private long priority; // higher numbers = later priorities, top priority is 1
	
	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	@JsonProperty("policyMetaData")
	@Embedded
	private OrgPolicyMetaData policyMetaData;
	
	@JsonProperty("policy")
	@Embedded
	private OrgInfoReleasePolicy policy;

	@JsonIgnore
	public Long getReturnedPolicyIdentifier() {
		return ReturnedPolicyIdentifier;
	}

	@JsonIgnore
	public void setReturnedPolicyIdentifier(Long returnedPolicyIdentifier) {
		ReturnedPolicyIdentifier = returnedPolicyIdentifier;
	}

	@JsonProperty("policyMetaData")
	public OrgPolicyMetaData getPolicyMetaData() {
		return policyMetaData;
	}

	@JsonProperty("policyMetaData")
	public void setPolicyMetaData(OrgPolicyMetaData policyMetaData) {
		this.policyMetaData = policyMetaData;
	}

	@JsonProperty("policy")
	public OrgInfoReleasePolicy getPolicy() {
		return policy;
	}

	@JsonProperty("policy")
	public void setPolicy(OrgInfoReleasePolicy policy) {
		this.policy = policy;
	}
	
	// No overrides yet
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
