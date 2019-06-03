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

import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class UserPolicyMetadata {
	@Embedded
	@JsonProperty("policyId")
	private PolicyId policyId;
	@Embedded
	@JsonProperty("creator")
	private CreatorId creator;
	// No longer Date, so no longer @Temporal @Temporal(value = TemporalType.TIMESTAMP)
	@JsonProperty("createTime")
	private long createTime;
	@JsonProperty("state")
	private PolicyState state;
	@Embedded
	@JsonProperty("supersededBy")
	private SupersedingPolicyId supersededBy;
	@JsonProperty("changeOrder")
	private String changeOrder;
	
	public String getChangeOrder() {
		return changeOrder;
	}
	public void setChangeOrder(String changeOrder) {
		this.changeOrder = changeOrder;
	}
	public PolicyId getPolicyId() {
		return policyId;
	}
	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}
	public CreatorId getCreator() {
		return creator;
	}
	public void setCreator(CreatorId creator) {
		this.creator = creator;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	@Enumerated(EnumType.STRING)
	public PolicyState getState() {
		return state;
	}
	public void setState(PolicyState state) {
		this.state = state;
	}
	public SupersedingPolicyId getSupersededBy() {
		return supersededBy;
	}
	public void setSupersededBy(SupersedingPolicyId supersededBy) {
		this.supersededBy = supersededBy;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		UserPolicyMetadata pm = (UserPolicyMetadata) o;
		return (pm.getPolicyId().equals(this.getPolicyId()) && pm.getCreator().equals(this.getCreator()) && pm.getCreateTime() == this.getCreateTime() && pm.getState().equals(this.getState()) && pm.getSupersededBy().equals(this.getSupersededBy()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(policyId,creator,createTime,state,supersededBy);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class UserPolicyMetadata {\n");
		sb.append("  policyId: ").append(policyId.toString()).append("\n");
		sb.append("  creator: ").append(creator.toString()).append("\n");
		sb.append("  createTime: ").append(createTime).append("\n");
		sb.append("  state: ").append(state.toString()).append("\n");
		if (supersededBy != null) {
			sb.append("  supersededBy: ").append(supersededBy.toString()).append("\n");
		}
		sb.append("}\n");
		return sb.toString();
	}
}
