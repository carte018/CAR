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
package edu.internet2.consent.icm.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DecisionOnValues {

	private List<String> valuesList;
	private UserReleaseDirective releaseDecision;
	private PolicyId policyId;
	
	public PolicyId getPolicyId() {
		return policyId;
	}
	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}
	@JsonProperty("valuesList")
	public List<String> getValuesList() {
		return valuesList;
	}
	@JsonProperty("valuesList")
	public void setValuesList(ArrayList<String> returnedValuesList) {
		this.valuesList = returnedValuesList;
	}
	@JsonProperty("returnedValuesList")
	public void setReturnedValuesList(ArrayList<String> returnedValuesList) {
		this.valuesList = returnedValuesList;
	}
	@JsonProperty("releaseDecision")
	public UserReleaseDirective getReleaseDecision() {
		return releaseDecision;
	}
	public void setReleaseDecision(UserReleaseDirective releaseDecision) {
		this.releaseDecision = releaseDecision;
	}
	
}
