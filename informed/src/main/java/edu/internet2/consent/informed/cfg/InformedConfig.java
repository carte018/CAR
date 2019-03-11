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
