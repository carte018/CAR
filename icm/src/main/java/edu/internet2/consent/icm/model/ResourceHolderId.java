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
package edu.internet2.consent.icm.model;

import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class ResourceHolderId {

	private String RHType;
	private String RHValue;
	
	@JsonProperty("RH-type")
	public String getRHType() {
		return RHType;
	}
	public void setRHType(String rHType) {
		RHType = rHType;
	}
	
	@JsonProperty("RH-value")
	public String getRHValue() {
		return RHValue;
	}
	public void setRHValue(String rHValue) {
		RHValue = rHValue;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		return (((ResourceHolderId) o).getRHType().equals(this.getRHType()) && ((ResourceHolderId) o).getRHValue().equals(this.getRHValue()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(RHType,RHValue);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ResourceHolderId {\n");
		sb.append("  RHType: ").append(RHType).append("\n");
		sb.append("  RHValue: ").append(RHValue).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
	
}
