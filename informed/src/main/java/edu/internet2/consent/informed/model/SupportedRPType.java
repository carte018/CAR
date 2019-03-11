package edu.internet2.consent.informed.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class SupportedRPType {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private long srpiid;
	
	@JsonProperty("rhtype")
	private String rhtype;
	
	@JsonProperty("rhid")
	private String rhid;
	
	@JsonProperty("rptype")
	private String rptype;
	
	@JsonProperty("description")
	private String description;

	public String getRptype() {
		return rptype;
	}

	public void setRptype(String rptype) {
		this.rptype = rptype;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public long getSrpiid() {
		return srpiid;
	}

	public void setSrpiid(long srpiid) {
		this.srpiid = srpiid;
	}

	public String getRhtype() {
		return rhtype;
	}

	public void setRhtype(String rhtype) {
		this.rhtype = rhtype;
	}

	public String getRhid() {
		return rhid;
	}

	public void setRhid(String rhid) {
		this.rhid = rhid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		SupportedRPType ri = (SupportedRPType) o;
		try {
			return (this.getDescription().equals(ri.getDescription()) && this.getRptype().equals(ri.getRptype()));
		} catch (Exception e) {
			// Nulls are not equal here
			return false;
		}
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		return om.writeValueAsString(this);
	}

}
