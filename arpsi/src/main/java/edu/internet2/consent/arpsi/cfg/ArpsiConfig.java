package edu.internet2.consent.arpsi.cfg;


import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class ArpsiConfig {

	//Singleton configuration for the COPSU
	private static ArpsiConfig config = null;
	private Properties properties = null;
	
	private ArpsiConfig() {
		properties = new Properties();
		

		
		InputStream etcfile = null;
		InputStream inputStream  = null;
		
		try {
			etcfile = new FileInputStream("/etc/car/arpsi/arpsi.conf");
			properties.load(etcfile);
		} catch (Exception f) {
			ClassLoader cl = ArpsiConfig.class.getClassLoader();
			URL url = cl.getResource("arpsi.conf");
			try {
				inputStream = url.openStream();
				properties.load(inputStream);
			} catch (Exception e) {
				throw new RuntimeException("Failed loading arpsi.conf properties",e);
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
	
	public static ArpsiConfig getInstance() {
		// Get the singleton
		if (config == null) {
			config = new ArpsiConfig();
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
