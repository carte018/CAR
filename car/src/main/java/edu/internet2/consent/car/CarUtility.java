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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
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

import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.InternationalizedString;
import edu.internet2.consent.informed.model.LocaleString;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.RPIdentifier;
import edu.internet2.consent.informed.model.ReturnedInfoItemMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPOptionalInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRPRequiredInfoItemList;
import edu.internet2.consent.informed.model.ReturnedUserRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedValueMetaInformation;
import edu.internet2.consent.informed.model.UserIdentifier;
import edu.internet2.consent.car.CarConfig;
import edu.internet2.consent.car.auth.AuthenticationDriver;
import edu.internet2.consent.icm.model.IcmDecisionResponseObject;
import edu.internet2.consent.icm.model.ResourceHolderId;
import edu.internet2.consent.icm.model.UserId;
import edu.internet2.consent.icm.model.UserInfoReleasePolicy;
import edu.internet2.consent.icm.model.UserReturnedPolicy;

public class CarUtility {
	
	private static final Log LOG=LogFactory.getLog(CarUtility.class);
	private static ResourceBundle locRB = ResourceBundle.getBundle("i18n.errors",new Locale("en")); // singleton for error processing, "en" default
	private static ResourceBundle locDB = ResourceBundle.getBundle("i18n.logs",new Locale("en"));   // singleton for logging debugs
	private static ResourceBundle locCB = ResourceBundle.getBundle("i18n.components",new Locale("en")); // default locale
	
	public static CarConfig init(HttpServletRequest request) {
		
		// Perform initialization -- get a config object and check authorization based on the user
		// Throw a CopsuInitializationException if any of this fails.  Return the config if not.
		
		CarConfig config = null;
		// Get the default message catalog ResourceBundle.  We default to "en" since that's the most common language.
		// This should be a cached singleton in the instance using the .getBundle() method
		ResourceBundle defRB = ResourceBundle.getBundle("i18n.errors", new Locale("en"));
		try {
			config = CarConfig.getInstance();
		} catch (Exception c) {
			locError("ERR0001",LogCriticality.error);
			throw new RuntimeException(defRB.getString("ERR0001"));
		}
		
		String sl = null;
		if ((sl = config.getProperty("car.defaultLocale", false)) != null) {
				locRB = ResourceBundle.getBundle("i18n.errors",new Locale(sl));  // override if found
				locDB = ResourceBundle.getBundle("i18n.logs",new Locale(sl));
		}
		locCB = ResourceBundle.getBundle("i18n.components",new Locale(prefLang(request)));
		return(config);
		
	}
	
	public static boolean isAuthenticated(HttpServletRequest request, HttpHeaders headers, String entity, CarConfig config) {
		
		// Return true or false depending on whether the request is authenticated.
		//
		// For now certain externally-drivable operations (like flushing individual 
		// entries in the CAR informed content cache) can be performed by any 
		// authenticated user.
		//
		if (AuthenticationDriver.getAuthenticatedUser(request, headers, config) != null) {
			return true;
		}
		return false;
	}
	
	public static String prefLang(HttpServletRequest req) {
		CarConfig config = CarConfig.getInstance();
		String defloc = config.getProperty("car.defaultLocale", true);
		String retval = defloc;
		try {
			retval = req.getLocale().getLanguage();
		} catch (Exception e) {
			// ignore
		}
		return retval;
	}
	
