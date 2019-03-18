package edu.internet2.consent.informed.auth;

import org.apache.commons.codec.digest.DigestUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import java.time.Duration;
import org.ehcache.expiry.ExpiryPolicy;
import java.time.temporal.ChronoUnit;


import edu.internet2.consent.informed.cfg.InformedConfig;
import edu.internet2.consent.informed.model.LogCriticality;
import edu.internet2.consent.informed.util.InformedUtility;
import com.sun.security.auth.module.Krb5LoginModule;
import javax.security.auth.Subject;
import java.util.HashMap;
import java.util.HashSet;
import javax.security.auth.kerberos.KerberosPrincipal;



public class KrbAuth implements BasicAuthHandler {

	private static CacheManager manager = null;
	private static Cache<String,String> cache = null;
	
	@Override
	public boolean validateCredential(String user, String credential,InformedConfig config) {

		String realm = config.getProperty("krbauth.realm", true);
		String defaultService = config.getProperty("krbauth.defaultService", true);
		
		String unscopedUser = user.split("@")[0];  // hacky, I know
		
		try {
			return checkAuthentication(unscopedUser, credential, realm, defaultService);
		} catch (Exception e) {
			InformedUtility.locError(500, "ERR0032", LogCriticality.error, this.getClass().getName());  // log
			return false;
		}
	}
	private boolean checkAuthentication(String username, String password, String realm, String service) {
		
		if (manager == null) {
			manager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
			try {
				ExpiryPolicy<Object,Object> ep = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(900,ChronoUnit.SECONDS));

				cache = manager.createCache("creds", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,String.class,ResourcePoolsBuilder.heap(1000)).withExpiry(ep));
			} catch (Exception e) {
				InformedUtility.locError(500,"ERR0066",LogCriticality.error,e.getMessage());
			}
		}
		
	//	String key = DigestUtils.sha256Hex(username + ":" + password + ":" + realm + ":" + service);
		String key = DigestUtils.sha256Hex(username + ":" + password + ":" + realm);  // service isn't relevant at this point
		if (cache != null && cache.containsKey(key)) {
			return (cache.get(key).equals("true"));
		}
		
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
				cache.put(key, "true");
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
}
