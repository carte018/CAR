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
public class SupportedRHType {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private long srhiid;
	
	@JsonProperty("rhtype")
	private String rhtype;
	
	@JsonProperty("description")
	private String description;

	public String getRhtype() {
		return rhtype;
	}

	public void setRhtype(String rhtype) {
		this.rhtype = rhtype;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public long getSrhiid() {
		return srhiid;
	}

	public void setSrhiid(long srhiid) {
		this.srhiid = srhiid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		SupportedRHType ri = (SupportedRHType) o;
		try {
			return (this.getDescription().equals(ri.getDescription()) && this.getRhtype().equals(ri.getRhtype()));
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
