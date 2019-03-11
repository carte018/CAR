package edu.internet2.consent.informed.auth;

import edu.internet2.consent.informed.cfg.InformedConfig;

public class NullAuth implements BasicAuthHandler {
	@Override
	public boolean validateCredential(String user, String credential,InformedConfig config) {
		return true;
	}
}
