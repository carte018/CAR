package edu.internet2.consent.icm.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IcmDecisionResponseObject {

	private String decisionId;
	private String timeOfDecision;
	private UserId userId;
	private RelyingPartyId relyingPartyId;
	private ResourceHolderId resourceHolderId;
	private List<IcmDecisionsForInfoStatement> arrayOfInfoDecisionStatement;

	
	public IcmDecisionResponseObject() {
		arrayOfInfoDecisionStatement = new ArrayList<IcmDecisionsForInfoStatement>();
	}

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

	public List<IcmDecisionsForInfoStatement> getArrayOfInfoDecisionStatement() {
		return arrayOfInfoDecisionStatement;
	}

	public void setArrayOfInfoDecisionStatement(List<IcmDecisionsForInfoStatement> arrayOfInfoDecisionStatement) {
		this.arrayOfInfoDecisionStatement = arrayOfInfoDecisionStatement;
	}


	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
