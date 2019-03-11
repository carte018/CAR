package edu.internet2.consent.car;

import java.util.HashMap;

public class NewRPDisplayObject {

	private String infoType;
	private String infoValue;


	public String getInfoValue() {
		return infoValue;
	}

	public void setInfoValue(String infoValue) {
		this.infoValue = infoValue;
	}
	
	public String getInfoType() {
		return infoType;
	}

	public void setInfoType(String attributeType) {
		this.infoType = attributeType;
	}

	private String attribute;
	private HashMap<String,String> decisions;
	private HashMap<String,String> displayValues;
	
	public NewRPDisplayObject() {
		decisions = new HashMap<String,String>();
		displayValues = new HashMap<String,String>();
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public HashMap<String, String> getDecisions() {
		return decisions;
	}

	public void setDecisions(HashMap<String, String> decisions) {
		this.decisions = decisions;
	}
	
	public HashMap<String,String> getDisplayValues() {
		return displayValues;
	}
	
	public void setDisplayValues(HashMap<String,String> values) {
		this.displayValues = values;
	}
	
	
}
