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
