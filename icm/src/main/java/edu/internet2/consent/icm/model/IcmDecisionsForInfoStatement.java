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

public class IcmDecisionsForInfoStatement {

	private InfoId infoId;
	private List<IcmDecisionOnValues> arrayOfDecisionOnValues;
	private IcmDecisionOnAllOtherValues decisionOnAllOtherValues;
	
	@JsonProperty("infoId")
	public InfoId getInfoId() {
		return infoId;
	}
	public void setInfoId(InfoId infoId) {
		this.infoId = infoId;
	}
	
	@JsonProperty("arrayOfDecisionOnValues")
	public List<IcmDecisionOnValues> getArrayOfDecisionOnValues() {
		return arrayOfDecisionOnValues;
	}
	public void setArrayOfDecisionOnValues(ArrayList<IcmDecisionOnValues> arrayOfDecisionOnValues) {
		this.arrayOfDecisionOnValues = arrayOfDecisionOnValues;
	}
	
	@JsonProperty("decisionOnAllOtherValues")
	public IcmDecisionOnAllOtherValues getDecisionOnAllOtherValues() {
		return decisionOnAllOtherValues;
	}
	public void setDecisionOnAllOtherValues(IcmDecisionOnAllOtherValues decisionOnAllOtherValues) {
		this.decisionOnAllOtherValues = decisionOnAllOtherValues;
	}
	
}
