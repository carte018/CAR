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
/* DEPRECATED */
/*package edu.internet2.consent.incon.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class InfoItemInformedContent {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long RPICIdentifier;
	
	@JsonProperty("infoName")
	private String infoName;
	@JsonProperty("infoValue")
	private String infoValue;
	@JsonProperty("modality")
	private String modality;  // "single","controlled"(multi),"uncontrolled"(multi)
	@JsonProperty("vocabulary")
	@ElementCollection(fetch=FetchType.EAGER)
	private Set<String> vocabulary;
	
	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public Long getRPICIdentifier() {
		return RPICIdentifier;
	}

	public void setRPICIdentifier(Long rPICIdentifier) {
		RPICIdentifier = rPICIdentifier;
	}

	public String getInfoName() {
		return infoName;
	}

	public void setInfoName(String infoName) {
		this.infoName = infoName;
	}

	public String getInfoValue() {
		return infoValue;
	}

	public void setInfoValue(String infoValue) {
		this.infoValue = infoValue;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@JsonProperty("displayName")
	private String displayName;
	
	public InfoItemInformedContent() {
		this.vocabulary = new HashSet<String>();
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
	
}*/
