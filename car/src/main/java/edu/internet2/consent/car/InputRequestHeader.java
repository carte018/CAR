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
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
