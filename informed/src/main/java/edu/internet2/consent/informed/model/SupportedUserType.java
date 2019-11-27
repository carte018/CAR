package edu.internet2.consent.informed.model;

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
public class SupportedUserType {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private long sutid;
	
	@JsonProperty("utype")
	private String utype;
	
	@JsonProperty("description")
	private String description;

	public long getSutid() {
		return sutid;
	}

	public void setSutid(long sutid) {
		this.sutid = sutid;
	}

	public String getUtype() {
		return utype;
	}

	public void setUtype(String utype) {
		this.utype = utype;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		SupportedUserType ri = (SupportedUserType) o;
		try {
			return (this.getDescription().equals(ri.getDescription()) && this.getUtype().equals(ri.getUtype()));
		} catch (Exception e) {
			// Nulls are not equal here
			return false;
		}
	}
	
	public String toJSON() throws JsonProcessingException {
		//ObjectMapper om = new ObjectMapper();
		ObjectMapper om = OMSingleton.getInstance().getOm();
		return om.writeValueAsString(this);
	}
}
