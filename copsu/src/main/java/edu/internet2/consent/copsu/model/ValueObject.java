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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
@Entity
public class ValueObject {

		@Id
		//@GeneratedValue(strategy=GenerationType.AUTO)
		@GeneratedValue(strategy=GenerationType.IDENTITY)
		@JsonIgnore
		private Long valueKey;
		
		private String value;
		
		public ValueObject() {
			
		}
		public ValueObject(String v) {
			value = v;
		}

		@JsonIgnore
		public Long getValueKey() {
			return valueKey;
		}

		@JsonIgnore
		public void setValueKey(Long valueKey) {
			this.valueKey = valueKey;
		}

		@JsonProperty("value")
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || o.getClass() != this.getClass()) {
				return false;
			}
			return (((ValueObject) o).getValue().equals(this.getValue()));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class ValueObject {\n");
			sb.append("  value: ").append(value).append("\n");
			sb.append("}\n");
			return sb.toString();
		}
}
