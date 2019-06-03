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

public class InjectedInfoItem {

	private String type;
	private String id;
	private String displayName;
	private String value;
	private String valueDisplayName;
	private String policyDirective;
	private String recommendedDirective;
	private String reason;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValueDisplayName() {
		return valueDisplayName;
	}
	public void setValueDisplayName(String valueDisplayName) {
		this.valueDisplayName = valueDisplayName;
	}
	public String getPolicyDirective() {
		return policyDirective;
	}
	public void setPolicyDirective(String policyDirective) {
		this.policyDirective = policyDirective;
	}
	public String getRecommendedDirective() {
		return recommendedDirective;
	}
	public void setRecommendedDirective(String recommendedDirective) {
		this.recommendedDirective = recommendedDirective;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	
}
