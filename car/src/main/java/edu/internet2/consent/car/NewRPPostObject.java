package edu.internet2.consent.car;

import java.util.ArrayList;

public class NewRPPostObject {
	
	ArrayList<NewRPDecision> newRPDecisions;
	public NewRPPostObject() {
		newRPDecisions = new ArrayList<NewRPDecision>();
	}
	public ArrayList<NewRPDecision> getNewRPDecisions() {
		return newRPDecisions;
	}
	public void setNewRPDecisions(ArrayList<NewRPDecision> newRPDecisions) {
		this.newRPDecisions = newRPDecisions;
	}
	public String getWhileImAwayDecision() {
		return WhileImAwayDecision;
	}
	public void setWhileImAwayDecision(String whileImAwayDecision) {
		WhileImAwayDecision = whileImAwayDecision;
	}
	String WhileImAwayDecision;
	String OtherAttributesDecision;
	public String getOtherAttributesDecision() {
		return OtherAttributesDecision;
	}
	public void setOtherAttributesDecision(String otherAttributesDecision) {
		OtherAttributesDecision = otherAttributesDecision;
	}
}
