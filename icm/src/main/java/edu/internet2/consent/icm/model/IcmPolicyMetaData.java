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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (createTime ^ (createTime >>> 32));
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result + ((policyId == null) ? 0 : policyId.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((supersededBy == null) ? 0 : supersededBy.hashCode());
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
		if (!(obj instanceof IcmPolicyMetaData)) {
			return false;
		}
		IcmPolicyMetaData other = (IcmPolicyMetaData) obj;
		if (createTime != other.createTime) {
			return false;
		}
		if (creator == null) {
			if (other.creator != null) {
				return false;
			}
		} else if (!creator.equals(other.creator)) {
			return false;
		}
		if (policyId == null) {
			if (other.policyId != null) {
				return false;
			}
		} else if (!policyId.equals(other.policyId)) {
			return false;
		}
		if (state != other.state) {
			return false;
		}
		if (supersededBy == null) {
			if (other.supersededBy != null) {
				return false;
			}
		} else if (!supersededBy.equals(other.supersededBy)) {
			return false;
		}
		return true;
	}
	
	
}
