package edu.internet2.consent.copsu.util;

import java.io.File;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.internet2.consent.copsu.model.LogCriticality;

public class FactoryFactory {
	public static SessionFactory factory;
	public static synchronized SessionFactory getSessionFactory() {
		if (factory == null) {
			File cfile = new File("/etc/car/copsu/hibernate.cfg.xml");
			if (cfile.exists()) 
				factory = new Configuration().configure(cfile).buildSessionFactory();
			else {
				CopsuUtility.locLog("ERR0037", LogCriticality.debug, "Failed to open /etc/car/copsu/hibernate.cfg.xml -- using local config");
				factory = new Configuration().configure().buildSessionFactory();
			}
		}
		return factory;
	}
}
