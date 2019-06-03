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

public class NewRPDecision {
	private String infoType;
	private String infoValue;
	private String decisionValue;
	public String getDecisionValue() {
		return decisionValue;
	}
	public void setDecisionValue(String decisionValue) {
		this.decisionValue = decisionValue;
	}
	private String decision;
	public String getInfoType() {
		return infoType;
	}
	public void setInfoType(String infoType) {
		this.infoType = infoType;
	}
	public String getInfoValue() {
		return infoValue;
	}
	public void setInfoValue(String infoValue) {
		this.infoValue = infoValue;
	}
	public String getDecision() {
		return decision;
	}
	public void setDecision(String decision) {
		this.decision = decision;
	}
}
