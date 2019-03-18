package edu.internet2.consent.arpsi.util;

import java.io.File;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.internet2.consent.arpsi.model.LogCriticality;

public class FactoryFactory {
	public static SessionFactory factory;
	public static synchronized SessionFactory getSessionFactory() {
		if (factory == null) {
			File cfile = new File("/etc/car/arpsi/hibernate.cfg.xml");
			if (cfile.exists()) 
				factory = new Configuration().configure(cfile).buildSessionFactory();
			else {
				ArpsiUtility.locLog("LOG0016",LogCriticality.debug);
				factory = new Configuration().configure().buildSessionFactory();
			}
		}
		return factory;
	}
}
