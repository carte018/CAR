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

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.arpsi.util.OMSingleton;

public class ListOfOrgReturnedPolicy {
	@JsonProperty("returnedPolicies")
	private ArrayList<OrgReturnedPolicy> contained;

	@JsonProperty("returnedPolicies")
	public ArrayList<OrgReturnedPolicy> getContained() {
		return contained;
	}

	@JsonProperty("returnedPolicies")
	public void setContained(ArrayList<OrgReturnedPolicy> contained) {
		this.contained = contained;
	}
	
	public ListOfOrgReturnedPolicy() {
		contained = new ArrayList<OrgReturnedPolicy>();
	}
	
	public void addPolicy(OrgReturnedPolicy o) {
		contained.add(o);
	}
	
	public String toJSON() throws JsonProcessingException {
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		String retval = mapper.writeValueAsString(this.contained);
		return retval;
	}
}