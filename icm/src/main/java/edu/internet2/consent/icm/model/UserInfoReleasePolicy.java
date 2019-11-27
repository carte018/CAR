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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.icm.util.OMSingleton;

@Embeddable
public class UserInfoReleasePolicy {

	@JsonProperty("description")
	private String description;
	@Embedded
	@JsonProperty("userId")
	private UserId userId;
	@Embedded
	@JsonProperty("relyingPartyId")
	private RelyingPartyId relyingPartyId;
	@Embedded
	@JsonProperty("resourceHolderId")
	private ResourceHolderId resourceHolderId;
	@Enumerated
	@JsonProperty("whileImAwayDirective")
	private WhileImAwayDirective whileImAwayDirective;
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch = FetchType.EAGER)
	@JsonProperty("arrayOfInfoReleaseStatement")
	private List<UserInfoReleaseStatement> arrayOfInfoReleaseStatement;
	@Embedded
	@JsonProperty("allOtherInfoReleaseStatement")
	private UserAllOtherInfoReleaseStatement userAllOtherInfoReleaseStatement;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public UserId getUserId() {
		return userId;
	}
	public void setUserId(UserId userId) {
		this.userId = userId;
	}
	public RelyingPartyId getRelyingPartyId() {
		return relyingPartyId;
	}
	public void setRelyingPartyId(RelyingPartyId relyingPartyId) {
		this.relyingPartyId = relyingPartyId;
	}
	public ResourceHolderId getResourceHolderId() {
		return resourceHolderId;
	}
	public void setResourceHolderId(ResourceHolderId resourceHolderId) {
		this.resourceHolderId = resourceHolderId;
	}
	public WhileImAwayDirective getWhileImAwayDirective() {
		return whileImAwayDirective;
	}
	public void setWhileImAwayDirective(WhileImAwayDirective whileImAwayDirective) {
		this.whileImAwayDirective = whileImAwayDirective;
	}
	public List<UserInfoReleaseStatement> getArrayOfInfoReleaseStatement() {
		return arrayOfInfoReleaseStatement;
	}
	public void setArrayOfInfoReleaseStatement(ArrayList<UserInfoReleaseStatement> arrayOfInfoReleaseStatement) {
		this.arrayOfInfoReleaseStatement = arrayOfInfoReleaseStatement;
	}
	public UserAllOtherInfoReleaseStatement getUserAllOtherInfoReleaseStatement() {
		return userAllOtherInfoReleaseStatement;
	}
	public void setUserAllOtherInfoReleaseStatement(UserAllOtherInfoReleaseStatement userAllOtherInfoReleaseStatement) {
		this.userAllOtherInfoReleaseStatement = userAllOtherInfoReleaseStatement;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		UserInfoReleasePolicy irp = (UserInfoReleasePolicy) o;
		return (irp.getDescription().equals(this.getDescription()) && irp.getUserId().equals(this.getUserId()) && irp.getRelyingPartyId().equals(this.getRelyingPartyId()) && irp.getResourceHolderId().equals(this.getResourceHolderId()) && irp.getWhileImAwayDirective().equals(this.getWhileImAwayDirective()) && irp.getArrayOfInfoReleaseStatement().equals(this.getArrayOfInfoReleaseStatement()) && irp.getUserAllOtherInfoReleaseStatement().equals(this.getUserAllOtherInfoReleaseStatement()));
	}
	
	@Override 
	public int hashCode() {
		return Objects.hash(description,userId,relyingPartyId,resourceHolderId,whileImAwayDirective,arrayOfInfoReleaseStatement,userAllOtherInfoReleaseStatement);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class UserInfoReleasePolicy {\n");
		sb.append("  description: ").append(description).append("\n");
		sb.append("  userId: ").append(userId.toString()).append("\n");
		sb.append("  relyingPartyId: ").append(relyingPartyId.toString()).append("\n");
		sb.append("  resourceHolderId: ").append(resourceHolderId.toString()).append("\n");
		sb.append("  whileImAwayDirective: ").append(whileImAwayDirective.toString()).append("\n");
		sb.append("  arrayOfInfoReleaseStatement: ");
		for (UserInfoReleaseStatement a : arrayOfInfoReleaseStatement) {
			sb.append(a.toString()).append("\n                               ");
		}
		sb.append("\n  userAllOtherInfoReleaseStatement: ").append(userAllOtherInfoReleaseStatement.toString()).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
	
	public String toJSON() throws JsonProcessingException {
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
