package edu.internet2.consent.car;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ServletMaintainer implements ServletContextListener {

	private static final Log LOG = LogFactory.getLog(ServletContextListener.class);

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
		// No init routines for now
	}

}
