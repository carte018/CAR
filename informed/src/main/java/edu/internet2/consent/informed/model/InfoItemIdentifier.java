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
public class InfoItemIdentifier {


	@Id
	@JsonIgnore
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long iiiid;
	
	@JsonProperty("iitype")
	private String iitype;
	@JsonProperty("iiid")
	private String iiid;
	public Long getIiiid() {
		return iiiid;
	}
	public void setIiiid(Long iiiid) {
		this.iiiid = iiiid;
	}

	public String getIitype() {
		return iitype;
	}
	public void setIitype(String iitype) {
		this.iitype = iitype;
	}
	public String getIiid() {
		return iiid;
	}
	public void setIiid(String iiid) {
		this.iiid = iiid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		InfoItemIdentifier ii = (InfoItemIdentifier) o;
		return (this.getIiid().equals(ii.getIiid()) && this.getIitype().equals(ii.getIitype()));
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
	
}
