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
