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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class ListablePolicyId {
	
	@JsonIgnore
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long ListablePolicyIdentifier;
	
	@JsonProperty("baseId")
	private String baseId;
	@JsonProperty("version")
	private String version;
	
	public String getBaseId() {
		return baseId;
	}
	public void setBaseId(String id) {
		this.baseId = id;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ListablePolicyId pi = (ListablePolicyId) o;
		return (pi.getBaseId().equals(this.getBaseId()) && pi.getVersion().equals(this.getVersion()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(baseId,version);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ListablePolicyId {\n");
		sb.append("  id: ").append(baseId).append("\n");
		sb.append("  version: ").append(version).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
