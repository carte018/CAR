package edu.internet2.consent.informed.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class InfoItemValueList {
	
	@JsonIgnore
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long ivlid;

	@JsonProperty("infoitemidentifier")
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private InfoItemIdentifier infoitemidentifier;
	
	@JsonProperty("sourceitemname")
	private String sourceitemname;  // non-mandatory map attribute -- if unset, assumed = infoitemidentifier.iiid
	
	@JsonProperty("reason")
	@OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	private InternationalizedString reason;
	public String getSourceitemname() {
		return sourceitemname;
	}
	public void setSourceitemname(String sourceitemname) {
		this.sourceitemname = sourceitemname;
	}

	@JsonProperty("valuelist")
	@ElementCollection 
	private List<String> valuelist;
	
	public Long getIvlid() {
		return ivlid;
	}
	public void setIvlid(Long ivlid) {
		this.ivlid = ivlid;
	}

	public InfoItemIdentifier getInfoitemidentifier() {
		return infoitemidentifier;
	}
	public void setInfoitemidentifier(InfoItemIdentifier infoitemidentifier) {
		this.infoitemidentifier = infoitemidentifier;
	}
	public InternationalizedString getReason() {
		return reason;
	}
	public void setReason(InternationalizedString reason) {
		this.reason = reason;
	}
	public List<String> getValuelist() {
		return valuelist;
	}
	public void setValuelist(List<String> valuelist) {
		this.valuelist = valuelist;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		InfoItemValueList ivl = (InfoItemValueList) o;
		return (this.getInfoitemidentifier().equals(ivl.getInfoitemidentifier()) && this.getValuelist().equals(ivl.getValuelist()) && this.getReason().equals(ivl.getReason()) && this.getSourceitemname().equals(ivl.getSourceitemname()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getInfoitemidentifier(),this.getValuelist(),this.getReason(),this.getSourceitemname());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
