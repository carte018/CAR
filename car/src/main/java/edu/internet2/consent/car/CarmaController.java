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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.arpsi.model.DecisionOnValues;
import edu.internet2.consent.arpsi.model.DecisionsForInfoStatement;
import edu.internet2.consent.arpsi.model.RelyingPartyProperty;
import edu.internet2.consent.arpsi.model.UserProperty;
import edu.internet2.consent.icm.model.AllOtherInfoId;
import edu.internet2.consent.icm.model.AllOtherInfoTypeConst;
import edu.internet2.consent.icm.model.AllOtherInfoValueConst;
import edu.internet2.consent.icm.model.AllOtherValuesConst;
import edu.internet2.consent.icm.model.IcmDecisionOnValues;
import edu.internet2.consent.icm.model.IcmDecisionResponseObject;
import edu.internet2.consent.icm.model.IcmDecisionsForInfoStatement;
import edu.internet2.consent.icm.model.InfoId;
import edu.internet2.consent.icm.model.PolicySourceEnum;
import edu.internet2.consent.icm.model.RelyingPartyId;
import edu.internet2.consent.icm.model.ResourceHolderId;
import edu.internet2.consent.icm.model.UserAllOtherInfoReleaseStatement;
import edu.internet2.consent.icm.model.UserDirectiveAllOtherValues;
import edu.internet2.consent.icm.model.UserDirectiveOnValues;
import edu.internet2.consent.icm.model.UserId;
import edu.internet2.consent.icm.model.UserInfoReleasePolicy;
import edu.internet2.consent.icm.model.UserInfoReleaseStatement;
import edu.internet2.consent.icm.model.UserReleaseDirective;
import edu.internet2.consent.icm.model.UserReturnedPolicy;
import edu.internet2.consent.icm.model.ValueObject;
import edu.internet2.consent.icm.model.WhileImAwayDirective;
import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.InfoItemValueList;
import edu.internet2.consent.informed.model.ReturnedInfoItemMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPOptionalInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRPProperty;
import edu.internet2.consent.informed.model.ReturnedRPRequiredInfoItemList;
import edu.internet2.consent.informed.model.ReturnedValueMetaInformation;

@Controller
public class CarmaController {

	private String sconvo;
	private int convo;
	
	// We may be called upon to post a large bound object when handling 
	// newRPTemplate updates.  Increase the max limit for autogrowing bound lists
	// based on a value possibly present in the config file
	
	@InitBinder
    public void initBinder(WebDataBinder dataBinder) {
		CarConfig config = CarConfig.getInstance();
		
		String limit = config.getProperty("carma.autogrow.limit",false);
		if (limit != null) {
			dataBinder.setAutoGrowCollectionLimit(Integer.parseInt(limit));
		} else {
			dataBinder.setAutoGrowCollectionLimit(2048);
		}
    }
	
	private String generateCSRFToken() {
		String foo = RandomStringUtils.random(32,true,true);
		String bar = Base64.encodeBase64URLSafeString(foo.getBytes());
		return bar;
	}
	
	
	// Populate a UserInformation object with the user info we have in the request for the logged in user
	private UserInformation getUserInfo(HttpServletRequest req) {
		UserInformation retval = new UserInformation();
		
		CarConfig config = CarUtility.init(req);

		// Get the REMOTE_USER value
		retval.addValue("remoteUser", req.getRemoteUser());
		
		// Determine the user identifier from the config
		String uid = config.getProperty("car.userIdentifier", true);
		retval.setUserType(uid);
		retval.setUserId((String) req.getAttribute(uid));
		retval.addValue(uid, (String) req.getAttribute(uid));
		
		// TODO:  Determine whether to include other attributes based on informed content attribute list
		return retval;
	}
	@RequestMapping(value="/carma/dumpheaders", method=RequestMethod.GET) 
	public ModelAndView dumpHeaders(HttpServletRequest req) {
		ModelAndView retval = new ModelAndView("errorPage");
		retval.addObject("message",getUserInfo(req).toString());
		return retval;
	}
	
