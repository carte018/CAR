package edu.internet2.consent.informed.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class ReturnedRPProperty {

	@Id
	@JsonIgnore
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long rppropertyid;
	
	@JsonProperty("rppropertyname")
	private String rppropertyname;
	
	@JsonProperty("rppropertyvalue")
	private String rppropertyvalue;

	public long getRppropertyid() {
		return rppropertyid;
	}

	public void setRppropertyid(long rppropertyid) {
		this.rppropertyid = rppropertyid;
	}

	public String getRppropertyname() {
		return rppropertyname;
	}

	public void setRppropertyname(String rppropertyname) {
		this.rppropertyname = rppropertyname;
	}

	public String getRppropertyvalue() {
		return rppropertyvalue;
	}

	public void setRppropertyvalue(String rppropertyvalue) {
		this.rppropertyvalue = rppropertyvalue;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ReturnedRPProperty rrpp = (ReturnedRPProperty) o;
		return (this.getRppropertyname().equals(rrpp.getRppropertyname()) && this.getRppropertyvalue().equals(rrpp.getRppropertyvalue()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getRppropertyname(),this.getRppropertyvalue());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
