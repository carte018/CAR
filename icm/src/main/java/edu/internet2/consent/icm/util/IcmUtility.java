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
package edu.internet2.consent.icm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.internet2.consent.icm.cfg.IcmConfig;
import edu.internet2.consent.icm.model.LogCriticality;
import edu.internet2.consent.icm.util.FactoryFactory;
import edu.internet2.consent.icm.auth.AuthenticationDriver;
import edu.internet2.consent.icm.model.ErrorModel;

public class IcmUtility {
	
	private static final Logger LOG=LoggerFactory.getLogger(IcmUtility.class);
	private static ResourceBundle locRB = ResourceBundle.getBundle("i18n.errors",new Locale("en")); // singleton for error processing, "en" default
	private static ResourceBundle locDB = ResourceBundle.getBundle("i18n.logs",new Locale("en"));   // singleton for logging debugs
	private static boolean registered = false;
	
	//
	// Mechanisms for keeping Session instances threadlocal to avoid race conditions in 
	// c3p0 cache (collections returned while background threads are maintaining the cache
	//
	private static final ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();
    private static org.hibernate.SessionFactory sessionFactory = null;
	
	private static Configuration config = new Configuration();
	
	public static boolean isAuthorized(HttpServletRequest request, HttpHeaders headers, String entity, IcmConfig config) {
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
	
	public static boolean isAuthorized(String operation, HttpServletRequest request, HttpHeaders headers, String entity, IcmConfig config) {
		// For the moment, base authorization solely on app authorization -- if you can call
		// this API, you can currently call all of this API.
		if (isAuthorized(request,headers,entity,config)) {
			return true;
		}
		return false;
	}
	
	public static IcmConfig init(String operation, HttpServletRequest request, HttpHeaders headers, String entity) {
		
		// Perform initialization -- get a config object and check authorization based on the user
		// Throw a CopsuInitializationException if any of this fails.  Return the config if not.
		
		IcmConfig config = null;
		// Get the default message catalog ResourceBundle.  We default to "en" since that's the most common language.
		// This should be a cached singleton in the instance using the .getBundle() method
		ResourceBundle defRB = ResourceBundle.getBundle("i18n.errors", new Locale("en"));
		try {
			config = IcmConfig.getInstance();
		} catch (Exception c) {
			locError(500,"ERR0001", LogCriticality.error);
			throw new RuntimeException(defRB.getString("ERR0001"));
		}
		
		// We have a configuration object -- use it with the input request info to do authorization
		if (! isAuthorized(request,headers,entity,config)) {
			// no authorization to use the interface at all
			locError(500,"ERR0002",LogCriticality.error);
			throw new RuntimeException(defRB.getString("ERR0002"));
		}
		
		if (! isAuthorized(operation,request,headers,entity,config)) {
			locError(500,"ERR0003",LogCriticality.error);
			throw new RuntimeException(defRB.getString("ERR0003"));
		}
		String sl = null;
		if ((sl = config.getProperty("serverLanguage", false)) != null) {
				locRB = ResourceBundle.getBundle("i18n.errors",new Locale(sl));  // override if found
		}
		if (sl != null) {
				locDB = ResourceBundle.getBundle("i18n.logs",new Locale(sl));  // override if found
		}
		
		// Adjust the logger's config'd log level if logLevel is specified in configuration
		
		if (config.getProperty("logLevel", false) != null) {
			((ch.qos.logback.classic.Logger)LOG).setLevel(Level.toLevel(config.getProperty("logLevel", false)));
		}
		
		return(config);
		
	}
	
	// Process criticality comparisons
	private static boolean isLog(LogCriticality crit) {
		// Determine log level and then return isLog(crit,level)
		IcmConfig config = IcmConfig.getInstance();
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
	
	public static Response locError(int retcode, String errcode) {
		return locError(retcode,errcode,LogCriticality.error);
	}
	public static Response locError(int retcode, String errcode, String... strings) {
		return locError(retcode,errcode,LogCriticality.error,strings);
	}
	
	public static void locLog(String errcode) {
		locLog(errcode, LogCriticality.info);
	}
	public static void locLog(String errcode, String...strings) {
		locLog(errcode, LogCriticality.info, strings);
	}
	
	public static void locDebug(String errcode) {
		locDebug(errcode, LogCriticality.debug);
	}
	public static void locDebug(String errcode, String...strings) {
		locDebug(errcode, LogCriticality.debug, strings);
	}
	
	public static Response locError(int retcode, String errcode,LogCriticality crit) {
		// return the Response object to use for an error return.  Along the way, write a log message.
		// No longer necessary
		//if (crit == null)
		//	crit = LogCriticality.error;
		//if (isLog(crit))
		if (crit == null || LogCriticality.error.equals(crit))
			LOG.error("ERROR ecode=" + errcode + ": " + locRB.getString(errcode));
		else
			LOG.info("ERROR ecode=" + errcode + ": " + locRB.getString(errcode));
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
		// No longer needed
		//if (crit == null)
		//	crit = LogCriticality.error;
		//if (isLog(crit))
		if (crit == null || LogCriticality.error.equals(crit))
			LOG.error("ERROR ecode=" + errcode + ": " + raw);
		else
			LOG.info("ERROR ecode=" + errcode + ": " + raw);
		return new ErrorModel(retcode,raw).toResponse();
	}
	
	public static void locLog(String errcode,LogCriticality crit) {
		// No longer necessary
		//if (crit == null)
		//	crit = LogCriticality.error;
		//if (isLog(crit))
		
		LOG.info(crit.toString().toUpperCase() + " ecode=" + errcode + " "+ locDB.getString(errcode));
	}
	
	public static void locLog(String errcode, LogCriticality crit, String...strings) {
		String ret = locDB.getString(errcode);
		if (ret != null && strings != null && strings.length > 0) {
			for (int i=0; i<strings.length; i++) {
				if (strings[i] != null) 
					ret = ret.replaceAll("\\{"+i+"\\}",strings[i]);
			}
		}
		// No longer necessary
		//if (crit == null)
		//	crit = LogCriticality.error;
		//if (isLog(crit))
		
		LOG.info(crit.toString().toUpperCase() + " ecode=" + errcode + " " + ret);
	}
	
	public static void locDebug(String errcode,LogCriticality crit) {
		LOG.debug(crit.toString().toUpperCase() + " ecode=" + errcode + " "+ locDB.getString(errcode));
	}
	public static void locDebug(String errcode, LogCriticality crit, String...strings) {
		String ret = locDB.getString(errcode);
		if (ret != null && strings != null && strings.length > 0) {
			for (int i=0;i<strings.length;i++) {
				if (strings[i] != null) 
					ret = ret.replaceAll("\\{"+i+"\\}", strings[i]);
			}
		}
		LOG.debug(crit.toString().toUpperCase() + " ecode=" + errcode + " " + ret);
	}

	public static Session getHibernateSession() {
		Session session = (Session) threadLocal.get(); 
		
		if (!registered) {
			try {
				File cfile = new File("/etc/car/icm/hibernate.cfg.xml");
				if (cfile.exists())
					Class.forName(config.configure(cfile).getProperty("hibernate.connection.driver.class"));
				else
					Class.forName(config.configure().getProperty("hibernate.connection.driver.class"));
				registered = true;
			} catch (Exception e) {
				return null;   // Return null if we cannot register configuration
			}
		}
		// Otherwise, we have hibernate config'd
		
		if (session == null || ! session.isOpen()) {
			// Create the static sessionFactory if needed
			if (sessionFactory == null)
				sessionFactory = FactoryFactory.getSessionFactory();
		
			session = (sessionFactory != null) ? sessionFactory.openSession()
					: null;
			threadLocal.set(session);
		}
		
		return session;
	}
	
	/* Replaced with a threadlocal pattern session manager
	public static Session getHibernateSession() {
		if (! registered) {
			try {
				File cfile = new File("/etc/car/icm/hibernate.cfg.xml");
				if (cfile.exists())
					Class.forName(new Configuration().configure(cfile).getProperty("hibernate.connection.driver.class"));
				else
					Class.forName(new Configuration().configure().getProperty("hibernate.connection.driver.class"));
				registered = true;
			} catch (Exception e) {
				return null;
			}
		}
		SessionFactory sf = FactoryFactory.getSessionFactory();
		Session sess = sf.openSession();
		return sess;
	}
	*/
	public static String buildAuthorizationHeader(IcmConfig config) {
		String basicUser = config.getProperty("icmUser", true);
		String basicCred = config.getProperty("icmCred", true);
		
		String authString = basicUser + ":" + basicCred;
		byte [] encodedString = Base64.encodeBase64(authString.getBytes());
		String retval = "Basic " + new String(encodedString);
		
		return retval;
	}
	
	public static int extractStatusCode(HttpResponse response) {
		return response.getStatusLine().getStatusCode();
	}
	
	public static String extractBody(HttpResponse response) {
		String retval = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader((response.getEntity().getContent()),StandardCharsets.UTF_8));
			StringBuilder rsb = new StringBuilder();
			String body = null;
			while((body = br.readLine()) != null) {
				rsb.append(body);
			}
			retval = rsb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return retval;
	}
	
	public static HttpResponse forwardRequest(HttpClient httpClient, String targetMethod, String targetServer, String targetPort, String targetURI, HttpServletRequest request, String entity) {

		String authzHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		
		return sendRequest(httpClient,targetMethod,targetServer,targetPort,targetURI,entity,authzHeader);

		
	}
	public static HttpResponse sendRequest(HttpClient httpClient, String targetMethod,String targetServer,String targetPort,String targetURI,String payload,String authzHeader) {
		
		HttpResponse retval = null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("https://");
		sb.append(targetServer);
		sb.append(":");
		sb.append(targetPort);
		if (targetURI.startsWith("/")) {
			sb.append(targetURI);
		} else {
			sb.append("/");
			sb.append(targetURI);
		}
		
		HttpRequestBase httpRequest = null;
		
		StringEntity sendEntity = null;
		
		if (targetMethod.equalsIgnoreCase("GET")) {
			httpRequest = new HttpGet(sb.toString());
		} else if (targetMethod.equalsIgnoreCase("PUT")) {
			httpRequest = new HttpPut(sb.toString());
			try {
				sendEntity = new StringEntity(payload,"UTF-8");
				sendEntity.setContentType("application/json");
				((HttpPut)httpRequest).setEntity(sendEntity);
			} catch (Exception e) {
				IcmUtility.locError(400, "ERR0037", LogCriticality.error,"Exception in sendRequest (1): " + e.getMessage());
				throw new RuntimeException(e);
			}
		} else if (targetMethod.equalsIgnoreCase("POST")) {
			httpRequest = new HttpPost(sb.toString());
			try {
				sendEntity = new StringEntity(payload,"UTF-8");
				sendEntity.setContentType("application/json");
				((HttpPost)httpRequest).setEntity(sendEntity);
			} catch (Exception e) {
				IcmUtility.locError(400, "ERR0037", LogCriticality.error, "Exception in sendRequest (2): " + e.getMessage());
				throw new RuntimeException(e);
			}
		} else if (targetMethod.equalsIgnoreCase("DELETE")) {
			httpRequest = new HttpDelete(sb.toString());
		} else if (targetMethod.equalsIgnoreCase("PATCH")) {
			httpRequest = new HttpPatch(sb.toString());
			try {
				sendEntity = new StringEntity(payload,"UTF-8");
				sendEntity.setContentType("application/json");
				((HttpPatch)httpRequest).setEntity(sendEntity);
			} catch (Exception e) {
				IcmUtility.locError(400, "ERR0037", LogCriticality.error,"Exception in sendRequest (3): " + e.getMessage());
				throw new RuntimeException(e);
			}
		}
		
		httpRequest.setHeader("Accept","application/json");
		httpRequest.setHeader(HttpHeaders.AUTHORIZATION,authzHeader);
		try {
			retval = httpClient.execute(httpRequest);
			return retval;
		} catch (Exception e) {
			IcmUtility.locError(400,"ERR0037",LogCriticality.error,"Exception in sendRequest (4): " + e.getMessage());
			throw new RuntimeException(e);
		} 
		// No finally here -- we must return the response and let the caller close it and the client
	}
		
}
