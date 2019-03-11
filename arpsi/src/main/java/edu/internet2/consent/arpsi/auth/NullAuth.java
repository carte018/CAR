package edu.internet2.consent.arpsi.auth;

import edu.internet2.consent.arpsi.cfg.ArpsiConfig;

public class NullAuth implements edu.internet2.consent.arpsi.auth.BasicAuthHandler {


	// Null or "always succeed" BasicAuthHandler implementation supporting null authentication

	@Override
	public boolean validateCredential(String user, String credential,ArpsiConfig config) {
		return true;
	}

}
