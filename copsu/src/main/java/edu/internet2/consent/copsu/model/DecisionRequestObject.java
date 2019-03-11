package edu.internet2.consent.copsu.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DecisionRequestObject {

	private UserId userId;
	private RelyingPartyId relyingPartyId;
	private ResourceHolderId resourceHolderId;
	private List<InfoIdPlusValues> arrayOfInfoIdsPlusValues;
	private List<InfoIdPlusValues> arrayOfUserInfo;
	private List<InfoIdPlusValues> arrayOfRPInfo;
	
	@JsonProperty("userId")
	public UserId getUserId() {
		return userId;
	}
	public void setUserId(UserId userId) {
		this.userId = userId;
	}
	@JsonProperty("relyingPartyId")
	public RelyingPartyId getRelyingPartyId() {
		return relyingPartyId;
	}
	public void setRelyingPartyId(RelyingPartyId relyingPartyId) {
		this.relyingPartyId = relyingPartyId;
	}
	@JsonProperty("resourceHolderId")
	public ResourceHolderId getResourceHolderId() {
		return resourceHolderId;
	}
	public void setResourceHolderId(ResourceHolderId resourceHolderId) {
		this.resourceHolderId = resourceHolderId;
	}
	@JsonProperty("arrayOfInfoIdsPlusValues")
	public List<InfoIdPlusValues> getArrayOfInfoIdsPlusValues() {
		return arrayOfInfoIdsPlusValues;
	}
	public void setArrayOfInfoIdsPlusValues(ArrayList<InfoIdPlusValues> arrayOfInfoIdsPlusValues) {
		this.arrayOfInfoIdsPlusValues = arrayOfInfoIdsPlusValues;
	}
	@JsonProperty("arrayOfUserInfo")
	public List<InfoIdPlusValues> getArrayOfUserInfo() {
		return arrayOfUserInfo;
	}
	public void setArrayOfUserInfo(ArrayList<InfoIdPlusValues> arrayofUserInfo) {
		this.arrayOfUserInfo = arrayofUserInfo;
	}
	@JsonProperty("arrayOfRPInfo")
	public List<InfoIdPlusValues> getArrayOfRPInfo() {
		return arrayOfRPInfo;
	}
	public void setArrayOfRPInfo(ArrayList<InfoIdPlusValues> arryOfRPInfo) {
		this.arrayOfRPInfo = arryOfRPInfo;
	}
}
