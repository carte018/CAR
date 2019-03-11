package edu.internet2.consent.icm.util;

import java.io.File;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class FactoryFactory {
	public static SessionFactory factory;
	public static synchronized SessionFactory getSessionFactory() {
		if (factory == null) {
			File cfile = new File("/etc/car/icm/hibernate.cfg.xml");
			if (cfile.exists()) 
				factory = new Configuration().configure(cfile).buildSessionFactory();
			else
				factory = new Configuration().configure().buildSessionFactory();
		}
		return factory;
	}
}
