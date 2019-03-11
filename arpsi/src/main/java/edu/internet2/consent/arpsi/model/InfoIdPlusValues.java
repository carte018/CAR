package edu.internet2.consent.arpsi.model;

	import java.util.ArrayList;
	import java.util.List;

	import com.fasterxml.jackson.annotation.JsonProperty;

	public class InfoIdPlusValues {

		private InfoId infoId;
		private List<String> infoItemValues;
		
		@JsonProperty("infoId")
		public InfoId getInfoId() {
			return infoId;
		}
		public void setInfoId(InfoId infoId) {
			this.infoId = infoId;
		}
		
		@JsonProperty("infoItemValues")
		public List<String> getInfoItemValues() {
			return infoItemValues;
		}
		public void setInfoItemValues(ArrayList<String> infoItemValues) {
			this.infoItemValues = infoItemValues;
		}
}
