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
import javax.persistence.Column;
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

import edu.internet2.consent.informed.util.OMSingleton;

@Entity
public class ReturnedRPMetaInformation {

	@Id
	@JsonIgnore
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long rpmiid;
	
	@JsonProperty("rhidentifier")
	@Embedded
	private RHIdentifier rhidentifier;
	
	@JsonProperty("rpidentifier")
	@OneToOne(cascade=CascadeType.ALL)
	private RPIdentifier rpidentifier;
	
	@JsonProperty("displayname")
	@OneToOne(cascade=CascadeType.ALL)
	private InternationalizedString displayname;
	
	@JsonProperty("description")
	@OneToOne(cascade=CascadeType.ALL)
	private InternationalizedString description;
	
	@JsonProperty("iconurl")
	@Column(length=40000)
	private String iconurl;
	
	@JsonProperty("privacyurl")
	@Column(length=4000)
	private String privacyurl;
	
	@JsonProperty("rpproperties")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private List<ReturnedRPProperty> rpproperties;
	
	@JsonProperty("version")
	private int version;
	
	@JsonProperty("updated")
	private long updated;
	
	@JsonProperty("state")
	private String state;
	
	@JsonProperty("defaultshowagain")
	private String defaultshowagain;
	
	public String getDefaultshowagain() {
		return defaultshowagain;
	}
	
	public void setDefaultshowagain(String def) {
		this.defaultshowagain = def;
	}

	public List<ReturnedRPProperty> getRpproperties() {
		return rpproperties;
	}

	public void setRpproperties(List<ReturnedRPProperty> rpproperties) {
		this.rpproperties = rpproperties;
	}

	public Long getRpmiid() {
		return rpmiid;
	}

	public void setRpmiid(Long rpmiid) {
		this.rpmiid = rpmiid;
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

	public String getIconurl() {
		return iconurl;
	}

	public void setIconurl(String iconurl) {
		this.iconurl = iconurl;
	}

	public String getPrivacyurl() {
		return privacyurl;
	}

	public void setPrivacyurl(String privacyurl) {
		this.privacyurl = privacyurl;
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
		ReturnedRPMetaInformation rrmi = (ReturnedRPMetaInformation) o;
		return (this.getRpproperties().equals(rrmi.getRpproperties()) && this.getRhidentifier().equals(rrmi.getRhidentifier()) && this.getRpidentifier().equals(rrmi.getRpidentifier()) && this.getDescription().equals(rrmi.getDescription()) && this.getDisplayname().equals(rrmi.getDisplayname()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getRpproperties(),this.getRhidentifier(),this.getRpidentifier(),this.getDescription(),this.getDisplayname());
	}
	
	public String toJSON() throws JsonProcessingException {
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		return mapper.writeValueAsString(this);
	}
}
