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
public class PolicyId {

		@JsonProperty("baseId")
		private String baseId;
		@JsonProperty("version")
		private String version;
		
		@JsonProperty("baseId")
		public String getBaseId() {
			return baseId;
		}
		@JsonProperty("baseId")
		public void setBaseId(String baseId) {
			this.baseId = baseId;
		}
		
		@JsonProperty("version")
		public String getVersion() {
			return version;
		}
		@JsonProperty("version")
		public void setVersion(String version) {
			this.version = version;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o==null || this.getClass() != o.getClass()) {
				return false;
			}
			PolicyId p = (PolicyId) o;
			return (p.getBaseId().equals(this.getBaseId()) && p.getVersion().equals(this.getVersion()));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(baseId,version);
		}
		
		@Override
		public String toString() {
			return "baseId=" + baseId + ",version=" + version;
		}
}
