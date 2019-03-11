package edu.internet2.consent.arpsi.auth;

import edu.internet2.consent.arpsi.cfg.ArpsiConfig;

public interface BasicAuthHandler {

	public boolean validateCredential(String user, String credential, ArpsiConfig config);
	
}
