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
