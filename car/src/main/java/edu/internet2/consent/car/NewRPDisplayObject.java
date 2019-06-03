/*
 * Copyright 2015 - 2019 Duke University
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2 as published by
    the Free Software Foundation.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License Version 2
    along with this program.  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt>.

 */
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
