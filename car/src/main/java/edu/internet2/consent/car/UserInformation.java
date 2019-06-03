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
