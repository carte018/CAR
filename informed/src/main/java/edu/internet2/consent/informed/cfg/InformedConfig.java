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
package edu.internet2.consent.informed.cfg;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class InformedConfig {

	//Singleton configuration for the COPSU
	private static InformedConfig config = null;
	private Properties properties = null;
	
	private InformedConfig() {
		properties = new Properties();
		
		ClassLoader cl = InformedConfig.class.getClassLoader();
		URL url = cl.getResource("informed.conf");
		
		InputStream etcfile = null;
		InputStream inputStream  = null;
		
		try {
			etcfile = new FileInputStream("/etc/car/informed/informed.conf");
			properties.load(etcfile);
		} catch (Exception f) {
			
			try {
				inputStream = url.openStream();
				properties.load(inputStream);
			} catch (Exception e) {
				throw new RuntimeException("Failed loading informed.conf properties",e);
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Exception ign) {
						//	ignore
					}
				}
			}
		}
	}	
	public static InformedConfig getInstance() {
		// Get the singleton
		if (config == null) {
			config = new InformedConfig();
		}
		return config;
	}
	
	// And get a property out of it as needed
	public String getProperty(String property, boolean exceptionIfNotFound) {
		String value = properties.getProperty(property);
		if (value == null && exceptionIfNotFound) {
			throw new RuntimeException("property:" + property + " not found");
		}
		return value;
	}
}
