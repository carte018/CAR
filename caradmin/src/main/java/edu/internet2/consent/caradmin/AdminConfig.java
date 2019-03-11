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

