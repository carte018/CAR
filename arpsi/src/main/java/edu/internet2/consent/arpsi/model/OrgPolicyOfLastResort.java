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

import edu.internet2.consent.arpsi.util.OMSingleton;

@Entity
public class OrgPolicyOfLastResort {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long OrgPolicyOfLastResortIdentifier;
	
	@JsonIgnore
	private long priority; // higher numbers = later priorities, top priority is 1
	
	public long getPriority() {
		return 0;
	}

	public void setPriority(long priority) {
		this.priority = 0;
	}

	@JsonProperty("policyMetaData")
	@Embedded
	private OrgPolicyMetaData policyMetaData;
	
	@JsonProperty("policy")
	@Embedded
	private OrgInfoReleasePolicy policy;

	@JsonIgnore
	public Long getOrgPolicyOfLastResortIdentifier() {
		return OrgPolicyOfLastResortIdentifier;
	}

	@JsonIgnore
	public void setOrgPolicyOfLastResortIdentifier(Long returnedPolicyIdentifier) {
		OrgPolicyOfLastResortIdentifier = returnedPolicyIdentifier;
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
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((OrgPolicyOfLastResortIdentifier == null) ? 0 : OrgPolicyOfLastResortIdentifier.hashCode());
		result = prime * result + ((policy == null) ? 0 : policy.hashCode());
		result = prime * result + ((policyMetaData == null) ? 0 : policyMetaData.hashCode());
		result = prime * result + (int) (priority ^ (priority >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OrgPolicyOfLastResort)) {
			return false;
		}
		OrgPolicyOfLastResort other = (OrgPolicyOfLastResort) obj;
		if (OrgPolicyOfLastResortIdentifier == null) {
			if (other.OrgPolicyOfLastResortIdentifier != null) {
				return false;
			}
		} else if (!OrgPolicyOfLastResortIdentifier.equals(other.OrgPolicyOfLastResortIdentifier)) {
			return false;
		}
		if (policy == null) {
			if (other.policy != null) {
				return false;
			}
		} else if (!policy.equals(other.policy)) {
			return false;
		}
		if (policyMetaData == null) {
			if (other.policyMetaData != null) {
				return false;
			}
		} else if (!policyMetaData.equals(other.policyMetaData)) {
			return false;
		}
		if (priority != other.priority) {
			return false;
		}
		return true;
	}
}
