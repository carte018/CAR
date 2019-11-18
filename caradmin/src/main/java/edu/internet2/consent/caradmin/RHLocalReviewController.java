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

import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.InternationalizedString;
import edu.internet2.consent.informed.model.LocaleString;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.ReturnedInfoItemMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;

@Controller
public class RHLocalReviewController {
	
    private String sconvo;
    private int convo;


    private String generateCSRFToken() {
            String foo = RandomStringUtils.random(32,true,true);
            String bar = Base64.encodeBase64URLSafeString(foo.getBytes());
            return bar;
    }

	@RequestMapping(value="/rhlocalereview/{rhtype}/{rhid}",method=RequestMethod.POST)
	public ModelAndView handlePostRHLocaleReview(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhid) {
		
		ModelAndView retval = new ModelAndView("redirect:/rhlocalereview/"+rhtype+"/"+rhid+"/?state=1");
		String rhiv = CarAdminUtils.idUnEscape(rhid);
		
		// Translators and RHRegistrars (general or delegated) only.
		
		ArrayList<String> roles = new ArrayList<String>();
		ArrayList<String> targets = new ArrayList<String>();
		
		roles.add("Translator");
		roles.add("RHRegistrar");
		roles.add("DelegatedRHRegistrar");
		
		
		targets.add(CarAdminUtils.idUnEscape(rhid));
		
		if (CarAdminUtils.init(req,roles,targets) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
            eval.addObject("logouturl","/Shibboleth.sso/Logout");  // config failure precludesusing config'd logouturl
            CarAdminUtils.injectStrings(eval, new String[] {
                              "top_heading",
                              "sign_out",
                              "top_logo_url"
            });
			eval.addObject("message",CarAdminUtils.getLocalComponent("unauthorized_msg"));
			return eval;
		}
		
        // Validate CSRF protection
        // First, get the conversation number
        sconvo = req.getParameter("conversation");
        if (sconvo == null) {
                ModelAndView err = new ModelAndView("errorPage");
                err.addObject("message",CarAdminUtils.getLocalComponent("missing_convo"));
                return err;
        }
        HttpSession sess = req.getSession(false);
        if (sess == null || sess.getAttribute(sconvo + ":" + "csrftoken") == null || ! sess.getAttribute(sconvo + ":" + "csrftoken").equals(req.getParameter("csrftoken"))) {
                // CSRF failure
                ModelAndView err = new ModelAndView("errorPage");
                err.addObject("message",CarAdminUtils.getLocalComponent("csrf_fail"));
                return err;
        }

		ActivityStreamEntry ase = new ActivityStreamEntry();
		
		// formname indicates which part of the page was submitted
		
		if (req.getParameter("formname") != null && req.getParameter("formname").equals("rhmeditform")) {
			// This is an edit to the RH metainformation.  Get the current metainfo to start with.
			ReturnedRHMetaInformation rrhmi = CarAdminUtils.getResourceHolderMetaInformation(rhtype, rhiv);
			
			// Update with what's in the form
			
			ArrayList<String> slangs = CarAdminUtils.getSupportedLanguages();
			InternationalizedString newdisplay = new InternationalizedString();
			ArrayList<LocaleString> disploc = new ArrayList<LocaleString>();
			InternationalizedString newdescr = new InternationalizedString();
			ArrayList<LocaleString> descloc = new ArrayList<LocaleString>();
			
			for (String lang : slangs) {
				// for each language
				if (req.getParameter("rhdisplayname_"+lang) != null && req.getParameter("rhdisplayname_"+lang).length() > 0) {
					LocaleString l = new LocaleString();
					l.setLocale(lang);
					l.setValue(req.getParameter("rhdisplayname_"+lang));
					disploc.add(l);
				}
				if (req.getParameter("rhdescription_"+lang) != null && req.getParameter("rhdescription_"+lang).length() > 0) {
					LocaleString l = new LocaleString();
					l.setLocale(lang);;
					l.setValue(req.getParameter("rhdescription_"+lang));
					descloc.add(l);
				}
			}
			newdisplay.setLocales(disploc);
			newdescr.setLocales(descloc);
			
			rrhmi.setDisplayname(newdisplay);
			rrhmi.setDescription(newdescr);
			
			// Send the update
			
			CarAdminUtils.putRHMetaInformation(rrhmi);
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setOperation("updated locale bindings for RH metainfo for " + rrhmi.getDisplayname().getLocales().get(0).getValue());
			ase.setTimestamp(System.currentTimeMillis());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore on failure
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equals("rhieditform")) {
			// This is the info item localization form
			// This must be processed as a set of pairs of values.
			
			if (req.getParameter("icount") == null || req.getParameter("icount").equals("0")) {
				return retval;  // Nothing to do
			}
			
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(rhtype);
			rhi.setRhid(rhiv);
			
			for (int i = 1; i <= Integer.parseInt(req.getParameter("icount")); i++) {
				// For each info item in the form
				// Get the Ii metainformation and update the string set accordingly
				String itype = req.getParameter("itype_"+i);
				String iid = req.getParameter("iid_"+i);
				InfoItemIdentifier iii = new InfoItemIdentifier();
				iii.setIitype(itype);
				iii.setIiid(iid);
				
				ReturnedInfoItemMetaInformation riimi = CarAdminUtils.getIIMetaInformation(rhi, iii);
				
				if (riimi == null) {
					// This is the odd case in which bad pre-existing data in the database results in a
					// nonexistent return for an attribute already identified as attached to this RH.
					// Create a stub entry to populate with the data from the form.
					riimi = new ReturnedInfoItemMetaInformation();
					riimi.setRhidentifier(rhi);
					riimi.setIiidentifier(iii);
					InternationalizedString is = new InternationalizedString();
					riimi.setDisplayname(is);
					riimi.setDescription(is);
				}
				
				ArrayList<String> slangs = CarAdminUtils.getSupportedLanguages();
				InternationalizedString newdisplay = new InternationalizedString();
				ArrayList<LocaleString> disploc = new ArrayList<LocaleString>();
				InternationalizedString newdescr = new InternationalizedString();
				ArrayList<LocaleString> descloc = new ArrayList<LocaleString>();
				
				for (String lang : slangs) {
					// For each supported language
					if (req.getParameter("iidisplayname_"+i+"_"+lang) != null && req.getParameter("iidisplayname_"+i+"_"+lang).length() > 0) {
						LocaleString l = new LocaleString();
						l.setLocale(lang);
						l.setValue(req.getParameter("iidisplayname_"+i+"_"+lang));
						disploc.add(l);
					}
					if (req.getParameter("iidescription_"+i+"_"+lang) != null && req.getParameter("iidescription_"+i+"_"+lang).length() > 0) {
						LocaleString l = new LocaleString();
						l.setLocale(lang);;
						l.setValue(req.getParameter("iidescription_"+i+"_"+lang));
						descloc.add(l);
					}
				}
				newdisplay.setLocales(disploc);
				newdescr.setLocales(descloc);
				
				riimi.setDisplayname(newdisplay);
				riimi.setDescription(newdescr);
				
				CarAdminUtils.putIIMetaInformation(riimi);  // actually update the data
			}
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setOperation("updated locale bindings for information items for " + rhi.getRhid());
			ase.setTimestamp(System.currentTimeMillis());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore on failure
			}
		}

		
		return retval;
		
	}

	@RequestMapping(value="/rhlocalereview/{rhtype}/{rhid}",method=RequestMethod.GET)
	public ModelAndView handleRHLocaleReview(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhid) {
		
		ModelAndView retval = new ModelAndView("RHLocaleReview");
		AdminConfig config = null;
		
		// Translators can always perform locale reviews, but so can 
		// RH registrars in this case.
		
		ArrayList<String> roles = new ArrayList<String>();
		ArrayList<String> targets = new ArrayList<String>();
		
		roles.add("Translator");
		roles.add("RHRegistrar");
		roles.add("DelegatedRHRegistrar");
		
		// RHAuditors can read
		roles.add("RHAuditor");
		roles.add("DelegatedRHAuditor");
		
		targets.add(CarAdminUtils.idUnEscape(rhid));
		
		if ((config = CarAdminUtils.init(req,roles,targets)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
            eval.addObject("logouturl","/Shibboleth.sso/Logout");  // config failure precludesusing config'd logouturl
            CarAdminUtils.injectStrings(eval, new String[] {
                              "top_heading",
                              "sign_out",
                              "top_logo_url"
            });
			eval.addObject("message",CarAdminUtils.getLocalComponent("unauthorized_msg"));
			return eval;
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
        sess.setMaxInactiveInterval(1200);  // 20 minutes max

        // Marshall injections for the page

        // CSRF protection
        retval.addObject("csrftoken",csrftoken); // to set csrftoken in the form
        retval.addObject("sconvo",sconvo);  // for formulating URLs

		// Retrieve the metainformation for this RH
		
		String rhiv = CarAdminUtils.idUnEscape(rhid);

		ReturnedRHMetaInformation rrhmi = CarAdminUtils.getResourceHolderMetaInformation(rhtype, rhiv);
		RHIdentifier rhi = new RHIdentifier();
		rhi.setRhtype(rhtype);
		rhi.setRhid(rhiv);
		ReturnedRHInfoItemList rhiil = CarAdminUtils.getIiList(rhi);
		
		ArrayList<ReturnedInfoItemMetaInformation> arimi = new ArrayList<ReturnedInfoItemMetaInformation>();
		if (rhiil != null && rhiil.getInfoitemlist() != null) {
			for (InfoItemIdentifier iii : rhiil.getInfoitemlist()) {
				// For each of the info items in the list...
				ReturnedInfoItemMetaInformation riimi = CarAdminUtils.getIIMetaInformation(rhi, iii);
				arimi.add(riimi);
			}
		}
		
		ArrayList<String> languages = CarAdminUtils.getSupportedLanguages();
		String displayname;
		String lang;
		
		if (req.getLocale() != null && req.getLocale().getLanguage() != null) {
			lang = req.getLocale().getLanguage();
			displayname = CarAdminUtils.localize(rrhmi.getDisplayname(), lang);
		} else {
			lang = languages.get(0);
			displayname = CarAdminUtils.localize(rrhmi.getDisplayname(), lang);
		}
		
		ArrayList<ReturnedRHMetaInformation> rhmil = CarAdminUtils.getAllDefinedResourceHolders();
		Collections.sort(rhmil,new ReturnedRHMetaInformationComparator());

		retval.addObject("rhmetainfo",rrhmi);
		retval.addObject("arimi",arimi);
		retval.addObject("languages",languages);
		retval.addObject("lang",lang);
		retval.addObject("displayname",displayname);
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("activetab","rhregistration");
		retval.addObject("availablerhs",rhmil);
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		
		
		if (req.getParameter("state") != null && req.getParameter("state").equals("1")) {
			retval.addObject("successmsg","Successfully updated language mappings");
		}
		
		CarAdminUtils.injectStrings(retval, new String[] { "rh_metainfo_label",
															"displayname_label",
															"description_label",
															"update_label",
															"ii_metainfo_label",
															"no_info_msg",
															"property_label",
															"missing_label",
															"top_heading",
															"sign_out",
															"top_logo_url"
															
		});
		return retval;
	}
}
