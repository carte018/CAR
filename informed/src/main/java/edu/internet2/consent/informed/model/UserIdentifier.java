package edu.internet2.consent.informed.model;

import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// Not an entity -- just an identifier compound
@Embeddable
public class UserIdentifier {

	@JsonProperty("usertype")
	private String usertype;
	public String getUsertype() {
		return usertype;
	}
	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	@JsonProperty("userid")
	private String userid;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		UserIdentifier u = (UserIdentifier) o;
		return (u.getUserid().equals(this.getUserid()) && u.getUsertype().equals(this.getUsertype()));
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getUserid(),this,getUsertype());
	}
	
	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
