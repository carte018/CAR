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

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import java.util.List;

import javax.persistence.CascadeType;


import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class IcmInfoReleasePolicy {

	@JsonProperty("description")
	private String description;
	
	@JsonProperty("userPropertyArray")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL)
	private List<UserProperty> userPropertyArray;
	
	@JsonProperty("relyingPartyPropertyArray")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL)
	private List<RelyingPartyProperty> relyingPartyPropertyArray;
	
	@JsonProperty("resourceHolderId")
	@Embedded
	private ResourceHolderId resourceHolderId;
	
	@JsonProperty("arrayOfInfoReleaseStatement")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private List<IcmInfoReleaseStatement> arrayOfInfoReleaseStatement;
	
	@Embedded
	@JsonProperty("allOtherOrgInfoReleaseStatement")
	private IcmAllOtherInfoReleaseStatement allOtherOrgInfoReleaseStatement;

	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("userPropertyArray")
	public List<UserProperty> getUserPropertyArray() {
		return userPropertyArray;
	}

	@JsonProperty("userPropertyArray")
	public void setUserPropertyArray(List<UserProperty> userPropertyArray) {
		this.userPropertyArray = userPropertyArray;
	}

	@JsonProperty("relyingPartyPropertyArray")
	public List<RelyingPartyProperty> getRelyingPartyPropertyArray() {
		return relyingPartyPropertyArray;
	}

	@JsonProperty("relyingPartyPropertyArray")
	public void setRelyingPartyPropertyArray(List<RelyingPartyProperty> relyingPartyPropertyArray) {
		this.relyingPartyPropertyArray = relyingPartyPropertyArray;
	}

	@JsonProperty("resourceHolderId")
	public ResourceHolderId getResourceHolderId() {
		return resourceHolderId;
	}

	@JsonProperty("resourceHolderId")
	public void setResourceHolderId(ResourceHolderId resourceHolderId) {
		this.resourceHolderId = resourceHolderId;
	}

	@JsonProperty("arrayOfInfoReleaseStatement")
	public List<IcmInfoReleaseStatement> getArrayOfInfoReleaseStatement() {
		return arrayOfInfoReleaseStatement;
	}

	@JsonProperty("arrayOfInfoReleaseStatement")
	public void setArrayOfInfoReleaseStatement(List<IcmInfoReleaseStatement> arrayOfInfoReleaseStatement) {
		this.arrayOfInfoReleaseStatement = arrayOfInfoReleaseStatement;
	}

	@JsonProperty("allOtherOrgInfoReleaseStatement")
	public IcmAllOtherInfoReleaseStatement getAllOtherOrgInfoReleaseStatement() {
		return allOtherOrgInfoReleaseStatement;
	}

	@JsonProperty("allOtherOrgInfoReleaseStatement")
	public void setAllOtherOrgInfoReleaseStatement(IcmAllOtherInfoReleaseStatement allOtherOrgInfoReleaseStatement) {
		this.allOtherOrgInfoReleaseStatement = allOtherOrgInfoReleaseStatement;
	}
	
	@JsonProperty("icmAllOtherInfoReleaseStatement")
	public void setIcmAllOtherInfoReleaseStatement(IcmAllOtherInfoReleaseStatement allOtherOrgInfoReleaseStatement) {
		this.allOtherOrgInfoReleaseStatement = allOtherOrgInfoReleaseStatement;
	}
	
}
