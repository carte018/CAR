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
package edu.internet2.consent.arpsi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.internet2.consent.arpsi.model.AllOtherValuesConst;
import edu.internet2.consent.arpsi.model.PolicyId;
import edu.internet2.consent.arpsi.model.OrgReleaseDirective;

	public class DecisionOnAllOtherValues {

		private OrgReleaseDirective releaseDecision;
		private AllOtherValuesConst allOtherValuesConst;
		private PolicyId policyId;
		
		@JsonProperty("releaseDecision")
		public OrgReleaseDirective getReleaseDecision() {
			return releaseDecision;
		}
		public void setReleaseDecision(OrgReleaseDirective releaseDecision) {
			this.releaseDecision = releaseDecision;
		}
		@JsonProperty("allOtherValuesConst")
		public AllOtherValuesConst getAllOtherValuesConst() {
			return allOtherValuesConst;
		}
		public void setAllOtherValuesConst(AllOtherValuesConst allOtherValuesConst) {
			this.allOtherValuesConst = allOtherValuesConst;
		}
		@JsonProperty("policyId")
		public PolicyId getPolicyId() {
			return policyId;
		}
		public void setPolicyId(PolicyId policyId) {
			this.policyId = policyId;
		}
}
