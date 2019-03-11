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
public class ReturnedRHInfoItemList {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long infoitemlistid;
	
	@JsonProperty("rhidentifier")
	@Embedded
	private RHIdentifier rhidentifier;
	@JsonProperty("infoitemlist")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch = FetchType.EAGER)
	private List<InfoItemIdentifier> infoitemlist;
	public Long getInfoitemlistid() {
		return infoitemlistid;
	}
	public void setInfoitemlistid(Long infoitemlistid) {
		this.infoitemlistid = infoitemlistid;
	}
	public RHIdentifier getRhidentifier() {
		return rhidentifier;
	}
	public void setRhidentifier(RHIdentifier rhidentifier) {
		this.rhidentifier = rhidentifier;
	}
	public List<InfoItemIdentifier> getInfoitemlist() {
		return infoitemlist;
	}
	public void setInfoitemlist(List<InfoItemIdentifier> infoitemlist) {
		this.infoitemlist = infoitemlist;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ReturnedRHInfoItemList rrhil = (ReturnedRHInfoItemList) o;
		return (this.getRhidentifier().equals(rrhil.getRhidentifier()) && this.getInfoitemlist().equals(rrhil.getInfoitemlist()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getRhidentifier(),this.getInfoitemlist());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
