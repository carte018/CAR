package edu.internet2.consent.caradmin;

public class UnrolledDirective {
	private String rowid;
	private String iitype;
	private String iiid;
	private String directive;
	private String value;
	private String basis;
	
	public String getRowid() {
		return rowid;
	}
	public void setRowid(String rowid) {
		this.rowid = rowid;
	}
	public String getIitype() {
		return iitype;
	}
	public void setIitype(String iitype) {
		this.iitype = iitype;
	}
	public String getIiid() {
		return iiid;
	}
	public void setIiid(String iiid) {
		this.iiid = iiid;
	}
	public String getDirective() {
		return directive;
	}
	public void setDirective(String directive) {
		this.directive = directive;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getBasis() {
		return basis;
	}
	public void setBasis(String basis) {
		this.basis = basis;
	}
}
