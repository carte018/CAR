package edu.internet2.consent.car;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OMSingleton {

	private static final ObjectMapper om = new ObjectMapper();
	
	/**
	 * @return the om
	 */
	public ObjectMapper getOm() {
		return om;
	}

	private OMSingleton() {
	}
	
	private static ThreadLocal<OMSingleton> oms = new ThreadLocal<OMSingleton>() {
		@Override
		protected OMSingleton initialValue() {
				return new OMSingleton();
		}
	};
	
	public static OMSingleton getInstance() {
		return oms.get();
	}
	
}
