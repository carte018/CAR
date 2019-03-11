package edu.internet2.consent.icm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewRPTemplateRPId {

	private NewRPTemplateTypeConst newRTemplateType;
	private NewRPTemplateValueConst newRPTemplateValue;
	
	@JsonProperty("newRPTemplateType")
	public NewRPTemplateTypeConst getNewRTemplateType() {
		return newRTemplateType;
	}
	public void setNewRTemplateType(NewRPTemplateTypeConst newRTemplateType) {
		this.newRTemplateType = newRTemplateType;
	}
	@JsonProperty("newRPTemplateValue")
	public NewRPTemplateValueConst getNewRPTemplateValue() {
		return newRPTemplateValue;
	}
	public void setNewRPTemplateValue(NewRPTemplateValueConst newRPTemplateValue) {
		this.newRPTemplateValue = newRPTemplateValue;
	}
	
}
