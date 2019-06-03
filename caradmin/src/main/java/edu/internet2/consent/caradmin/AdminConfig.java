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
package edu.internet2.consent.caradmin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;


public class AdminConfig {

		//Singleton configuration for the COPSU
		private static AdminConfig config = null;
		private Properties properties = null;
		
		private AdminConfig() {
			properties = new Properties();
			
			InputStream etcfile = null;
			InputStream inputStream  = null;
			
			try {
					etcfile = new FileInputStream("/etc/car/caradmin/caradmin.conf");
					properties.load(etcfile);
			} catch (Exception f) {
				ClassLoader cl = AdminConfig.class.getClassLoader();
				URL url = cl.getResource("caradmin.conf");

				try {
					inputStream = url.openStream();
					properties.load(inputStream);
				} catch (Exception e) {
					throw new RuntimeException("Failed loading caradmin.conf properties",e);
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (Exception ign) {
							//ignore
						}
					}
				}
			}
		}
		
		public static AdminConfig getInstance() {
			// Get the singleton
			if (config == null) {
				config = new AdminConfig();
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

