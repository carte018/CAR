package edu.internet2.consent.informed.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.ElementCollection;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.informed.util.OMSingleton;

@javax.persistence.Entity
public class ScopeMapping {

	// mandatory ID for index
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long smapID;
	
	// Scope mappings are unique to RH's -- two RH's can 
	// map the same scope name differently if they want
	
	// RH type 
	@JsonProperty("rhtype")
	private String rhtype;
	
	// RH identifier
	@JsonProperty("rhvalue")
	private String rhvalue;
	
	// Name of the scope being mapped.  This is used for 
	// searching, so it should be indexed in the schema
	@JsonProperty("scopename")
	private String scopename;   
	
	// This is the most natural way to store the mapping - 
	// scope maps to info items by name.  The reality may 
	// be that this eventually needs to be flipped to 
	// map by iiid, but for initial purposes we'll start with this.
	// If performance can be maintained with occasional double 
	// indirection into the informed database, this will be fine.
	
	@ElementCollection 
	@JsonProperty("infoitems")
	private List<String> infoitems;

	/**
	 * @return the smapID
	 */
	public Long getSmapID() {
		return smapID;
	}

	/**
	 * @param smapID the smapID to set
	 */
	public void setSmapID(Long smapID) {
		this.smapID = smapID;
	}

	/**
	 * @return the rhtype
	 */
	public String getRhtype() {
		return rhtype;
	}

	/**
	 * @param rhtype the rhtype to set
	 */
	public void setRhtype(String rhtype) {
		this.rhtype = rhtype;
	}

	/**
	 * @return the rhvalue
	 */
	public String getRhvalue() {
		return rhvalue;
	}

	/**
	 * @param rhvalue the rhvalue to set
	 */
	public void setRhvalue(String rhvalue) {
		this.rhvalue = rhvalue;
	}

	/**
	 * @return the scopename
	 */
	public String getScopename() {
		return scopename;
	}

	/**
	 * @param scopename the scopename to set
	 */
	public void setScopename(String scopename) {
		this.scopename = scopename;
	}

	/**
	 * @return the infoitems
	 */
	public List<String> getInfoitems() {
		return infoitems;
	}

	/**
	 * @param infoitems the infoitems to set
	 */
	public void setInfoitems(List<String> infoitems) {
		this.infoitems = infoitems;
	}
	
	//Overrides
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ScopeMapping rco = (ScopeMapping)o;
		return(rco.getRhvalue().equals(this.getRhvalue()) && rco.getRhtype().equals(this.getRhtype()) && rco.getInfoitems().containsAll(this.getInfoitems()) && this.getInfoitems().containsAll(rco.getInfoitems()));
			}
	
	@Override
	public int hashCode() {
		return Objects.hash(rhtype,rhvalue,infoitems);
	}
	
	public String toJSON() throws JsonProcessingException{
		// Return JSON representation of self
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
}
