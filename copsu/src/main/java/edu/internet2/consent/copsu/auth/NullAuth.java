package edu.internet2.consent.copsu.auth;

import edu.internet2.consent.copsu.cfg.CopsuConfig;

// Null or "always succeed" BasicAuthHandler implementation supporting null authentication


public class NullAuth implements BasicAuthHandler {

	@Override
	public boolean validateCredential(String user, String credential,CopsuConfig config) {
		return true;
	}

}
