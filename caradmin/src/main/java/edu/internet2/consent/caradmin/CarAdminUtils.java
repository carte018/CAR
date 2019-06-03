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
package edu.internet2.consent.caradmin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.arpsi.model.OrgInfoReleasePolicy;
import edu.internet2.consent.arpsi.model.OrgReturnedPolicy;
import edu.internet2.consent.arpsi.model.ReturnedPrecedenceObject;
import edu.internet2.consent.caradmin.AdminConfig;
import edu.internet2.consent.icm.model.IcmInfoReleasePolicy;
import edu.internet2.consent.icm.model.IcmReturnedPolicy;
import edu.internet2.consent.icm.model.ListOfReturnedPrecedenceObject;
import edu.internet2.consent.icm.model.UserReturnedPolicy;
import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.InternationalizedString;
import edu.internet2.consent.informed.model.LocaleString;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.RPIdentifier;
import edu.internet2.consent.informed.model.ReturnedInfoItemMetaInformation;
import edu.internet2.consent.informed.model.ReturnedInfoTypeList;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPOptionalInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRPRequiredInfoItemList;
import edu.internet2.consent.informed.model.ActivityStreamEntry;

public class CarAdminUtils {

	private static final Log LOG=LogFactory.getLog(CarAdminUtils.class);
	private static ResourceBundle locRB = ResourceBundle.getBundle("i18n.errors",new Locale("en")); // singleton for error processing, "en" default
	private static ResourceBundle locDB = ResourceBundle.getBundle("i18n.logs",new Locale("en"));   // singleton for logging debugs
	private static ResourceBundle locCB = ResourceBundle.getBundle("i18n.components",new Locale("en")); // default locale
	// Static utility routines
	
	public static AdminConfig init(HttpServletRequest request) {
		
		// Perform initialization -- get a config object and check authorization based on the user
		// Throw a CopsuInitializationException if any of this fails.  Return the config if not.
		
		AdminConfig config = null;
		// Get the default message catalog ResourceBundle.  We default to "en" since that's the most common language.
		// This should be a cached singleton in the instance using the .getBundle() method

		try {
			config = AdminConfig.getInstance();
		} catch (Exception c) {
			// ignore
		}
		
		String sl = null;
		if ((sl = config.getProperty("serverLanguage", false)) != null) {
				locRB = ResourceBundle.getBundle("i18n.errors",new Locale(sl));  // override if found
				locDB = ResourceBundle.getBundle("i18n.logs",new Locale(sl));
				locCB = ResourceBundle.getBundle("i18n.components", new Locale(sl));
		}
		
		String admins = null;
		if ((admins = config.getProperty("AdminEPPNs", false)) != null) {
			if (! admins.contains(request.getRemoteUser())) {
				return null;
			}
		} else {
			if (! request.getRemoteUser().equals("0042752") && ! request.getRemoteUser().equals("0374183") && ! request.getRemoteUser().equals("0277213") && ! request.getRemoteUser().equals("0427503")) {
				return null;
			}
		}
		return(config);
		
	}
	
	public static void locDebugErr(String message) {
		AdminConfig config = AdminConfig.getInstance();
		if (config.getProperty("caradmin.logging.debug", false) != null && config.getProperty("caramin.logging.debug", false).contentEquals("true"))
			LOG.error("ERROR ecode=" + message + ":" + locRB.getString(message));
	}
	public static void locDebugErr(String errcode, String... args) {
		// Return the Response object to use for na error return with substitution(s).
		AdminConfig config = AdminConfig.getInstance();
		if (config.getProperty("caradmin.logging.debug", false) != null && config.getProperty("caradmin.logging.debug", false).contentEquals("true")) {
			String raw = locRB.getString(errcode);
			if (raw != null && args != null && args.length > 0 && args[0] != null) {
				for (int i=0; i<args.length; i++) {
					if (args[i] != null) 
						raw = raw.replace("{"+i+"}", args[i]);
				}
			}
			LOG.error("ERROR ecode=" + errcode + ": " + raw);
		}
	}
	
