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
public class SupportedIIType {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private long sitiid;
	
	@JsonProperty("rhtype")
	private String rhtype;
	
	@JsonProperty("rhid")
	private String rhid;
	
	@JsonProperty("iitype")
	private String iitype;
	
	@JsonProperty("description")
	private String description;

	public String getIitype() {
		return iitype;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public long getSitiid() {
		return sitiid;
	}

	public void setSitiid(long sitiid) {
		this.sitiid = sitiid;
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

	public void setIitype(String iitype) {
		this.iitype = iitype;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		SupportedIIType ri = (SupportedIIType) o;
		try {
			return (this.getDescription().equals(ri.getDescription()) && this.getIitype().equals(ri.getIitype()));
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
