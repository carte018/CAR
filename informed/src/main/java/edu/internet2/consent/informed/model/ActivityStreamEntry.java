package edu.internet2.consent.informed.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class ActivityStreamEntry {

	@Id
	@JsonIgnore
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long aseid;
	
	@JsonProperty("type")
	private String type;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("timestamp")
	private long timestamp;
	
	@JsonProperty("user")
	private String user;
	
	@JsonProperty("operation")
	private String operation;

	public long getAseid() {
		return aseid;
	}

	public void setAseid(long aseid) {
		this.aseid = aseid;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		return om.writeValueAsString(this);
	}
	
	
}
