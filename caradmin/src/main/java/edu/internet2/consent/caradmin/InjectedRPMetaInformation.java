/*
 * Copyright 2015 - 2019 Duke University
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2 as published by
    the Free Software Foundation.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License Version 2
    along with this program.  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt>.

 */
package edu.internet2.consent.caradmin;

@SuppressWarnings("rawtypes")
public class InjectedRPMetaInformation implements Comparable {


	String rhtype;
	String rhid;
	String rptype;
	String rpid;
	String displayname;
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
	public String getRptype() {
		return rptype;
	}
	public void setRptype(String rptype) {
		this.rptype = rptype;
	}
	public String getRpid() {
		return rpid;
	}
	public void setRpid(String rpid) {
		this.rpid = rpid;
	}
	public String getDisplayname() {
		return displayname;
	}
	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}
	@Override
	public int compareTo(Object x) {
		// TODO Auto-generated method stub
		InjectedRPMetaInformation i = (InjectedRPMetaInformation) x;
		if (this.getDisplayname() != null & i != null && i.getDisplayname()!= null)
			return this.getDisplayname().compareTo(i.getDisplayname());
		else
			return 1;
	}
	
}
