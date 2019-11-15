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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class OrgInfoReleasePolicy {
	@JsonProperty("description")
	private String description;
	
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
	public List<OrgInfoReleaseStatement> getArrayOfInfoReleaseStatement() {
		return arrayOfInfoReleaseStatement;
	}

	@JsonProperty("arrayOfInfoReleaseStatement")
	public void setArrayOfInfoReleaseStatement(List<OrgInfoReleaseStatement> arrayOfInfoReleaseStatement) {
		this.arrayOfInfoReleaseStatement = arrayOfInfoReleaseStatement;
	}

	@JsonProperty("allOtherInfoReleaseStatement")
	public AllOtherOrgInfoReleaseStatement getAllOtherOrgInfoReleaseStatement() {
		return allOtherOrgInfoReleaseStatement;
	}

	@JsonProperty("allOtherInfoReleaseStatement")
	public void setAllOtherOrgInfoReleaseStatement(AllOtherOrgInfoReleaseStatement allOtherOrgInfoReleaseStatement) {
		this.allOtherOrgInfoReleaseStatement = allOtherOrgInfoReleaseStatement;
	}
	
	// Masquerade for the ICM
	@JsonProperty("allOtherOrgInfoReleaseStatement")
	public void setAOOIRS(AllOtherOrgInfoReleaseStatement allOtherOrgInfoReleaseStatement) {
		this.allOtherOrgInfoReleaseStatement = allOtherOrgInfoReleaseStatement;
	}

	@JsonProperty("userPropertyArray")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY)
	private List<UserProperty> userPropertyArray;
	
	@JsonProperty("relyingPartyPropertyArray")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY)
	private List<RelyingPartyProperty> relyingPartyPropertyArray;
	
	@JsonProperty("resourceHolderId")
	@Embedded
	private ResourceHolderId resourceHolderId;
	
	@JsonProperty("arrayOfInfoReleaseStatement")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private List<OrgInfoReleaseStatement> arrayOfInfoReleaseStatement;
	
	@Embedded
	@JsonProperty("allOtherInfoReleaseStatement")
	private AllOtherOrgInfoReleaseStatement allOtherOrgInfoReleaseStatement;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((allOtherOrgInfoReleaseStatement == null) ? 0 : allOtherOrgInfoReleaseStatement.hashCode());
		result = prime * result + ((arrayOfInfoReleaseStatement == null) ? 0 : arrayOfInfoReleaseStatement.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((relyingPartyPropertyArray == null) ? 0 : relyingPartyPropertyArray.hashCode());
		result = prime * result + ((resourceHolderId == null) ? 0 : resourceHolderId.hashCode());
		result = prime * result + ((userPropertyArray == null) ? 0 : userPropertyArray.hashCode());
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
		if (!(obj instanceof OrgInfoReleasePolicy)) {
			return false;
		}
		OrgInfoReleasePolicy other = (OrgInfoReleasePolicy) obj;
		if (allOtherOrgInfoReleaseStatement == null) {
			if (other.allOtherOrgInfoReleaseStatement != null) {
				return false;
			}
		} else if (!allOtherOrgInfoReleaseStatement.equals(other.allOtherOrgInfoReleaseStatement)) {
			return false;
		}
		if (arrayOfInfoReleaseStatement == null) {
			if (other.arrayOfInfoReleaseStatement != null) {
				return false;
			}
		} else if (!arrayOfInfoReleaseStatement.equals(other.arrayOfInfoReleaseStatement)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (relyingPartyPropertyArray == null) {
			if (other.relyingPartyPropertyArray != null) {
				return false;
			}
		} else if (!relyingPartyPropertyArray.equals(other.relyingPartyPropertyArray)) {
			return false;
		}
		if (resourceHolderId == null) {
			if (other.resourceHolderId != null) {
				return false;
			}
		} else if (!resourceHolderId.equals(other.resourceHolderId)) {
			return false;
		}
		if (userPropertyArray == null) {
			if (other.userPropertyArray != null) {
				return false;
			}
		} else if (!userPropertyArray.equals(other.userPropertyArray)) {
			return false;
		}
		return true;
	}

	// no overrides here
		
}
