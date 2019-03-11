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
