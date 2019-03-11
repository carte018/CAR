package edu.internet2.consent.icm.model;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class IcmAllOtherInfoReleaseStatement {

	@Embedded
	@JsonProperty("allOtherInfoId")
	AllOtherInfoId allOtherInfoId;
	
	@Embedded
	@JsonProperty("icmDirectiveAllOtherValues")
	IcmDirectiveAllOtherValues icmDirectiveAllOtherValues;

	@JsonProperty("allOtherInfoId")
	public AllOtherInfoId getAllOtherInfoId() {
		return allOtherInfoId;
	}

	@JsonProperty("allOtherInfoId")
	public void setAllOtherInfoId(AllOtherInfoId allOtherInfoId) {
		this.allOtherInfoId = allOtherInfoId;
	}

	@JsonProperty("icmDirectiveAllOtherValues")
	public IcmDirectiveAllOtherValues getIcmDirectiveAllOtherValues() {
		return icmDirectiveAllOtherValues;
	}

	@JsonProperty("icmDirectiveAllOtherValues")
	public void setIcmDirectiveAllOtherValues(IcmDirectiveAllOtherValues icmDirectiveAllOtherValues) {
		this.icmDirectiveAllOtherValues = icmDirectiveAllOtherValues;
	}
	
	@JsonProperty("directiveAllOtherValues")
	public void setDirectiveAllOtherValues(IcmDirectiveAllOtherValues icmDirectiveAllOtherValues) {
		this.icmDirectiveAllOtherValues = icmDirectiveAllOtherValues;
	}
	
	
}