	// Handler for deleting the session and starting over (cancel)
	//
	@RequestMapping(value="/carma/cancel",method=RequestMethod.GET)
	public ModelAndView canceler(HttpServletRequest req) {
		HttpSession sess = req.getSession(false);
		if (sess != null && req.getParameter("conversation") != null) {
			sconvo = req.getParameter("conversation");
			sess.removeAttribute(sconvo + ":" + "returntourl");
			sess.removeAttribute(sconvo + ":" + "csrftoken");
		}
		CarConfig config = CarConfig.getInstance();
		String cancelurl = config.getProperty("cancelURL", false);
		if (cancelurl != null && ! cancelurl.contentEquals("")) 
			return new ModelAndView("redirect:"+cancelurl);
		else
			return new ModelAndView("redirect:https://www.duke.edu");
	}
	// Handler for updates returned from the editpolicy page
	@SuppressWarnings("unchecked")
	//
	@RequestMapping(value="/carma/updatepolicy",method=RequestMethod.POST)
	public ModelAndView postUpdatePolicy(HttpServletRequest req) {
		CarConfig config = CarUtility.init(req);
	
	//	Enumeration<String> names = req.getParameterNames();
		
		// We must construct a user policy based on the existing user policy and the updates present in the 
		// inputs we receive.
		// For now, there are no values presented for whileImAway directives and no values presented for the 
		// AllOtherValues 
		// 
		// We operate for now only on the array of userinforeleasestatement objects in the policy
		
		String baseId = req.getParameter("baseId");
		if (baseId == null) {
			ModelAndView e = new ModelAndView("errorPage");
			e.addObject("message", "Missing baseId");
			return e;
		}
		
		// Get the COPSU policy we're operating on
		UserReturnedPolicy urp = CarUtility.getCOPSUPolicy(baseId, config);
		
		// Validate ownership
		String policyUser = null;
		try {
			policyUser = urp.getUserInfoReleasePolicy().getUserId().getUserValue();
		} catch (Exception e) {
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("message","Unable to retrieve user from policy: " + baseId);
			return err;
		}
		
		if (! policyUser.equalsIgnoreCase(req.getRemoteUser())) {
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("message","Logged in user: " + req.getRemoteUser() + " does not match owner of policy: " + policyUser);
			return err;
		}
		
		// Validate CSRF protection
		// First, get the conversation number
		sconvo = req.getParameter("conversation");
		if (sconvo == null) {
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("message","Your browser did not provide needed information.  Typically, this indicates a bug.");
			return err;
		}
		HttpSession sess = req.getSession(false);
		if (sess == null || sess.getAttribute(sconvo + ":" + "csrftoken") == null || ! sess.getAttribute(sconvo + ":" + "csrftoken").equals(req.getParameter("csrftoken"))) {
			// CSRF failure
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("message","CSRF violation.  Your session with the server may have expired (sessions expire after 10 minutes of inactivity).");
			return err;
		}
	//	UserInfoReleasePolicy uirp = urp.getUserInfoReleasePolicy();

	//	ArrayList<UserInfoReleaseStatement> auir = (ArrayList<UserInfoReleaseStatement>) uirp.getArrayOfInfoReleaseStatement();
		
		// Merge the settings we receive with the settings in the arraylist of info release statements.

		// The previous approach was:
		// This is a bit contorted, but it produces the desired semantic of updating what's provided and leaving 
		// what isn't alone.  Start from the policy UserInfoReleaseStatement array and for each ii involved, compute 
		// the list of permit, deny, askMe, and useAdvice values in the policy.  Likewise, compute the list of permit
		// deny, askMe, and useAdvice values in the input form.  In the ii collection, apply the requisite changes 
		// for each ii/value pair in the input form to the permit, deny, askMe, and useAdvice arrays in the policy.

		// The current approach presumes that since only operant policies are being presented to the user for 
		// modification (currently, a policy which pertains to a value of an attribute the user does not actually have
		// will not be processed for display in the interface) there is no value (in fact, some negative value) in 
		// attempting to preserve policy elements not referenced in the imnput.
		// As such, we take the input data as the full content of the policy and re-create it from whole cloth 
		// rather than attempting to merge in changes.
		

		HashMap<String,HashMap<String,String>> inputSet = new HashMap<String,HashMap<String,String>>();

		// Longhand version of loading the inputSet from the input data.
		for (String s : (Set<String>) req.getParameterMap().keySet()) {
			// for every parameter name...
			if (s.contains(":")) {
				String[] parts = s.split(":",2);
				String iiid = parts[0];
				String val = parts[1];
			
				if (! inputSet.containsKey(iiid)) {
					HashMap<String,String> hm = new HashMap<String,String>();
					inputSet.put(iiid,hm);
				}
				String[] vals = req.getParameterValues(s);
				inputSet.get(iiid).put(val, vals[0]); // there must always be exactly one at this point, right?
			}
		}
		
		// inputSet should now contain a map of the iiid -> {value, decision} for each of the input decisions.
		// Construct a new policy starting from the original policy and using the new values.
		
		UserReturnedPolicy origPolicy = CarUtility.getCOPSUPolicy(baseId, config);
		UserInfoReleasePolicy newPolicy = new UserInfoReleasePolicy();
		
		// pull in the non-variable tourist information
		
		newPolicy.setDescription(origPolicy.getUserInfoReleasePolicy().getDescription());
		newPolicy.setRelyingPartyId(origPolicy.getUserInfoReleasePolicy().getRelyingPartyId());
		newPolicy.setResourceHolderId(origPolicy.getUserInfoReleasePolicy().getResourceHolderId());
		
		// for now, pull in the components we don't allow to be edited in the self-service interface
		
		newPolicy.setUserAllOtherInfoReleaseStatement(origPolicy.getUserInfoReleasePolicy().getUserAllOtherInfoReleaseStatement());
		newPolicy.setUserId(origPolicy.getUserInfoReleasePolicy().getUserId());
		newPolicy.setWhileImAwayDirective(origPolicy.getUserInfoReleasePolicy().getWhileImAwayDirective());
		
		// Now newPolicy is ready to accept a fresh ArrayOfInfoReleaseStatement
		
		// In each case, for the moment, we make the DirectiveAllOtherValues "askMe", since we don't 
		// have instrumentation in the UI to support managing the "all other values" choice on an attribute-by-attribute
		// basis yet.
		
		ArrayList<UserInfoReleaseStatement> newAORS = new ArrayList<UserInfoReleaseStatement>();
		
		for (String iiid : inputSet.keySet()) {
			
			// for every iiid in the input set...
			UserInfoReleaseStatement uirs = new UserInfoReleaseStatement();

			ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(newPolicy.getResourceHolderId().getRHValue(), iiid, config);
			
			InfoId ii = new InfoId();
			ii.setInfoType(CarUtility.getInfoType(newPolicy.getResourceHolderId()));
			ii.setInfoValue(iiid);
			
			uirs.setInfoId(ii);
			
			uirs.setPersistence("onChange");  // static for now
		
			UserDirectiveAllOtherValues udaov = new UserDirectiveAllOtherValues();
			
			udaov.setAllOtherValues(AllOtherValuesConst.allOtherValues);
			if (riimi.getPolicytype().equalsIgnoreCase("PAO")) {
				// TODO: ugh
				String k = (String) inputSet.get(iiid).keySet().toArray()[0];
				udaov.setUserReleaseDirective(UserReleaseDirective.valueOf(inputSet.get(iiid).get(k)));
			} else {
				udaov.setUserReleaseDirective(UserReleaseDirective.askMe);
			}
						
			uirs.setUserDirectiveAllOtherValues(udaov);
			
			// And only populate directives on values if the policy type is not PAO
			
			if (! riimi.getPolicytype().equalsIgnoreCase("PAO")) {
			ArrayList<UserDirectiveOnValues> audov = new ArrayList<UserDirectiveOnValues>();
			
			// Populate the audov with the data in the input stream for this iiid, aggregating by directive
			HashMap<String,ArrayList<ValueObject>> pivot = new HashMap<String,ArrayList<ValueObject>>();
			for (String r : inputSet.get(iiid).keySet()) {
				String dec = inputSet.get(iiid).get(r);
				if (! pivot.containsKey(dec)) {
					ArrayList<ValueObject> als = new ArrayList<ValueObject>();
					pivot.put(dec, als);
				}
				ValueObject vo = new ValueObject();
				vo.setValue(r);
				pivot.get(dec).add(vo);
			}
			for (String d : pivot.keySet()) {
				UserDirectiveOnValues udov = new UserDirectiveOnValues();
				udov.setUserReleaseDirective(UserReleaseDirective.valueOf(d));
				udov.setValuesList(pivot.get(d));
				audov.add(udov);
			}
			uirs.setArrayOfDirectiveOnValues(audov);
			}
			newAORS.add(uirs);
		}
		
		newPolicy.setArrayOfInfoReleaseStatement(newAORS);
		
		// Now we add the tourist information (whileImAway and allOtherInfo)
		newPolicy.setWhileImAwayDirective(WhileImAwayDirective.valueOf(req.getParameter("whileImAway")));
		CarUtility.locError("ERR1118",LogCriticality.debug,"whileImAway",req.getParameter("whileImAway"));
		UserAllOtherInfoReleaseStatement uaoirs = new UserAllOtherInfoReleaseStatement();
		edu.internet2.consent.icm.model.AllOtherInfoId aoii = new edu.internet2.consent.icm.model.AllOtherInfoId();
		aoii.setAllOtherInfoType(AllOtherInfoTypeConst.allOtherInfoType);
		aoii.setAllOtherInfoValue(AllOtherInfoValueConst.allOtherInfoValue);
		uaoirs.setAllOtherInfoId(aoii);
		UserDirectiveAllOtherValues udaov = new UserDirectiveAllOtherValues();
		udaov.setAllOtherValues(AllOtherValuesConst.allOtherValues);
		udaov.setUserReleaseDirective(UserReleaseDirective.valueOf(req.getParameter("allOtherInfo")));
		CarUtility.locError("ERR1118",LogCriticality.debug,"allOtherInfo",req.getParameter("allOtherInfo"));
		uaoirs.setUserDirectiveAllOtherValues(udaov);
		newPolicy.setUserAllOtherInfoReleaseStatement(uaoirs);
				
		// Now we push the new policy upstream
		boolean succ = false;
		for (int i = 0; i < 5 && !succ; i++) {
			if (CarUtility.putCOPSUPolicy(baseId, newPolicy, config))
				succ = true;
		}
		
		// and return the browser to the original URL, already in progress
		return new ModelAndView("redirect:/carma/selfservice/sites?updated=true");
	}
	//
	// Handler for getting the policy editor document for individual policies.
	// Here, we're passed in the appropriate policy baseId.  We operate only on the current version of the policy.
	//
	// We inject simply the COPSU policy and the ARPSI decisions and let the velocity template take care of the rest.
	//
	@RequestMapping(value="/carma/editpolicy",method=RequestMethod.GET)
	public ModelAndView getEditPolicy(HttpServletRequest req) {
		//ModelAndView retval = new ModelAndView("editpolicyPage");
		ModelAndView retval = new ModelAndView("rp");
		CarConfig config = CarUtility.init(req);
		
		String preflang = CarUtility.prefLang(req);
		
		String baseId = req.getParameter("baseId");
		
		if (baseId == null || baseId.equals("")) {
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("message","Empty or missing policy ID is not allowed");
			return err;
		}
		
		// Get the authenticated user 
		//
		String user = req.getRemoteUser();
		String userType = config.getProperty("car.userIdentifier", true);
		
		// Get the locale to use
		String myLocale = config.getProperty("car.defaultLocale", true);
		if (req.getLocale() != null && req.getLocale().getLanguage() != null) {
			myLocale = req.getLocale().getLanguage();
		}
		

		// Get the requested policy
		//
		UserReturnedPolicy urp = CarUtility.getCOPSUPolicy(baseId,config);
		
		if (urp == null) {
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("message","Policy ID: " + baseId + " not found");
			return err;
		}

		// Check that the policy is owned by the user
		//
		String policyUser = null;
		try {
			policyUser = urp.getUserInfoReleasePolicy().getUserId().getUserValue();
		} catch (Exception e) {
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("message","Unable to retrieve user from policy: " + baseId);
			return err;
		}
		
		if (! policyUser.equalsIgnoreCase(user)) {
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("message","Logged in user: " + user + " does not match owner of policy: " + policyUser);
			return err;
		}
		
		// Establish session for CSRF protection
		HttpSession sess = req.getSession(true);
		String csrftoken = generateCSRFToken();
		// Establish a conversation numer
		if (req.getParameter("conversation") != null) {
			sconvo = (String) req.getParameter("conversation");
		} else {
			if (sess.getAttribute("maxconv") != null) {
				convo = Integer.parseInt((String) sess.getAttribute("maxconv")) + 1;
				sconvo = String.valueOf(convo);
				sess.setAttribute("maxconv", sconvo);
			} else {
				// start at 0
				convo = 0;
				sconvo = String.valueOf(convo);
				sess.setAttribute("maxconv", sconvo);
			}
		}
		sess.setAttribute(sconvo + ":" + "csrftoken", csrftoken);
		sess.setMaxInactiveInterval(600);  // 10 minutes max
		
		// Marshall injections for the page
		
		// CSRF protection
		retval.addObject("csrftoken",csrftoken); // to set csrftoken in the form
		retval.addObject("sconvo",sconvo);  // for formulating URLs
		
		retval.addObject("userid",user);  // inject the user's identifier
		if (req.getAttribute("displayName") != null) {
			retval.addObject("authuser",req.getAttribute("displayName"));
			retval.addObject("username",req.getAttribute("displayName"));  // inject username as ePPN if it exists
		} else if (req.getAttribute("eduPersonPrincipalName") != null) {
			retval.addObject("authuser",req.getAttribute("eduPersonPrincipalName"));
			retval.addObject("username",req.getAttribute("eduPersonPrincipalName"));
		}
		
		retval.addObject("userPolicy",urp);  // inject the user's policy
		
		// Get tourist information
		String rhid = urp.getUserInfoReleasePolicy().getResourceHolderId().getRHValue();
		String rhtype = urp.getUserInfoReleasePolicy().getResourceHolderId().getRHType();
		
		String rpid = urp.getUserInfoReleasePolicy().getRelyingPartyId().getRPvalue();
		String rptype = urp.getUserInfoReleasePolicy().getRelyingPartyId().getRPtype();
		
		// Get list of optional info items
		ReturnedRPOptionalInfoItemList rpoii = CarUtility.getRPOptionalIIList(rhid,rptype, rpid,config);
		
		// And list of required info items
		ReturnedRPRequiredInfoItemList rprii = CarUtility.getRPRequiredIIList(rhid, rptype, rpid, config);
		
		// And inject them
		retval.addObject("optionalIIList",rpoii);
		retval.addObject("requiredIIList",rprii);
		
		// Marshall the displayname values for the relevant attributes
		HashMap<String,String> adisp = new HashMap<String,String>();
		HashMap<String,String> policytype = new HashMap<String,String>();  // include policy type as we go
		
		if (rpoii != null && rpoii.getOptionallist() != null) {
			for (InfoItemValueList ovl : rpoii.getOptionallist()) {
				String id = ovl.getInfoitemidentifier().getIiid();
				ReturnedInfoItemMetaInformation iimi = CarUtility.getInfoItemMetaInformation(rhid, id, config);
				/*	if (iimi!= null && iimi.getDescription() != null && iimi.getDescription() != null && iimi.getDescription().getLocales() != null && !iimi.getDescription().getLocales().isEmpty()) {
				adisp.put(id,iimi.getDescription().getLocales().get(0).getValue());
				} else {*/  /* Now we use displayname, not description */
				if (iimi != null && ! iimi.isAsnd()) {
					if (iimi != null && iimi.getDisplayname() != null) {
						adisp.put(id, CarUtility.localize(iimi.getDisplayname(), preflang));
					} else {
						adisp.put(id,id);
					}
					/* Deprecated
					if (iimi != null && iimi.getDisplayname()!= null && iimi.getDisplayname().getLocales() != null && ! iimi.getDisplayname().getLocales().isEmpty()) {
						adisp.put(id,iimi.getDisplayname().getLocales().get(0).getValue());
					} else {
						adisp.put(id, id);
					}
					*/
					
					// 	Handle policytype
					policytype.put(id, iimi.getPolicytype());
				}
			}
		}
		if (rprii != null && rprii.getRequiredlist() != null) {
			for (InfoItemValueList rvl : rprii.getRequiredlist()) {
				String id = rvl.getInfoitemidentifier().getIiid();
				ReturnedInfoItemMetaInformation iimi = CarUtility.getInfoItemMetaInformation(rhid,  id,  config);
				/*if (iimi != null && iimi.getDescription() != null && iimi.getDescription() != null && iimi.getDescription().getLocales() != null && !iimi.getDescription().getLocales().isEmpty()) {
				adisp.put(id, iimi.getDescription().getLocales().get(0).getValue());
				} else {*/ /* Now we use displayname, not description */
				if (iimi != null && ! iimi.isAsnd()) {
					if (iimi != null && iimi.getDisplayname() != null) {
						adisp.put(id, CarUtility.localize(iimi.getDisplayname(),preflang));
					} else {
						adisp.put(id, id);
					}
					
					/* Deprecated
					if (iimi != null && iimi.getDisplayname()!=null && iimi.getDisplayname().getLocales()!= null && ! iimi.getDisplayname().getLocales().isEmpty()) {
						adisp.put(id, iimi.getDisplayname().getLocales().get(0).getValue());
					} else {
						adisp.put(id, id);
					}
					*/
					
					// 	Handle policytype
					policytype.put(id, iimi.getPolicytype());
				}
			}
		}
		// And inject it
		retval.addObject("iiDisplayNames",adisp);

		// And the policy type hash
		retval.addObject("policytype",policytype);
		
		// Marshall rp metainformation
		ReturnedRPMetaInformation rpmi = CarUtility.getRPMetaInformation(rhid, rptype, rpid, config);
		
		// And inject it
		retval.addObject("rpMetaInformation",rpmi);
		
		// And inject the localized displayName for convenience
		retval.addObject("localizedRPDisplayName",CarUtility.localize(rpmi.getDisplayname(),myLocale));
		// And description
		retval.addObject("localizedRPDescription",CarUtility.localize(rpmi.getDescription(),myLocale));
		
		// Now, acquire the institutional recommendations (urf)
		//
		// We have the user, the rh, and the rp types and values, the list of required and optional attribute ids, 
		// but no values (as yet).
		// Build up a decision request object to send 
		
		// Because there is no ICM exposure of the decision interface from the ARPSI, we must contact the ARPSI directly
		//
		edu.internet2.consent.arpsi.model.DecisionRequestObject dro = new edu.internet2.consent.arpsi.model.DecisionRequestObject();
		
		edu.internet2.consent.arpsi.model.UserId ui = new edu.internet2.consent.arpsi.model.UserId();
		ui.setUserType(userType);
		ui.setUserValue(user);
		dro.setUserId(ui);
		
		edu.internet2.consent.arpsi.model.RelyingPartyId rpi = new edu.internet2.consent.arpsi.model.RelyingPartyId();
		rpi.setRPtype(rptype);
		rpi.setRPvalue(rpid);
		dro.setRelyingPartyId(rpi);
		
		edu.internet2.consent.arpsi.model.ResourceHolderId rhi = new edu.internet2.consent.arpsi.model.ResourceHolderId();
		rhi.setRHType(rhtype);
		rhi.setRHValue(rhid);
		dro.setResourceHolderId(rhi);
		
		// Collect relying party properties and add them to the dro
		ArrayList<edu.internet2.consent.arpsi.model.RelyingPartyProperty> arpp = new ArrayList<edu.internet2.consent.arpsi.model.RelyingPartyProperty>();
		if (rpmi != null && rpmi.getRpproperties() != null) {
			for (ReturnedRPProperty rrpp : rpmi.getRpproperties()) {
				RelyingPartyProperty rpp = new RelyingPartyProperty();
				rpp.setRpPropName(rrpp.getRppropertyname());
				rpp.setRpPropValue(rrpp.getRppropertyvalue());
				arpp.add(rpp);
			}
		}
		dro.setArrayOfRelyingPartyProperty(arpp);
		
	//	ArrayList<InfoIdPlusValues> aiipv = new ArrayList<InfoIdPlusValues>();
		
		// Here, we have to merge info items from the optional list and info items from the required list
		// Possibility exists that a single info item has some required values and some optional values
		// Hence, we have to track which info items have been processed already and add to existing request 
		// if one has been built previously, or create a new request if one has not.
		HashMap<String,edu.internet2.consent.arpsi.model.InfoIdPlusValues> iiset = new HashMap<String,edu.internet2.consent.arpsi.model.InfoIdPlusValues>();
		if (rpoii != null && rpoii.getOptionallist() != null) {
			for (InfoItemValueList iivl : rpoii.getOptionallist()) {
				// for every infoitem and value slated for release to the RP...
				// Add a new infoidplusvalues if one does not already exist for the name, else
				// append to the one that's already there
				edu.internet2.consent.arpsi.model.InfoIdPlusValues ipv = null;
				ArrayList<String> vlist = null;
				if (iiset.containsKey(iivl.getInfoitemidentifier().getIiid())) {
					// already exists
					ipv = iiset.get(iivl.getInfoitemidentifier().getIiid());
					vlist = (ArrayList<String>) ipv.getInfoItemValues();
				} else {
					// create a new one
					ipv = new edu.internet2.consent.arpsi.model.InfoIdPlusValues();
					edu.internet2.consent.arpsi.model.InfoId ii = new edu.internet2.consent.arpsi.model.InfoId();
					ii.setInfoType(iivl.getInfoitemidentifier().getIitype());
					ii.setInfoValue(iivl.getInfoitemidentifier().getIiid());
					ipv.setInfoId(ii);
					vlist = new ArrayList<String>();
				}
			
				// Now we must add to vlist and write it back to the iiset map element
				vlist.addAll(iivl.getValuelist());
				ipv.setInfoItemValues(vlist);
				iiset.put(ipv.getInfoId().getInfoValue(),ipv);
			}
		}
		// And repeat for the required list
		if (rprii != null && rprii.getRequiredlist() != null) {
			for (InfoItemValueList iivl : rprii.getRequiredlist()) {
				edu.internet2.consent.arpsi.model.InfoIdPlusValues ipv = null;
				ArrayList<String> vlist = null;
				if (iiset.containsKey(iivl.getInfoitemidentifier().getIiid())) {
					ipv = iiset.get(iivl.getInfoitemidentifier().getIiid());
					vlist=(ArrayList<String>) ipv.getInfoItemValues();
				} else {
					ipv = new edu.internet2.consent.arpsi.model.InfoIdPlusValues();
					edu.internet2.consent.arpsi.model.InfoId ii = new edu.internet2.consent.arpsi.model.InfoId();
					ii.setInfoType(iivl.getInfoitemidentifier().getIitype());
					ii.setInfoValue(iivl.getInfoitemidentifier().getIiid());
					ipv.setInfoId(ii);
					vlist = new ArrayList<String>();
				}
				vlist.addAll(iivl.getValuelist());
				ipv.setInfoItemValues(vlist);
				iiset.put(ipv.getInfoId().getInfoValue(),ipv);
			}
		}

		// Now we have the relevant attribute descriptions (with associated value specifications) on a per attribute basis
		// for this relying party.  We need to construct the set of actual values in play that meet the release 
		// criteria provided and also send the entire set list as user properties.
		
		// Begin by getting the list of IIs that are supported by this RP's RH, and for those that have httpHeader
		// mappings specified, store the mappings.
		
		HashMap<String,String> httpMap = new HashMap<String,String>();
		
		edu.internet2.consent.informed.model.RHIdentifier rhidentifier = new edu.internet2.consent.informed.model.RHIdentifier();
		rhidentifier.setRhtype(rhi.getRHType());
		rhidentifier.setRhid(rhi.getRHValue());
		ArrayList<InfoItemIdentifier> rhiis = CarUtility.getRHIIList(rhidentifier, config);
		
		if (rhiis != null) {
			for (InfoItemIdentifier iiid : rhiis) {
				// Get the iimetainformation
				ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhidentifier.getRhid(), iiid.getIiid(), config);
				if (riimi != null) {
					if (riimi.getHttpHeader() != null) {
						httpMap.put(iiid.getIiid(), riimi.getHttpHeader());
					}
				} else {
					CarUtility.locError("ERR1130",LogCriticality.info,iiid.getIiid() + " under " + rhidentifier.getRhid());
				}
				
			}
		} else {
			CarUtility.locError("ERR1131",LogCriticality.info,rhidentifier.getRhtype() + "," + rhidentifier.getRhid());
		}
		
