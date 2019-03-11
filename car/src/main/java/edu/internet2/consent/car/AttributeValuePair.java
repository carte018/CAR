package edu.internet2.consent.car;

public class AttributeValuePair {

	private String attrname;
	private String attrvalue;
	private String currentdecision;
	private String policySource;
	public String getPolicySource() {
		return policySource;
	}
	public void setPolicySource(String policySource) {
		this.policySource = policySource;
	}
	public String getCurrentdecision() {
		return currentdecision;
	}
	public void setCurrentdecision(String currentdecision) {
		this.currentdecision = currentdecision;
	}
	public String getAttrname() {
		return attrname;
	}
	public void setAttrname(String attrname) {
		this.attrname = attrname;
	}
	public String getAttrvalue() {
		return attrvalue;
	}
	public void setAttrvalue(String attrvalue) {
		this.attrvalue = attrvalue;
	}
	
}
