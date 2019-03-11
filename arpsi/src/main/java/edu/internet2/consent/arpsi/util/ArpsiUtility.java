package edu.internet2.consent.arpsi.util;

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

import edu.internet2.consent.arpsi.auth.AuthenticationDriver;
import edu.internet2.consent.arpsi.cfg.ArpsiConfig;
import edu.internet2.consent.arpsi.model.ErrorModel;

public class ArpsiUtility {
	
	private static final Log LOG=LogFactory.getLog(ArpsiUtility.class);
	private static ResourceBundle locRB = ResourceBundle.getBundle("i18n.errors",new Locale("en")); // singleton for error processing, "en" default
	private static ResourceBundle locDB = ResourceBundle.getBundle("i18n.logs",new Locale("en"));   // singleton for logging debugs
	
	// Determine if the header-authenticated user is authorized to use the ARPSI service at 
	// all.
	// Authorization may be via direct matching against one of two authorization lists in 
	// the configuration -- appUsers and appAdmins.
	// Since this is the ARPSI, we will typically see a single service user authorized here.
	//
	public static boolean isAuthorized(HttpServletRequest request, HttpHeaders headers, String entity, ArpsiConfig config) {
		// For the moment, ARPSI authorization is always granted
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
	
	public static boolean isAuthorized(String operation, HttpServletRequest request, HttpHeaders headers, String entity, ArpsiConfig config) {
		// For the moment, ARPSI authorization is always granted for specific operations to 
		// anyone authorized to use the interface at all -- all operations are equivalent 
		if (isAuthorized(request,headers,entity,config)) {
			return true;
		}

		return false;
	}
	
	public static ArpsiConfig init(String operation, HttpServletRequest request, HttpHeaders headers, String entity) {
		
		// Perform initialization -- get a config object and check authorization based on the user
		// Throw a CopsuInitializationException if any of this fails.  Return the config if not.
		
		ArpsiConfig config = null;
		// Get the default message catalog ResourceBundle.  We default to "en" since that's the most common language.
		// This should be a cached singleton in the instance using the .getBundle() method
		ResourceBundle defRB = ResourceBundle.getBundle("i18n.errors", new Locale("en"));
		try {
			config = ArpsiConfig.getInstance();
		} catch (Exception c) {
			locError(500,"ERR0001");
			throw new RuntimeException(defRB.getString("ERR0001"));
		}
		
		// We have a configuration object -- use it with the input request info to do authorization
		if (! isAuthorized(request,headers,entity,config)) {
			// no authorization to use the interface at all
			locError(500,"ERR0002");
			throw new RuntimeException(defRB.getString("ERR0002"));
		}
		
		if (! isAuthorized(operation,request,headers,entity,config)) {
			locError(500,"ERR0003");
			throw new RuntimeException(defRB.getString("ERR0003"));
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
	
	public static Response locError(int retcode, String errcode) {
		// return the Response object to use for an error return.  Along the way, write a log message.
		LOG.error("ERROR ecode=" + errcode + ": " + locRB.getString(errcode));
		return new ErrorModel(retcode,locRB.getString(errcode)).toResponse();
	}
	
	
	public static Response locError(int retcode, String errcode, String... args) {
		// Return the Response object to use for an error return.  With substitution.
		String raw=locRB.getString(errcode);
		if (raw != null && args != null && args.length > 0 && args[0] != null) {
			for (int i=0; i<args.length; i++) {
				if (args[i] != null) 
					raw = raw.replaceAll("\\{"+i+"\\}", args[i]);
			}
		}
		LOG.error("ERROR ecode=" + errcode + ": " + raw);
		return new ErrorModel(retcode,raw).toResponse();
	}
	
	public static void debugLog(String errcode) {
		LOG.debug("DEBUG ecode=" + errcode + " "+ locDB.getString(errcode));
	}
	
	public static void debugLog(String errcode, String...strings) {
		String ret = locDB.getString(errcode);
		if (ret != null && strings != null && strings.length > 0) {
			for (int i=0; i<strings.length; i++) {
				if (strings[i] != null) 
					ret = ret.replaceAll("\\{"+i+"\\}",strings[i]);
			}
		}
		LOG.debug("DEBUG ecode=" + errcode + " " + ret);
	}
	public static void infoLog(String errcode) {
		LOG.info("INFO ecode=" + errcode + " " + locDB.getString(errcode));
	}
	public static void infoLog(String errcode, String...strings) {
		String ret = locDB.getString(errcode);
		if (ret != null) {
			for (int i=0; i<strings.length; i++) {
				ret = ret.replaceAll("\\{"+i+"\\}", strings[i]);
			}
		}
		LOG.info("INFO ecode=" + errcode + " " + ret);
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
