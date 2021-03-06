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

import javax.persistence.ElementCollection;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.informed.util.OMSingleton;


@javax.persistence.Entity
public class ReturnedInfoTypeList {

	// mandatory ID for index
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long typelistID;
	
	// RH type 
	@JsonProperty("rhtype")
	private String rhtype;
	
	// RH identifier
	@JsonProperty("rhvalue")
	private String rhvalue;
	
	// List of string infotypes supported by {rhtype,rhvalue}
	@ElementCollection 
	@JsonProperty("infotypes")
	private List<String> infotypes;
	
	// [SG]etters
	public String getRhtype() {
		return rhtype;
	}
	public void setRhtype(String rhtype) {
		this.rhtype = rhtype;
	}
	public String getRhvalue() {
		return rhvalue;
	}
	public void setRhvalue(String rhvalue) {
		this.rhvalue = rhvalue;
	}
	public List<String> getInfotypes() {
		return infotypes;
	}
	public void setInfotypes(List<String> infotypes) {
		this.infotypes = infotypes;
	}
	
	//Overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ReturnedInfoTypeList rco = (ReturnedInfoTypeList)o;
		return(rco.getRhvalue().equals(this.getRhvalue()) && rco.getRhtype().equals(this.getRhtype()) && rco.getInfotypes().containsAll(this.getInfotypes()) && this.getInfotypes().containsAll(rco.getInfotypes()));
			}
	
	@Override
	public int hashCode() {
		return Objects.hash(rhtype,rhvalue,infotypes);
	}
	
	public String toJSON() throws JsonProcessingException{
		// Return JSON representation of self
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
