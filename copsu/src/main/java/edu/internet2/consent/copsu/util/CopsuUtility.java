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
package edu.internet2.consent.copsu.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.internet2.consent.copsu.auth.AuthenticationDriver;
import edu.internet2.consent.copsu.cfg.CopsuConfig;
import edu.internet2.consent.copsu.model.ErrorModel;
import edu.internet2.consent.copsu.model.LogCriticality;
import edu.internet2.consent.exceptions.CopsuConfigurationException;
import edu.internet2.consent.exceptions.CopsuInitializationException;

public class CopsuUtility {
	
	private static final Log LOG=LogFactory.getLog(CopsuUtility.class);
	private static ResourceBundle locRB = ResourceBundle.getBundle("i18n.errors",new Locale("en")); // singleton for error processing, "en" default
	private static ResourceBundle locDB = ResourceBundle.getBundle("i18n.logs",new Locale("en"));   // singleton for logging debugs
	
	public static boolean isAuthorized(HttpServletRequest request, HttpHeaders headers, String entity, CopsuConfig config) {
		// For the moment, COPSU authorization is always granted
		// Base authorization on membership in one of two lists explicitly expressed in 
		// the config file.  Members of either list are valid users of the app.
		if (AuthenticationDriver.getAuthenticatedUser(request, headers, config) != null) {
			String appauth = config.getProperty("appUsers", false);
			String appadmins = config.getProperty("appAdmins", false);
			if (appauth != null) {
				ArrayList<String> appusers = new ArrayList<String>(Arrays.asList(appauth.split(",")));
				if (appadmins != null)
					appusers.addAll(Arrays.asList(appadmins.split(",")));
				for (String u : appusers) {
					if (AuthenticationDriver.getAuthenticatedUser(request, headers, config).equalsIgnoreCase(u)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isAuthorized(String operation, HttpServletRequest request, HttpHeaders headers, String entity, CopsuConfig config) {
		// For the moment, COPSU authorization is always granted
		// For the moment, base authorization solely on app authorization -- if you can call
		// this API, you can currently call all of this API.
		if (isAuthorized(request,headers,entity,config)) {
			return true;
		}
		return false;
	}
	
	public static CopsuConfig init(String operation, HttpServletRequest request, HttpHeaders headers, String entity) {
		
		// Perform initialization -- get a config object and check authorization based on the user
		// Throw a CopsuInitializationException if any of this fails.  Return the config if not.
		
		CopsuConfig config = null;
		// Get the default message catalog ResourceBundle.  We default to "en" since that's the most common language.
		// This should be a cached singleton in the instance using the .getBundle() method
		ResourceBundle defRB = ResourceBundle.getBundle("i18n.errors", new Locale("en"));
		try {
			config = CopsuConfig.getInstance();
		} catch (CopsuConfigurationException c) {
			locError(500,"ERR0001",LogCriticality.error);
			throw new CopsuInitializationException(defRB.getString("ERR0001"));
		}
		
		// We have a configuration object -- use it with the input request info to do authorization
		if (! isAuthorized(request,headers,entity,config)) {
			// no authorization to use the interface at all
			locError(500,"ERR0002",LogCriticality.error);
			throw new CopsuInitializationException(defRB.getString("ERR0002"));
		}
		
		if (! isAuthorized(operation,request,headers,entity,config)) {
			locError(500,"ERR0003",LogCriticality.error);
			throw new CopsuInitializationException(defRB.getString("ERR0003"));
		}
		String sl = null;
		if ((sl = config.getProperty("serverLanguage", false)) != null) {
				locRB = ResourceBundle.getBundle("i18n.errors",new Locale(sl));  // override if found
		}
		if (sl != null) {
				locDB = ResourceBundle.getBundle("i18n.logs",new Locale(sl));  // override if found
		}
		return(config);
		
	}
	
	// Process criticality comparisons
	private static boolean isLog(LogCriticality crit) {
		// Determine log level and then return isLog(crit,level)
		CopsuConfig config = CopsuConfig.getInstance();
		String level = "error";
		if (config != null) {
			String nlevel = config.getProperty("logLevel", false);
			if (nlevel != null) {
				level = nlevel;
			}
		}
		return isLog(crit,level);
	}
	
	private static boolean isLog(LogCriticality crit, String level) {
		// Return true if criticality is at or above level
		if (crit.equals(LogCriticality.error)
				|| (crit.equals(LogCriticality.info) && (level.contentEquals("info") || level.equals("debug")))
				|| (crit.equals(LogCriticality.debug)&& (level.contentEquals("debug")))) {
			return true;
		} else {
			return false;
		}

	}
	
	// The locError methods generate HTTP errors as well as (subject to criticality 
	// testing) logging errors.  *Not all* locError() calls will result in a log being written
	// but all of them will result in an error Response being generated (and possibly returned)
	
	public static Response locError(int retcode, String errcode, LogCriticality crit) {
		// return the Response object to use for an error return.  Along the way, write a log message.
		if (crit == null)
			crit = LogCriticality.error;
		if (isLog(crit))
			LOG.error("ERROR ecode=" + errcode + ": " + locRB.getString(errcode));
		return new ErrorModel(retcode,locRB.getString(errcode)).toResponse();
	}
	
	
	public static Response locError(int retcode, String errcode, LogCriticality crit, String... args) {
		// Return the Response object to use for an error return.  With substitution.
		String raw=locRB.getString(errcode);
		if (raw != null && args != null && args.length > 0 && args[0] != null) {
			for (int i=0; i<args.length; i++) {
				if (args[i] != null) 
					raw = raw.replaceAll("\\{"+i+"\\}", args[i]);
			}
		}
		if (crit == null)
			crit = LogCriticality.error;
		if (isLog(crit))
			LOG.error("ERROR ecode=" + errcode + ": " + raw);
		return new ErrorModel(retcode,raw).toResponse();
	}
	
	// locLog() methods are pur logging methods and do not return anything.
	
	public static void locLog(String errcode, LogCriticality crit) {
		if (crit == null)
			crit = LogCriticality.error;
		if (isLog(crit))
			LOG.info(crit.toString().toUpperCase() + " ecode=" + errcode + " "+ locDB.getString(errcode));
	}
	
	public static void locLog(String errcode, LogCriticality crit, String...strings) {
		if (crit == null)
			crit = LogCriticality.error;
		if (! isLog(crit)) 
			return;
		String ret = locDB.getString(errcode);
		if (ret != null && strings != null && strings.length > 0) {
			for (int i=0; i<strings.length; i++) {
				if (strings[i] != null) 
					ret = ret.replaceAll("\\{"+i+"\\}",strings[i]);
			}
		}
		LOG.info(crit.toString().toUpperCase() + " ecode=" + errcode + " " + ret);
	}

	
	public static Session getHibernateSession() {
		try {
			Class.forName(new Configuration().configure().getProperty("hibernate.connection.driver.class"));
		} catch (Exception e) {
			return null;
		}
		SessionFactory sf = FactoryFactory.getSessionFactory();
		Session sess = sf.openSession();
		return sess;
	}
		
}
