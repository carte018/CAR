package edu.internet2.consent.informed.model;

import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Embeddable
public class RHIdentifier {

	@JsonProperty("rhtype")
	private String rhtype;
	@JsonProperty("rhid")
	private String rhid;
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
		RHIdentifier rhi = (RHIdentifier) o;
		return (this.getRhid().equals(rhi.getRhid()) && this.getRhtype().equals(rhi.getRhtype()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getRhid(),this.getRhtype());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
