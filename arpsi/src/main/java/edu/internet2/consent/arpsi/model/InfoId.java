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
