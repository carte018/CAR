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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allOtherInfoId == null) ? 0 : allOtherInfoId.hashCode());
		result = prime * result + ((icmDirectiveAllOtherValues == null) ? 0 : icmDirectiveAllOtherValues.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IcmAllOtherInfoReleaseStatement)) {
			return false;
		}
		IcmAllOtherInfoReleaseStatement other = (IcmAllOtherInfoReleaseStatement) obj;
		if (allOtherInfoId == null) {
			if (other.allOtherInfoId != null) {
				return false;
			}
		} else if (!allOtherInfoId.equals(other.allOtherInfoId)) {
			return false;
		}
		if (icmDirectiveAllOtherValues == null) {
			if (other.icmDirectiveAllOtherValues != null) {
				return false;
			}
		} else if (!icmDirectiveAllOtherValues.equals(other.icmDirectiveAllOtherValues)) {
			return false;
		}
		return true;
	}
	
	
}
