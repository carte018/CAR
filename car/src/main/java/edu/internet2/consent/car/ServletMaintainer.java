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
package edu.internet2.consent.car;

import java.util.ArrayList;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import edu.internet2.consent.informed.model.ReturnedInfoItemMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedValueMetaInformation;


public class ServletMaintainer implements ServletContextListener {

	private static final Log LOG = LogFactory.getLog(ServletContextListener.class);

	private static CacheIniter ci = null;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// Terminate the TimerTask handling cache scrubbing to avoid leakage during redeploys
		LOG.error("CANCELLING CACHE SCRUBBER THREAD DUE TO UNDEPLOY");
		MetaInformationCacheManager.getInstance().getTimed().cancel();
		MetaInformationCacheManager.getInstance().getTimer().purge();
		MetaInformationCacheManager.getInstance().getTimer().cancel();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		// Now start a one-time thread on startup to 
		// initialize the cache.  Note that this thread will 
		// block until the other services are available and then 
		// proceed to load the cache.
		
		LOG.error("Cache pre-load thread initialized");
		
		if (ci == null) {
			ci = new CacheIniter();
		}
		
		new Thread(ci).start();
	}

}

class CacheIniter implements Runnable {
	private static boolean initialized = false;
	
	public void run() {
		// If the cache has not been initialized, initialize it on this run and
		// return.  If it has, continue normally.
		//
		if (!initialized) {
			
			initialized = true;  // bypass in future
			
			CarConfig config = CarConfig.getInstance();
			
			if ("false".equalsIgnoreCase(config.getProperty("car.precache", false))) 
				return;  // if car.precache is explicitly set to "false" in the config, do no init work
			
			HttpClient httpClient = null;
			try {
				httpClient = CarHttpClientFactory.getHttpsClient();
			} catch (Exception e) {
				// Log and create a raw client instead
				CarUtility.locError("ERR1136", LogCriticality.error,"Falling back to default HttpClient d/t failed client initialization");
				httpClient = HttpClientBuilder.create().build();
			}
			
			if (! "false".equalsIgnoreCase(config.getProperty("car.precache.value", false))) {
			ArrayList<ReturnedValueMetaInformation> arvm = CarUtility.getAllValueMetaInformation(config, httpClient);
			
			if (arvm != null) {
				CarUtility.locError("ERR1134", LogCriticality.error,"InitCaching " + arvm.size() + " value cache entries");
				
				ValueMetaInformationCache vmc = ValueMetaInformationCache.getInstance();
				for (ReturnedValueMetaInformation r : arvm) {
					vmc.storeCachedValueMetaInformation(r.getInfoitemname(), r.getInfoitemvalue(), r);
				}
			}
			}
			
			if (! "false".equalsIgnoreCase(config.getProperty("car.precache.info", false))) {
			ArrayList<ReturnedInfoItemMetaInformation> arim = CarUtility.getAllInfoItemMetaInformation(config, httpClient);
			
			if (arim != null) {
				CarUtility.locError("ERR1134", LogCriticality.error,"InitCaching " + arim.size() + " infoitem cache entries");

				InfoItemMetaInformationCache imc = InfoItemMetaInformationCache.getInstance();
				for (ReturnedInfoItemMetaInformation i : arim) {
					imc.storeCachedInfoItemMetaInformation(i.getIiidentifier().getIitype(), i.getIiidentifier().getIiid(), i);
				}
			}
			}

			if (! "false".equalsIgnoreCase(config.getProperty("car.precache.rp", false))) {
			ArrayList<ReturnedRPMetaInformation> arpm = CarUtility.getAllRPMetaInformation(config,httpClient);
			
			if (arpm != null) {
				CarUtility.locError("ERR1134", LogCriticality.error,"InitCaching " + arpm.size() + " rp cache entries");

				RPMetaInformationCache rpc = RPMetaInformationCache.getInstance();
				for (ReturnedRPMetaInformation m : arpm) {
					rpc.storeCachedRPMetaInformation(m.getRhidentifier().getRhid(), m.getRpidentifier().getRpid(), m);
				}
			}
			}
			
			return;  // if we initialize, we're done for this run
		}
	
	}
}
