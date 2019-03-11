package edu.internet2.consent.copsu.model;

import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class PolicyId {
	
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
		PolicyId pi = (PolicyId) o;
		return (pi.getBaseId().equals(this.getBaseId()) && pi.getVersion().equals(this.getVersion()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(baseId,version);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PolicyId {\n");
		sb.append("  id: ").append(baseId).append("\n");
		sb.append("  version: ").append(version).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
