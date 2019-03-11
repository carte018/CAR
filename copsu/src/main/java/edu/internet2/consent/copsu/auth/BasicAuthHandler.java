package edu.internet2.consent.copsu.auth;

import edu.internet2.consent.copsu.cfg.CopsuConfig;

public interface BasicAuthHandler {
	
	public boolean validateCredential(String user, String credential, CopsuConfig config); 
	
	
}
