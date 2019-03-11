/* DEPRECATED */
/*package edu.internet2.consent.incon.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class RelyingPartyInformedContent {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long RPICIdentifier;
	
	@JsonProperty("rpType")
	private String rpType;
	@JsonProperty("rpValue")
	private String rpValue;
	public String getRpType() {
		return rpType;
	}
	public void setRpType(String rpType) {
		this.rpType = rpType;
	}
	public String getRpValue() {
		return rpValue;
	}
	public void setRpValue(String rpValue) {
		this.rpValue = rpValue;
	}

	@JsonProperty("displayName")
	private String displayName;
	@JsonProperty("accessUrl")
	private String accessUrl;
	@JsonProperty("iconUrl")
	private String iconUrl;
	@JsonProperty("reasonMap")
	@ElementCollection(fetch=FetchType.EAGER)
	private Map<String,String> reasonMap;  // infoitem -> reason_for_release
	@JsonProperty("displayMode")
	@ElementCollection(fetch=FetchType.EAGER)
	private Map<String,String> displayMode;  // infoitem -> (unconsentable, masked)
	@JsonProperty("requiredAttributes")
	@ElementCollection(fetch=FetchType.EAGER)
	private Set<String> requiredAttributes;
	@JsonProperty("optionalAttributes")
	@ElementCollection(fetch=FetchType.EAGER)
	private Set<String> optionalAttributes;
	@JsonProperty("privacyPolicyUrl")
	private String privacyPolicyUrl;
	@JsonProperty("description")
	private String description;

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Map<String, String> getDisplayMode() {
		return displayMode;
	}
	public void setDisplayMode(Map<String, String> displayMode) {
		this.displayMode = displayMode;
	}
	public Set<String> getRequiredAttributes() {
		return requiredAttributes;
	}
	public void setRequiredAttributes(Set<String> requiredAttributes) {
		this.requiredAttributes = requiredAttributes;
	}
	public Set<String> getOptionalAttributes() {
		return optionalAttributes;
	}
	public void setOptionalAttributes(Set<String> optionalAttributes) {
		this.optionalAttributes = optionalAttributes;
	}
	public String getPrivacyPolicyUrl() {
		return privacyPolicyUrl;
	}
	public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
		this.privacyPolicyUrl = privacyPolicyUrl;
	}
	public Long getRPICIdentifier() {
		return RPICIdentifier;
	}
	public void setRPICIdentifier(Long rPICIdentifier) {
		RPICIdentifier = rPICIdentifier;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getAccessUrl() {
		return accessUrl;
	}
	public void setAccessUrl(String accessUrl) {
		this.accessUrl = accessUrl;
	}
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public Map<String, String> getReasonMap() {
		return reasonMap;
	}
	public void setReasonMap(Map<String, String> reasonMap) {
		this.reasonMap = reasonMap;
	}
	
	public RelyingPartyInformedContent() {
		this.reasonMap = new HashMap<String, String>();
		this.displayMode = new HashMap<String,String>();
		this.requiredAttributes = new HashSet<String>();
		this.optionalAttributes = new HashSet<String>();
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	} 
}*/
