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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class AllOtherInfoId {
	@Enumerated(EnumType.STRING)
	@JsonProperty("allOtherInfoType")
	private AllOtherInfoTypeConst allOtherInfoType;
	@Enumerated(EnumType.STRING)
	@JsonProperty("allOtherInfoValue")
	private AllOtherInfoValueConst allOtherInfoValue;
	
	public AllOtherInfoTypeConst getAllOtherInfoType() {
		return allOtherInfoType;
	}
	public void setAllOtherInfoType(AllOtherInfoTypeConst allOtherInfoType) {
		this.allOtherInfoType = allOtherInfoType;
	}
	public AllOtherInfoValueConst getAllOtherInfoValue() {
		return allOtherInfoValue;
	}
	public void setAllOtherInfoValue(AllOtherInfoValueConst allOtherInfoValue) {
		this.allOtherInfoValue = allOtherInfoValue;
	}
	
	@Override 
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		AllOtherInfoId aoi = (AllOtherInfoId) o;
		return (aoi.getAllOtherInfoType().equals(this.getAllOtherInfoType()) && aoi.getAllOtherInfoValue().equals(this.getAllOtherInfoValue()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(allOtherInfoType,allOtherInfoValue);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class AllOtherInfoId {\n");
		sb.append("  AllOtherInfoType: ").append(this.getAllOtherInfoType()).append("\n");
		sb.append("  AllOtherInfoValue: ").append(this.getAllOtherInfoValue()).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
