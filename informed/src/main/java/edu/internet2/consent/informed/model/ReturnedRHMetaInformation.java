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
public class ReturnedRHMetaInformation {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long rhmetainfoid;
	
	@JsonProperty("rhidentifier")
	@Embedded
	private RHIdentifier rhidentifier;
	
	@JsonProperty("displayname")
	@OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private InternationalizedString displayname;
	
	@JsonProperty("description")
	@OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private InternationalizedString description;
	
	@JsonProperty("version")
	private int version;
	
	@JsonProperty("updated")
	private long updated;
	
	@JsonProperty("state")
	private String state;

	public Long getRhmetainfoid() {
		return rhmetainfoid;
	}

	public void setRhmetainfoid(Long rhmetainfoid) {
		this.rhmetainfoid = rhmetainfoid;
	}

	public RHIdentifier getRhidentifier() {
		return rhidentifier;
	}

	public void setRhidentifier(RHIdentifier rhidentifier) {
		this.rhidentifier = rhidentifier;
	}

	public InternationalizedString getDisplayname() {
		return displayname;
	}

	public void setDisplayname(InternationalizedString displayname) {
		this.displayname = displayname;
	}

	public InternationalizedString getDescription() {
		return description;
	}

	public void setDescription(InternationalizedString description) {
		this.description = description;
	}
	
	public void setVersion(int value) {
		this.version = value;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setUpdated(long value) {
		this.updated = value;
	}
	
	public long getUpdated() {
		return updated;
	}
	
	public void setState(String value) {
		this.state = value;
	}
	
	public String getState() {
		return state;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ReturnedRHMetaInformation rrhmi = (ReturnedRHMetaInformation) o;
		return (this.getRhidentifier().equals(rrhmi.getRhidentifier()) && this.getDescription().equals(rrhmi.getDescription()) && this.getDisplayname().equals(rrhmi.getDisplayname()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getRhidentifier(),this.getDescription(),this.getDisplayname());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}

