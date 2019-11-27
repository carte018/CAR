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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.icm.util.OMSingleton;

public class ReturnedPrecedenceObject {

	private String policyBaseId;
	private String policyDescription;
	private long numericRank;
	public String getPolicyBaseId() {
		return policyBaseId;
	}
	public void setPolicyBaseId(String policyBaseId) {
		this.policyBaseId = policyBaseId;
	}
	public String getPolicyDescription() {
		return policyDescription;
	}
	public void setPolicyDescription(String policyDescription) {
		this.policyDescription = policyDescription;
	}
	public long getNumericRank() {
		return numericRank;
	}
	public void setNumericRank(long numericRank) {
		this.numericRank = numericRank;
	}
	
	public String toJSON() throws JsonProcessingException {
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