	public static String localize(InternationalizedString is, String loc) {
		String retval = null;
		CarConfig config = CarConfig.getInstance();
		String defloc = config.getProperty("car.defaultLocale", true);
		String defval = null;
		if (loc != null && ! loc.equalsIgnoreCase("") && is != null) {
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
	
	// Special processing for debug messages (separate from normal logging)
	
	public static void locDebugErr(String message) {
		CarConfig config = CarConfig.getInstance();
		if (config.getProperty("car.logging.debug", false) != null && (config.getProperty("car.logging.debug", false).contentEquals("true") || config.getProperty("logLevel", false).contentEquals("true")))
			LOG.error("ERROR ecode=" + message + ":" + locRB.getString(message));
	}
	public static void locDebugErr(String errcode, String... args) {
		// Return the Response object to use for na error return with substitution(s).
		CarConfig config = CarConfig.getInstance();
		if (config.getProperty("car.logging.debug", false) != null && (config.getProperty("car.logging.debug", false).contentEquals("true") || config.getProperty("logLevel", false).contentEquals("true"))) {
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
		CarConfig config = CarConfig.getInstance();
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
	
	// Normal error logging - now with criticality
	
	public static void locError(String message, LogCriticality crit) {
		if (crit == null)
			crit = LogCriticality.error;
		if (isLog(crit))
			LOG.error(crit.toString().toUpperCase() + " ecode=" + message + ":" + locRB.getString(message));
	}
	public static void locError(String errcode, LogCriticality crit, String... args) {
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
	
	public static void locLog(String message, LogCriticality crit) {
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
			LOG.info(crit.toString().toUpperCase() + " ecode=" +errcode + ":" + raw);
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

	public static String buildAuthorizationHeader(CarConfig config, String component) {
		String basicUser = config.getProperty("car."+component+"User", true);
		String basicCred = config.getProperty("car."+component+"Cred", true);
		String authString = basicUser + ":" + basicCred;
		byte [] encodedString = Base64.encodeBase64(authString.getBytes());
		String retval = "Basic " + new String(encodedString);
		return retval;
	}

	// We mostly talk to the ICM, so make ICM authorization the default
	public static String buildAuthorizationHeader(CarConfig config) {
		return buildAuthorizationHeader(config,"icm");
	}
	
	// TODO:  Refactor for multi-IItype RHs
	public static String getInfoType(ResourceHolderId rhid) {
		// for now, just return statically
		// eventually, retrieve some data from informed content to comply
		return "attribute";
	}
	
	public static String idEscape(String value) {
		// Convert all embedded slashes to !s 
		return value.replaceAll("/", "!").replaceAll(" ", "%20").replaceAll("\\|", "%7C");
	}
	
	public static String idUnEscape(String value) {
		// Convert all embedded "!" to "/"
		return value.replaceAll("!", "/").replaceAll("%20"," ").replaceAll("%7C", "|");
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
	
	public static HttpResponse forwardRequest(HttpClient httpClient, String targetMethod, String targetServer, String targetPort, String targetURI, HttpServletRequest request, String entity) {

		String authzHeader = request.getHeader("Authorization");
		
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
		httpRequest.setHeader("Authorization",authzHeader);
		try {
			retval = httpClient.execute(httpRequest);
			return retval;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		// No finally here -- we must return the response and let the caller close it and the client
	}
	
	public static ReturnedValueMetaInformation getValueMetaInformation(String iiid, String iivalue, CarConfig config) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		ReturnedValueMetaInformation retval = getValueMetaInformation(iiid, iivalue, config, httpClient);
		// Idempotent, so safe here
		HttpClientUtils.closeQuietly(httpClient);
		return retval;
	}
	
	public static ReturnedValueMetaInformation getValueMetaInformation(String iiid, String iivalue, CarConfig config, HttpClient httpClient) {
				
		// Start by checking the cacher
		ValueMetaInformationCache vcache = ValueMetaInformationCache.getInstance();
		CachedValueMetaInformation cv = null;
		if (vcache.hasCachedValueMetaInformation(iiid,  iivalue)) {
			cv = vcache.getCachedValueMetaInformation(iiid, iivalue);
			Random rand = new Random();
			int n = rand.nextInt(10);
			if (System.currentTimeMillis() <= cv.getCacheTime() + (50+n) * 60 * 1000) {
				CarUtility.locDebugErr("ERR0811","valueMetaData");
				return cv.getData();
			}
		}
		// otherwise, we do what we always did...
		
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port",true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/informed/iiic/valuemetainformation/");
		sb.append(iiid);
		sb.append("/");
		sb.append(CarUtility.idEscape(iivalue));
		
		CarUtility.locDebugErr("ERR0072",sb.toString());

		HttpResponse response = null;
		try {
			String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
			response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			String rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			
			if (status >= 300) {
				if (status == 404) {
					// cache not found errors
					vcache.storeCachedValueMetaInformation(iiid,iivalue,null);
				}
				try {
					EntityUtils.consumeQuietly(response.getEntity()); 
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			
			ObjectMapper om = new ObjectMapper();
			ReturnedValueMetaInformation lr = om.readValue(rbody,  ReturnedValueMetaInformation.class);
			if (lr != null) {
				//add to the cache
				vcache.storeCachedValueMetaInformation(iiid, iivalue, lr);
			}
			return lr;
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		
	}
	
	public static ArrayList<InfoItemIdentifier> getRHIIList(RHIdentifier rhid, CarConfig config) {
		
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rhic/iilist/");
		sb.append(rhid.getRhtype());
		sb.append("/");
		sb.append(idEscape(rhid.getRhid()));
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarUtility.sendRequest(httpClient,"GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300) 
				return null;
			ObjectMapper om = new ObjectMapper();
			ReturnedRHInfoItemList ril = om.readValue(rbody, ReturnedRHInfoItemList.class);
			List<InfoItemIdentifier> lr = null;
			if (ril != null) 
				lr = ril.getInfoitemlist();
			if (lr != null)
				return (ArrayList<InfoItemIdentifier>) lr;
			else {
				CarUtility.locError("ERR1132", LogCriticality.info);
				return null;
			}			
		} catch (Exception e) {
			CarUtility.locError("ERR1133", LogCriticality.error, rbody);
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static ArrayList<ReturnedRHMetaInformation> getRHMetaInformation(CarConfig config) {
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
	
		sb.append("/consent/v1/informed/rhic/metainformation/");
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String rbody = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		try {
			response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300)
				return null;
			ObjectMapper om = new ObjectMapper();
			List<ReturnedRHMetaInformation> lr = om.readValue(rbody,  new TypeReference<List<ReturnedRHMetaInformation>>() {});
			if (lr != null)
				return (ArrayList<ReturnedRHMetaInformation>) lr;
			else
				return null;		
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static ReturnedInfoItemMetaInformation getInfoItemMetaInformation(String rhid, String iivalue, CarConfig config) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		ReturnedInfoItemMetaInformation retval = getInfoItemMetaInformation(rhid, iivalue, config, httpClient);
		HttpClientUtils.closeQuietly(httpClient);
		return retval;
	}
	
	public static ReturnedInfoItemMetaInformation getInfoItemMetaInformation(String rhid, String iiid, CarConfig config, HttpClient httpClient) {
		
		// Start by checking the cacher
		InfoItemMetaInformationCache icache = InfoItemMetaInformationCache.getInstance();
		CachedInfoItemMetaInformation ci = null;
		if (icache.hasCachedInfoItemMetaInformation(rhid,  iiid)) {
			ci = icache.getCachedInfoItemMetaInformation(rhid, iiid);
			Random rand = new Random();
			int n = rand.nextInt(10);
			if (System.currentTimeMillis() <= ci.getCacheTime() + (50+n) * 60 * 1000) {
				CarUtility.locDebugErr("ERR0811","infoItemMetaData");
				return ci.getData();
			}
		}
		// otherwise, we do what we always did...
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		// TODO: stop hardcoding attribute type and rh type and actually let that be dynamic all the way through.
		// for now we only work with RH's that are of type entityId and with info items of type "attribute"
		sb.append("/consent/v1/informed/iiic/iimetainformation/");
		sb.append("entityId/");
		sb.append(CarUtility.idEscape(rhid) + "/");
		sb.append("attribute/");
		sb.append(iiid);
				
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(),null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			
			if (status >= 300) {
				if (status == 404) 
					icache.storeCachedInfoItemMetaInformation(rhid, iiid, null);
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ReturnedInfoItemMetaInformation lr = om.readValue(rbody, ReturnedInfoItemMetaInformation.class);
		
			if (lr != null) {
				icache.storeCachedInfoItemMetaInformation(rhid, iiid, lr);
				return lr;
			} else {
				return null;  // on error, just fail
			}
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	public static void setShowAgain(String utype,String uname,String rpid,boolean value,CarConfig config) {
		String escaped = CarUtility.idEscape(rpid);
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		StringBuilder sb = new StringBuilder();
		sb.append("/consent/v1/informed/uric/urmetainformation/");
		sb.append("entityId/");  // TODO:  not always perhaps?
		sb.append(escaped);
		sb.append("/"+utype+"/"+uname);
		ReturnedUserRPMetaInformation u = new ReturnedUserRPMetaInformation();
		u.setLastinteracted(System.currentTimeMillis());
		RPIdentifier rpi = new RPIdentifier();
		rpi.setRptype("entityId"); // TODO:  not always perhaps?
		rpi.setRpid(rpid);
		u.setRpidentifier(rpi);
		UserIdentifier ui = new UserIdentifier();
		ui.setUsertype(utype);
		ui.setUserid(uname);
		u.setUseridentifier(ui);
		u.setShowagain(value);
		ObjectMapper mapper = new ObjectMapper();
		String ujson = null;
		try {
			ujson = mapper.writeValueAsString(u);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// Note for posterity
		CarUtility.locError("ERR1135",LogCriticality.info, value?"true":"false");
		// and perform the put
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String rbody = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		try {
			response = CarUtility.sendRequest(httpClient, "PUT", informedhost, informedport, sb.toString(), ujson, authzheader);
			rbody = CarUtility.extractBody(response);
		} catch (Exception e) {
			CarUtility.locError("ERR0081", LogCriticality.debug, "#3 - value was: " + rbody + "Exception strack trace: " + CarUtility.exceptionStacktraceToString(e));
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}

	public static ReturnedUserRPMetaInformation getUserRPMetaInformation(String rpid, String usertype, String uservalue, CarConfig config) {
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/uric/urmetainformation/");
		sb.append("entityId/");
		sb.append(CarUtility.idEscape(rpid) + "/");
		sb.append(usertype + "/");
		sb.append(uservalue);
		
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		
		try {
			response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300)
				return null;
			ObjectMapper om = new ObjectMapper();
			ReturnedUserRPMetaInformation lr = om.readValue(rbody, ReturnedUserRPMetaInformation.class);
			if (lr == null)
				locError("ERR1116", LogCriticality.info, "serialization returned null ReturnedUserRPMetaInformation");
			return lr;
		} catch (Exception e) {
			locError("ERR1116",LogCriticality.error, e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static ReturnedRPMetaInformation getRPMetaInformation(String iiid, String iivalue, CarConfig config) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		ReturnedRPMetaInformation retval = getRPMetaInformation(iiid, iivalue, config, httpClient);
		HttpClientUtils.closeQuietly(httpClient);
		return retval;
	}
	
	public static ReturnedRPMetaInformation getRPMetaInformation(String rhid,String rptype, String rpid, CarConfig config) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		ReturnedRPMetaInformation retval = getRPMetaInformation(rhid,rptype,rpid,config,httpClient);
		HttpClientUtils.closeQuietly(httpClient);
		return retval;
	}
	
	public static ArrayList<ReturnedRPMetaInformation> getRPsForRH(String rhtype,String rhid,CarConfig config) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		ArrayList<ReturnedRPMetaInformation> retval = getRPsForRH(rhtype,rhid,config,httpClient);
		HttpClientUtils.closeQuietly(httpClient);
		return retval;
	}
	public static ArrayList<ReturnedRPMetaInformation> getRPsForRH(String rhtype,String rhid,CarConfig config,HttpClient httpClient) {
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rpic/metainformation/");
		sb.append(rhtype);
		sb.append("/");
		sb.append(CarUtility.idEscape(rhid));
		
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		try {
			response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(),null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300) {
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			@SuppressWarnings("unchecked")
			ArrayList<ReturnedRPMetaInformation> alr = (ArrayList<ReturnedRPMetaInformation>) om.readValue(rbody,  new TypeReference<List<ReturnedRPMetaInformation>>(){});
			return alr;			
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static ReturnedRPMetaInformation getRPMetaInformation(String rhid,String rptype, String rpid,CarConfig config,HttpClient httpClient) {
		
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		
		// Start by checking the cacher
		RPMetaInformationCache icache = RPMetaInformationCache.getInstance();
		CachedRPMetaInformation ci = null;
		if (icache.hasCachedRPMetaInformation(rhid,  rptype, rpid)) {
			ci = icache.getCachedRPMetaInformation(rhid, rptype, rpid);
			Random rand = new Random();
			int n = rand.nextInt(10);
			if (System.currentTimeMillis() <= ci.getCacheTime() + (50+n) * 60 * 1000) {
				CarUtility.locDebugErr("ERR0811","infoItemMetaData");
				return ci.getData();
			}
		}
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/informed/rpic/metainformation/");
		// TODO:  For now, this is hard-coded as entityId -- need to make it sensitive to the request
		// somehow, probably by allowing requests that contain an additional identifier not present in
		// the current IDPv3 caller JSON to indicate the type of the RP.
		sb.append("entityId/");
		sb.append(CarUtility.idEscape(rhid) + "/");
		// TODO:  For now, this is hard-coded as entityId -- same caveat as above
		// This version allows passing in the type as well as the id
		//sb.append("entityId/");
		sb.append(rptype + "/");
		sb.append(CarUtility.idEscape(rpid));
				
		HttpResponse response = null;
		String rbody = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		try {
			response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300) {
				if (status == 404) 
					icache.storeCachedRPMetaInformation(rhid, rptype, rpid,null);
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					//ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ReturnedRPMetaInformation lr = om.readValue(rbody, ReturnedRPMetaInformation.class);
			if (lr != null) 
				icache.storeCachedRPMetaInformation(rhid, rptype, rpid, lr);
			return lr;
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static ReturnedRPMetaInformation getRPMetaInformation(String rhid,String rpid,CarConfig config,HttpClient httpClient) {
		
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		
		// Start by checking the cacher
		RPMetaInformationCache icache = RPMetaInformationCache.getInstance();
		CachedRPMetaInformation ci = null;
		if (icache.hasCachedRPMetaInformation(rhid,  rpid)) {
			ci = icache.getCachedRPMetaInformation(rhid, rpid);
			Random rand = new Random();
			int n = rand.nextInt(10);
			if (System.currentTimeMillis() <= ci.getCacheTime() + (50+n) * 60 * 1000) {
				CarUtility.locDebugErr("ERR0811","infoItemMetaData");
				return ci.getData();
			}
		}
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rpic/metainformation/");
		// TODO:  For now, this is hard-coded as entityId -- need to make it sensitive to the request
		// somehow, probably by allowing requests that contain an additional identifier not present in
		// the current IDPv3 caller JSON to indicate the type of the RP.
		sb.append("entityId/");
		sb.append(CarUtility.idEscape(rhid) + "/");
		// TODO:  For now, this is hard-coded as entityId -- same caveat as above
		sb.append("entityId/");
		sb.append(CarUtility.idEscape(rpid));
		
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		
		try {
			response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300) {
				if (status == 404)
					icache.storeCachedRPMetaInformation(rhid, rpid, null);
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				return null;
			}
			ObjectMapper om = new ObjectMapper();
			ReturnedRPMetaInformation lr = om.readValue(rbody, ReturnedRPMetaInformation.class);
			if (lr != null)
				icache.storeCachedRPMetaInformation(rhid, rpid, lr);
			return lr;
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static ReturnedRPRequiredInfoItemList getRPRequiredIIList(String rhid,String rptype, String rpid,CarConfig config) {
		
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
	
		sb.append("/consent/v1/informed/rpic/requirediilist/");
		// TODO:  For now, this is hard-coded as entityId -- need to make it sensitive to the request
		// somehow, probably by allowing requests that contain an additional identifier not present in
		// the current IDPv3 caller JSON to indicate the type of the RP.
		sb.append("entityId/");
		sb.append(CarUtility.idEscape(rhid) + "/");
		// TODO:  For now, this is hard-coded as entityId -- same caveat as above
		// sb.append("entityId/");
		sb.append(rptype + "/");
		sb.append(CarUtility.idEscape(rpid));
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		
		try {
			response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300) 
				return null;
			ObjectMapper om = new ObjectMapper();
			ReturnedRPRequiredInfoItemList lr = om.readValue(rbody, ReturnedRPRequiredInfoItemList.class);
			return lr;
		} catch (Exception e) {
			CarUtility.locError("ERR0074", LogCriticality.error, e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static ReturnedRPRequiredInfoItemList getRPRequiredIIList(String rhid,String rpid,CarConfig config) {
		
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rpic/requirediilist/");
		// TODO:  For now, this is hard-coded as entityId -- need to make it sensitive to the request
		// somehow, probably by allowing requests that contain an additional identifier not present in
		// the current IDPv3 caller JSON to indicate the type of the RP.
		sb.append("entityId/");
		sb.append(CarUtility.idEscape(rhid) + "/");
		// TODO:  For now, this is hard-coded as entityId -- same caveat as above
		sb.append("entityId/");
		sb.append(CarUtility.idEscape(rpid));
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String rbody = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		try {
			response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300)
				return null;
			ObjectMapper om = new ObjectMapper();
			ReturnedRPRequiredInfoItemList lr = om.readValue(rbody, ReturnedRPRequiredInfoItemList.class);
			return lr;
		} catch (Exception e) {
			CarUtility.locError("ERR0074", LogCriticality.error, e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	// POST wrapper to create a new COPSU policy (used by manual site add feature)
	public static String postCOPSUPolicy(String json, CarConfig config) {
		String retval = null;
		
		String icmhost = config.getProperty("car.icm.hostname", true);
		String icmport = config.getProperty("car.icm.port", true);
				
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/user-info-release-policies");
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String rbody = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"icm");
		
		try {
			response = CarUtility.sendRequest(httpClient, "POST", icmhost, icmport, sb.toString(), json, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300)
				return null;
			ObjectMapper om = new ObjectMapper();
			UserReturnedPolicy urirp = om.readValue(rbody, UserReturnedPolicy.class);
			retval = urirp.getPolicyMetaData().getPolicyId().getBaseId();
			return retval;
		} catch (Exception e) {
			// Error
			return null;
		}
	}
	
	// blind put of a policy -- we simply ignore errors, since we can't do anything useful with them in this context
	public static boolean putCOPSUPolicy(String baseId, UserInfoReleasePolicy policy, CarConfig config) {
		boolean retval = false;
		ObjectMapper om = new ObjectMapper();
		String jsonToPut = null;
		String icmhost = config.getProperty("car.icm.hostname", true);
		String icmport = config.getProperty("car.icm.port", true);
		
		try {
			jsonToPut = om.writeValueAsString(policy);
		} catch (Exception e) {
			throw new RuntimeException(e);  // rethrow
		}
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/user-info-release-policies/");
		sb.append(baseId);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String rbody = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"icm");
		
		try {
			response = CarUtility.sendRequest(httpClient, "PUT", icmhost, icmport, sb.toString(), jsonToPut, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status < 300) 
				retval = true;
		} catch (Exception e) {
			CarUtility.locError("ERR0081", LogCriticality.debug,  "#3 - value was: " + rbody + "Exception strack trace: " + CarUtility.exceptionStacktraceToString(e));
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		return retval;
	}
	
	public static ArrayList<edu.internet2.consent.icm.model.UserReturnedPolicy> getCOPSUPolicies(String user,String rh,CarConfig config) {
		ArrayList<UserReturnedPolicy> retval = new ArrayList<UserReturnedPolicy>();
		
		String icmhost = config.getProperty("car.icm.hostname",true);
		String icmport = config.getProperty("car.icm.port",true);
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/user-info-release-policies?user=");
		sb.append(user);
		sb.append("&resource-holder=");
		sb.append(rh);
		sb.append("&relying-party=allRPs");
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String rbody = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"icm");
		
		try {
			response = CarUtility.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300)
				return null;
			ObjectMapper om = new ObjectMapper();
			List<edu.internet2.consent.icm.model.UserReturnedPolicy> retlist = om.readValue(rbody,new TypeReference<List<edu.internet2.consent.icm.model.UserReturnedPolicy>>(){});
			if (retlist == null || retlist.isEmpty())
				return null;
			retval.addAll(retlist);
			return retval;
		} catch (Exception e) {
			CarUtility.locError("ERR0081", LogCriticality.debug, "#4 - value was: " + rbody + "Exception strack trace: " + CarUtility.exceptionStacktraceToString(e));
			return null;  // null on any failure
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}

	public static edu.internet2.consent.icm.model.UserReturnedPolicy getCOPSUPolicy(String baseId,CarConfig config) {
		// Get a COPSU policy by its identifier
		String icmhost = config.getProperty("car.icm.hostname",true);
		String icmport = config.getProperty("car.icm.port",true);
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/user-info-release-policies/");
		sb.append(baseId);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		
		try {
			response = CarUtility.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300)
				return null;
			ObjectMapper om = new ObjectMapper();
			List<UserReturnedPolicy> retlist = om.readValue(rbody, new TypeReference<List<edu.internet2.consent.icm.model.UserReturnedPolicy>>(){});
			if (retlist == null || retlist.isEmpty()) 
				return null;
			return retlist.get(0);			
		} catch (Exception e) {
			CarUtility.locError("ERR0081", LogCriticality.debug, "#4 - value was: " + rbody + "Exception strack trace: " + CarUtility.exceptionStacktraceToString(e));
			return null;  // null on any failure
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	public static edu.internet2.consent.icm.model.UserReturnedPolicy getCOPSUPolicy(String user,String rh,String rp,CarConfig config) {
		UserReturnedPolicy retval = null;
		
		String icmhost = config.getProperty("car.icm.hostname",true);
		String icmport = config.getProperty("car.icm.port",true);
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/user-info-release-policies?user=");
		sb.append(user);
		sb.append("&resource-holder=");
		sb.append(rh);
		sb.append("&relying-party=");
		sb.append(rp);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		
		try {
			response = CarUtility.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300) 
				return null;
			ObjectMapper om = new ObjectMapper();
			List<edu.internet2.consent.icm.model.UserReturnedPolicy>retlist = om.readValue(rbody, new TypeReference<List<edu.internet2.consent.icm.model.UserReturnedPolicy>>(){});
			if (retlist == null || retlist.isEmpty())
				return null;
			retval = retlist.get(0);
			return retval;
		} catch (Exception e) {
			CarUtility.locError("ERR0081", LogCriticality.debug, "#4 - value was: " + rbody + "Exception strack trace: " + CarUtility.exceptionStacktraceToString(e));
			return null;  // null on any failure
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	public static edu.internet2.consent.arpsi.model.DecisionResponseObject sendARPSIDecisionRequest(String jsonRequest, CarConfig config) {
		String arpsihost = config.getProperty("car.arpsi.hostname", true);
		String arpsiport = config.getProperty("car.arpsi.port", true);
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/arpsi/org-info-release-decision");
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"arpsi");
		String rbody = null;
		
		try {
			response = CarUtility.sendRequest(httpClient, "POST", arpsihost, arpsiport, sb.toString(), jsonRequest, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300) 
				return null;
			ObjectMapper om = new ObjectMapper();
			edu.internet2.consent.arpsi.model.DecisionResponseObject retval = new edu.internet2.consent.arpsi.model.DecisionResponseObject();
			retval = om.readValue(rbody, edu.internet2.consent.arpsi.model.DecisionResponseObject.class);
			return retval;
		} catch (Exception e) {
			CarUtility.locError("ERR0081", LogCriticality.debug, "#3 - value was: " + rbody + "Exception strack trace: " + CarUtility.exceptionStacktraceToString(e));
			return null;  // null on any failure
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
public static IcmDecisionResponseObject sendDecisionRequest(String jsonRequest, CarConfig config) {
	
	String icmhost = config.getProperty("car.icm.hostname", true);
	String icmport = config.getProperty("car.icm.port", true);

	StringBuilder sb = new StringBuilder();
	
	sb.append("/consent/v1/icm/info-release-decision");
	
	HttpClient httpClient = HttpClientBuilder.create().build();
	HttpResponse response = null;
	String authzheader = CarUtility.buildAuthorizationHeader(config,"icm");
	String rbody = null;
	
	try {
		response = CarUtility.sendRequest(httpClient, "POST", icmhost, icmport, sb.toString(), jsonRequest, authzheader);
		rbody = CarUtility.extractBody(response);
		int status = CarUtility.extractStatusCode(response);
		if (status >= 300)  {
			CarUtility.locError("ERR1134",LogCriticality.info, String.valueOf(status));
			return null;
		}
		ObjectMapper om = new ObjectMapper();
		IcmDecisionResponseObject retval = new IcmDecisionResponseObject();
		retval = om.readValue(rbody, IcmDecisionResponseObject.class);
		return retval;		
	} catch (Exception e) {
		CarUtility.locError("ERR0081", LogCriticality.debug, "#2 - value was: " + rbody + "Exception strack trace: " + CarUtility.exceptionStacktraceToString(e));
		return null;  // null on any failure
	} finally {
		HttpClientUtils.closeQuietly(response);
		HttpClientUtils.closeQuietly(httpClient);
	}
}
public static ReturnedRPOptionalInfoItemList getRPOptionalIIList(String rhid,String rptype, String rpid,CarConfig config) {
	
	String informedhost = config.getProperty("car.informed.hostname", true);
	String informedport = config.getProperty("car.informed.port", true);
	
	StringBuilder sb = new StringBuilder();

	sb.append("/consent/v1/informed/rpic/optionaliilist/");
	// TODO:  For now, this is hard-coded as entityId -- need to make it sensitive to the request
	// somehow, probably by allowing requests that contain an additional identifier not present in
	// the current IDPv3 caller JSON to indicate the type of the RP.
	sb.append("entityId/");
	sb.append(CarUtility.idEscape(rhid) + "/");
	// TODO:  For now, this is hard-coded as entityId -- same caveat as above
	sb.append(rptype + "/");
	sb.append(CarUtility.idEscape(rpid));
		
	HttpClient httpClient = HttpClientBuilder.create().build();
	HttpResponse response = null;
	String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
	String rbody = null;
	
	try {
		response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
		rbody = CarUtility.extractBody(response);
		int status = CarUtility.extractStatusCode(response);
		if (status >= 300)
			return null;
		ObjectMapper om = new ObjectMapper();
		ReturnedRPOptionalInfoItemList lr = om.readValue(rbody, ReturnedRPOptionalInfoItemList.class);
		return lr;				
	} catch (Exception e) {
		return null;  // on error, just fail
	} finally {
		HttpClientUtils.closeQuietly(response);
		HttpClientUtils.closeQuietly(httpClient);
	}
}
public static ReturnedRPOptionalInfoItemList getRPOptionalIIList(String rhid,String rpid,CarConfig config) {
		
		String informedhost = config.getProperty("car.informed.hostname", true);
		String informedport = config.getProperty("car.informed.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/informed/rpic/optionaliilist/");
		// TODO:  For now, this is hard-coded as entityId -- need to make it sensitive to the request
		// somehow, probably by allowing requests that contain an additional identifier not present in
		// the current IDPv3 caller JSON to indicate the type of the RP.
		sb.append("entityId/");
		sb.append(CarUtility.idEscape(rhid) + "/");
		// TODO:  For now, this is hard-coded as entityId -- same caveat as above
		sb.append("entityId/");
		sb.append(CarUtility.idEscape(rpid));
				
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"informed");
		String rbody = null;
		
		try {
			response = CarUtility.sendRequest(httpClient, "GET", informedhost, informedport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300) 
				return null;
			ObjectMapper om = new ObjectMapper();
			ReturnedRPOptionalInfoItemList lr = om.readValue(rbody, ReturnedRPOptionalInfoItemList.class);
			return lr;
		} catch (Exception e) {
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}

	public static UserReturnedPolicy getNewRPTemplate(UserId user, CarConfig config) {
		
		String icmhost = config.getProperty("car.icm.hostname", true);
		String icmport = config.getProperty("car.icm.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/icm/user-info-release-policies");
		sb.append("?user=");
		sb.append(user.getUserValue());
		sb.append("&resource-holder=newRPTemplate&relying-party=newRPTemplateValue");
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String authzheader = CarUtility.buildAuthorizationHeader(config,"icm");
		String rbody = null;
		
		try {
			response = CarUtility.sendRequest(httpClient, "GET", icmhost, icmport, sb.toString(), null, authzheader);
			rbody = CarUtility.extractBody(response);
			int status = CarUtility.extractStatusCode(response);
			if (status >= 300) 
				return null;
		} catch (Exception e) {
			return null;
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		
		ArrayList<edu.internet2.consent.icm.model.UserReturnedPolicy> responseList = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			responseList = mapper.readValue(rbody, new TypeReference<List<UserReturnedPolicy>>() {});
		} catch (Exception e) {
			return null;
		}
		
		if (responseList != null && ! responseList.isEmpty()) 
			return responseList.get(0);
		else
			return null;
	}

	public static ArrayList<String> subsetValueList(List<String> source, List<String> target) {
		// Given a list of strings and a list of regular expressions, return the subset of the 
		// list of strings that are spanned by the list of regular expressions.
		// Essentially, for every string in source, for every string in target, if the source matches
		// the target, add it to output.
		
		ArrayList<String> retval = new ArrayList<String>();
		if (source == null || source.isEmpty()) {
			// no source data?
			CarUtility.locError("ERR0080",LogCriticality.info);
			return retval;
		}
		if (target == null || target.isEmpty()) {
			// If we have nothing to filter with, return the empty list
			CarUtility.locError("ERR0076",LogCriticality.info);
			return(retval);
		} else {
			CarUtility.locError("ERR0079",LogCriticality.info,""+source.size(),""+target.size());
		}
		
		for (String value : source) {
			for (String regex : target) {
				if (value.matches(regex) && ! retval.contains(value)) {  // now, with duplicate value suppression
					retval.add(value);
					break;
				}
			}
		}
		
		return retval;
	}
	
	public static String exceptionStacktraceToString(Exception e)
	{
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
	    e.printStackTrace(ps);
	    ps.close();
	    return baos.toString();
	}
	
	public static String cryptoInterceptUrl(CarConfig config) {
		StringBuilder sb = new StringBuilder();
		sb.append("https://");
		sb.append(config.getProperty("car.car.hostname",  true));
		sb.append(":");
		sb.append(config.getProperty("car.car.port", true));
		sb.append("/car/cryptofilteranddecide");
		return sb.toString();
	}
	
	public static String interceptUrl(CarConfig config) {
		// construct our intercept URL
		StringBuilder sb = new StringBuilder();
		sb.append("https://");
		sb.append(config.getProperty("car.car.hostname", true));
		sb.append(":");
		sb.append(config.getProperty("car.car.port", true));
		sb.append("/car/filteranddecide");
		return sb.toString();
	}
}
