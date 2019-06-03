/*
 * Copyright 2015 - 2019 Duke University
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2 as published by
    the Free Software Foundation.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License Version 2
    along with this program.  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt>.

 */
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
