package edu.internet2.consent.arpsi.model;


	import java.util.ArrayList;
	import java.util.List;

	import com.fasterxml.jackson.annotation.JsonProperty;

	public class DecisionOnValues {

		private List<String> returnedValuesList;
		private OrgReleaseDirective releaseDecision;
		private PolicyId policyId;
		
		public PolicyId getPolicyId() {
			return policyId;
		}
		public void setPolicyId(PolicyId policyId) {
			this.policyId = policyId;
		}
		@JsonProperty("returnedValuesList")
		public List<String> getReturnedValuesList() {
			return returnedValuesList;
		}
		public void setReturnedValuesList(ArrayList<String> returnedValuesList) {
			this.returnedValuesList = returnedValuesList;
		}
		@JsonProperty("releaseDecision")
		public OrgReleaseDirective getReleaseDecision() {
			return releaseDecision;
		}
		public void setReleaseDecision(OrgReleaseDirective releaseDecision) {
			this.releaseDecision = releaseDecision;
		}
		
}
