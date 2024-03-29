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

public class InjectedDecision implements Comparable<InjectedDecision> {

	private String attrName;
	private String attrDisplayName;
	private String attrDisplayValue;
	private boolean sensitivity;
	private boolean asnd;
	private String policytype;
	public String getAttrDisplayValue() {
		return attrDisplayValue;
	}
	public void setAttrDisplayValue(String attrDisplayValue) {
		this.attrDisplayValue = attrDisplayValue;
	}
	public String getAttrDisplayName() {
		return attrDisplayName;
	}
	public void setAttrDisplayName(String attrDisplayName) {
		this.attrDisplayName = attrDisplayName;
	}
	private String attrValue;
	private String recommendedDirective;
	private String chosenDirective;
	public String getAttrName() {
		return attrName;
	}
	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}
	public String getAttrValue() {
		return attrValue;
	}
	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}
	public String getRecommendedDirective() {
		return recommendedDirective;
	}
	public void setRecommendedDirective(String recommendedDirective) {
		this.recommendedDirective = recommendedDirective;
	}
	public String getChosenDirective() {
		return chosenDirective;
	}
	public void setChosenDirective(String chosenDirective) {
		this.chosenDirective = chosenDirective;
	}
	public boolean isSensitivity() {
		return sensitivity;
	}
	public void setSensitivity(boolean value) {
		this.sensitivity = value;
	}
	public boolean isAsnd() {
		return asnd;
	}
	public void setAsnd(boolean value) {
		this.asnd=value;
	}
	public String getPolicytype() {
		return policytype;
	}
	public void setPolicytype(String value) {
		this.policytype = value;
	}
	
	public int compareTo(InjectedDecision o) {
		if (attrDisplayName.compareTo(o.getAttrDisplayName()) != 0) {
			return attrDisplayName.compareTo(o.getAttrDisplayName());
		} else {
			return attrDisplayValue.compareTo(o.getAttrDisplayValue());
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (! this.getClass().equals(o.getClass())) {
			return false;
		}
		InjectedDecision i = (InjectedDecision) o;
		if (this.getAttrDisplayName().equals(i.getAttrDisplayName()) && this.getAttrDisplayValue().equals(i.getAttrDisplayValue())) {
			return true;
		} else {
			return false;
		}
	}
	
}
