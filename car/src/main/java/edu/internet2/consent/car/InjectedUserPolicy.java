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
