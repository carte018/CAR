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
public class SupportedLanguage {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private long sliid;
	
	@JsonProperty("lang")
	private String lang;
	
	@JsonProperty("displayname")
	private String displayname;

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}
	
	public long getSliid() {
		return sliid;
	}

	public void setSliid(long sliid) {
		this.sliid = sliid;
	}

	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		SupportedLanguage ri = (SupportedLanguage) o;
		try {
			return (this.getDisplayname().equals(ri.getDisplayname()) && this.getLang().equals(ri.getLang()));
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
