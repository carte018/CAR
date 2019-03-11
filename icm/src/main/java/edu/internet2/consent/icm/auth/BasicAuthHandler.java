package edu.internet2.consent.icm.auth;

import edu.internet2.consent.icm.cfg.IcmConfig;

public interface BasicAuthHandler {

	public boolean validateCredential(String user, String credential, IcmConfig config);
	
}