		// Then construct the set of values
		
		HashMap<String,ArrayList<String>> ivalmap = new HashMap<String,ArrayList<String>>();
		for (String iiname : iiset.keySet()) {
			// for every attribute this RP cares about
			ArrayList<String> l = new ArrayList<String>();
			String listofivals = (String) req.getAttribute(iiname);
			if (listofivals != null) {
				for (String val : listofivals.split(";")) {
					l.add(val);
				}
				ivalmap.put(iiname, l);
			} else {
				if (httpMap.containsKey(iiname)) {
					String listofivals2 = (String) req.getAttribute(httpMap.get(iiname));
					if (listofivals2 != null) {
						for (String val : listofivals2.split(";")) {
							l.add(val);
						}
						ivalmap.put(iiname, l);
					}
				} else {
					l.add("not available");
					ivalmap.put(iiname,  l);
				}
			}
		}
		
		retval.addObject("ivalmap",ivalmap);

		//Populate user properties
		ArrayList<UserProperty> aup = new ArrayList<UserProperty>();
		ArrayList<String> aupadded = new ArrayList<String>();
		
		for (String i : ivalmap.keySet()) {
			for (String v : ivalmap.get(i)) {
				UserProperty up = new UserProperty();
				up.setUserPropName(i);
				up.setUserPropValue(v);
				aup.add(up);
			}
			aupadded.add(i);				
		}
		
