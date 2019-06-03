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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
@Entity
public class DirectiveOnValues {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long DirectiveIdentifier;
	
	@JsonIgnore
	public Long getDirectiveIdentifier() {
		return DirectiveIdentifier;
	}
	@JsonIgnore
	public void setDirectiveIdentifier(Long directiveIdentifier) {
		DirectiveIdentifier = directiveIdentifier;
	}
	

	@JsonProperty("releaseDirective")
	@Enumerated(EnumType.STRING)
	ReleaseDirective releaseDirective;
	
	@JsonProperty("valueObjectList")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	List<ValueObject> valueObjectList;
	
	
	public ReleaseDirective getReleaseDirective() {
		return releaseDirective;
	}
	public void setReleaseDirective(ReleaseDirective releaseDirective) {
		this.releaseDirective = releaseDirective;
	}
	public List<ValueObject> getValueObjectList() {
		return valueObjectList;
	}
	public void setValuesList(ArrayList<ValueObject> valueList) {
		this.valueObjectList = valueList;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		return (((DirectiveOnValues) o).getReleaseDirective().equals(this.getReleaseDirective()) && ((DirectiveOnValues) o).getValueObjectList().equals(this.getValueObjectList()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(releaseDirective,valueObjectList);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class DirectiveOnValues {\n");
		sb.append("  releaseDirective: ").append(releaseDirective).append("\n");
		sb.append("  valuesList: ");
		for (ValueObject v : valueObjectList) {
			sb.append(v.getValue()).append("\n              ");
		}
		sb.append("\n}\n");
		return sb.toString();
	}
}
