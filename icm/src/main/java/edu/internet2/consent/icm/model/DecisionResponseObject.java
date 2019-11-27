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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.icm.util.OMSingleton;

public class DecisionResponseObject {

	private String decisionId;
	private String timeOfDecision;
	private UserId userId;
	private RelyingPartyId relyingPartyId;
	private ResourceHolderId resourceHolderId;
	private WhileImAwayDirective whileImAwayDirective;
	private List<DecisionsForInfoStatement> arrayOfInfoDecisionStatement;
	public String getDecisionId() {
		return decisionId;
	}
	public void setDecisionId(String decisionId) {
		this.decisionId = decisionId;
	}
	public String getTimeOfDecision() {
		return timeOfDecision;
	}
	public void setTimeOfDecision(String timeOfDecision) {
		this.timeOfDecision = timeOfDecision;
	}
	public UserId getUserId() {
		return userId;
	}
	public void setUserId(UserId userId) {
		this.userId = userId;
	}
	public RelyingPartyId getRelyingPartyId() {
		return relyingPartyId;
	}
	public void setRelyingPartyId(RelyingPartyId relyingPartyId) {
		this.relyingPartyId = relyingPartyId;
	}
	public ResourceHolderId getResourceHolderId() {
		return resourceHolderId;
	}
	public void setResourceHolderId(ResourceHolderId resourceHolderId) {
		this.resourceHolderId = resourceHolderId;
	}
	public WhileImAwayDirective getWhileImAwayDirective() {
		return whileImAwayDirective;
	}
	public void setWhileImAwayDirective(WhileImAwayDirective whileImAwayDirective) {
		this.whileImAwayDirective = whileImAwayDirective;
	}
	public List<DecisionsForInfoStatement> getArrayOfInfoDecisionStatement() {
		return arrayOfInfoDecisionStatement;
	}
	public void setArrayOfInfoDecisionStatement(ArrayList<DecisionsForInfoStatement> arrayOfInfoDecisionStatement) {
		this.arrayOfInfoDecisionStatement = arrayOfInfoDecisionStatement;
	}
	
	public String toJSON() throws JsonProcessingException {
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
