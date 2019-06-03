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
public class UserId {
	
	private String userType;
	private String userValue;
	
	@JsonProperty("user-type")
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	
	@JsonProperty("user-value")
	public String getUserValue() {
		return userValue;
	}
	public void setUserValue(String userValue) {
		this.userValue = userValue;
	}
	
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		return (((UserId) o).getUserType().equals(this.getUserType()) && ((UserId) o).getUserValue().equals(this.getUserValue()));
	}
	
	public int hashCode() {
		return Objects.hash(userType,userValue);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class UserId {\n");
		sb.append("  userType: ").append(userType).append("\n");
		sb.append("  userValue: ").append(userValue).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