		if (rhiis != null) {
			for (InfoItemIdentifier iii : rhiis) {
				String ii = iii.getIiid();
				if (! aupadded.contains(ii)) {
					// Not already in the aup -- see if we can add it
					ArrayList<String> l = new ArrayList<String>();
					String listofivals = (String) req.getAttribute(ii);
					if (listofivals == null) {
						if (httpMap.get(ii) != null) {
							listofivals = (String) req.getAttribute(httpMap.get(ii));
						}
					}
					if (listofivals != null) {
						for (String val : listofivals.split(";")) {
							l.add(val);
						}
					}
					if (! l.isEmpty()) {
						for (String v : l) {
							UserProperty up = new UserProperty();
							up.setUserPropName(ii);
							up.setUserPropValue(v);
							aup.add(up);
						}
					}
				}
			}
		}
		dro.setArrayOfUserProperty(aup);
		
		// And populate the attribute request list
		ArrayList<edu.internet2.consent.arpsi.model.InfoIdPlusValues> apv = new ArrayList<edu.internet2.consent.arpsi.model.InfoIdPlusValues>();

		for (String i : ivalmap.keySet()) {
			edu.internet2.consent.arpsi.model.InfoIdPlusValues iipv = new edu.internet2.consent.arpsi.model.InfoIdPlusValues();
			iipv.setInfoId(iiset.get(i).getInfoId());
			iipv.setInfoItemValues(new ArrayList<String>());
			for (String v : ivalmap.get(i)) {
				// for each value of each attribute the user has
				// Check the optional/required lists to see if the value matches anything
				boolean matters = false;
				outer3: for (InfoItemValueList subl : rpoii.getOptionallist()) {
					if (subl.getInfoitemidentifier().getIiid().equals(i)) {
						for (String reg : subl.getValuelist()) {
							if (v.matches(reg)) {
								matters=true;
								CarUtility.locError("ERR0810",LogCriticality.debug,v,i,reg,"optional");
								break outer3;
							}
						}
					}
				}
				if (! matters && rprii != null && rprii.getRequiredlist() != null) {
					outer4: for (InfoItemValueList subl : rprii.getRequiredlist()) {
						if (subl.getInfoitemidentifier().getIiid().equals(i)) {
							for (String reg : subl.getValuelist()) {
								if (v.matches(reg)) {
									matters=true;
									CarUtility.locError("ERR0810",LogCriticality.debug,v,i,reg,"required");
									break outer4;
								}
							}
						}
					}
				}
				if (matters) {
					iipv.getInfoItemValues().add(v);
				}
			}
			if (! iipv.getInfoItemValues().isEmpty()) {
				apv.add(iipv);
			}
		}
		
		dro.setArrayOfInfoIdsPlusValues(apv);
		String jsonObject = null;
		try {
			ObjectMapper om = new ObjectMapper();
			jsonObject = om.writeValueAsString(dro);
		} catch (Exception e) {
			// ignore exception here -- we'll push the fail up
		}
		edu.internet2.consent.arpsi.model.DecisionResponseObject respo = CarUtility.sendARPSIDecisionRequest(jsonObject, config);		
		retval.addObject("arpsiDecision",respo);
		
		//
		// Now we need to retrieve the relevant ICM policy in order to determine what's going to be released
		// without user consent (by contract).
		//
		// Rather than retrieve policies here and perform the sort, we simply replay the ARPSI request to the ICM 
		// and rely on the decision sourcing information to make the determination (releases marked as "PERMIT" with 
		// ARPSI policies as definitive are mandatory releases).
		//
		// TODO: this should probably become a constructor for the icm DRO but for now, we'll just do it inline here 
		edu.internet2.consent.icm.model.DecisionRequestObject idro = new edu.internet2.consent.icm.model.DecisionRequestObject();
		
		edu.internet2.consent.icm.model.UserId iui = new UserId();
		edu.internet2.consent.icm.model.ResourceHolderId irhi = new ResourceHolderId();
		edu.internet2.consent.icm.model.RelyingPartyId irpi = new RelyingPartyId();
		ArrayList<edu.internet2.consent.icm.model.UserProperty> iaup = new ArrayList<edu.internet2.consent.icm.model.UserProperty>();
		ArrayList<edu.internet2.consent.icm.model.RelyingPartyProperty> iarp = new ArrayList<edu.internet2.consent.icm.model.RelyingPartyProperty>();
		ArrayList<edu.internet2.consent.icm.model.InfoIdPlusValues>iaipv = new ArrayList<edu.internet2.consent.icm.model.InfoIdPlusValues>();
		
		iui.setUserType(ui.getUserType());
		iui.setUserValue(ui.getUserValue());
		idro.setUserId(iui);
		irhi.setRHType(rhi.getRHType());
		irhi.setRHValue(rhi.getRHValue());
		idro.setResourceHolderId(irhi);
		irpi.setRPtype(rpi.getRPtype());
		irpi.setRPvalue(rpi.getRPvalue());
		idro.setRelyingPartyId(irpi);
		for (edu.internet2.consent.arpsi.model.UserProperty u : dro.getArrayOfUserProperty()) {
			edu.internet2.consent.icm.model.UserProperty iu = new edu.internet2.consent.icm.model.UserProperty();
			iu.setUserPropName(u.getUserPropName());
			iu.setUserPropValue(u.getUserPropValue());
			iaup.add(iu);
		}
		idro.setArrayofUserProperty(iaup);
		for (edu.internet2.consent.arpsi.model.RelyingPartyProperty r : dro.getArrayOfRelyingPartyProperty()) {
			edu.internet2.consent.icm.model.RelyingPartyProperty ir = new edu.internet2.consent.icm.model.RelyingPartyProperty();
			ir.setRpPropName(r.getRpPropName());
			ir.setRpPropValue(r.getRpPropValue());
			iarp.add(ir);
		}
		idro.setArrayOfRelyingPartyProperty(iarp);
		for (edu.internet2.consent.arpsi.model.InfoIdPlusValues i : dro.getArrayOfInfoIdsPlusValues()) {
			edu.internet2.consent.icm.model.InfoIdPlusValues ii = new edu.internet2.consent.icm.model.InfoIdPlusValues();
			edu.internet2.consent.icm.model.InfoId iii = new edu.internet2.consent.icm.model.InfoId();
			iii.setInfoType(i.getInfoId().getInfoType());
			iii.setInfoValue(i.getInfoId().getInfoValue());
			ii.setInfoId(iii);
			ii.setInfoItemValues((ArrayList<String>) i.getInfoItemValues());
			iaipv.add(ii);
		}
		idro.setArrayOfInfoIdsPlusValues(iaipv);
		
		// And make the ICM request
		String ijson = null;
		try {
			ObjectMapper om = new ObjectMapper();
			ijson = om.writeValueAsString(idro);
		} catch (Exception e) {
			// ignore and throw up the line
		}
		IcmDecisionResponseObject idrespo = CarUtility.sendDecisionRequest(ijson,config);
		
		// Previously, we simply injected this set of decisions into the page;  now
		// we compute a set of values to pass in instead
		//retval.addObject("idrespo",idrespo);
		boolean hasMandatory = false;
		HashMap<String,ArrayList<InjectedInfoItem>> mandatory = new HashMap<String,ArrayList<InjectedInfoItem>>();
		
