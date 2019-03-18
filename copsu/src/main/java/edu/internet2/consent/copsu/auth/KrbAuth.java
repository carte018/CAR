package edu.internet2.consent.copsu.auth;

import edu.internet2.consent.copsu.cfg.CopsuConfig;
import edu.internet2.consent.copsu.model.LogCriticality;
import edu.internet2.consent.copsu.util.CopsuUtility;
import com.sun.security.auth.module.Krb5LoginModule;
import javax.security.auth.Subject;
import java.util.HashMap;
import java.util.HashSet;
import javax.security.auth.kerberos.KerberosPrincipal;

public class KrbAuth implements BasicAuthHandler {

	@Override
	public boolean validateCredential(String user, String credential,CopsuConfig config) {

		String realm = config.getProperty("krbauth.realm", true);
		String defaultService = config.getProperty("krbauth.defaultService", true);
		
		String unscopedUser = user.split("@")[0];  // hacky, I know
		
		try {
			return checkAuthentication(unscopedUser, credential, realm, defaultService);
		} catch (Exception e) {
			CopsuUtility.locError(500, "ERR0032", LogCriticality.error, this.getClass().getName());  // log
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
