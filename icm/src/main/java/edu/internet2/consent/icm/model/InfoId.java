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
