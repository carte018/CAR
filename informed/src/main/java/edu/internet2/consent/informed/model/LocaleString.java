package edu.internet2.consent.informed.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// representation of a string inside a locale

@Entity
public class LocaleString {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long localstringid;
	
	@JsonProperty("locale")
	private String locale;
	@JsonProperty("value")
	@Column(length=4000)
	private String value;
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		LocaleString ls = (LocaleString) o;
		return (this.getLocale().equals(ls.getLocale()) && this.getValue().equals(ls.getValue()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getLocale(),this.getValue());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
