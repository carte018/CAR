package edu.internet2.consent.informed.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class ReturnedRHRPList {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long rplistid;
	
	@JsonProperty("rhidentifier")
	@Embedded
	private RHIdentifier rhidentifier;
	
	@JsonProperty("rplist")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch = FetchType.EAGER)
	private List<RPIdentifier> rplist;

	public Long getRplistid() {
		return rplistid;
	}

	public void setRplistid(Long rplistid) {
		this.rplistid = rplistid;
	}

	public RHIdentifier getRhidentifier() {
		return rhidentifier;
	}

	public void setRhidentifier(RHIdentifier rhidentifier) {
		this.rhidentifier = rhidentifier;
	}

	public List<RPIdentifier> getRplist() {
		return rplist;
	}

	public void setRplist(List<RPIdentifier> rplist) {
		this.rplist = rplist;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ReturnedRHRPList rrhrl = (ReturnedRHRPList) o;
		return (this.getRhidentifier().equals(rrhrl.getRhidentifier()) && this.getRplist().equals(rrhrl.getRplist()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getRhidentifier(),this.getRplist());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
