package edu.internet2.consent.arpsi.model;

import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class ResourceHolderId {
	
		@JsonProperty("RH-type")
		private String RHType;
		@JsonProperty("RH-value")
		private String RHValue;
		@JsonProperty("RH-type")
		public String getRHType() {
			return RHType;
		}
		public void setRHType(String rHType) {
			RHType = rHType;
		}
		@JsonProperty("RH-value")
		public String getRHValue() {
			return RHValue;
		}
		public void setRHValue(String rHValue) {
			RHValue = rHValue;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || this.getClass() != o.getClass()) {
				return false;
			}
			ResourceHolderId r = (ResourceHolderId) o;
			return (r.getRHType().equals(this.getRHType()) && r.getRHValue().equals(this.getRHValue()));
		}
		@Override
		public int hashCode() {
			return Objects.hash(RHType,RHValue);
		}
		@Override
		public String toString() {
			return "RHType="+RHType+", RHValue="+RHValue;
		}
}
