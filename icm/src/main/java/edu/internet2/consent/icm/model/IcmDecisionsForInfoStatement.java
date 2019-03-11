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
