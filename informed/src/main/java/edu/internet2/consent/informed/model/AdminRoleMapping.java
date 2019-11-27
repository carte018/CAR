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
package edu.internet2.consent.informed.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.informed.util.OMSingleton;

@Entity
public class AdminRoleMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long adminRoleId;
	
	@JsonProperty("subject")
	private String subject;
	
	@JsonProperty("roleName")
	private String roleName;
	
	@JsonProperty("target")
	private String target;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("created")
	private long created;
	
	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public long getArchived() {
		return archived;
	}

	public void setArchived(long archived) {
		this.archived = archived;
	}

	@JsonProperty("archived")
	private long archived;

	public long getAdminRoleId() {
		return adminRoleId;
	}

	public void setAdminRoleId(long adminRoleId) {
		this.adminRoleId = adminRoleId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String toJSON() throws JsonProcessingException {
		//ObjectMapper om = new ObjectMapper();
		ObjectMapper om = OMSingleton.getInstance().getOm();
		return om.writeValueAsString(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (adminRoleId ^ (adminRoleId >>> 32));
		result = prime * result + (int) (archived ^ (archived >>> 32));
		result = prime * result + (int) (created ^ (created >>> 32));
		result = prime * result + ((roleName == null) ? 0 : roleName.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		if (!(obj instanceof AdminRoleMapping)) {
			return false;
		}
		AdminRoleMapping other = (AdminRoleMapping) obj;
		if (adminRoleId != other.adminRoleId) {
			return false;
		}
		if (archived != other.archived) {
			return false;
		}
		if (created != other.created) {
			return false;
		}
		if (roleName == null) {
			if (other.roleName != null) {
				return false;
			}
		} else if (!roleName.equals(other.roleName)) {
			return false;
		}
		if (status == null) {
			if (other.status != null) {
				return false;
			}
		} else if (!status.equals(other.status)) {
			return false;
		}
		if (subject == null) {
			if (other.subject != null) {
				return false;
			}
		} else if (!subject.equals(other.subject)) {
			return false;
		}
		if (target == null) {
			if (other.target != null) {
				return false;
			}
		} else if (!target.equals(other.target)) {
			return false;
		}
		return true;
	}
}
