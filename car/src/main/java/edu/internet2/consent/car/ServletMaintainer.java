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
