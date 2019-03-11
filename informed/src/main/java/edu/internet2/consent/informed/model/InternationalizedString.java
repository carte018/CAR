package edu.internet2.consent.informed.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class InternationalizedString {

	@Id
	@JsonIgnore
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long istringid;
	
	public Long getIstringid() {
		return istringid;
	}

	public void setIstringid(Long istringid) {
		this.istringid = istringid;
	}

	@JsonProperty("locales")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL)
	private List<LocaleString> locales;

	public List<LocaleString> getLocales() {
		return locales;
	}

	public void setLocales(List<LocaleString> locales) {
		this.locales = locales;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		InternationalizedString is = (InternationalizedString) o;
		return (is.getLocales().containsAll(this.getLocales()) && this.getLocales().containsAll(is.getLocales()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getLocales());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
