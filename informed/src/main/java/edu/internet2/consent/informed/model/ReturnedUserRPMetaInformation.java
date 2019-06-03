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

import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class ReturnedUserRPMetaInformation {

	@Id
	@JsonIgnore
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long rumiid;
	
	@Embedded
	@JsonProperty("useridentifier")
	private UserIdentifier useridentifier;
	
	@JsonProperty("rpidentifier")
	@OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private RPIdentifier rpidentifier;
	
	private boolean showagain;
	
	private long lastinteracted;

	public Long getRumiid() {
		return rumiid;
	}

	public void setRumiid(Long rumiid) {
		this.rumiid = rumiid;
	}

	public UserIdentifier getUseridentifier() {
		return useridentifier;
	}

	public void setUseridentifier(UserIdentifier useridentifier) {
		this.useridentifier = useridentifier;
	}

	public RPIdentifier getRpidentifier() {
		return rpidentifier;
	}

	public void setRpidentifier(RPIdentifier rpidentifier) {
		this.rpidentifier = rpidentifier;
	}

	public boolean isShowagain() {
		return showagain;
	}

	public void setShowagain(boolean showagain) {
		this.showagain = showagain;
	}

	public long getLastinteracted() {
		return lastinteracted;
	}

	public void setLastinteracted(long lastinteracted) {
		this.lastinteracted = lastinteracted;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ReturnedUserRPMetaInformation rurpi = (ReturnedUserRPMetaInformation) o;
		return (this.getLastinteracted() == rurpi.getLastinteracted() && this.getUseridentifier().equals(rurpi.getUseridentifier()) && this.getRpidentifier().equals(rurpi.getRpidentifier()) && this.isShowagain() == rurpi.isShowagain());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getLastinteracted(),this.getUseridentifier(),this.getRpidentifier(),this.isShowagain());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
