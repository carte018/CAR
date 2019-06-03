/*
 * Copyright 2015 - 2019 Duke University
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2 as published by
    the Free Software Foundation.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License Version 2
    along with this program.  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt>.

 */
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
public class RelyingPartyProperty {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long relyingPartyIdKey;
	
	@JsonIgnore
	public Long getRelyingPartyIdKey() {
		return relyingPartyIdKey;
	}
	@JsonIgnore
	public void setRelyingPartyIdKey(Long x) {
		relyingPartyIdKey = x;
	}
	
	@JsonProperty("RP-PropName")
	private String rpPropName;
	@JsonProperty("RP-PropValue")
	private String rpPropValue;
	
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
