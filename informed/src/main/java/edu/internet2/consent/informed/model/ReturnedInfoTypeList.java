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


@javax.persistence.Entity
public class ReturnedInfoTypeList {

	// mandatory ID for index
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
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
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
