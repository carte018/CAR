package edu.internet2.consent.icm.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ListOfReturnedPrecedenceObject {

		@JsonProperty("returnedPrecedenceObjects")
		private ArrayList<ReturnedPrecedenceObject> contained;

		@JsonProperty("returnedPrecedenceObjects")
		public ArrayList<ReturnedPrecedenceObject> getContained() {
			return contained;
		}

		@JsonProperty("returnedPrecedenceObjects")
		public void setContained(ArrayList<ReturnedPrecedenceObject> contained) {
			this.contained = contained;
		}
		
		public ListOfReturnedPrecedenceObject() {
			this.contained = new ArrayList<ReturnedPrecedenceObject>();
		}
		
		public void addObject(ReturnedPrecedenceObject o) {
			contained.add(o);
		}
		
		public String toJSON() throws JsonProcessingException {
			ObjectMapper mapper = new ObjectMapper();
			String retval = mapper.writeValueAsString(this.contained);
			return retval;
		}
		
}
