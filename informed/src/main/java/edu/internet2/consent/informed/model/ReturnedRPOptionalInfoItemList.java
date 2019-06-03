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

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class ReturnedRPOptionalInfoItemList {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long infoitemlistid;
	
	@JsonProperty("rhidentifier")
	@Embedded
	private RHIdentifier rhidentifier;
	
	@JsonProperty("rpidentifier")
	@OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private RPIdentifier rpidentifier;
	
	@JsonProperty("optionallist")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch = FetchType.EAGER)
	private List<InfoItemValueList> optionallist;
	
	@JsonProperty("version")
	private int version;
	
	@JsonProperty("updated")
	private long updated;
	
	@JsonProperty("state")
	private String state;

	public Long getInfoitemlistid() {
		return infoitemlistid;
	}

	public void setInfoitemlistid(Long infoitemlistid) {
		this.infoitemlistid = infoitemlistid;
	}

	public RHIdentifier getRhidentifier() {
		return rhidentifier;
	}

	public void setRhidentifier(RHIdentifier rhidentifier) {
		this.rhidentifier = rhidentifier;
	}

	public RPIdentifier getRpidentifier() {
		return rpidentifier;
	}

	public void setRpidentifier(RPIdentifier rpidentifier) {
		this.rpidentifier = rpidentifier;
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
		ReturnedRPOptionalInfoItemList rpil = (ReturnedRPOptionalInfoItemList) o;
		return (this.getRhidentifier().equals(rpil.getRhidentifier()) && this.getRpidentifier().equals(rpil.getRpidentifier()) && this.getOptionallist().equals(rpil.getOptionallist()));
	}
	
	public List<InfoItemValueList> getOptionallist() {
		return optionallist;
	}

	public void setOptionallist(List<InfoItemValueList> optionallist) {
		this.optionallist = optionallist;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getRhidentifier(),this.getRpidentifier(),this.getOptionallist());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
