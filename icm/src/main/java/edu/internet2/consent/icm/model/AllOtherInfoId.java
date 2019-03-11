package edu.internet2.consent.icm.model;

import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class AllOtherInfoId {
	@Enumerated(EnumType.STRING)
	@JsonProperty("allOtherInfoType")
	private AllOtherInfoTypeConst allOtherInfoType;
	@Enumerated(EnumType.STRING)
	@JsonProperty("allOtherInfoValue")
	private AllOtherInfoValueConst allOtherInfoValue;
	
	public AllOtherInfoTypeConst getAllOtherInfoType() {
		return allOtherInfoType;
	}
	public void setAllOtherInfoType(AllOtherInfoTypeConst allOtherInfoType) {
		this.allOtherInfoType = allOtherInfoType;
	}
	public AllOtherInfoValueConst getAllOtherInfoValue() {
		return allOtherInfoValue;
	}
	public void setAllOtherInfoValue(AllOtherInfoValueConst allOtherInfoValue) {
		this.allOtherInfoValue = allOtherInfoValue;
	}
	
	@Override 
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		AllOtherInfoId aoi = (AllOtherInfoId) o;
		return (aoi.getAllOtherInfoType().equals(this.getAllOtherInfoType()) && aoi.getAllOtherInfoValue().equals(this.getAllOtherInfoValue()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(allOtherInfoType,allOtherInfoValue);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class AllOtherInfoId {\n");
		sb.append("  AllOtherInfoType: ").append(this.getAllOtherInfoType()).append("\n");
		sb.append("  AllOtherInfoValue: ").append(this.getAllOtherInfoValue()).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
}
