package edu.internet2.consent.arpsi.model;

import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
@Entity
public class ValueObject {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long valueKey;
	
	private String value;

	@JsonIgnore
	public Long getValueKey() {
		return valueKey;
	}

	@JsonIgnore
	public void setValueKey(Long valueKey) {
		this.valueKey = valueKey;
	}

	@JsonProperty("value")
	public String getValue() {
		return value;
	}

	@JsonProperty("value")
	public void setValue(String value) {
		this.value = value;
	}
	
	public ValueObject(String x) {
		this.value = x;
	}
	
	public ValueObject() {
		// do nothing
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ValueObject v = (ValueObject) o;
		return (v.getValue().equals(this.getValue()));
	}
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}
	@Override
	public String toString() {
		return "value="+value;
	}
}
