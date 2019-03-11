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

// Not an entity -- just an identifier


@Entity
public class RPIdentifier {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long rpiid;
	
	@JsonProperty("rptype")
	private String rptype;
	@JsonProperty("rpid")
	private String rpid;
	public String getRptype() {
		return rptype;
	}
	public void setRptype(String rptype) {
		this.rptype = rptype;
	}
	public String getRpid() {
		return rpid;
	}
	public void setRpid(String rpid) {
		this.rpid = rpid;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		RPIdentifier ri = (RPIdentifier) o;
		return (ri.getRpid().equals(this.getRpid()) && ri.getRptype().equals(this.getRptype()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getRpid(),this.getRptype());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
