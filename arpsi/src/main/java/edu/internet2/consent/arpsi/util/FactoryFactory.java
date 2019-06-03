/*
 * Copyright 2015 - 2019 Duke University
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2 as published by
    the Free Software Foundation.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License Version 2
    along with this program.  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt>.

 */
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
