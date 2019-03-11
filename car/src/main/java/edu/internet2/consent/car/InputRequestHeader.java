package edu.internet2.consent.car;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InputRequestHeader {

	@JsonProperty("identifierName")
	private String identifierName;  // info item to use as user identifier
	@JsonProperty("callbackUrl")
	private String callbackUrl;		// full URL for return from CAR
	@JsonProperty("spRelyingPartyId")
	private String relyingPartyId;  // entity ID, etc. of relying party
	@JsonProperty("idpRelyingPartyId")
	private String rhId;  // entity Id, etc. of requesting RH (idp in this case)
	
	public String getIdentifierName() {
		return identifierName;
	}
	public void setIdentifierName(String identifierName) {
		this.identifierName = identifierName;
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	public String getRelyingPartyId() {
		return relyingPartyId;
	}
	public void setRelyingPartyId(String relyingPartyId) {
		this.relyingPartyId = relyingPartyId;
	}
	public String getRhId() {
		return rhId;
	}
	public void setRhId(String rhId) {
		this.rhId = rhId;
	}
	
	public String toJson() throws JsonProcessingException {
		// Return JSON representation of self
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
