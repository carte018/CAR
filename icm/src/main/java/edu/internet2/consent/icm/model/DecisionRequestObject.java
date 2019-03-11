package edu.internet2.consent.icm.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DecisionRequestObject {

	private UserId userId;
	private RelyingPartyId relyingPartyId;
	private ResourceHolderId resourceHolderId;
	private List<InfoIdPlusValues> arrayOfInfoIdsPlusValues;
	private List<UserProperty> arrayOfUserProperty;
	private List<RelyingPartyProperty> arrayOfRelyingPartyProperty;
	
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
	@JsonProperty("arrayOfUserProperty")
	public List<UserProperty> getArrayOfUserProperty() {
		return arrayOfUserProperty;
	}
	public void setArrayofUserProperty(ArrayList<UserProperty> arrayofUserProperty) {
		this.arrayOfUserProperty = arrayofUserProperty;
	}
	@JsonProperty("arrayOfRelyingPartyProperty")
	public List<RelyingPartyProperty> getArrayOfRelyingPartyProperty() {
		return arrayOfRelyingPartyProperty;
	}
	public void setArrayOfRelyingPartyProperty(ArrayList<RelyingPartyProperty> arrayOfRelyingPartyProperty) {
		this.arrayOfRelyingPartyProperty = arrayOfRelyingPartyProperty;
	}
}
