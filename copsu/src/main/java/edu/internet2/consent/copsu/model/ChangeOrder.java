package edu.internet2.consent.copsu.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class ChangeOrder {
	@JsonProperty("description")
	private String description;
	@JsonProperty("changeOrderType")
	@Enumerated(EnumType.STRING)
	private ChangeOrderType changeOrderType;
	@JsonProperty("userIdArray")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY)
	private List<ListableUserId> userIdArray;
	@JsonProperty("relyingPartyIdArray")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<ListableRelyingPartyId> relyingPartyIdArray;
	@JsonProperty("policyIdArray")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<ListablePolicyId> policyIdArray;
	@Embedded
	@JsonProperty("resourceHolderId")
	private ResourceHolderId resourceHolderId;
	@Enumerated(EnumType.STRING)
	@JsonProperty("whileImAwayDirective")
	private WhileImAwayDirective whileImAwayDirective;
	@JsonProperty("arrayOfInfoReleaseStatement")
	@ElementCollection @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private List<InfoReleaseStatement> arrayOfInfoReleaseStatement;
	@JsonProperty("allOtherInfoReleaseStatement")
	@Embedded
	private AllOtherInfoReleaseStatement allOtherInfoReleaseStatement;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ChangeOrderType getChangeOrderType() {
		return changeOrderType;
	}
	public void setChangeOrderType(ChangeOrderType changeOrderType) {
		this.changeOrderType = changeOrderType;
	}
	public List<ListableUserId> getUserIdArray() {
		return userIdArray;
	}
	public void setUserIdArray(List<ListableUserId> userIdArray) {
		this.userIdArray = userIdArray;
	}
	public List<ListableRelyingPartyId> getRelyingPartyIdArray() {
		return relyingPartyIdArray;
	}
	public void setRelyingPartyIdArray(List<ListableRelyingPartyId> relyingPartyIdArray) {
		this.relyingPartyIdArray = relyingPartyIdArray;
	}
	public List<ListablePolicyId> getPolicyIdArray() {
		return policyIdArray;
	}
	public void setPolicyIdArray(List<ListablePolicyId> policyIdArray) {
		this.policyIdArray = policyIdArray;
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
	public List<InfoReleaseStatement> getArrayOfInfoReleaseStatement() {
		return arrayOfInfoReleaseStatement;
	}
	public void setArrayOfInfoReleaseStatement(List<InfoReleaseStatement> arrayOfInfoReleaseStatement) {
		this.arrayOfInfoReleaseStatement = arrayOfInfoReleaseStatement;
	}
	public AllOtherInfoReleaseStatement getAllOtherInfoReleaseStatement() {
		return allOtherInfoReleaseStatement;
	}
	public void setAllOtherInfoReleaseStatement(AllOtherInfoReleaseStatement allOtherInfoReleaseStatement) {
		this.allOtherInfoReleaseStatement = allOtherInfoReleaseStatement;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ChangeOrder co = (ChangeOrder) o;
		return(co.getDescription().equals(this.getDescription()) && co.getChangeOrderType().equals(this.getChangeOrderType()) && co.getArrayOfInfoReleaseStatement().equals(this.getArrayOfInfoReleaseStatement()) && co.getUserIdArray().equals(this.getUserIdArray()) && co.getRelyingPartyIdArray().equals(this.getRelyingPartyIdArray()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(description,userIdArray,relyingPartyIdArray,arrayOfInfoReleaseStatement);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ChangeOrder {\n");
		sb.append(" description: ").append(description).append("\n");
		sb.append("}");
		return sb.toString();
	}				
}
