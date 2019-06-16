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
package edu.internet2.consent.car.auth;

import edu.internet2.consent.car.CarConfig;
import edu.internet2.consent.car.LogCriticality;
import edu.internet2.consent.car.CarUtility;
import com.sun.security.auth.module.Krb5LoginModule;
import javax.security.auth.Subject;
import java.util.HashMap;
import java.util.HashSet;
import javax.security.auth.kerberos.KerberosPrincipal;

public class KrbAuth implements BasicAuthHandler {

	@Override
	public boolean validateCredential(String user, String credential,CarConfig config) {

		String realm = config.getProperty("krbauth.realm", true);
		String defaultService = config.getProperty("krbauth.defaultService", true);
		
		String unscopedUser = user.split("@")[0];  // hacky, I know
		
		try {
			return checkAuthentication(unscopedUser, credential, realm, defaultService);
		} catch (Exception e) {
			CarUtility.locError("ERR0032", LogCriticality.error,"KrbAuth");
			return false;
		}
	}
	private boolean checkAuthentication(String username, String password, String realm, String service) {
		
		Krb5LoginModule mod = new Krb5LoginModule();
		
		HashMap<String,Object> shared = new HashMap<String,Object>();
		HashMap<String,String> options = new HashMap<String,String>();
		
		HashSet<KerberosPrincipal> princs = new HashSet<KerberosPrincipal>();
		KerberosPrincipal p = new KerberosPrincipal(username + "@" + realm);
		princs.add(p);
		
		shared.put("javax.security.auth.login.name", username);
		shared.put("javax.security.auth.login.password",password.toCharArray());
		options.put("useFirstPass", "true");
		
		mod.initialize(new Subject(false,princs,new HashSet<String>(),new HashSet<String>()),null,shared,options);
		
		try {
			if (mod.login()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
}
