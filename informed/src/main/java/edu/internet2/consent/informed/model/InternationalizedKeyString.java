package edu.internet2.consent.informed.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InternationalizedKeyString {

	@JsonProperty("key")
	private String key;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public InternationalizedString getI18nstring() {
		return i18nstring;
	}
	public void setI18nstring(InternationalizedString i18nstring) {
		this.i18nstring = i18nstring;
	}
	@JsonProperty ("i18nstring")
	private InternationalizedString i18nstring;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		InternationalizedKeyString iks = (InternationalizedKeyString) o;
		return (this.getKey().equals(iks.getKey()) && this.getI18nstring().equals(iks.getI18nstring()));
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
