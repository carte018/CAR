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
