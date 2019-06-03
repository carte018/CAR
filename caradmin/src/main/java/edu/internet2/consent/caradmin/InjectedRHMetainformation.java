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

import edu.internet2.consent.informed.model.InternationalizedString;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;

public class InjectedRHMetainformation {

	String rhtype;
	String rhidentifier;
	String displayname;
	String description;
	ReturnedRHInfoItemList iilistmap;
	InternationalizedString idisplayname;
	InternationalizedString idescription;

	public InternationalizedString getIdisplayname() {
		return idisplayname;
	}
	public void setIdisplayname(InternationalizedString idisplayname) {
		this.idisplayname = idisplayname;
	}
	public InternationalizedString getIdescription() {
		return idescription;
	}
	public void setIdescription(InternationalizedString idescription) {
		this.idescription = idescription;
	}
	
	public ReturnedRHInfoItemList getIilistmap() {
		return iilistmap;
	}
	public void setIilistmap(ReturnedRHInfoItemList rl) {
		this.iilistmap = rl;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRhtype() {
		return rhtype;
	}
	public void setRhtype(String rhtype) {
		this.rhtype = rhtype;
	}
	public String getRhidentifier() {
		return rhidentifier;
	}
	public void setRhidentifier(String rhidentifier) {
		this.rhidentifier = rhidentifier;
	}
	public String getDisplayname() {
		return displayname;
	}
	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}
	
	
}
