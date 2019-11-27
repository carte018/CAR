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
package edu.internet2.consent.copsu.model;

import java.util.Objects;

import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.copsu.util.OMSingleton;

@javax.persistence.Entity
public class ReturnedPolicy {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long PolicyIdentifier;
	
	@JsonProperty("policyMetaData")
	private PolicyMetadata policyMetaData;
	@Embedded
	@JsonProperty("infoReleasePolicy")
	private InfoReleasePolicy infoReleasePolicy;
	public PolicyMetadata getPolicyMetaData() {
		return policyMetaData;
	}
	
	@JsonIgnore
	public Long getPolicyIdentifier() {
		return PolicyIdentifier;
	}

	@JsonIgnore
	public void setPolicyIdentifier(Long policyIdentifier) {
		PolicyIdentifier = policyIdentifier;
	}

	public void setPolicyMetaData(PolicyMetadata policyMetaData) {
		this.policyMetaData = policyMetaData;
	}
	public InfoReleasePolicy getInfoReleasePolicy() {
		return infoReleasePolicy;
	}
	public void setInfoReleasePolicy(InfoReleasePolicy infoReleasePolicy) {
		this.infoReleasePolicy = infoReleasePolicy;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ReturnedPolicy rp = (ReturnedPolicy) o;
		return (rp.getPolicyMetaData().equals(this.getPolicyMetaData()) && rp.getInfoReleasePolicy().equals(this.getInfoReleasePolicy())); 
	}
	
	@Override 
	public int hashCode() {
		return Objects.hash(policyMetaData,infoReleasePolicy);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ReturnedPolicy {\n");
		sb.append("  policyMetaData: ").append(policyMetaData.toString()).append("\n");
		sb.append("  infoReleasePolicy: ").append(infoReleasePolicy.toString()).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
	
	public String toJSON() throws JsonProcessingException{
		// Return JSON representation of self
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
	
}
