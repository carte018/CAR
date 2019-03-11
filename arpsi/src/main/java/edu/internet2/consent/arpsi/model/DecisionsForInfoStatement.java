package edu.internet2.consent.arpsi.model;


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
