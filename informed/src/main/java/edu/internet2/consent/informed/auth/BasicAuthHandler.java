package edu.internet2.consent.informed.auth;

import edu.internet2.consent.informed.cfg.InformedConfig;

public interface BasicAuthHandler {

	public boolean validateCredential(String user, String credential, InformedConfig config);
	
}
