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
package edu.internet2.consent.informed.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import edu.internet2.consent.informed.cfg.InformedConfig;
import edu.internet2.consent.informed.model.LogCriticality;
import edu.internet2.consent.informed.util.InformedUtility;
import org.apache.commons.codec.binary.Base64;

public class AuthenticationDriver {
	@SuppressWarnings("unchecked")
	public static String getAuthenticatedUser(HttpServletRequest request,HttpHeaders headers, InformedConfig config) {
		String retUser = null;
		
		// Get the validator out of the configuration file
		String validator = null;
		try {
			validator = config.getProperty("informed.basicauth.validator.class", true);
		} catch (Exception e) {
			throw new RuntimeException(InformedUtility.locError(500,"ERR0029",LogCriticality.error).getEntity().toString());
		}
		Class<BasicAuthHandler> validatorClass = null;
		try {
			validatorClass = (Class<BasicAuthHandler>) Class.forName(validator);
		} catch (Exception e) {
			throw new RuntimeException(InformedUtility.locError(500, "ERR0030",LogCriticality.error, validator).getEntity().toString());
		}
		BasicAuthHandler v;
		try {
			v = (BasicAuthHandler) (validatorClass.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(InformedUtility.locError(500,"ERR0031",LogCriticality.error, validator).getEntity().toString());
		}
		
		// Get credential information from headers
		List<String> baHeaders = headers.getRequestHeader("Authorization");
		if (baHeaders == null) {
			return null;  // if no auth header, return null
		}
		String basicAuth=baHeaders.get(0);  // only inspect the first one
		
		if (basicAuth == null || basicAuth.equals("")) {
			return null;  // No logging here -- this could be a routine failed authN on the first request
		}
		
		// Otherwise, we return a userName or null to indicate another authorization is required
		
		String authData[] = basicAuth.split("\\s+");  // get the parts of the header
		String b64 = authData[1];  // basic auth data
		String unb64 = new String(Base64.decodeBase64(b64.getBytes()));
		String parts[] = unb64.split(":");
		if (v.validateCredential(parts[0], parts[1],config)) {
			retUser = parts[0];
		} else {
			retUser = null;
		}
		return retUser;
	}
}
