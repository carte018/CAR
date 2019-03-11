package edu.internet2.consent.icm.model;

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
public class RelyingPartyProperty {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long relyingPartyIdKey;
	
	@JsonProperty("RP-PropName")
	private String rpPropName;
	@JsonProperty("RP-PropValue")
	private String rpPropValue;
	
	@JsonIgnore
	public Long getRelyingPartyIdKey() {
		return relyingPartyIdKey;
	}
	@JsonIgnore
	public void setRelyingPartyIdKey(Long relyingPartyIdKey) {
		this.relyingPartyIdKey = relyingPartyIdKey;
	}
	
	@JsonProperty("RP-PropName")
	public String getRpPropName() {
		return rpPropName;
	}
	
	@JsonProperty("RP-PropName")
	public void setRpPropName(String rpPropName) {
		this.rpPropName = rpPropName;
	}
	
	@JsonProperty("RP-PropValue")
	public String getRpPropValue() {
		return rpPropValue;
	}
	
	@JsonProperty("RP-PropValue")
	public void setRpPropValue(String rpPropValue) {
		this.rpPropValue = rpPropValue;
	}
	
	//Overrides
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		RelyingPartyProperty r = (RelyingPartyProperty) o;
		return (r.getRelyingPartyIdKey() == this.getRelyingPartyIdKey() && r.getRpPropName().equals(this.getRpPropName()) && r.getRpPropValue().equals(this.getRpPropValue()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(rpPropName,rpPropValue,relyingPartyIdKey);
	}
	
	@Override
	public String toString() {
		return "rpPropName="+rpPropName+", rpPropValue="+rpPropValue;
	}
}
