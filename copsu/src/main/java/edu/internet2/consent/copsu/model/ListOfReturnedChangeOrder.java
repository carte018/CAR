package edu.internet2.consent.copsu.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ListOfReturnedChangeOrder {
	@JsonProperty("ReturnedChangeOrders")
	private ArrayList<ReturnedChangeOrder> contained;

	public ArrayList<ReturnedChangeOrder> getContained() {
		return contained;
	}

	public void setContained(ArrayList<ReturnedChangeOrder> contained) {
		this.contained = contained;
	}
	
	public ListOfReturnedChangeOrder() {
		contained = new ArrayList<ReturnedChangeOrder>();
	}
	
	public void addChangeOrder(ReturnedChangeOrder r) {
		contained.add(r);
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String retval = mapper.writeValueAsString(this.contained);
		return retval;
	}
}
