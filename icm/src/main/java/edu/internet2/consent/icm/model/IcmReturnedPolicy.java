package edu.internet2.consent.icm.model;

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
public class IcmReturnedPolicy {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long ReturnedPolicyIdentifier;
	
	@JsonIgnore
	private long priority;  // higher = less important
	
	@JsonProperty("policyMetaData")
	@Embedded
	private IcmPolicyMetaData policyMetaData;
	
	@JsonProperty("policy")
	@Embedded
	private IcmInfoReleasePolicy policy;

	@JsonIgnore
	public Long getReturnedPolicyIdentifier() {
		return ReturnedPolicyIdentifier;
	}
	@JsonIgnore
	public void setReturnedPolicyIdentifier(Long returnedPolicyIdentifier) {
		ReturnedPolicyIdentifier = returnedPolicyIdentifier;
	}

	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	@JsonProperty("policyMetaData")
	public IcmPolicyMetaData getPolicyMetaData() {
		return policyMetaData;
	}

	@JsonProperty("policyMetaData")
	public void setPolicyMetaData(IcmPolicyMetaData policyMetaData) {
		this.policyMetaData = policyMetaData;
	}

	@JsonProperty("policy")
	public IcmInfoReleasePolicy getPolicy() {
		return policy;
	}

	@JsonProperty("policy")
	public void setPolicy(IcmInfoReleasePolicy policy) {
		this.policy = policy;
	}
	
	// JSON mapper
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
