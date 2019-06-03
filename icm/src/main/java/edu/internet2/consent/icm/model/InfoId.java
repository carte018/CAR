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
public class InfoId {

		private String infoType;
		private String infoValue;
		
		@JsonProperty("info-type")
		public String getInfoType() {
			return infoType;
		}
		public void setInfoType(String infoType) {
			this.infoType = infoType;
		}
		
		@JsonProperty("info-value")
		public String getInfoValue() {
			return infoValue;
		}
		public void setInfoValue(String infoValue) {
			this.infoValue = infoValue;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || o.getClass() != this.getClass()) {
				return false;
			}
			return (((InfoId) o).getInfoType().equals(this.getInfoType()) && ((InfoId) o).getInfoValue().equals(this.getInfoValue()));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(infoType,infoValue);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class InfoId {\n");
			sb.append("  infoType: ").append(infoType).append("\n");
			sb.append("  infoValue: ").append(infoValue).append("\n");
			sb.append("}\n");
			return sb.toString();
		}
}
