package edu.internet2.consent.copsu.util;

import org.glassfish.jersey.server.ResourceConfig;
import org.hibernate.Session;

public class AppConfig extends ResourceConfig {

	public AppConfig() {
		
		// preinit the DB session pool by getting a Hibernate session 
		// in the init thread.  Hack much?
		
		packages("edu.internet2.consent.copsu");
		
		Session sess = CopsuUtility.getHibernateSession();
		sess.close();
	}
}