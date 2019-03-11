package edu.internet2.consent.car;

public class InjectedUserPolicy implements Comparable<InjectedUserPolicy> {
	String rpName;
	String rpUrl;
	String policyUpdateDate;
	String baseId;
	public String getRpName() {
		return rpName;
	}
	public void setRpName(String rpName) {
		this.rpName = rpName;
	}
	public String getRpUrl() {
		return rpUrl;
	}
	public void setRpUrl(String rpUrl) {
		this.rpUrl = rpUrl;
	}
	public String getPolicyUpdateDate() {
		return policyUpdateDate;
	}
	public void setPolicyUpdateDate(String policyUpdateDate) {
		this.policyUpdateDate = policyUpdateDate;
	}
	public String getBaseId() {
		return baseId;
	}
	public void setBaseId(String baseId) {
		this.baseId = baseId;
	}
	
	public int compareTo(InjectedUserPolicy o) {
		return this.getRpName().compareToIgnoreCase(o.getRpName());
	}
	
}
