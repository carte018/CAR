package edu.internet2.consent.icm.model;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class IcmPolicyMetaData {

	@Embedded
	@JsonProperty("policyId")
	private PolicyId policyId;
	
	@Embedded
	@JsonProperty("creator")
	private UserId creator;
	
	@JsonProperty("createTime")
	private long createTime;
	
	@JsonProperty("state")
	private PolicyState state;
	
	@Embedded
	@JsonProperty("supersededBy")
	private SupersedingPolicyId supersededBy;

	@JsonProperty("policyId")
	public PolicyId getPolicyId() {
		return policyId;
	}

	@JsonProperty("policyId")
	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}

	@JsonProperty("creator")
	public UserId getCreator() {
		return creator;
	}

	@JsonProperty("creator")
	public void setCreator(UserId creator) {
		this.creator = creator;
	}

	@JsonProperty("createTime")
	public long getCreateTime() {
		return createTime;
	}

	@JsonProperty("createTime")
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@JsonProperty("state")
	public PolicyState getState() {
		return state;
	}

	@JsonProperty("state")
	public void setState(PolicyState state) {
		this.state = state;
	}

	@JsonProperty("supersededBy")
	public SupersedingPolicyId getSupersededBy() {
		return supersededBy;
	}

	@JsonProperty("supersededBy")
	public void setSupersededBy(SupersedingPolicyId supersededBy) {
		this.supersededBy = supersededBy;
	}
	
	
}
