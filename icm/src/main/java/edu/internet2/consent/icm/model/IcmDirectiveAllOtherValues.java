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
public class IcmDirectiveAllOtherValues {

	@Enumerated(EnumType.STRING)
	@JsonProperty("icmReleaseDirective")
	IcmReleaseDirective icmReleaseDirective;
	
	@Enumerated(EnumType.STRING)
	AllOtherValuesConst allOtherValuesConst;
	
	
	@JsonProperty("icmReleaseDirective")
	public IcmReleaseDirective getIcmReleaseDirective() {
		return icmReleaseDirective;
	}

	@JsonProperty("icmReleaseDirective")
	public void setIcmReleaseDirective(IcmReleaseDirective icmReleaseDirective) {
		this.icmReleaseDirective = icmReleaseDirective;
	}
	
	@JsonProperty("orgReleaseDirective")
	public void setOrgReleaseDirective(IcmReleaseDirective icmReleaseDirective) {
		this.icmReleaseDirective = icmReleaseDirective;
	}

	@JsonProperty("allOtherValuesConst")
	public AllOtherValuesConst getAllOtherValuesConst() {
		return allOtherValuesConst;
	}

	@JsonProperty("allOtherValuesConst")
	public void setAllOtherValuesConst(AllOtherValuesConst allOtherValuesConst) {
		this.allOtherValuesConst = allOtherValuesConst;
	}
	
	// Overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		IcmDirectiveAllOtherValues i = (IcmDirectiveAllOtherValues) o;
		return (i.getIcmReleaseDirective() == this.getIcmReleaseDirective());
	}
	@Override
	public int hashCode() {
		return Objects.hash(icmReleaseDirective);
	}
	
	@Override
	public String toString() {
		return "icmReleaseDirective=" + icmReleaseDirective.toString();
	}
}
