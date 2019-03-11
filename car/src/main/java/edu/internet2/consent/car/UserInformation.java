package edu.internet2.consent.car;

import java.util.ArrayList;
import java.util.HashMap;

public class UserInformation {

	// carrier class for user information retrieved from SAML assertions
	private String userId;
	private String userType;
	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	private HashMap<String,ArrayList<String>> attributes;
	
	public UserInformation() {
		attributes = new HashMap<String,ArrayList<String>>();
	}
	
	public void addValue(String attr,String value) {
		if (attributes.containsKey(attr)) {
			attributes.get(attr).add(value);
		} else {
			ArrayList<String> foo = new ArrayList<String>();
			foo.add(value);
			attributes.put(attr, foo);
		}
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public HashMap<String, ArrayList<String>> getAttributes() {
		return attributes;
	}
	public void setAttributes(HashMap<String, ArrayList<String>> attributes) {
		this.attributes = attributes;
	}
	
	public String toString() {
		String retval = "";
		retval += "Type: "+userType+", UserId: "+userId+"\n";
		for (String x : attributes.keySet()) {
			ArrayList<String> y = attributes.get(x);
			for (String z : y) {
				retval = retval + "," + x + "=" + z;
			}
		}
		return retval;
	}
}
