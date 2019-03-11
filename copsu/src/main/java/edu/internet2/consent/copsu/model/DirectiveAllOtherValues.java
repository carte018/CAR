package edu.internet2.consent.copsu.model;

import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class DirectiveAllOtherValues {
	
	@Enumerated(EnumType.STRING)
	
	ReleaseDirective releaseDirective;
	@Enumerated(EnumType.STRING)
	AllOtherValuesConst allOtherValues;
	
	@JsonProperty("releaseDirective")
	public ReleaseDirective getReleaseDirective() {
		return releaseDirective;
	}
	public void setReleaseDirective(ReleaseDirective releaseDirective) {
		this.releaseDirective = releaseDirective;
	}
	
	@JsonProperty("allOtherValuesConst")
	public AllOtherValuesConst getAllOtherValues() {
		return allOtherValues;
	}
	public void setAllOtherValues(AllOtherValuesConst allOtherValues) {
		this.allOtherValues = allOtherValues;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		
		// Value of allOtherValuesConst is constant
		return (((DirectiveAllOtherValues) o).getReleaseDirective().equals(this.getReleaseDirective()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(releaseDirective,allOtherValues);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class DirectiveAllOtherValue {\n");
		sb.append("  releaseDirective: ").append(releaseDirective).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
