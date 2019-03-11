package edu.internet2.consent.icm.model;

public class AugmentedPolicyId {
	private PolicySourceEnum policySource;
	private String policyBaseId;
	private String policyVersion;
	private String ICM_policyBaseId;
	private String ICM_policyVersion;
	public String getICM_policyBaseId() {
		return ICM_policyBaseId;
	}
	public void setICM_policyBaseId(String iCM_policyBaseId) {
		ICM_policyBaseId = iCM_policyBaseId;
	}
	public String getICM_policyVersion() {
		return ICM_policyVersion;
	}
	public void setICM_policyVersion(String iCM_policyVersion) {
		ICM_policyVersion = iCM_policyVersion;
	}
	public PolicySourceEnum getPolicySource() {
		return policySource;
	}
	public void setPolicySource(PolicySourceEnum policySource) {
		this.policySource = policySource;
	}
	public String getPolicyBaseId() {
		return policyBaseId;
	}
	public void setPolicyBaseId(String policyBaseId) {
		this.policyBaseId = policyBaseId;
	}
	public String getPolicyVersion() {
		return policyVersion;
	}
	public void setPolicyVersion(String policyVersion) {
		this.policyVersion = policyVersion;
	}
	
}