		for (IcmDecisionsForInfoStatement ids : idrespo.getArrayOfInfoDecisionStatement()) {
			ArrayList<InjectedInfoItem> il = new ArrayList<InjectedInfoItem>();
			for (IcmDecisionOnValues dov : ids.getArrayOfDecisionOnValues()) {
				if (dov.getAugmentedPolicyId().getPolicySource().equals(PolicySourceEnum.ARPSI) && dov.getReleaseDecision().equals(UserReleaseDirective.permit)) {
					hasMandatory=true;
					ArrayList<String> vseen = new ArrayList<String>();
					for (String v : dov.getReturnedValuesList()) {
						if (! vseen.contains(v)) {
							vseen.add(v);
							InjectedInfoItem j = new InjectedInfoItem();
							j.setDisplayName(adisp.get(ids.getInfoId().getInfoValue()));
							if (j.getDisplayName() == null) {
								j.setDisplayName(ids.getInfoId().getInfoValue());  // in case no displayname is set
							}
							j.setValue(v);
							ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid, ids.getInfoId().getInfoValue(), config);
							ReturnedValueMetaInformation rvmi = null;
							if (riimi != null && riimi.getPresentationtype() != null && riimi.getPresentationtype().equalsIgnoreCase("ENCODED"))
								rvmi = CarUtility.getValueMetaInformation(ids.getInfoId().getInfoValue(),v, config);
							if (rvmi != null) {
								j.setValueDisplayName(rvmi.getDisplayname());
							} else {
								j.setValueDisplayName(v);
							}
							j.setPolicyDirective("permit");
							il.add(j);
						}
						//mandatory.put(ids.getInfoId().getInfoValue(),j);
					}
					mandatory.put(ids.getInfoId().getInfoValue(),il);
				}
			}
		}
		retval.addObject("hasMandatory",hasMandatory);
		retval.addObject("mandatory",mandatory);
		
		// Add the logout URL to the page
		retval.addObject("logouturl",config.getProperty("car.carma.logouturl", false));
		
		// We need to now construct a hashmap from attributes to injected infoitems and inject that for the UI
		//
		
		HashMap<String,String> dispvals = new HashMap<String,String>();
		
		TreeMap<String,InjectedInfoItem> injectedDecisions = new TreeMap<String,InjectedInfoItem>();
		for (edu.internet2.consent.arpsi.model.InfoIdPlusValues iipv : apv) {
			String ii = iipv.getInfoId().getInfoValue();
			ArrayList<String> dispAdded = new ArrayList<String>();
			for (String v : iipv.getInfoItemValues()) {
				CarUtility.locError("ERR1134",LogCriticality.debug,"value processing: " + v + " for " + ii);
				// for every ii/value pair
				InjectedInfoItem j = new InjectedInfoItem();
				j.setDisplayName(adisp.get(ii));
				j.setValue(v);
				ReturnedValueMetaInformation rvmi = null;
				ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid, ii, config);
				if (riimi != null && riimi.getPresentationtype() != null && riimi.getPresentationtype().equalsIgnoreCase("ENCODED")) 
					rvmi = CarUtility.getValueMetaInformation(ii, v, config);
				if (rvmi != null) {
					j.setValueDisplayName(rvmi.getDisplayname());
					if (policytype.get(ii) != null && policytype.get(ii).equalsIgnoreCase("PAO")) {
						if (dispvals.containsKey(ii) && ! dispAdded.contains(rvmi.getDisplayname())) {
							dispvals.put(ii, dispvals.get(ii).concat("," + rvmi.getDisplayname()));
							dispAdded.add(rvmi.getDisplayname());
						} else if (!dispAdded.contains(rvmi.getDisplayname())) {
							dispvals.put(ii, rvmi.getDisplayname());
							dispAdded.add(rvmi.getDisplayname());
						} 
					}
				} else {
					j.setValueDisplayName(v);
					if (policytype.get(ii) != null && policytype.get(ii).equalsIgnoreCase("PAO")) {
						if (dispvals.containsKey(ii) && ! dispAdded.contains(v)) {
							dispvals.put(ii, dispvals.get(ii).concat("," + v));
							dispAdded.add(v);
						} else if (!dispAdded.contains(v)){
							dispvals.put(ii,v);
							dispAdded.add(v);
						}
					}
				}

				String policyDirective = null;
				if (urp.getUserInfoReleasePolicy() != null && urp.getUserInfoReleasePolicy().getArrayOfInfoReleaseStatement() != null) {
				outer: for (UserInfoReleaseStatement x : urp.getUserInfoReleasePolicy().getArrayOfInfoReleaseStatement()) {
					if (x.getInfoId().getInfoValue().equals(ii)) {
						for (UserDirectiveOnValues y : x.getArrayOfDirectiveOnValues()) {
							for (ValueObject vm : y.getValueObjectList()) {
								if (v.matches(vm.getValue())) {
									policyDirective=y.getUserReleaseDirective().toString();
									break outer;
								}
							}
						}
						if (policyDirective == null) {
							// found the attribute, but not the value
							// use the all other values policy
							policyDirective = x.getUserDirectiveAllOtherValues().getUserReleaseDirective().toString();
							break outer;
						}
					}
				}
				}
				if (policyDirective == null) {
					// no explicit policy, use all other attribute policy
					policyDirective = urp.getUserInfoReleasePolicy().getUserAllOtherInfoReleaseStatement().getUserDirectiveAllOtherValues().getUserReleaseDirective().toString();
				}
				j.setPolicyDirective(policyDirective);
				String recommended = null;
				if (respo.getArrayOfInfoDecisionStatement() != null) {
				outer2: for (DecisionsForInfoStatement z : respo.getArrayOfInfoDecisionStatement()) {
					if (z.getInfoId().getInfoValue().equals(ii)) {
						for (DecisionOnValues d : z.getArrayOfDecisionOnValues()) {
							if (d.getReturnedValuesList().contains(v)) {
								recommended = d.getReleaseDecision().toString();
								break outer2;
							}
						}
						if (recommended == null) {
							// found the attribute but not the value
							// use the all other values policy if present
							if (z.getDecisionOnAllOtherValues() != null && z.getDecisionOnAllOtherValues().getReleaseDecision() != null) {
								recommended = z.getDecisionOnAllOtherValues().getReleaseDecision().toString();
								break outer2;
							}
						}
					}
				}
				}
				if (recommended == null) {
					// no explicit policy, use all other attribute policy
					// This should never happen, but if it does, default to the safe option - "DENY"
					recommended = "deny";
				}
				j.setRecommendedDirective(recommended);
				String reason = null;
				for ( InfoItemValueList rim : rpoii.getOptionallist()) {
					if (rim.getInfoitemidentifier().getIiid().equals(ii)) {
						for (String vm : rim.getValuelist()) {
							if (v.matches(vm)) {
								if (rim.getReason() != null) {
									reason = CarUtility.localize(rim.getReason(),preflang);
								}
								/* Deprecated
								if (rim.getReason() != null && rim.getReason().getLocales() != null && ! rim.getReason().getLocales().isEmpty()) {
									reason = rim.getReason().getLocales().get(0).getValue();
								} 
								*/
								break;
							}
						}
					}
				}
				if (reason == null) {
					for (InfoItemValueList rrm : rprii.getRequiredlist()) {
						if (rrm.getInfoitemidentifier().getIiid().equals(ii)) {
							for (String vm : rrm.getValuelist()) {
								if (v.matches(vm)) {
									if (rrm.getReason() != null) {
										reason = CarUtility.localize(rrm.getReason(),preflang);
									}
									
									/* Deprecated
									if (rrm.getReason() != null && rrm.getReason().getLocales() != null && ! rrm.getReason().getLocales().isEmpty()) {
										reason = rrm.getReason().getLocales().get(0).getValue();
									}
									*/
									break;
								}
							}
						}
					}
				}
				if (reason == null) {
					j.setReason("");
				} else {
					j.setReason(reason);
				}
				// Here, we now check before adding to the injectedDecisions map whether the attr/value pair are already
				// going to be released by institutional policy.  If there's institutional policy indicating the release, 
				// we suppress the display in the self service interface to avoid confusion (as the user's decision will
				// have no effect).  
				boolean ok=true;
				if (hasMandatory && mandatory.containsKey(ii)) {
					// check the injections
					for (InjectedInfoItem k : mandatory.get(ii)) {
						if (k.getValue().equals(j.getValue())) {
							ok=false;
						}
					}
				}
				if (ok && (riimi == null || ! riimi.isAsnd())) {
					injectedDecisions.put(ii+":"+v, j);
				}
			}
		}
		
		retval.addObject("injectedDecisions",injectedDecisions);
		retval.addObject("dispvals",dispvals);
		
		// HashMap<String,Integer> counters = new HashMap<String,Integer>();
		
		// Determine the relevant dates and inject them as well
		String upVersion = urp.getPolicyMetaData().getPolicyId().getVersion();
	//	String upbaseId = urp.getPolicyMetaData().getPolicyId().getBaseId();
		Date updateDate = new Date(urp.getPolicyMetaData().getCreateTime());
		DateFormat df = new SimpleDateFormat("MM-dd-yy hh:mm:ss a");
		retval.addObject("updateDate",df.format(updateDate));
		retval.addObject("version",upVersion);
		retval.addObject("page-title","Manage policies");
		retval.addObject("baseId",baseId);
		
		// Bulk component injections
		
		retval.addObject("manage_heading",CarUtility.getLocalComponent("manage_heading"));
		retval.addObject("requested_heading",CarUtility.getLocalComponent("requested_heading"));
		retval.addObject("required_heading",CarUtility.getLocalComponent("required_heading"));
		retval.addObject("cancel_label",CarUtility.getLocalComponent("cancel_label"));
		retval.addObject("no_optional",CarUtility.getLocalComponent("no_optional"));
		retval.addObject("required_description",CarUtility.getLocalComponent("required_description"));
		retval.addObject("requested_description",CarUtility.getLocalComponent("requested_description"));
		retval.addObject("all_other_prefix",CarUtility.getLocalComponent("all_other_prefix"));
		retval.addObject("all_other_suffix",CarUtility.getLocalComponent("all_other_suffix"));
		retval.addObject("while_away_prefix",CarUtility.getLocalComponent("while_away_prefix"));
		retval.addObject("while_away_suffix",CarUtility.getLocalComponent("while_away_suffix"));
		retval.addObject("no_required",CarUtility.getLocalComponent("no_required"));
		retval.addObject("history",CarUtility.getLocalComponent("history"));
		retval.addObject("updated_label",CarUtility.getLocalComponent("updated_label"));
		retval.addObject("policy_version",CarUtility.getLocalComponent("policy_version"));
		retval.addObject("privacy_policy_label",CarUtility.getLocalComponent("privacy_policy_label"));
		retval.addObject("short_institution",CarUtility.getLocalComponent("short_institution"));
		retval.addObject("recommends",CarUtility.getLocalComponent("recommends"));
		retval.addObject("current_value",CarUtility.getLocalComponent("current_value"));
		retval.addObject("current_choice",CarUtility.getLocalComponent("current_choice"));
		retval.addObject("additional_heading",CarUtility.getLocalComponent("additional_heading"));
		retval.addObject("while_away_heading",CarUtility.getLocalComponent("while_away_heading"));
		retval.addObject("all_other_heading",CarUtility.getLocalComponent("all_other_heading"));
		retval.addObject("any_values",CarUtility.getLocalComponent("any_values"));
		retval.addObject("permit_description",CarUtility.getLocalComponent("permit_description"));
		retval.addObject("deny_description",CarUtility.getLocalComponent("deny_description"));
		retval.addObject("askme_description",CarUtility.getLocalComponent("askme_description"));
		retval.addObject("userec_prefix",CarUtility.getLocalComponent("userec_prefix"));
		retval.addObject("userec_suffix",CarUtility.getLocalComponent("userec_suffix"));
		retval.addObject("save_label",CarUtility.getLocalComponent("save_label"));
		retval.addObject("item",CarUtility.getLocalComponent("item"));
		retval.addObject("no_privacy",CarUtility.getLocalComponent("no_privacy"));
		retval.addObject("edit_label",CarUtility.getLocalComponent("edit_label"));

		return retval;

	}
	
	//
	// Handle GET of the default selfservice page, which is now the HomePage tab
	//
	
	@RequestMapping(value="/carma/selfservice", method=RequestMethod.GET) 
	public ModelAndView getSelfServiceHome(HttpServletRequest req) {
		
		ModelAndView retval = new ModelAndView("selfserviceHome");
		CarConfig config = CarUtility.init(req);
		
	//	String user = req.getRemoteUser();
		
		retval.addObject("activetab","homepage");
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("logouturl",config.getProperty("car.carma.logouturl", false));

		return retval;
		

	}
	
	// 
	// Handler for carma/selfservice/addsite - manually specify a policy for an RP that
	// has never been the target of a decision request from its RH (eg., before the user 
	// ever tries to log in, or in the case of a backchannel RH like the batch IDM web service 
	// at Duke, where no user interaction is ever possible.
	//
	// We have a GET handler and a POST handler -- GET for the initial form, and POST for
	// the selected request. 
	// 
	// POST to handle the response
	@RequestMapping(value="/carma/selfservice/addsite", method=RequestMethod.POST)
	public ModelAndView postAddSite(HttpServletRequest req) {
		
	//	ModelAndView retval = null;
		
		// Validate CSRF protection
		// First, get the conversation number
		sconvo = req.getParameter("conversation");
		if (sconvo == null) {
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("message","Your browser did not provide needed information.  Typically, this indicates a bug.");
			return err;
		}
		HttpSession sess = req.getSession(false);
		if (sess == null || sess.getAttribute(sconvo + ":" + "csrftoken") == null || ! sess.getAttribute(sconvo + ":" + "csrftoken").equals(req.getParameter("csrftoken"))) {
			// CSRF failure
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("message","CSRF violation.  Your session with the server may have expired (sessions expire after 10 minutes of inactivity).");
			return err;
		}
		
		CarConfig config = CarUtility.init(req);
		
		// Marshall the info from the form
		String rhtype = req.getParameter("add_rhtype");
		String rhid = req.getParameter("add_rhid");
		String rptype = req.getParameter("add_rptype");
		String rpid = req.getParameter("add_rpid");
		String usertype = req.getParameter("add_usertype");
		String userid = req.getParameter("add_userid");
		
		// Start by POSTing a nil default policy to instantiate something to edit.
		// Capture the response and parse out the policy number from it
		
		UserInfoReleasePolicy u = new UserInfoReleasePolicy();
		u.setDescription("Default policy for " + rpid);
		ResourceHolderId rhi = new ResourceHolderId();
		rhi.setRHType(rhtype);
		rhi.setRHValue(rhid);
		u.setResourceHolderId(rhi);
		RelyingPartyId rpi = new RelyingPartyId();
		rpi.setRPtype(rptype);
		rpi.setRPvalue(rpid);
		u.setRelyingPartyId(rpi);
		UserId uid = new UserId();
		uid.setUserType(usertype);
		uid.setUserValue(userid);
		u.setUserId(uid);
		u.setWhileImAwayDirective(WhileImAwayDirective.valueOf("deny"));
		u.setArrayOfInfoReleaseStatement(null);
		UserAllOtherInfoReleaseStatement uairs = new UserAllOtherInfoReleaseStatement();
		AllOtherInfoId aoii = new AllOtherInfoId();
		aoii.setAllOtherInfoType(AllOtherInfoTypeConst.allOtherInfoType);
		aoii.setAllOtherInfoValue(AllOtherInfoValueConst.allOtherInfoValue);
		uairs.setAllOtherInfoId(aoii);
		UserDirectiveAllOtherValues udaov = new UserDirectiveAllOtherValues();
		udaov.setAllOtherValues(AllOtherValuesConst.allOtherValues);
		udaov.setUserReleaseDirective(UserReleaseDirective.askMe);
		uairs.setUserDirectiveAllOtherValues(udaov);
		u.setUserAllOtherInfoReleaseStatement(uairs);
		String json = null;
		try {
			//json = u.toJSON();
			ObjectMapper om = new ObjectMapper();
			json = om.writeValueAsString(u);
			
			String baseid = CarUtility.postCOPSUPolicy(json, config);
			if (baseid == null) {
				return new ModelAndView("redirect:/carma/selfservice/addsite?error=Unable to add site policy");
			} else {
				return new ModelAndView("redirect:/carma/editpolicy?baseId="+baseid);
			}
		} catch (Exception e) {
			return new ModelAndView("redirect:/carma/selfservice/addsite?error=Unable to add site policy");
		}
	}
	// GET to get the form
	@RequestMapping(value="/carma/selfservice/addsite", method=RequestMethod.GET)
	public ModelAndView getAddSite(HttpServletRequest req) {
		
		ModelAndView retval = new ModelAndView("addSitePage");
		
		CarConfig config = CarUtility.init(req);
		
		// Establish session for CSRF protection
		HttpSession sess = req.getSession(true);
		String csrftoken = generateCSRFToken();
		// Establish a conversation numer
		if (req.getParameter("conversation") != null) {
			sconvo = (String) req.getParameter("conversation");
		} else {
			if (sess.getAttribute("maxconv") != null) {
				convo = Integer.parseInt((String) sess.getAttribute("maxconv")) + 1;
				sconvo = String.valueOf(convo);
				sess.setAttribute("maxconv", sconvo);
			} else {
				// start at 0
				convo = 0;
				sconvo = String.valueOf(convo);
				sess.setAttribute("maxconv", sconvo);
			}
		}
		sess.setAttribute(sconvo + ":" + "csrftoken", csrftoken);
		sess.setMaxInactiveInterval(600);  // 10 minutes max
		
		// Establish the user typing and convey that to the VM
		String userType = null;
		userType = config.getProperty("car.userIdentifier", false);
		if (userType == null) {
			// Default to ePPN
			userType = "eduPersonPrincipalName";
		}
		retval.addObject("userType",userType);
		
		// Build up the main injection
		ArrayList<AddableItem> items = new ArrayList<AddableItem>();
		
		// Start by getting the list of RHs of interest
		ArrayList<ReturnedRHMetaInformation> rhma = CarUtility.getRHMetaInformation(config);
		for (ReturnedRHMetaInformation rhmi : rhma) {
			// For each of the RHs...
			// Get the user's COPSU policies
			ArrayList<UserReturnedPolicy> aurp = CarUtility.getCOPSUPolicies(req.getRemoteUser(),rhmi.getRhidentifier().getRhid(), config);
			// And the list of RPs for this RH (this is a slow-ish process)
			ArrayList<ReturnedRPMetaInformation> arpmi = CarUtility.getRPsForRH(rhmi.getRhidentifier().getRhtype(), rhmi.getRhidentifier().getRhid(), config);
			
			// Construct a matchlist from the COPSU policies (of which there are almost always fewer than the RPs for the RH)
			ArrayList<String> already = new ArrayList<String>();
			for (UserReturnedPolicy urp : aurp) {
				already.add(urp.getUserInfoReleasePolicy().getRelyingPartyId().getRPvalue());
			}
			
			String preflang = null;
			if (req.getLocale() != null && req.getLocale().getLanguage() != null && ! req.getLocale().getLanguage().equals("")) {
				preflang = req.getLocale().getLanguage();
				retval.addObject("preflang",preflang);
			}
			else {
				preflang = config.getProperty("car.defaultLocale", true);
				retval.addObject("preflang",preflang);
			}
			
			// And construct AddableItems for each of the RPs not in the list
			for (ReturnedRPMetaInformation rpmi : arpmi) {
				if (! already.contains(rpmi.getRpidentifier().getRpid())) {
					// This is one that's not already in the user's COPSU set
					AddableItem ai = new AddableItem();
					if (rhmi.getDisplayname() != null)
						ai.setRhdisp(CarUtility.localize(rhmi.getDisplayname(),preflang));
					if (ai.getRhdisp() == null || ai.getRhdisp().equals("")) {
						ai.setRhdisp(rhmi.getRhidentifier().getRhid());
					}
					if (rpmi.getDisplayname() != null)
						ai.setRpdisp(CarUtility.localize(rpmi.getDisplayname(), preflang));
					if (ai.getRpdisp() == null || ai.getRpdisp().equals("")) {
						ai.setRpdisp(rpmi.getRpidentifier().getRpid());
					}
					ai.setRhid(rhmi.getRhidentifier().getRhid());
					ai.setRhtype(rhmi.getRhidentifier().getRhtype());
					ai.setRpid(rpmi.getRpidentifier().getRpid());
					ai.setRptype(rpmi.getRpidentifier().getRptype());
					ai.setUsertype(config.getProperty("car.userIdentifier", true));
					items.add(ai);
				}
			}
			
		}
		
		retval.addObject("items",items);
		
		retval.addObject("CarUtility",CarUtility.class);

		// Tourist information
		
		retval.addObject("remoteuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		
		retval.addObject("logouturl",config.getProperty("car.carma.logouturl", false));
		
		retval.addObject("activetab","mysites");
		
		retval.addObject("conversation",sconvo);
		retval.addObject("csrftoken",csrftoken);
		
		if (req.getParameter("error") != null && ! req.getParameter("error").equals("")) {
			retval.addObject("failmsg",req.getParameter("error"));
		} else if (req.getParameter("success") != null && ! req.getParameter("success").equals("")) {
			retval.addObject("successmsg",req.getParameter("success"));
		}
		
		return retval;
	}
	//
	// Handle initial self-service request at carma/selfservice/sites
	//
	// This is the GET handler, to return the initial page
	@RequestMapping(value="/carma/selfservice/sites", method=RequestMethod.GET)
	public ModelAndView getSelfServiceSites(HttpServletRequest req) {

		ModelAndView retval = new ModelAndView("selfservicePage");
		
		CarConfig config = CarUtility.init(req);
		
		String preflang = CarUtility.prefLang(req);
		
		// Get a list of the RHs supported by this CAR instance
		ArrayList<ReturnedRHMetaInformation> rhma = CarUtility.getRHMetaInformation(config);

		// Identify the logged-in user
		String user = req.getRemoteUser();
		String userType = config.getProperty("car.userIdentifier", true);
		
		UserId uid = new UserId();
		uid.setUserType(userType);
		uid.setUserValue(user);
		
		// Generate an injection object as a HashMap from rhid String to ArrayList of InjectedUserPolicy
		// and populate the InjectedUserPolicy objects based on the policies the user has.
		// Take care of ignore newRPTemplate policies, as these are not in our purview here
		HashMap<String,ArrayList<InjectedUserPolicy>> policyMap = new HashMap<String,ArrayList<InjectedUserPolicy>>();

		// populate the hashmap
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		for (ReturnedRHMetaInformation rhi: rhma) {
			String rhid = rhi.getRhidentifier().getRhid();
			ArrayList<InjectedUserPolicy> iupa = new ArrayList<InjectedUserPolicy>();
			for (UserReturnedPolicy urp : CarUtility.getCOPSUPolicies(uid.getUserValue(),rhid, config)) {
				// for each user policy the user owns in this 
				InjectedUserPolicy iup = new InjectedUserPolicy();
				iup.setBaseId(urp.getPolicyMetaData().getPolicyId().getBaseId());
				String ud = df.format(new Date(urp.getPolicyMetaData().getCreateTime()));
				iup.setPolicyUpdateDate(ud);
				String rpid = urp.getUserInfoReleasePolicy().getRelyingPartyId().getRPvalue();
				String rptype = urp.getUserInfoReleasePolicy().getRelyingPartyId().getRPtype();
				ReturnedRPMetaInformation rpmi = CarUtility.getRPMetaInformation(rhid, rptype, rpid, config);
				if (rpmi == null) {
					continue;
				}
				if (rpmi.getDisplayname() != null) {
					iup.setRpName(CarUtility.localize(rpmi.getDisplayname(),preflang));
				} else {
					iup.setRpName(rpmi.getRpidentifier().getRpid());
				}
				
				/* Deprecated
				if (rpmi.getDisplayname() != null && rpmi.getDisplayname().getLocales() != null && ! rpmi.getDisplayname().getLocales().isEmpty()) {
					iup.setRpName(rpmi.getDisplayname().getLocales().get(0).getValue());
				} else {
					iup.setRpName(rpmi.getRpidentifier().getRpid());
				}
				*/
				
				String urpid = rpmi.getRpidentifier().getRpid();
				String rpUrl = urpid.replaceAll("http.*//", "");
				rpUrl = rpUrl.replaceAll("/.*$","");
				iup.setRpUrl(rpUrl);
				iupa.add(iup);
			}
			if (! iupa.isEmpty()) {
				Collections.sort(iupa); // sort the list alphabetically
				policyMap.put(rhid,iupa);
			}
		}
		

		
		// At this point, we should have policyMap populated
	
		
		// Test injections
		
		retval.addObject("locale",config.getProperty("car.defaultLocale", true));
		
		retval.addObject("rplistHeading",CarUtility.getLocalComponent("mysites"));
		retval.addObject("rplistDescr",CarUtility.getLocalComponent("rplistdescr"));
		retval.addObject("nspName",CarUtility.getLocalComponent("nspname"));
		retval.addObject("nspUrl","https://" + config.getProperty("car.carma.hostname",true) + ":" + config.getProperty("car.carma.port", true) + "/carma/new_rp");
		retval.addObject("nspDescr",CarUtility.getLocalComponent("nspdescr"));
		
		retval.addObject("rhmiList",rhma);
		retval.addObject("policyMap",policyMap);
		
		retval.addObject("page-title","CARMa self-service");
		retval.addObject("username",req.getAttribute("eppn"));
		
		// And authuser
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		
		retval.addObject("logouturl",config.getProperty("car.carma.logouturl", false));
		
		retval.addObject("activetab","mysites");
		if (req.getParameter("updated") != null) {
			retval.addObject("successmsg","Policy successfully updated");
		}
		if (req.getParameter("canceled") != null) {
			retval.addObject("noticemsg","Update cancelled");
		}
		if (req.getParameter("added") != null) {
				retval.addObject("successmsg","Policy successfully added");
		}
		
		// Additional injections
		
		retval.addObject("mysss",CarUtility.getLocalComponent("mysss"));
		retval.addObject("manage_link",CarUtility.getLocalComponent("manage_link"));
		retval.addObject("name_heading",CarUtility.getLocalComponent("name_heading"));
		retval.addObject("url_heading",CarUtility.getLocalComponent("url_heading"));
		retval.addObject("updated_heading",CarUtility.getLocalComponent("updated_heading"));
		retval.addObject("add_site",CarUtility.getLocalComponent("add_site"));
		retval.addObject("aboutsite_header",CarUtility.getLocalComponent("aboutsite_header"));
		retval.addObject("aboutsite_body",CarUtility.getLocalComponent("aboutsite_body"));
		return retval;
	}
	
	// This was originally part of a separate "/carma" app, but it's being migrated into the 
	// /car space, just as the selfservice interface has been.
	
	// Handle retrieval of the newRP edit form
	
	@RequestMapping(value="/carma/new_rp", method=RequestMethod.GET)
	public ModelAndView newRPGet(HttpServletRequest request) {
		ModelAndView model = new ModelAndView("newRPPage");
		
		CarConfig config = CarUtility.init(request);
		
		String preflang = CarUtility.prefLang(request);
		
		// Collect a list of the resource holders that may be of interest.
		// In future, we'll support better inter-RH mappings, but for now, we do at least
		// need to pick up the RHs that may be of import.
		
		ArrayList<ReturnedRHMetaInformation> rhmil = CarUtility.getRHMetaInformation(config);
		
		if (rhmil == null) {
			ModelAndView error = new ModelAndView("errorPage");
			error.addObject("message","Unable to retrieve RH list");
			return(error);
		}
		
		UserId u = new UserId();
		u.setUserValue(request.getRemoteUser());
		UserReturnedPolicy newRPPolicy = CarUtility.getNewRPTemplate(u, config);
		
		// Bifurcate on the presence of individual elements in the RP template.
		// If there are none, build a full tabulation of ii/value combinations
		// If there are any, use them instead.
		
		ArrayList<NewRPDisplayObject> decisions = new ArrayList<NewRPDisplayObject>();
		
		if (newRPPolicy.getUserInfoReleasePolicy().getArrayOfInfoReleaseStatement() == null || newRPPolicy.getUserInfoReleasePolicy().getArrayOfInfoReleaseStatement().isEmpty()) {
			// If there are no specific directives provided
			String directive = newRPPolicy.getUserInfoReleasePolicy().getUserAllOtherInfoReleaseStatement().getUserDirectiveAllOtherValues().getUserReleaseDirective().toString();
			for (ReturnedRHMetaInformation rhmi : rhmil) {
				ArrayList<InfoItemIdentifier> aliii = CarUtility.getRHIIList(rhmi.getRhidentifier(),config);
				for (InfoItemIdentifier iii : aliii) {
					ReturnedInfoItemMetaInformation i = CarUtility.getInfoItemMetaInformation(rhmi.getRhidentifier().getRhid(), iii.getIiid(), config);
					NewRPDisplayObject nrdo = new NewRPDisplayObject();
					if (i.getDisplayname() != null) {
						nrdo.setAttribute(CarUtility.localize(i.getDisplayname(),preflang));
					} else {
						nrdo.setAttribute(i.getIiidentifier().getIiid());
					}
					
					/* Deprecated
					if (i.getDisplayname() != null && i.getDisplayname().getLocales() != null && ! i.getDisplayname().getLocales().isEmpty())
						nrdo.setAttribute(i.getDisplayname().getLocales().get(0).getValue());
					else
						nrdo.setAttribute(i.getIiidentifier().getIiid());
					*/
					
					nrdo.setInfoType(i.getIiidentifier().getIitype());
					nrdo.setInfoValue(i.getIiidentifier().getIiid());
					String [] vals = {"value unavailable"};
					if (request.getAttribute(nrdo.getInfoValue()) != null) {
						vals = ((String)(request.getAttribute(nrdo.getInfoValue()))).split(";");
					} 
					if (vals[0].equalsIgnoreCase("value unavilable")) {
						continue;
					}
					for (String v : vals) {
						nrdo.getDecisions().put(v, directive);
						ReturnedValueMetaInformation rvmi = CarUtility.getValueMetaInformation(nrdo.getInfoValue(),v,config);
			//			String dv = null;
						if (rvmi == null || rvmi.getDisplayname() == null) {
							nrdo.getDisplayValues().put(v, v);
						} else {
							nrdo.getDisplayValues().put(v, rvmi.getDisplayname());
						}
					}
					decisions.add(nrdo);
				}
			}
		} else {
			// build the list from the existing object, whatever is there
			
			for (UserInfoReleaseStatement uirs : newRPPolicy.getUserInfoReleasePolicy().getArrayOfInfoReleaseStatement()) {
				NewRPDisplayObject nrdo = new NewRPDisplayObject();
				nrdo.setInfoType(uirs.getInfoId().getInfoType());
				nrdo.setInfoValue(uirs.getInfoId().getInfoValue());
				String attr = uirs.getInfoId().getInfoValue();
				String attrName = null;
				
				for (ReturnedRHMetaInformation rhmi : rhmil) {
					ArrayList<InfoItemIdentifier> aliii = CarUtility.getRHIIList(rhmi.getRhidentifier(),config);
					
					if (aliii == null) {
						continue;  // absence of iis is not an error, but it does short circuit here
					}
					
					for (InfoItemIdentifier ii : aliii) {
						if (ii.getIiid().equalsIgnoreCase(attr)) {
							ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhmi.getRhidentifier().getRhid(), ii.getIiid(), config);
							if (riimi.getDisplayname() != null) {
								attrName = CarUtility.localize(riimi.getDisplayname(),preflang);
							} else {
								attrName = ii.getIiid();
							}
							
							/* Deprecated
							if (riimi.getDisplayname() != null && riimi.getDisplayname().getLocales() != null && ! riimi.getDisplayname().getLocales().isEmpty())
								attrName = riimi.getDisplayname().getLocales().get(0).getValue();
							else
								attrName = ii.getIiid();
							*/
						}
					}
				}
				if (attrName == null) {
					attrName = attr;
				}
				nrdo.setAttribute(attrName);
				String [] vals = {"value unavailable"};
				if (request.getAttribute(nrdo.getInfoValue()) != null) {
					vals = ((String)(request.getAttribute(nrdo.getInfoValue()))).split(";");
				} 
				if (vals[0].equalsIgnoreCase("value unavilable")) {
					continue;
				}
				for (String v : vals) {
					ValueObject vo = new ValueObject();
					vo.setValue(v);;
					boolean setv = false;
					for (UserDirectiveOnValues a : uirs.getArrayOfDirectiveOnValues()) {
						if (a.getValueObjectList().contains(vo)) {
							nrdo.getDecisions().put(v, a.getUserReleaseDirective().toString());
							setv = true;
						}
					}
					if (!setv) {
						nrdo.getDecisions().put(v,  newRPPolicy.getUserInfoReleasePolicy().getUserAllOtherInfoReleaseStatement().getUserDirectiveAllOtherValues().getUserReleaseDirective().toString());
					}
					ReturnedValueMetaInformation rvmi = CarUtility.getValueMetaInformation(nrdo.getInfoValue(),v,config);
			//		String dv = null;
					if (rvmi == null || rvmi.getDisplayname() == null) {
						nrdo.getDisplayValues().put(v, v);
					} else {
						nrdo.getDisplayValues().put(v, rvmi.getDisplayname());
					}
				}
				decisions.add(nrdo);
			}
		}
		NewRPDisplayObject others = new NewRPDisplayObject();
		others.setAttribute("Other Attributes");
		others.setInfoType("allOtherInfoType");
		others.setInfoValue("allOtherInfoValue");
		String otherDecision = newRPPolicy.getUserInfoReleasePolicy().getUserAllOtherInfoReleaseStatement().getUserDirectiveAllOtherValues().getUserReleaseDirective().toString();
		others.getDecisions().put(CarUtility.getLocalComponent("not_above"),otherDecision);
		/*
		others.getDecisions().put("Information not listed above", otherDecision);
		*/
		
		model.addObject("activetab","preference");
		model.addObject("decisions",decisions);
		model.addObject("others",others);
		model.addObject("whileImAwayDecision",newRPPolicy.getUserInfoReleasePolicy().getWhileImAwayDirective().toString());
		model.addObject("institution_name",CarUtility.getLocalComponent("institution_name"));
		model.addObject("loggedin_user",request.getRemoteUser());
		model.addObject("authuser",((String)request.getAttribute("eppn")).replaceAll(";.*$",""));
		if (request.getParameter("success") != null) {
			model.addObject("successmsg",request.getParameter("success"));
		}
		if (request.getParameter("error") != null) {
			model.addObject("errmsg", request.getParameter("error"));
		}
		if (request.getParameter("notice") != null) {
			model.addObject("noticemsg",request.getParameter("notice"));
		}
		String logouturl = config.getProperty("car.carma.logouturl", false);
		if (logouturl == null || logouturl.contentEquals("")) 
			model.addObject("logouturl","/Shibboleth.sso/Logout?return=https://shib.oit.duke.edu/cgi-bin/logout.pl");
		else 
			model.addObject("logouturl",logouturl);
		
		// Add internationalized items
		
		model.addObject("new_site_prefs",CarUtility.getLocalComponent("new_site_prefs"));
		model.addObject("permit_description",CarUtility.getLocalComponent("permit_description"));
		model.addObject("deny_description",CarUtility.getLocalComponent("deny_description"));
		model.addObject("askme_description",CarUtility.getLocalComponent("askme_description"));
		model.addObject("userec_prefix",CarUtility.getLocalComponent("userec_prefix"));
		model.addObject("userec_suffix",CarUtility.getLocalComponent("userec_suffix"));
		model.addObject("not_above",CarUtility.getLocalComponent("not_above"));
		model.addObject("notpresent_header",CarUtility.getLocalComponent("notpresent_header"));
		model.addObject("notpresent_description",CarUtility.getLocalComponent("notpresent_description"));
		model.addObject("permit_longdescr",CarUtility.getLocalComponent("permit_longdescr"));
		model.addObject("deny_longdescr",CarUtility.getLocalComponent("deny_longdescr"));
		model.addObject("userec_longprefix",CarUtility.getLocalComponent("userec_longprefix"));
		model.addObject("userec_longsuffix",CarUtility.getLocalComponent("userec_longsuffix"));
		model.addObject("save_label",CarUtility.getLocalComponent("save_label"));
		model.addObject("new_site_subheading",CarUtility.getLocalComponent("new_site_subheading"));
		model.addObject("whatis_header",CarUtility.getLocalComponent("whatis_header"));
		model.addObject("whatis_body",CarUtility.getLocalComponent("whatis_body"));
		
		return model;
	}
	
	// Handle POSTing of a response form with a (possibly updated) newRPTemplate
	
	@RequestMapping(value="/carma/new_rp",method = RequestMethod.POST)
	public ModelAndView newRPPost(HttpServletRequest request, @ModelAttribute("postData") NewRPPostObject postData) {
		UserInfoReleasePolicy constructedPolicy = new UserInfoReleasePolicy();
		
		CarConfig config = CarConfig.getInstance();
	//	String icmHost = config.getProperty("car.icm.hostname", true);
	//	String icmPort = config.getProperty("car.icm.port", true);
		
		// Get the current newRP policy
		UserId u = new UserId();
		String ut = config.getProperty("car.userIdentifier", false);
		if (ut == null) {
			u.setUserType("eduPersonPrincipalName");
			CarUtility.locError("ERR1134",LogCriticality.debug,"set User Type to default (eduPersonPrincipalName)");

		} else {
			u.setUserType(ut);
			CarUtility.locError("ERR1134",LogCriticality.debug,"UserType from config is " + ut);
		}
		u.setUserValue(request.getRemoteUser());
		UserReturnedPolicy newRPPolicy = CarUtility.getNewRPTemplate(u, config);
		CarUtility.locError("ERR1134",LogCriticality.debug,"Retrieved policy.");
		String newRPBaseId = newRPPolicy.getPolicyMetaData().getPolicyId().getBaseId();
		
		constructedPolicy.setDescription("Default policy for NewRPTemplate");
		constructedPolicy.setUserId(newRPPolicy.getUserInfoReleasePolicy().getUserId());
		constructedPolicy.setRelyingPartyId(newRPPolicy.getUserInfoReleasePolicy().getRelyingPartyId());
		constructedPolicy.setResourceHolderId(newRPPolicy.getUserInfoReleasePolicy().getResourceHolderId());

		constructedPolicy.setWhileImAwayDirective(WhileImAwayDirective.valueOf(postData.getWhileImAwayDecision()));
		
		HashMap<InfoId,HashMap<UserReleaseDirective, ArrayList<String>>> foldMap = new HashMap<InfoId,HashMap<UserReleaseDirective,ArrayList<String>>>();
		if (postData.getNewRPDecisions() != null) {
			for (NewRPDecision nrpd : postData.getNewRPDecisions()) {
				
				InfoId ii = new InfoId();
				ii.setInfoType(nrpd.getInfoType());
				ii.setInfoValue(nrpd.getInfoValue());
				HashMap<UserReleaseDirective,ArrayList<String>> innerMap = foldMap.get(ii);
				if (innerMap == null) {
					innerMap = new HashMap<UserReleaseDirective,ArrayList<String>>();
				}
				ArrayList<String> values = innerMap.get(UserReleaseDirective.valueOf(nrpd.getDecision()));
				if (values == null) {
					values = new ArrayList<String>();
				}
				values.add(nrpd.getDecisionValue());
				innerMap.put(UserReleaseDirective.valueOf(nrpd.getDecision()), values);
				foldMap.put(ii, innerMap);
			}
		}
		
		ArrayList<UserInfoReleaseStatement> auirs = new ArrayList<UserInfoReleaseStatement>();
		for (InfoId i : foldMap.keySet()) {
			UserInfoReleaseStatement uirs = new UserInfoReleaseStatement();
			uirs.setInfoId(i);
			uirs.setPersistence("persist");
			ArrayList<UserDirectiveOnValues> adov = new ArrayList<UserDirectiveOnValues>();
			for (UserReleaseDirective urd : foldMap.get(i).keySet()) {
				UserDirectiveOnValues dov = new UserDirectiveOnValues();
				dov.setUserReleaseDirective(urd);;
				ArrayList<String> vlist = foldMap.get(i).get(urd);
				ArrayList<ValueObject> volist = new ArrayList<ValueObject>();
				for (String v : vlist) {
					ValueObject o = new ValueObject();
					o.setValue(v);
					volist.add(o);
				}
				dov.setValuesList(volist);
				adov.add(dov);
			}
			uirs.setArrayOfDirectiveOnValues(adov);;
			UserDirectiveAllOtherValues udaov = new UserDirectiveAllOtherValues();
			udaov.setAllOtherValues(AllOtherValuesConst.allOtherValues);
			udaov.setUserReleaseDirective(UserReleaseDirective.valueOf(postData.getOtherAttributesDecision()));
			uirs.setUserDirectiveAllOtherValues(udaov);
			auirs.add(uirs);
		}
		constructedPolicy.setArrayOfInfoReleaseStatement(auirs);;
		UserAllOtherInfoReleaseStatement aoirs = new UserAllOtherInfoReleaseStatement();
		AllOtherInfoId aoii = new AllOtherInfoId();
		aoii.setAllOtherInfoType(AllOtherInfoTypeConst.allOtherInfoType);
		aoii.setAllOtherInfoValue(AllOtherInfoValueConst.allOtherInfoValue);
		
		aoirs.setAllOtherInfoId(aoii);
		
		UserDirectiveAllOtherValues udaov = new UserDirectiveAllOtherValues();
		udaov.setAllOtherValues(AllOtherValuesConst.allOtherValues);
		udaov.setUserReleaseDirective(UserReleaseDirective.valueOf(postData.getOtherAttributesDecision()));
		aoirs.setUserDirectiveAllOtherValues(udaov);;
		
		constructedPolicy.setUserAllOtherInfoReleaseStatement(aoirs);;
		constructedPolicy.setWhileImAwayDirective(WhileImAwayDirective.valueOf(postData.getWhileImAwayDecision()));
		
		String entity = null;
		try {
			entity = constructedPolicy.toJSON();
		} catch (Exception e) {
			ModelAndView model = new ModelAndView("errorPage");
			model.addObject("Failed to serialize new RP template for storage");
			return model;
		}
		
		//CarUtility.postCOPSUPolicy(entity, config);
		CarUtility.putCOPSUPolicy(newRPBaseId, constructedPolicy, config);
		
		CarUtility.locError("ERR1134",LogCriticality.info,"newRPPolicy JSON is: " + entity);
		return new ModelAndView("redirect:new_rp?success=Preferences Updated");
			
	}
	
}


