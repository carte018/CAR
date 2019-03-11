package edu.internet2.consent.arpsi.model;

public class PendingDecision {

	// Internal class for keeping up with what remains to be decided during sieving
	//
	
	private InfoId infoId;
	private String value;   // one value per in this case, to keep separate values decisions
	public InfoId getInfoId() {
		return infoId;
	}
	public void setInfoId(InfoId infoId) {
		this.infoId = infoId;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
