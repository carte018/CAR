package edu.internet2.consent.arpsi.model;

import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class AllOtherInfoId {
	
	@Enumerated(EnumType.STRING)
	@JsonProperty("allOtherInfoType")
	AllOtherInfoTypeConst allOtherInfoType;
	
	@Enumerated(EnumType.STRING)
	@JsonProperty("allOtherInfoValue")
	AllOtherInfoValueConst allOtherInfoValue;

	@JsonProperty("allOtherInfoType")
	public AllOtherInfoTypeConst getAllOtherInfoType() {
		return allOtherInfoType;
	}
	@JsonProperty("allOtherInfoType")
	public void setAllOtherInfoType(AllOtherInfoTypeConst allOtherInfoType) {
		this.allOtherInfoType = allOtherInfoType;
	}

	@JsonProperty("allOtherInfoValue")
	public AllOtherInfoValueConst getAllOtherInfoValue() {
		return allOtherInfoValue;
	}

	@JsonProperty("allOtherInfoValue")
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
		return true;
	}
	@Override 
	public int hashCode() {
		return Objects.hash(allOtherInfoType,allOtherInfoValue);
	}
	@Override
	public String toString() {
		return "AllOtherValues:";
	}
	
}
