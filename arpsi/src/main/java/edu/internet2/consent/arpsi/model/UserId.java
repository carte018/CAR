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
public class UserId {
	@JsonProperty("user-type")
	private String userType;
	@JsonProperty("user-value")
	private String userValue;
	
	@JsonProperty("user-value")
	public String getUserValue() {
		return userValue;
	}
	@JsonProperty("user-value")
	public void setUserValue(String userValue) {
		this.userValue = userValue;
	}
	
	@JsonProperty("user-type")
	public String getUserType() {
		return userType;
	}
	@JsonProperty("user-type")
	public void setUserType(String userType) {
		this.userType = userType;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		UserId u = (UserId) o;
		return (u.getUserType().equals(this.getUserType()) && u.getUserValue().equals(this.getUserValue()));
	}
	@Override
	public int hashCode() {
		return Objects.hash(userType,userValue);
	}
	@Override
	public String toString() {
		return "userType="+userType+", userValue="+userValue;
	}
}
