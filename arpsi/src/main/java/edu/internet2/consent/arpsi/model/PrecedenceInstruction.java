package edu.internet2.consent.arpsi.model;

public class PrecedenceInstruction {
	private String policyToChange;
	private String operation;
	private String policy;
	public String getPolicyToChange() {
		return policyToChange;
	}
	public void setPolicyToChange(String policyToChange) {
		this.policyToChange = policyToChange;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getPolicy() {
		return policy;
	}
	public void setPolicy(String policy) {
		this.policy = policy;
	}
	
}
