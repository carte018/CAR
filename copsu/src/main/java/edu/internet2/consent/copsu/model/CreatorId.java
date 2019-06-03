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
package edu.internet2.consent.copsu.model;

import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
 
@Embeddable
public class CreatorId {
	private String creatingUserType;
	private String creatingUserValue;
	
	@JsonProperty("user-type")
	public String getCreatingUserType() {
		return creatingUserType;
	}
	public void setCreatingUserType(String creatingUserType) {
		this.creatingUserType = creatingUserType;
	}
	@JsonProperty("user-value")
	public String getCreatingUserValue() {
		return creatingUserValue;
	}
	public void setCreatingUserValue(String creatingUserValue) {
		this.creatingUserValue = creatingUserValue;
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		CreatorId ci = (CreatorId) o;
		return (ci.getCreatingUserType().equals(this.getCreatingUserType()) && ci.getCreatingUserValue().equals(this.getCreatingUserValue()));
	}
	@Override
	public int hashCode() {
		return Objects.hash(creatingUserType,creatingUserValue);
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CreatorId {\n");
		sb.append("    creatingUserType: ").append(creatingUserType).append("\n");
		sb.append("     creatingUserValue: ").append(creatingUserValue).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
	
}
