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

import javax.persistence.Embedded;
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
public class ReturnedValueMetaInformation {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long vmiid;
	
	@JsonProperty("infoitemname")
	private String infoitemname;
	
	@JsonProperty("infoitemvalue")
	private String infoitemvalue;
	
	@JsonProperty("displayname")
	private String displayname;
	
	@JsonProperty("version")
	private int version;
	
	@JsonProperty("updated")
	private long updated;
	
	@JsonProperty("state")
	private String state;
	
	@JsonProperty("asnd")
	private boolean asnd;
	
	@JsonProperty("rhidentifier")
	@Embedded
	private RHIdentifier rhidentifier;
	
	@JsonProperty("infoitemtype")
	private String infoitemtype;
	
	
	public RHIdentifier getRhidentifier() {
		return rhidentifier;
	}

	public void setRhidentifier(RHIdentifier rhidentifier) {
		this.rhidentifier = rhidentifier;
	}

	public String getInfoitemtype() {
		return infoitemtype;
	}

	public void setInfoitemtype(String infoitemtype) {
		this.infoitemtype = infoitemtype;
	}

	public Long getVmiid() {
		return vmiid;
	}

	public void setVmiid(Long vmiid) {
		this.vmiid = vmiid;
	}

	public String getInfoitemname() {
		return infoitemname;
	}

	public void setInfoitemname(String infoitemname) {
		this.infoitemname = infoitemname;
	}

	public String getInfoitemvalue() {
		return infoitemvalue;
	}

	public void setInfoitemvalue(String infoitemvalue) {
		this.infoitemvalue = infoitemvalue;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
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
	
	public boolean getAsnd() {
		return asnd;
	}
	
	public void setAsnd(boolean v) {
		this.asnd = v;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ReturnedValueMetaInformation rvmi = (ReturnedValueMetaInformation) o;
		return (this.getInfoitemname().equals(rvmi.getInfoitemname()) && this.getInfoitemvalue().equals(rvmi.getInfoitemvalue()) && this.getDisplayname().equals(rvmi.getDisplayname()));

	}
	
	@Override 
	public int hashCode() {
		return Objects.hash(this.getDisplayname(),this.getInfoitemname(),this.getInfoitemvalue());
	}
	
	public String toJSON() throws JsonProcessingException {
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		return mapper.writeValueAsString(this);
	}
	
}
