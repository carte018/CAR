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
package edu.internet2.consent.copsu.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DecisionsForInfoStatement {

	private InfoId infoId;
	private List<DecisionOnValues> arrayOfDecisionOnValues;
	private DecisionOnAllOtherValues decisionOnAllOtherValues;
	
	@JsonProperty("infoId")
	public InfoId getInfoId() {
		return infoId;
	}
	public void setInfoId(InfoId infoId) {
		this.infoId = infoId;
	}
	
	@JsonProperty("arrayOfDecisionOnValues")
	public List<DecisionOnValues> getArrayOfDecisionOnValues() {
		return arrayOfDecisionOnValues;
	}
	public void setArrayOfDecisionOnValues(ArrayList<DecisionOnValues> arrayOfDecisionOnValues) {
		this.arrayOfDecisionOnValues = arrayOfDecisionOnValues;
	}
	
	@JsonProperty("decisionOnAllOtherValues")
	public DecisionOnAllOtherValues getDecisionOnAllOtherValues() {
		return decisionOnAllOtherValues;
	}
	public void setDecisionOnAllOtherValues(DecisionOnAllOtherValues decisionOnAllOtherValues) {
		this.decisionOnAllOtherValues = decisionOnAllOtherValues;
	}
	
}
