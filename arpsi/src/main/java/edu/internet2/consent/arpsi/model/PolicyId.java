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
