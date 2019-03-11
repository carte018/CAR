package edu.internet2.consent.icm.model;

import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class SupersedingPolicyId {

	@JsonProperty("id")
	private String supersedingId;
	@JsonProperty("version")
	private String supersedingVersion;
	public String getSupersedingId() {
		return supersedingId;
	}
	public void setSupersedingId(String supersedingId) {
		this.supersedingId = supersedingId;
	}
	public String getSupersedingVersion() {
		return supersedingVersion;
	}
	public void setSupersedingVersion(String supersedingVersion) {
		this.supersedingVersion = supersedingVersion;
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		SupersedingPolicyId spi = (SupersedingPolicyId) o;
		return (spi.getSupersedingId().equals(this.getSupersedingId()) && spi.getSupersedingVersion().equals(this.getSupersedingVersion())); 
	}
	@Override
	public int hashCode() {
		return Objects.hash(supersedingId,supersedingVersion);
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Class SupersedingPolicyId {\n");
		sb.append("     supersedingId:").append(supersedingId).append("\n");
		sb.append("     supersedingVersion:").append(supersedingVersion).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
