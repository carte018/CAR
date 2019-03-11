package edu.internet2.consent.arpsi.model;

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
	@JsonProperty("userId")
	public void setUserId(UserId userId) {
		this.userId = userId;
	}
	@JsonProperty("relyingPartyId")
	public RelyingPartyId getRelyingPartyId() {
		return relyingPartyId;
	}
	@JsonProperty("relyingPartyId")
	public void setRelyingPartyId(RelyingPartyId relyingPartyId) {
		this.relyingPartyId = relyingPartyId;
	}
	@JsonProperty("resourceHolderId")
	public ResourceHolderId getResourceHolderId() {
		return resourceHolderId;
	}
	@JsonProperty("resourceHolderId")
	public void setResourceHolderId(ResourceHolderId resourceHolderId) {
		this.resourceHolderId = resourceHolderId;
	}
	@JsonProperty("arrayOfInfoIdsPlusValues")
	public List<InfoIdPlusValues> getArrayOfInfoIdsPlusValues() {
		return arrayOfInfoIdsPlusValues;
	}
	@JsonProperty("arrayOfInfoIdsPlusValues")
	public void setArrayOfInfoIdsPlusValues(List<InfoIdPlusValues> arrayOfInfoIdsPlusValues) {
		this.arrayOfInfoIdsPlusValues = arrayOfInfoIdsPlusValues;
	}
	@JsonProperty("arrayOfUserProperty")
	public List<UserProperty> getArrayOfUserProperty() {
		return arrayOfUserProperty;
	}
	@JsonProperty("arrayOfUserProperty")
	public void setArrayOfUserProperty(List<UserProperty> arrayOfUserProperty) {
		this.arrayOfUserProperty = arrayOfUserProperty;
	}
	@JsonProperty("arrayOfRelyingPartyProperty")
	public List<RelyingPartyProperty> getArrayOfRelyingPartyProperty() {
		return arrayOfRelyingPartyProperty;
	}
	@JsonProperty("arrayOfRelyingPartyProperty")
	public void setArrayOfRelyingPartyProperty(List<RelyingPartyProperty> arrayOfRelyingPartyProperty) {
		this.arrayOfRelyingPartyProperty = arrayOfRelyingPartyProperty;
	}
}
