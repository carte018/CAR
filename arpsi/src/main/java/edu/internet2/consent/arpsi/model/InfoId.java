package edu.internet2.consent.arpsi.model;

import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class InfoId {
	
	@JsonProperty("info-type")
	String infoType;
	@JsonProperty("info-value")
	String infoValue;
	@JsonProperty("info-type")
	public String getInfoType() {
		return infoType;
	}
	@JsonProperty("info-type")
	public void setInfoType(String infoType) {
		this.infoType = infoType;
	}
	@JsonProperty("info-value")
	public String getInfoValue() {
		return infoValue;
	}
	@JsonProperty("info-value")
	public void setInfoValue(String infoValue) {
		this.infoValue = infoValue;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		InfoId i = (InfoId) o;
		return (i.getInfoType().equals(this.getInfoType()) && i.getInfoValue().equals(this.getInfoValue()));
	}
	@Override
	public int hashCode() {
		return Objects.hash(infoType,infoValue);
	}
	@Override
	public String toString() {
		return "infoType="+infoType+", infoValue="+infoValue;
	}
}
