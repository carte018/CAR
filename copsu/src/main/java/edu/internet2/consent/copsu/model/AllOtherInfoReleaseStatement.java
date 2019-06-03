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
import javax.persistence.Embedded;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class AllOtherInfoReleaseStatement {
	
	@Embedded
	@JsonProperty("allOtherInfoId")
	private AllOtherInfoId allOtherInfoId;
	@Embedded
	@JsonProperty("directiveAllOtherValues")
	private DirectiveAllOtherValues directiveAllOtherValues;
	public AllOtherInfoId getAllOtherInfoId() {
		return allOtherInfoId;
	}
	public void setAllOtherInfoId(AllOtherInfoId allOtherInfoId) {
		this.allOtherInfoId = allOtherInfoId;
	}
	public DirectiveAllOtherValues getDirectiveAllOtherValues() {
		return directiveAllOtherValues;
	}
	public void setDirectiveAllOtherValues(DirectiveAllOtherValues directiveAllOtherValues) {
		this.directiveAllOtherValues = directiveAllOtherValues;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		AllOtherInfoReleaseStatement aoir = (AllOtherInfoReleaseStatement) o;
		return (this.getDirectiveAllOtherValues().equals(aoir.getDirectiveAllOtherValues()) && this.getAllOtherInfoId().equals(aoir.getAllOtherInfoId()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(directiveAllOtherValues,allOtherInfoId);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class AllOtherInfoReleaseStatement {\n");
		sb.append("  allOtherInfoId: ").append(allOtherInfoId.toString()).append("\n");
		if (directiveAllOtherValues != null) {
			sb.append("  directiveAllOtherValues: ").append(directiveAllOtherValues.toString()).append("\n");
		} else {
			sb.append("  ERROR - directiveAllOtherValues is null!\n");
		}
		sb.append("}\n");
		return sb.toString();
	}
}
