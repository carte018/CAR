package edu.internet2.consent.car;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FullInputRequestHeader {
	@JsonProperty("userIdentifierType")
	private String userIdentifierType;  // info item type for user identifier (usually "attribute")
	@JsonProperty("userIdentifierName")
	private String userIdentifierName;  // info item to use as user identifier
	@JsonProperty("callbackUrl")
	private String callbackUrl;		// full URL for return from CAR
	@JsonProperty("relyingPartyType")
	private String relyingPartyType;  // rpID type for relying party
	@JsonProperty("relyingPartyId")
	private String relyingPartyId;  // entity ID, etc. of relying party
	@JsonProperty("rhType")
	private String rhType;  // identifier type for RH
	@JsonProperty("rhId")
	private String rhId;  // entity Id, etc. of requesting RH (idp in this case)
	
	
	
	/**
	 * @return the userIdentifierType
	 */
	public String getUserIdentifierType() {
		return userIdentifierType;
	}



	/**
	 * @param userIdentifierType the userIdentifierType to set
	 */
	public void setUserIdentifierType(String userIdentifierType) {
		this.userIdentifierType = userIdentifierType;
	}



	/**
	 * @return the userIdentifierName
	 */
	public String getUserIdentifierName() {
		return userIdentifierName;
	}



	/**
	 * @param userIdentifierName the userIdentifierName to set
	 */
	public void setUserIdentifierName(String userIdentifierName) {
		this.userIdentifierName = userIdentifierName;
	}



	/**
	 * @return the callbackUrl
	 */
	public String getCallbackUrl() {
		return callbackUrl;
	}



	/**
	 * @param callbackUrl the callbackUrl to set
	 */
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}



	/**
	 * @return the relyingPartyType
	 */
	public String getRelyingPartyType() {
		return relyingPartyType;
	}



	/**
	 * @param relyingPartyType the relyingPartyType to set
	 */
	public void setRelyingPartyType(String relyingPartyType) {
		this.relyingPartyType = relyingPartyType;
	}



	/**
	 * @return the relyingPartyId
	 */
	public String getRelyingPartyId() {
		return relyingPartyId;
	}



	/**
	 * @param relyingPartyId the relyingPartyId to set
	 */
	public void setRelyingPartyId(String relyingPartyId) {
		this.relyingPartyId = relyingPartyId;
	}



	/**
	 * @return the rhType
	 */
	public String getRhType() {
		return rhType;
	}



	/**
	 * @param rhType the rhType to set
	 */
	public void setRhType(String rhType) {
		this.rhType = rhType;
	}



	/**
	 * @return the rhId
	 */
	public String getRhId() {
		return rhId;
	}



	/**
	 * @param rhId the rhId to set
	 */
	public void setRhId(String rhId) {
		this.rhId = rhId;
	}



	public String toJson() throws JsonProcessingException {
		// Return JSON representation of self
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
