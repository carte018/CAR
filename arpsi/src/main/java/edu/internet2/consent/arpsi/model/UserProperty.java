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
public class UserProperty {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long userPropertyKey;
	
	@JsonProperty("userPropName")
	private String userPropName;
	@JsonProperty("userPropValue")
	private String userPropValue;
	
	@JsonIgnore
	public Long getUserPropertyKey() {
		return userPropertyKey;
	}
	@JsonIgnore
	public void setUserPropertyKey(Long userPropertyKey) {
		this.userPropertyKey = userPropertyKey;
	}
	
	@JsonProperty("userPropName")
	public String getUserPropName() {
		return userPropName;
	}
	@JsonProperty("userPropName")
	public void setUserPropName(String userPropName) {
		this.userPropName = userPropName;
	}
	
	@JsonProperty("userPropValue")
	public String getUserPropValue() {
		return userPropValue;
	}
	@JsonProperty("userPropValue")
	public void setUserPropValue(String userPropValue) {
		this.userPropValue = userPropValue;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		UserProperty u = (UserProperty) o;
		return (u.getUserPropertyKey() == this.getUserPropertyKey() && u.getUserPropName().equals(this.getUserPropName()) && u.getUserPropValue().equals(this.getUserPropValue()));
	}
	@Override
	public int hashCode() {
		return Objects.hash(userPropName,userPropValue,userPropertyKey);
	}
	@Override
	public String toString() {
		return "userPropName="+userPropName+", userPropValue="+userPropValue;
	}
}
