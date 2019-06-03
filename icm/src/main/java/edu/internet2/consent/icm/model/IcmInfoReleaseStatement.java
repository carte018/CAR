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

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
@Entity
public class IcmInfoReleaseStatement {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long OIRKey;
	
	@Embedded
	@JsonProperty("infoId")
	private InfoId infoId;
	
	@ElementCollection @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	@JsonProperty("arrayOfIcmDirectiveOnValues")
	List<IcmDirectiveOnValues> arrayOfIcmDirectiveOnValues;
	
	@Embedded
	@JsonProperty("icmDirectiveAllOtherValues")
	IcmDirectiveAllOtherValues icmDirectiveAllOtherValues;

	@JsonIgnore
	public Long getOIRKey() {
		return OIRKey;
	}

	@JsonIgnore
	public void setOIRKey(Long oIRKey) {
		OIRKey = oIRKey;
	}

	@JsonProperty("infoId")
	public InfoId getInfoId() {
		return infoId;
	}

	@JsonProperty("infoId")
	public void setInfoId(InfoId infoId) {
		this.infoId = infoId;
	}

	@JsonProperty("arrayOfIcmDirectiveOnValues")
	public List<IcmDirectiveOnValues> getArrayOfIcmDirectiveOnValues() {
		return arrayOfIcmDirectiveOnValues;
	}

	@JsonProperty("arrayOfIcmDirectiveOnValues")
	public void setArrayOfIcmDirectiveOnValues(List<IcmDirectiveOnValues> arrayOfIcmDirectiveOnValues) {
		this.arrayOfIcmDirectiveOnValues = arrayOfIcmDirectiveOnValues;
	}

	@JsonProperty("icmDirectiveAllOtherValues")
	public IcmDirectiveAllOtherValues getIcmDirectiveAllOtherValues() {
		return icmDirectiveAllOtherValues;
	}

	@JsonProperty("icmDirectiveAllOtherValues")
	public void setIcmDirectiveAllOtherValues(IcmDirectiveAllOtherValues icmDirectiveAllOtherValues) {
		this.icmDirectiveAllOtherValues = icmDirectiveAllOtherValues;
	}
	
	// Overrides
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		IcmInfoReleaseStatement i = (IcmInfoReleaseStatement) o;
		return (i.getInfoId().equals(this.getInfoId()) && i.getOIRKey().equals(this.getOIRKey()));  // shallow comparison
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(infoId,OIRKey); // weak hashing
	}
	
	@Override
	public String toString() {
		return "IcmInfoReleaseStatemetn #" + OIRKey;
	}
}
