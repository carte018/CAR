package edu.internet2.consent.icm.auth;

import edu.internet2.consent.icm.cfg.IcmConfig;

public class NullAuth implements BasicAuthHandler {
	@Override
	public boolean validateCredential(String user, String credential,IcmConfig config) {
		return true;
	}
}