	// Process criticality comparisons
	private static boolean isLog(LogCriticality crit) {
		// Determine log level and then return isLog(crit,level)
		AdminConfig config = AdminConfig.getInstance();
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
	public static void locError(String message,LogCriticality crit) {
		if (crit == null)
			crit = LogCriticality.error;
		if (isLog(crit))
			LOG.error(crit.toString().toUpperCase() + " ecode=" + message + ":" + locRB.getString(message));
	}
	public static void locError(String errcode, LogCriticality crit,String... args) {
		// Return the Response object to use for an error return.  With substitution.
		String raw=locRB.getString(errcode);
		if (raw != null && args != null && args.length > 0 && args[0] != null) {
			for (int i=0; i<args.length; i++) {
				if (args[i] != null) 
					raw = raw.replace("{"+i+"}", args[i]);
			}
		}
		if (crit == null)
			crit = LogCriticality.error;
		if (isLog(crit))
			LOG.error(crit.toString().toUpperCase() + " ecode=" + errcode + ": " + raw);
		
	}
	
	public static void locLog(String message,LogCriticality crit) {
		if (crit == null)
			crit = LogCriticality.error;
		if (isLog(crit))
			LOG.info(crit.toString().toUpperCase() + " ecode=" + message + ":" + locDB.getString(message));
	}
	
	public static void locLog(String errcode, LogCriticality crit, String... args) {
		String raw = locDB.getString(errcode);
		if (raw != null && args != null && args.length > 0 && args[0] != null) {
			for (int i=0; i<args.length; i++) {
				if (args[i] != null) 
					raw = raw.replace("{"+i+"}",  args[i]);
			}
		}
		if (crit == null)
			crit = LogCriticality.error;
		if (isLog(crit))
			LOG.info(crit.toString().toUpperCase() + " ecode=" + errcode + ": " + raw);
	}
	
	public static String getLocalComponent(String key) {
		return locCB.getString(key);
	}
	public static String getLocalComponent(String key, String...args) {
		String raw = locCB.getString(key);
		if (raw != null && args != null && args.length > 0 && args[0] != null) {
			for (int i =0; i< args.length; i++) {
				if (args[i] != null) 
					raw = raw.replace("{"+i+"}",  args[i]);
			}
		}
		return raw;
	}
	
	public static String getLocalError(String key) {
		return locRB.getString(key);
	}
	
	public static String getLocalError(String key, String... args) {
		String raw = locRB.getString(key);
		if (raw != null && args != null && args.length > 0 && args[0] != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i] != null) 
					raw = raw.replaceAll("\\{"+i+"\\}", args[i]);
			}
		}
		return raw;
	}
	
	public static String getOtherComponent(String key, String locale) {
		// return specific locale string unless locale not populated, then return default bundle string
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.components",new Locale(locale));
		if (bundle == null) {
			bundle = locCB;
		}
		return bundle.getString(key);
	}
	
	// Stub routines for retreiving lists of configurable pull-down entities for the UI
	// For now, just  hard-coded values.
	// One for support(ed|able) languages, one for support(ed|able) RH types,
	// one for support(ed|able) RP types, and one for support(ed|able) II types,
	// and also support(ed|able) user types.
	
	// TODO:  Factor these out and make them configurable
	public static ArrayList<String> getSupportedLanguages() {
			// Return list of supported languages (eventually from some datbase config)
			// for now hard-coded as english and spanish
			//
			ArrayList<String> retval = new ArrayList<String>();
			retval.add("en");
			retval.add("es");
			retval.add("de");
			return retval;
	}
	
	public static ArrayList<String> getSupportedRHIDTypes() {
		ArrayList<String> retval = new ArrayList<String>();
		retval.add("entityId");
		return retval;
	}
	
	public static ArrayList<String> getSupportedRPIDTypes() {
		ArrayList<String> retval = new ArrayList<String>();
		retval.add("entityId");
		retval.add("extractId");
		retval.add("requesterId");
		return retval;
	}
	
	public static ArrayList<String> getSupportedIITypes() {
			ArrayList<String> retval = new ArrayList<String>();
			retval.add("attribute");
			retval.add("operation");
			retval.add("database_field");
			return retval;
	}
	
	
	public static ArrayList<String> getSupportedUserTypes() {
			ArrayList<String> retval = new ArrayList<String>();
			retval.add("eduPersonPrincipalName");
			retval.add("duDukeID");
			return retval;
	}
	
	public static String buildAuthorizationHeader(AdminConfig config, String service) {
		String basicUser = config.getProperty("caradmin."+service+"User", true);
		String basicCred = config.getProperty("caradmin."+service+"Cred", true);
		String authString = basicUser + ":" + basicCred;
		byte [] encodedString = Base64.encodeBase64(authString.getBytes());
		String retval = "Basic " + new String(encodedString);
		return retval;
	}
	
	public static String buildAuthorizationHeader(AdminConfig config) {
		return buildAuthorizationHeader(config,"icm");
	}
	
	public static String idEscape(String value) {
		// Convert all embedded slashes to !s 
		return value.replaceAll("/", "!").replaceAll(" ", "%20").replaceAll("\\|", "%7C");
	}
	
	public static String idUnEscape(String value) {
		// Convert all embedded "!" to "/"
		return value.replaceAll("!", "/").replaceAll("%20"," ").replaceAll("%7C", "|");
	}
	
	public static String localize(InternationalizedString is, String loc) {
		String retval = null;
		AdminConfig config = AdminConfig.getInstance();
		String defloc = config.getProperty("caradmin.defaultlocale", true);
		String defval = null;
		if (loc != null && ! loc.equalsIgnoreCase("")) {
			// specific locale
			for (LocaleString ls : is.getLocales()) {
				if (ls.getLocale().equals(loc)) {
					retval = ls.getValue();
				}
				if (ls.getLocale().equals(defloc)) {
					defval = ls.getValue();
				}
			}
			// in case there's nothing for that locale, see if it can be stripped before falling back
			// to the default
			for (LocaleString ls : is.getLocales()) {
				String stripleft = ls.getLocale().replaceAll("-.*$", "");
				String stripright = loc.replaceAll("-.*$", "");
				if (stripleft.equals(stripright)) {
					retval = ls.getValue();
				}
			}
			if (retval == null && defloc != null) {
				retval = defval;
			}
		}
		return retval;
	}
	
	public static String localize(InternationalizedString is, String loc, boolean fallback) {
		String retval = null;
		AdminConfig config = AdminConfig.getInstance();
		String defloc = config.getProperty("caradmin.defaultlocale", true);
		String defval = null;
		if (loc != null && ! loc.equalsIgnoreCase("")) {
			// specific locale
			for (LocaleString ls : is.getLocales()) {
				if (ls.getLocale().equals(loc)) {
					retval = ls.getValue();
				}
				if (ls.getLocale().equals(defloc)) {
					defval = ls.getValue();
				}
			}
			// in case there's nothing for that locale, see if it can be stripped before falling back
			// to the default
			for (LocaleString ls : is.getLocales()) {
				String stripleft = ls.getLocale().replaceAll("-.*$", "");
				String stripright = loc.replaceAll("-.*$", "");
				if (stripleft.equals(stripright)) {
					retval = ls.getValue();
				}
			}
			if (retval == null && defloc != null) {
				if (fallback) 
					retval = defval;
				else
					retval = null;
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
				sendEntity = new StringEntity(payload);
				sendEntity.setContentType("application/json");
				((HttpPut)httpRequest).setEntity(sendEntity);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (targetMethod.equalsIgnoreCase("POST")) {
			httpRequest = new HttpPost(sb.toString());
			try {
				sendEntity = new StringEntity(payload);
				sendEntity.setContentType("application/json");
				((HttpPost)httpRequest).setEntity(sendEntity);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (targetMethod.equalsIgnoreCase("DELETE")) {
			httpRequest = new HttpDelete(sb.toString());
		} else if (targetMethod.equalsIgnoreCase("PATCH")) {
			httpRequest = new HttpPatch(sb.toString());
			try {
				sendEntity = new StringEntity(payload);
				sendEntity.setContentType("application/json");
				((HttpPatch)httpRequest).setEntity(sendEntity);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		httpRequest.setHeader("Accept","application/json");
		httpRequest.setHeader(HttpHeaders.AUTHORIZATION,authzHeader);
		try {
			retval = httpClient.execute(httpRequest);
			return retval;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		// No finally here -- we must return the response and let the caller close it and the client
	}
	
	public static int extractStatusCode(HttpResponse response) {
		return response.getStatusLine().getStatusCode();
	}
	
	public static String extractBody(HttpResponse response) {
		String retval = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
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
	
	public static void putRelyingPartyMetaInformation(ReturnedRPMetaInformation rpmi) {
		AdminConfig config = AdminConfig.getInstance();
		
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rpic/metainformation/");
		sb.append(rpmi.getRhidentifier().getRhtype());
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rpmi.getRhidentifier().getRhid()));
		sb.append("/");
		sb.append(rpmi.getRpidentifier().getRptype());
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rpmi.getRpidentifier().getRpid()));
		sb.append("/");  //trailing slash
		
		String json = null;
		
		try {
			json = rpmi.toJSON();
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0019",LogCriticality.error);
			throw new RuntimeException(e);
		}
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PUT", informedhost, informedport, sb.toString(), json, authzheader);		
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response); // Must consume response content, even when we don't care
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0010",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void putRHIIList(ReturnedRHInfoItemList r) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port",true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/informed/rhic/iilist/");
		sb.append(r.getRhidentifier().getRhtype());
		sb.append("/");
		sb.append(idEscape(r.getRhidentifier().getRhid()));
		
		String json = null;
		try {
			json = r.toJSON();
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0009",LogCriticality.error);
			throw new RuntimeException(e);
		}
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PUT", informedhost, informedport, sb.toString(), json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0010", LogCriticality.error, e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	public static ArrayList<ActivityStreamEntry> getActivityStreamList() {
		return getActivityStreamList(15,"admin");
	}
	public static ArrayList<ActivityStreamEntry> getActivityStreamList(String type) {
		return getActivityStreamList(15,type);
	}
	public static ArrayList<ActivityStreamEntry> getActivityStreamList(int count) {
		return getActivityStreamList(count,"admin");
	}
	
	public static ArrayList<ActivityStreamEntry> getActivityStreamList(int count, String type) {
		AdminConfig config = AdminConfig.getInstance();
		
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/informed/activitystream/"); 
		if (count != 0) {
			sb.append("?recent="+count);
			if (type != null) {
				sb.append("&type="+type);
			}
		} else if (type != null) {
			sb.append("?type="+type);
		}
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String rbody = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(),null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ArrayList<ActivityStreamEntry> aase = om.readValue(rbody, new TypeReference<List<ActivityStreamEntry>>(){});
			return aase;
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void postIcmInfoReleasePolicy(IcmInfoReleasePolicy iirp) {
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/icm/icm-info-release-policies/");
		
		String json = null;
		ObjectMapper om = new ObjectMapper();
		try {
			json = om.writeValueAsString(iirp);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0046",LogCriticality.error);
			throw new RuntimeException(x);
		}
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "POST", icmhost, icmport, sb.toString(), json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);			

		} catch (Exception x) {
			CarAdminUtils.locError("ERR0047",LogCriticality.error,x.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void putIcmInfoReleasePolicy(IcmInfoReleasePolicy oirp,String pid) {
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port",true);
		
		StringBuilder sb = new StringBuilder();
	
		sb.append("/consent/v1/icm/icm-info-release-policies/"+pid);
		
		String json = null;
		ObjectMapper om = new ObjectMapper();
		try {
			json = om.writeValueAsString(oirp);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0046",LogCriticality.error);
			throw new RuntimeException(x);
		}
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PUT", icmhost, icmport, sb.toString(), json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0047",LogCriticality.error,x.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void putOrgInfoReleasePolicy(OrgInfoReleasePolicy oirp,String pid) {
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname",true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		
		StringBuilder sb = new StringBuilder();
		sb.append("/consent/v1/icm/org-info-release-policies/"+pid);
		
		String json = null;
		ObjectMapper om = new ObjectMapper();
		try {
			json = om.writeValueAsString(oirp);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0046",LogCriticality.error);
			throw new RuntimeException(x);
		}
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PUT", icmhost, icmport, sb.toString(), json, authzheader);	
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0047",LogCriticality.error,x.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void postOrgInfoReleasePolicy(OrgInfoReleasePolicy oirp) {
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/icm/org-info-release-policies/");
		
		String json = null;
		ObjectMapper om = new ObjectMapper();
		try {
			json = om.writeValueAsString(oirp);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0046",LogCriticality.error);
			throw new RuntimeException(x);
		}
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "POST", icmhost, icmport, sb.toString(), json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0047",LogCriticality.error,x.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}	
	}
	
	public static void postActivityStreamEntry(ActivityStreamEntry e) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname",true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		StringBuilder sb = new StringBuilder();
		sb.append("/consent/v1/informed/activitystream/");
		
		String json = null;
		ObjectMapper om = new ObjectMapper();
		try {
			json = om.writeValueAsString(e);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0046",LogCriticality.error);
			throw new RuntimeException(x);
		}
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "POST", informedhost, informedport, sb.toString(), json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0047",LogCriticality.error,x.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	public static void postRPMetaInformation(ReturnedRPMetaInformation r) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rpic/metainformation/");
		sb.append(r.getRhidentifier().getRhtype());
		sb.append("/");
		sb.append(idEscape(r.getRhidentifier().getRhid()));
		sb.append("/");
		sb.append(r.getRpidentifier().getRptype());
		sb.append("/");
		sb.append(idEscape(r.getRpidentifier().getRpid()));
		
		String json = null;
		try {
			json = r.toJSON();
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0046",LogCriticality.error);
			throw new RuntimeException(e);
		}
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PUT", informedhost, informedport, sb.toString(), json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0047",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
		
	public static void putRHMetaInformation(ReturnedRHMetaInformation r) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		StringBuilder sb = new StringBuilder();		
		sb.append("/consent/v1/informed/rhic/metainformation/");
		sb.append(r.getRhidentifier().getRhtype());
		sb.append("/");
		sb.append(idEscape(r.getRhidentifier().getRhid()));
		
		// Construct the JSON payload
		String json = null;
		try {
			json = r.toJSON();
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0007",LogCriticality.error);
			// Cannot continue
			throw new RuntimeException(e);
		}
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PUT", informedhost, informedport, sb.toString(),json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0008",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		
	}
	
	public static edu.internet2.consent.arpsi.model.ListOfReturnedPrecedenceObject getOrgPrecedence(String rhtype, String rhid, String baseId) {
		// JSON representations of ARPSI and ICM ReturnedPrecedenceObject values are interchangeable
		// This returns the ICM version, based on the icm-policy-precedence endpoint
		
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname",true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/org-policy-precedence?resource-holder-type=");
		sb.append(rhtype);
		sb.append("&resource-holder=");
		sb.append(idEscape(rhid));
		sb.append("&policyIdList=");
		sb.append(baseId);
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(), null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ArrayList<ReturnedPrecedenceObject> arpo = om.readValue(rbody,  new TypeReference<List<ReturnedPrecedenceObject>>(){});
			edu.internet2.consent.arpsi.model.ListOfReturnedPrecedenceObject lrpo = new edu.internet2.consent.arpsi.model.ListOfReturnedPrecedenceObject();
			lrpo.setContained(arpo);
			return lrpo;
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void postIcmPolicyPrecedence(ReorderRequest rr) {
		// Take a JSON representation of an ICM metapolicy precedence operation and act on it
		
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/icm-policy-precedence");
				
		String json = null;
		try {
			json = rr.toJSON();
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0061",LogCriticality.error);
			throw new RuntimeException(e);
		}
		
		json = "[" + json + "]";   // upconvert to array
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		try {
			
			response = CarAdminUtils.sendRequest(httpClient, "PATCH", icmhost, icmport, sb.toString(), json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0047",LogCriticality.error,x.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		
	}
	
	public static void postOrgPolicyPrecedence(ReorderRequest rr) {
		// Take a JSON representation of an ICM metapolicy precedence operation and act on it
		
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("cardmin.icm.port",true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/org-policy-precedence");
				
		String json = null;
		try {
			json = rr.toJSON();
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0062",LogCriticality.error);
			throw new RuntimeException(e);
		}
		
		json = "=" + json + "]";  // upconvert to array
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PATCH", icmhost, icmport, sb.toString(),json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception x) {
			CarAdminUtils.locError("ERR0047",LogCriticality.error,x.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		
	}
	
	public static ListOfReturnedPrecedenceObject getIcmPrecedence(String rhtype, String rhid, String baseId) {
		// JSON representations of ARPSI and ICM ReturnedPrecedenceObject values are interchangeable
		// This returns the ICM version, based on the icm-policy-precedence endpoint
		
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/icm-policy-precedence?resource-holder-type=");
		sb.append(rhtype);
		sb.append("&resource-holder=");
		sb.append(idEscape(rhid));
		sb.append("&policyIdList=");
		sb.append(baseId);
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", icmhost,icmport, sb.toString(), null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ArrayList<edu.internet2.consent.icm.model.ReturnedPrecedenceObject>arpo = om.readValue(rbody, new TypeReference<List<edu.internet2.consent.icm.model.ReturnedPrecedenceObject>>(){});
			ListOfReturnedPrecedenceObject lrpo = new ListOfReturnedPrecedenceObject();
			lrpo.setContained(arpo);
			return lrpo;
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static ReturnedInfoTypeList getInfoTypes(RHIdentifier ri) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname",true);
		String informedport = config.getProperty("caradmin.informed.port",true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rhic/infotypes/");
		sb.append(ri.getRhtype());
		sb.append("/");
		sb.append(idEscape(ri.getRhid()));
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(),null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					//	ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ReturnedInfoTypeList ritl = om.readValue(rbody, ReturnedInfoTypeList.class);
			return ritl;
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}	
	}
	
	public static void putInfoTypes(ReturnedInfoTypeList ritl) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rhic/infotypes/");
		sb.append(ritl.getRhtype());
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(ritl.getRhvalue()));
		
		String json = null;
		try {
			json = ritl.toJSON();
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0052",LogCriticality.error);
			throw new RuntimeException(e);
		}
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PUT", informedhost, informedport, sb.toString(),json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0053",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void putRPRequiredInfoItemList(ReturnedRPRequiredInfoItemList rriil) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rpic/requirediilist/");
		sb.append(rriil.getRhidentifier().getRhtype());
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rriil.getRhidentifier().getRhid()));
		sb.append("/");
		sb.append(rriil.getRpidentifier().getRptype());
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rriil.getRpidentifier().getRpid()));
		
		// Construct Json payload
		String json = null;
		try {
			json = rriil.toJSON();
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0020",LogCriticality.error);
			// no continuing now
			throw new RuntimeException(e);
		}
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PUT", informedhost, informedport, sb.toString(),json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0021",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void putRPOptionalInfoItemList(ReturnedRPOptionalInfoItemList rriil) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rpic/optionaliilist/");
		sb.append(rriil.getRhidentifier().getRhtype());
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rriil.getRhidentifier().getRhid()));
		sb.append("/");
		sb.append(rriil.getRpidentifier().getRptype());
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rriil.getRpidentifier().getRpid()));
		
		
		// Construct Json payload
		String json = null;
		try {
			json = rriil.toJSON();
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0024",LogCriticality.error);
			// no continuing now
			throw new RuntimeException(e);
		}

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PUT", informedhost, informedport, sb.toString(),json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0025",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void putIIMetaInformation(ReturnedInfoItemMetaInformation riimi) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/informed/iiic/iimetainformation/"+riimi.getRhidentifier().getRhtype()+"/"+idEscape(riimi.getRhidentifier().getRhid())+"/"+riimi.getIiidentifier().getIitype()+"/"+idEscape(riimi.getIiidentifier().getIiid())+"/");

		// Construct Json payload
		String json = null;
		try {
			json = riimi.toJSON();
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0028",LogCriticality.error);
			// no continuing now
			throw new RuntimeException(e);
		}

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "PUT", informedhost, informedport, sb.toString(), json, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0029",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void archiveOrgPolicy(RHIdentifier rhi, String baseid) {
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradin.icm.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/icm/org-info-release-policies/"+baseid);
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "DELETE", icmhost, icmport, sb.toString(), null, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
		} catch (Exception e) {
			return;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}	
	}
	
	public static void archiveMetaPolicy(RHIdentifier rhi, String baseid) {
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/icm-info-release-policies/"+baseid);
			
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "DELETE", icmhost, icmport, sb.toString(), null, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return;
			}
		} catch (Exception e) {
			return;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}	
	}
	
	public static void archiveRPMetaInformation(RHIdentifier rhi, RPIdentifier rpi) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/informed/rpic/metainformation/"+rhi.getRhtype()+"/"+idEscape(rhi.getRhid())+"/"+rpi.getRptype()+"/"+idEscape(rpi.getRpid()));
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "DELETE", informedhost, informedport, sb.toString(), null, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return;
			}
		} catch (Exception e) {
			/*locError("ERR0006","Delete on " + metaUrl + " yielded Exception message " + e.getMessage());*/
			return;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}	
	}
	public static void archiveMetaInformation(RHIdentifier rhi) {
		// Call the delete endpoint for the RH specified
		// This marks the RH as "inactive" and removes it from sight (but not from the database)
		//
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/informed/rhic/metainformation/" + rhi.getRhtype() + "/" + idEscape(rhi.getRhid()) + "/");
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		try {
			response = CarAdminUtils.sendRequest(httpClient, "DELETE", informedhost, informedport, sb.toString(),null, authzheader);
			@SuppressWarnings("unused")
			String rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					//ignore
				}
				return;
			}
		} catch (Exception e) {
			locError("ERR0006",LogCriticality.error,"Exception message " + e.getMessage());
			return;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}	
	}

	public static ReturnedInfoItemMetaInformation getIIMetaInformation(RHIdentifier rhi, InfoItemIdentifier iii) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port",true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/iiic/iimetainformation/" + rhi.getRhtype()+"/"+idEscape(rhi.getRhid()) +"/");
		sb.append(iii.getIitype());
		sb.append("/");
		sb.append(iii.getIiid());
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// 	ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ReturnedInfoItemMetaInformation ri = om.readValue(rbody, ReturnedInfoItemMetaInformation.class);
			return ri;
		} catch (Exception e) {
			locError("ERR0006",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}	
	}
	

	public static ReturnedRHInfoItemList getIiList(RHIdentifier rhi) {
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port",true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/informed/rhic/iilist/"+rhi.getRhtype()+"/"+idEscape(rhi.getRhid()));
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					//ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ReturnedRHInfoItemList ri = om.readValue(rbody,  ReturnedRHInfoItemList.class);
			return ri;
		} catch (Exception e) {
			locError("ERR0006",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static ArrayList<IcmReturnedPolicy> getIcmPoliciesForRH(String rhtype, String rhid) {
		
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port",true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/icm/icm-info-release-policies");
		sb.append("?resource-holder-type=");
		sb.append(rhtype);
		sb.append("&resource-holder=");
		sb.append(rhid);

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(),null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					//ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			List<IcmReturnedPolicy> lr = om.readValue(rbody,  new TypeReference<List<IcmReturnedPolicy>>(){});
			if (lr != null ) {
				ArrayList<IcmReturnedPolicy> alr = new ArrayList<IcmReturnedPolicy>();
				alr.addAll(lr);
				return alr;
			} else {
				return null;
			}
		} catch (Exception e) {
			locError("ERR0045",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}

	}
	public static ArrayList<OrgReturnedPolicy> getArpsiPoliciesForRH(String rhtype, String rhid) {
		
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port",true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/org-info-release-policies");
		sb.append("?resource-holder-type=");
		sb.append(rhtype);
		sb.append("&resource-holder=");
		sb.append(rhid);

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(), null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			List<OrgReturnedPolicy> lr = om.readValue(rbody, new TypeReference<List<OrgReturnedPolicy>>(){});
			if (lr != null) {
				ArrayList<OrgReturnedPolicy> alr = new ArrayList<OrgReturnedPolicy>();
				alr.addAll(lr);
				return alr;
			} else {
				return null;
			}
		} catch (Exception e) {
			locError("ERR0042",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}

	}
	
	public static ArrayList<UserReturnedPolicy> getUserPoliciesForRH(String rhtype, String rhid) {
		
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/icm/user-info-release-policies");
		sb.append("?resource-holder-type=");
		sb.append(rhtype);
		sb.append("&resource-holder=");
		sb.append(rhid);
		sb.append("&user=allUsers&relying-party=allRPs");
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(), null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					//ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			List<UserReturnedPolicy> lr = om.readValue(rbody,  new TypeReference<List<UserReturnedPolicy>>(){});
			if (lr != null) {
				ArrayList<UserReturnedPolicy> alr = new ArrayList<UserReturnedPolicy>();
				alr.addAll(lr);
				return alr;
			} else {
				return null;
			}
		} catch (Exception e) {
			locError("ERR0039",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		
	}
	
	public static ArrayList<ReturnedRPMetaInformation> getAllRPsForRH(String rhtype, String rhid) {
		
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/informed/rpic/metainformation/");
		sb.append(rhtype);
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rhid));
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			List<ReturnedRPMetaInformation> lr = om.readValue(rbody, new TypeReference<List<ReturnedRPMetaInformation>>(){});
			if (lr != null) {
				ArrayList<ReturnedRPMetaInformation> alr = new ArrayList<ReturnedRPMetaInformation>();
				alr.addAll(lr);
				return alr;
			} else {
				return null;
			}
		} catch (Exception e) {
			locError("ERR0013",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	
	public static ArrayList<ReturnedRHMetaInformation> getAllDefinedResourceHolders() {
		// Get the configuration file
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port",true);
		
		// We take the existence (or not) of metainformation for an RH as proof of the existence of the RH (or not)
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rhic/metainformation");
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(),null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			List<ReturnedRHMetaInformation> lr = om.readValue(rbody, new TypeReference<List<ReturnedRHMetaInformation>>(){});
			if (lr != null) {
				ArrayList<ReturnedRHMetaInformation> alr = new ArrayList<ReturnedRHMetaInformation>();
				alr.addAll(lr);
				return alr;
			} else {
				return null;
			}
		} catch (Exception e) {
			locError("ERR0003",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		
	}
	
	public static ReturnedRHMetaInformation getResourceHolderMetaInformation(String rhtype, String rhid) {
		// Get the configuration file
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		// We take the existence (or not) of metainformation for an RH as proof of the existence of the RH (or not)
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rhic/metainformation/");
		sb.append(rhtype);
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rhid));
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(),null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
			}
			ObjectMapper om = new ObjectMapper();
			ReturnedRHMetaInformation lr = om.readValue(rbody, ReturnedRHMetaInformation.class);
			return lr;
		} catch (Exception e) {
			locError("ERR0003",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		
	}
	

	public static ReturnedRPMetaInformation getRelyingPartyMetaInformation(String rhtype, String rhid, String rptype, String rpid) {
		// Get the configuration file
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port",true);
		// We take the existence (or not) of metainformation for an RH as proof of the existence of the RH (or not)
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rpic/metainformation/");
		sb.append(rhtype);
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rhid));
		sb.append("/");
		sb.append(rptype);
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rpid));
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ReturnedRPMetaInformation lr = om.readValue(rbody, ReturnedRPMetaInformation.class);
			return lr;
		} catch (Exception e) {
			locError("ERR0003",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static ReturnedRPOptionalInfoItemList getRelyingPartyOptionalInfoItemList(String rhtype, String rhid, String rptype, String rpid) {
		// Get the configuration file
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		// We take the existence (or not) of metainformation for an RH as proof of the existence of the RH (or not)
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rpic/optionaliilist/");
		sb.append(rhtype);
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rhid));
		sb.append("/");
		sb.append(rptype);
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rpid));
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(),null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception e) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ReturnedRPOptionalInfoItemList lr = om.readValue(rbody,  ReturnedRPOptionalInfoItemList.class);
			return lr;
		} catch (Exception e) {
			locError("ERR0003",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static void updateRPDisplayName(String rhtype, String rhid, String rptype, String rpid, InternationalizedString newval) {
		
		// Get the current version
		ReturnedRPMetaInformation rpmi = CarAdminUtils.getRelyingPartyMetaInformation(rhtype, rhid, rptype, rpid);
		
		// update it
		
		rpmi.setDisplayname(newval);
		
		// PUT it back to the other side
		
		CarAdminUtils.putRelyingPartyMetaInformation(rpmi);
	}
	
	public static void updateRPDescription(String rhtype, String rhid, String rptype, String rpid, InternationalizedString newval) {
		
		// Get current version
		ReturnedRPMetaInformation rpmi = CarAdminUtils.getRelyingPartyMetaInformation(rhtype,  rhid,  rptype,  rpid);
		// Update
		rpmi.setDescription(newval);;
		CarAdminUtils.putRelyingPartyMetaInformation(rpmi);
	}
	public static void updateRPIconUrl(String rhtype, String rhid, String rptype, String rpid, String newval) {
		ReturnedRPMetaInformation rpmi = CarAdminUtils.getRelyingPartyMetaInformation(rhtype,  rhid,  rptype,  rpid);
		rpmi.setIconurl(newval);
		CarAdminUtils.putRelyingPartyMetaInformation(rpmi);
	}
	
	public static void updateRPProperties(ReturnedRPMetaInformation rpmi) {
		String rhtype = rpmi.getRhidentifier().getRhtype();
		String rhid = rpmi.getRhidentifier().getRhid();
		String rptype = rpmi.getRpidentifier().getRptype();
		String rpid = rpmi.getRpidentifier().getRpid();
		ReturnedRPMetaInformation curr = CarAdminUtils.getRelyingPartyMetaInformation(rhtype, CarAdminUtils.idEscape(rhid), rptype, CarAdminUtils.idEscape(rpid));
		curr.setRpproperties(rpmi.getRpproperties());
		CarAdminUtils.putRelyingPartyMetaInformation(curr);
	}
	
	public static void updateRequiredList(ReturnedRPRequiredInfoItemList rrpil) {
		String rhtype = rrpil.getRhidentifier().getRhtype();
		String rhid = rrpil.getRhidentifier().getRhid();
		String rptype = rrpil.getRpidentifier().getRptype();
		String rpid = rrpil.getRpidentifier().getRpid();
		ReturnedRPRequiredInfoItemList curr = CarAdminUtils.getRelyingPartyRequiredInfoItemList(rhtype, CarAdminUtils.idEscape(rhid), rptype, CarAdminUtils.idEscape(rpid));
		curr.setRequiredlist(rrpil.getRequiredlist());
		CarAdminUtils.putRPRequiredInfoItemList(curr);
	}
	
	public static void updateOptionalList(ReturnedRPOptionalInfoItemList rrpil) {
		String rhtype = rrpil.getRhidentifier().getRhtype();
		String rhid = rrpil.getRhidentifier().getRhid();
		String rptype = rrpil.getRpidentifier().getRptype();
		String rpid = rrpil.getRpidentifier().getRpid();
		ReturnedRPOptionalInfoItemList curr = CarAdminUtils.getRelyingPartyOptionalInfoItemList(rhtype, CarAdminUtils.idEscape(rhid), rptype, CarAdminUtils.idEscape(rpid));
		curr.setOptionallist(rrpil.getOptionallist());
		CarAdminUtils.putRPOptionalInfoItemList(curr);
	}
	
	public static void updateRPPrivacyUrl(String rhtype, String rhid, String rptype, String rpid, String newval) {
		ReturnedRPMetaInformation rpmi = CarAdminUtils.getRelyingPartyMetaInformation(rhtype,  rhid,  rptype,  rpid);
		rpmi.setPrivacyurl(newval);
		CarAdminUtils.putRelyingPartyMetaInformation(rpmi);
	}
	
	public static void updateDefaultShowAgain(String rhtype, String rhid, String rptype, String rpid, String newval) {
		ReturnedRPMetaInformation rpmi = CarAdminUtils.getRelyingPartyMetaInformation(rhtype,  rhid,  rptype,  rpid);
		rpmi.setDefaultshowagain(newval);
		CarAdminUtils.putRelyingPartyMetaInformation(rpmi);
	}
	
	public static ArrayList<IcmReturnedPolicy> getRHIcmInfoReleasePolicies(String rhtype,String rhid) {
		// Return an ArrayList populated with the OrgInfoReleasePolicies from the ARPSI for the RH we're passed
		// Returned values should be sorted in rank order, so that the caller can immediately walk the list
		// to determine "first matched" policy.
		//
		ArrayList<IcmReturnedPolicy> retval = new ArrayList<IcmReturnedPolicy>();
		
		AdminConfig config = AdminConfig.getInstance();
		
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/icm-info-release-policies?resource-holder-type=");
		sb.append(rhtype);
		sb.append("&resource-holder=");
		sb.append(rhid);
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(),null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			retval = om.readValue(rbody, new TypeReference<List<IcmReturnedPolicy>>() {});
			return retval;
		} catch (Exception e) {
			locError("ERR0036",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
}	
	
	public static IcmReturnedPolicy getIcmInfoReleasePolicyById(String id) {
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname", true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/icm/icm-info-release-policies/");
		sb.append(id);
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(),null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ArrayList<IcmReturnedPolicy> alr = om.readValue(rbody,  new TypeReference<List<IcmReturnedPolicy>>() {});
			if (alr == null || alr.isEmpty()) {
				return null;
			} else {
				return alr.get(0);
			}
		} catch (Exception e) {
			locError("ERR0036",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	public static OrgReturnedPolicy getOrgInfoReleasePolicyById(String id) {
		
		// Return the OrgReturnedPolicy (current version) with the specified ID if it exists, or 
		// return null if it does not.
		
		AdminConfig config = AdminConfig.getInstance();
		String icmhost = config.getProperty("caradmin.icm.hostname",true);
		String icmport = config.getProperty("caradmin.icm.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/org-info-release-policies/");
		sb.append(id);
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(),null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ArrayList<OrgReturnedPolicy>alr = om.readValue(rbody, new TypeReference<List<OrgReturnedPolicy>>() {});
			if (alr == null || alr.isEmpty()) {
				return null;
			} else {
				return alr.get(0);
			}	
		} catch (Exception e) {
			locError("ERR0036",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	public static ArrayList<OrgReturnedPolicy> getRHOrgInfoReleasePolicies(String rhtype,String rhid) {
			// Return an ArrayList populated with the OrgInfoReleasePolicies from the ARPSI for the RH we're passed
			// Returned values should be sorted in rank order, so that the caller can immediately walk the list
			// to determine "first matched" policy.
			//
			ArrayList<OrgReturnedPolicy> retval = new ArrayList<OrgReturnedPolicy>();
			
			AdminConfig config = AdminConfig.getInstance();
			String icmhost = config.getProperty("caradmin.icm.hostname",true);
			String icmport = config.getProperty("caradmin.icm.port", true);
			
			StringBuilder sb = new StringBuilder();
			sb.append("/consent/v1/icm/org-info-release-policies?resource-holder-type=");
			sb.append(rhtype);
			sb.append("&resource-holder=");
			sb.append(rhid);
						
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpResponse response = null;
			String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"icm");
			String rbody = null;
			try {
				response = CarAdminUtils.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(),null, authzheader);
				rbody = CarAdminUtils.extractBody(response);
				int status = CarAdminUtils.extractStatusCode(response);
				if (status >= 300) {
					try {
						EntityUtils.consumeQuietly(response.getEntity());
					} catch (Exception x) {
						// ignore
					}
					return null;
				}
				ObjectMapper om = new ObjectMapper();
				retval = om.readValue(rbody,  new TypeReference<List<OrgReturnedPolicy>>() {});
				return retval;
			} catch (Exception e) {
				locError("ERR0036",LogCriticality.error,"Exception message " + e.getMessage());
				return null;  // on error, just fail
			} finally {
				HttpClientUtils.closeQuietly(response);
				HttpClientUtils.closeQuietly(httpClient);
			}
	}
	
	public static ReturnedRPRequiredInfoItemList getRelyingPartyRequiredInfoItemList(String rhtype, String rhid, String rptype, String rpid) {
		// Get the configuration file
		AdminConfig config = AdminConfig.getInstance();
		String informedhost = config.getProperty("caradmin.informed.hostname", true);
		String informedport = config.getProperty("caradmin.informed.port", true);
		
		// We take the existence (or not) of metainformation for an RH as proof of the existence of the RH (or not)
		
		StringBuilder sb = new StringBuilder();
		sb.append("/consent/v1/informed/rpic/requirediilist/");
		sb.append(rhtype);
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rhid));
		sb.append("/");
		sb.append(rptype);
		sb.append("/");
		sb.append(CarAdminUtils.idEscape(rpid));
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarAdminUtils.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarAdminUtils.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(),null, authzheader);
			rbody = CarAdminUtils.extractBody(response);
			int status = CarAdminUtils.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ReturnedRPRequiredInfoItemList lr = om.readValue(rbody,  ReturnedRPRequiredInfoItemList.class);
			return lr;
		} catch (Exception e) {
			locError("ERR0003",LogCriticality.error,"Exception message " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
}
