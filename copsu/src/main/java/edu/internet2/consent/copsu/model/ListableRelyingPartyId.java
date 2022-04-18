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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class ListableRelyingPartyId {
	
		@JsonIgnore
		@Id
		//@GeneratedValue(strategy=GenerationType.AUTO)
		@GeneratedValue(strategy=GenerationType.IDENTITY)
		private Long ListableRelyingPartyIdentifier;

		private String RPtype;
		private String RPvalue;
		
		@JsonProperty("RP-type")
		public String getRPtype() {
			return RPtype;
		}
		public void setRPtype(String rPtype) {
			RPtype = rPtype;
		}
		
		@JsonProperty("RP-value")
		public String getRPvalue() {
			return RPvalue;
		}
		public void setRPvalue(String rPvalue) {
			RPvalue = rPvalue;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || o.getClass() != this.getClass()) {
				return false;
			}
			return (((ListableRelyingPartyId) o).getRPtype().equals(this.getRPtype()) && ((ListableRelyingPartyId) o).getRPvalue().equals(this.getRPvalue()));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(RPtype, RPvalue);
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class RelyingPartyId { \n");
			sb.append("  RP-type: ").append(RPtype).append("\n");
			sb.append("  RP-value: ").append(RPvalue).append("\n");
			sb.append("} \n");
			return sb.toString();
		}	
}
