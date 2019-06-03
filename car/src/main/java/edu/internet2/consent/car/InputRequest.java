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
package edu.internet2.consent.car;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// This will be posted to our endpoint as a base64 blob 
public class InputRequest {

	@JsonProperty("header")
	private InputRequestHeader header;
	@JsonProperty("attributes")
	private ArrayList<AttributeValuelist> attributes;
	public InputRequestHeader getHeader() {
		return header;
	}
	public void setHeader(InputRequestHeader header) {
		this.header = header;
	}
	public ArrayList<AttributeValuelist> getAttributes() {
		return attributes;
	}
	public void setAttributes(ArrayList<AttributeValuelist> attributes) {
		this.attributes = attributes;
	}
	
	public String toJson() throws JsonProcessingException {
		// Return JSON representation of self
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
