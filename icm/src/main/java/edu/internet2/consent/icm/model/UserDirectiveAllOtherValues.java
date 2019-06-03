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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class UserDirectiveAllOtherValues {
	
	@Enumerated(EnumType.STRING)
	
	UserReleaseDirective userReleaseDirective;
	@Enumerated(EnumType.STRING)
	AllOtherValuesConst allOtherValues;
	
	@JsonProperty("releaseDirective")
	public UserReleaseDirective getUserReleaseDirective() {
		return userReleaseDirective;
	}
	public void setUserReleaseDirective(UserReleaseDirective userReleaseDirective) {
		this.userReleaseDirective = userReleaseDirective;
	}
	
	@JsonProperty("allOtherValuesConst")
	public AllOtherValuesConst getAllOtherValues() {
		return allOtherValues;
	}
	public void setAllOtherValues(AllOtherValuesConst allOtherValues) {
		this.allOtherValues = allOtherValues;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		
		// Value of allOtherValuesConst is constant
		return (((UserDirectiveAllOtherValues) o).getUserReleaseDirective().equals(this.getUserReleaseDirective()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(userReleaseDirective,allOtherValues);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class DirectiveAllOtherValue {\n");
		sb.append("  userReleaseDirective: ").append(userReleaseDirective).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
