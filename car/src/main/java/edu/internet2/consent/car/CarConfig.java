package edu.internet2.consent.car;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;



public class CarConfig {
	//Singleton configuration for the COPSU
	private static CarConfig config = null;
	private Properties properties = null;
	
	private CarConfig() {
		properties = new Properties();
		

		InputStream etcfile = null;
		InputStream inputStream  = null;
		
		try {
			etcfile = new FileInputStream("/etc/car/car/car.conf");
			properties.load(etcfile);
		} catch (Exception f) {
			ClassLoader cl = CarConfig.class.getClassLoader();
			URL url = cl.getResource("car.conf");
						
			try {
				inputStream = url.openStream();
				properties.load(inputStream);
			} catch (Exception e) {
				throw new RuntimeException("Failed loading car.conf properties",e);
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
	public static CarConfig getInstance() {
		// Get the singleton
		if (config == null) {
			config = new CarConfig();
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
