package edu.internet2.consent.caradmin;

import edu.internet2.consent.informed.model.InternationalizedString;

public class InjectedDashboardInfo {

	public String rhtype;
	public String rhid;
	public InternationalizedString displayname;
	public InternationalizedString description;
	public int infoitemcount;
	public int rpcount;
	public int activerpcount;
	public int usercount;
	public int arpsipolcount;
	public int carmapolcount;
	public int userpolcount;
	
	public int getUserpolcount() {
		return userpolcount;
	}
	public void setUserpolcount(int userpolcount) {
		this.userpolcount = userpolcount;
	}
	public String getRhtype() {
		return rhtype;
	}
	public void setRhtype(String rhtype) {
		this.rhtype = rhtype;
	}
	public String getRhid() {
		return rhid;
	}
	public void setRhid(String rhid) {
		this.rhid = rhid;
	}
	public InternationalizedString getDisplayname() {
		return displayname;
	}
	public void setDisplayname(InternationalizedString displayname) {
		this.displayname = displayname;
	}
	public InternationalizedString getDescription() {
		return description;
	}
	public void setDescription(InternationalizedString description) {
		this.description = description;
	}
	public int getInfoitemcount() {
		return infoitemcount;
	}
	public void setInfoitemcount(int infoitemcount) {
		this.infoitemcount = infoitemcount;
	}

	public int getRpcount() {
		return rpcount;
	}
	public void setRpcount(int rpcount) {
		this.rpcount = rpcount;
	}
	public int getActiverpcount() {
		return activerpcount;
	}
	public void setActiverpcount(int activerpcount) {
		this.activerpcount = activerpcount;
	}
	public int getUsercount() {
		return usercount;
	}
	public void setUsercount(int usercount) {
		this.usercount = usercount;
	}
	public int getArpsipolcount() {
		return arpsipolcount;
	}
	public void setArpsipolcount(int arpsipolcount) {
		this.arpsipolcount = arpsipolcount;
	}
	public int getCarmapolcount() {
		return carmapolcount;
	}
	public void setCarmapolcount(int carmapolcount) {
		this.carmapolcount = carmapolcount;
	}
	
	
}
