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
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.InfoItemValueList;
import edu.internet2.consent.informed.model.InternationalizedString;
import edu.internet2.consent.informed.model.LocaleString;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPOptionalInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRPRequiredInfoItemList;

@Controller
public class RPLocaleReviewController {
	
    private String sconvo;
    private int convo;


    private String generateCSRFToken() {
            String foo = RandomStringUtils.random(32,true,true);
            String bar = Base64.encodeBase64URLSafeString(foo.getBytes());
            return bar;
    }

	@RequestMapping(value="/rplocalereview/{rhtype}/{rhid}/{rptype}/{rpid}",method=RequestMethod.POST)
	public ModelAndView handlePostRPLocaleReview(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhidin, @PathVariable("rptype") String rptype, @PathVariable("rpid") String rpidin) {
		
		ModelAndView retvals = new ModelAndView("redirect:/rplocalereview/"+rhtype+"/"+rhidin+"/"+rptype+"/"+rpidin+"/?state=1");
		@SuppressWarnings("unused")
		ModelAndView retvalf = new ModelAndView("redirect:/rplocalereview/"+rhtype+"/"+rhidin+"/"+rptype+"/"+rpidin+"/?state=0");
		String rhid = CarAdminUtils.idUnEscape(rhidin);
		String rpid = CarAdminUtils.idUnEscape(rpidin);
		
		// Translators and RPRegistrars (general or delegated) only.
		
		ArrayList<String> roles = new ArrayList<String>();
		ArrayList<String> targets = new ArrayList<String>();
		
		roles.add("Translator");
		roles.add("RPRegistrar");
		roles.add("DelegatedRPRegistrar");
		targets.add(rhid);
		targets.add(rpid);
		
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
                err.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
                err.addObject("logouturl","/Shibboleth.sso/Logout");  // config failure precludesusing config'd logouturl
                CarAdminUtils.injectStrings(err, new String[] {
                                  "top_heading",
                                  "sign_out",
                                  "top_logo_url"
                });
                err.addObject("message",CarAdminUtils.getLocalComponent("missing_convo"));
                return err;
        }
        HttpSession sess = req.getSession(false);
        if (sess == null || sess.getAttribute(sconvo + ":" + "csrftoken") == null || ! sess.getAttribute(sconvo + ":" + "csrftoken").equals(req.getParameter("csrftoken"))) {
                // CSRF failure
                ModelAndView err = new ModelAndView("errorPage");
                err.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
                err.addObject("logouturl","/Shibboleth.sso/Logout");  // config failure precludesusing config'd logouturl
                CarAdminUtils.injectStrings(err, new String[] {
                                  "top_heading",
                                  "sign_out",
                                  "top_logo_url"
                });
                err.addObject("message",CarAdminUtils.getLocalComponent("csrf_fail"));
                return err;
        }

		String defaultlanguage = req.getParameter("defaultlanguage");
		
		// formname distinguishes the components being manipulated
		
		if (req.getParameter("formname") != null && req.getParameter("formname").equals("rpmeditform")) {
			// Editing RP metainformation (display name and description)
			
			ReturnedRPMetaInformation rpmi = CarAdminUtils.getRelyingPartyMetaInformation(rhtype, rhid, rptype, rpid);
			
			ArrayList<String> slangs = CarAdminUtils.getSupportedLanguages();
			InternationalizedString newdisplay = new InternationalizedString();
			ArrayList<LocaleString> disploc = new ArrayList<LocaleString>();
			InternationalizedString newdescr = new InternationalizedString();
			ArrayList<LocaleString> descloc = new ArrayList<LocaleString>();
		
			for (String lang : slangs) {
				if (req.getParameter("rpdisplayname_"+lang) != null && req.getParameter("rpdisplayname_"+lang).length() > 0) {
					LocaleString ls = new LocaleString();
					ls.setLocale(lang);
					ls.setValue(req.getParameter("rpdisplayname_"+lang));
					disploc.add(ls);
				}
				if (req.getParameter("rpdescription_"+lang) != null && req.getParameter("rpdescription_"+lang).length() > 0) {
					LocaleString ls = new LocaleString();
					ls.setLocale(lang);
					ls.setValue(req.getParameter("rpdescription_"+lang));
					descloc.add(ls);
				}
			}
			newdisplay.setLocales(disploc);
			newdescr.setLocales(descloc);
			
			rpmi.setDisplayname(newdisplay);
			rpmi.setDescription(newdescr);
			
			CarAdminUtils.putRelyingPartyMetaInformation(rpmi);
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated RP metainformation for " + rpmi.getDisplayname().getLocales().get(0).getValue());
			
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equals("rprieditform")) {
			// Editing the required items' reason values
			// Update
			
			ReturnedRPRequiredInfoItemList orig = CarAdminUtils.getRelyingPartyRequiredInfoItemList(rhtype, rhid, rptype, rpid);
			
			// Model is optimized for retrieval, so we need to fold to optimize for searching
			//HashMap<String,HashMap<String,LocaleString>> folded = new HashMap<String,HashMap<String,LocaleString>>();
			HashMap<String,String> sources = new HashMap<String,String>();
			HashMap<String,ArrayList<String>> valuelists = new HashMap<String,ArrayList<String>>();
			
			for (InfoItemValueList iivl : orig.getRequiredlist()) {
				HashMap<String,LocaleString> inner = new HashMap<String,LocaleString>();
				if (iivl.getReason() == null) {
					// No reason specified yet -- create one from whole cloth with the default language in it
					ArrayList<LocaleString> nals = new ArrayList<LocaleString>();
					LocaleString nls = new LocaleString();
					nls.setLocale(defaultlanguage);
					nls.setValue("");  // empty value for now
					nals.add(nls);
					InternationalizedString nis = new InternationalizedString();
					nis.setLocales(nals);
					iivl.setReason(nis);
				}
				
				for (LocaleString ls : iivl.getReason().getLocales()) {
					// for each localestring in the Reason
					inner.put(ls.getLocale(),ls);
				}
			//	folded.put(iivl.getInfoitemidentifier().getIiid(),inner);
				sources.put(iivl.getInfoitemidentifier().getIiid(),iivl.getSourceitemname());
				valuelists.put(iivl.getInfoitemidentifier().getIiid(),(ArrayList<String>) iivl.getValuelist());
			}
			ArrayList<String> slangs = CarAdminUtils.getSupportedLanguages();

			ArrayList<InfoItemValueList> aivl = new ArrayList<InfoItemValueList>();
			
			
			for (int i = 1; i <= Integer.parseInt(req.getParameter("icount")); i++) {
				// Iterate over the rows in the form
				InfoItemValueList ivl = new InfoItemValueList();
				
				String iitype = req.getParameter("iitype_"+i);
				String iiid = req.getParameter("iiid_"+i);
				InfoItemIdentifier iii = new InfoItemIdentifier();
				iii.setIitype(iitype);
				iii.setIiid(iiid);
				
				ivl.setInfoitemidentifier(iii);				
			
				ivl.setSourceitemname(sources.get(iiid));
				ivl.setValuelist(valuelists.get(iiid));
				
				InternationalizedString is = new InternationalizedString();
				ArrayList<LocaleString> als = new ArrayList<LocaleString>();
				
				for(String lang : slangs) {
					LocaleString ls = new LocaleString();
					ls.setLocale(lang);
					if (req.getParameter("ii_reason_"+i+"_"+lang) != null && req.getParameter("ii_reason_"+i+"_"+lang).length() > 0)
						ls.setValue(req.getParameter("ii_reason_"+i+"_"+lang));
					else
						ls.setValue(null);
					als.add(ls);
				}
				is.setLocales(als);
				
				ivl.setReason(is);
				
				aivl.add(ivl);
			}
			
			orig.setRequiredlist(aivl);
			
			CarAdminUtils.putRPRequiredInfoItemList(orig);
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated required information items for " + orig.getRpidentifier().getRpid());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equals("rpoieditform")) {
			// Editing the optional items' reason values
			// Update
			
			ReturnedRPOptionalInfoItemList orig = CarAdminUtils.getRelyingPartyOptionalInfoItemList(rhtype, rhid, rptype, rpid);
			
			// Model is optimized for retrieval, so we need to fold to optimize for searching
			//HashMap<String,HashMap<String,LocaleString>> folded = new HashMap<String,HashMap<String,LocaleString>>();
			HashMap<String,String> sources = new HashMap<String,String>();
			HashMap<String,ArrayList<String>> valuelists = new HashMap<String,ArrayList<String>>();
			
			for (InfoItemValueList iivl : orig.getOptionallist()) {
				HashMap<String,LocaleString> inner = new HashMap<String,LocaleString>();
				if (iivl.getReason() == null) {
					// No reason specified yet -- create one from whole cloth with the default language in it
					ArrayList<LocaleString> nals = new ArrayList<LocaleString>();
					LocaleString nls = new LocaleString();
					nls.setLocale(defaultlanguage);
					nls.setValue("");  // empty value for now
					nals.add(nls);
					InternationalizedString nis = new InternationalizedString();
					nis.setLocales(nals);
					iivl.setReason(nis);
				}
				
				for (LocaleString ls : iivl.getReason().getLocales()) {
					// for each localestring in the Reason
					inner.put(ls.getLocale(),ls);
				}
			//	folded.put(iivl.getInfoitemidentifier().getIiid(),inner);
				sources.put(iivl.getInfoitemidentifier().getIiid(),iivl.getSourceitemname());
				valuelists.put(iivl.getInfoitemidentifier().getIiid(),(ArrayList<String>) iivl.getValuelist());
			}
			ArrayList<String> slangs = CarAdminUtils.getSupportedLanguages();

			ArrayList<InfoItemValueList> aivl = new ArrayList<InfoItemValueList>();
			
			
			for (int i = 1; i <= Integer.parseInt(req.getParameter("icount")); i++) {
				// Iterate over the rows in the form
				InfoItemValueList ivl = new InfoItemValueList();
				
				String iitype = req.getParameter("iitype_"+i);
				String iiid = req.getParameter("iiid_"+i);
				InfoItemIdentifier iii = new InfoItemIdentifier();
				iii.setIitype(iitype);
				iii.setIiid(iiid);
				
				ivl.setInfoitemidentifier(iii);				
			
				ivl.setSourceitemname(sources.get(iiid));
				ivl.setValuelist(valuelists.get(iiid));
				
				InternationalizedString is = new InternationalizedString();
				ArrayList<LocaleString> als = new ArrayList<LocaleString>();
				
				for(String lang : slangs) {
					LocaleString ls = new LocaleString();
					ls.setLocale(lang);
					if (req.getParameter("ii_reason_"+i+"_"+lang) != null && req.getParameter("ii_reason_"+i+"_"+lang).length() > 0)
						ls.setValue(req.getParameter("ii_reason_"+i+"_"+lang));
					else
						ls.setValue(null);
					als.add(ls);
				}
				is.setLocales(als);
				
				ivl.setReason(is);
				
				aivl.add(ivl);
			}
			
			orig.setOptionallist(aivl);
			
			CarAdminUtils.putRPOptionalInfoItemList(orig);
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated optional information items for " + orig.getRpidentifier().getRpid());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		}
			
		return retvals;
	}
	
	@RequestMapping(value="/rplocalereview/{rhtype}/{rhid}/{rptype}/{rpid}",method=RequestMethod.GET)
	public ModelAndView handleGetRPLocaleReview(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhidin, @PathVariable("rptype") String rptype,@PathVariable("rpid") String rpidin) {
		
		ModelAndView retval = new ModelAndView("RPLocaleReview");
		
		AdminConfig config = null;
		
		// Translators can always perform locale reviews, but so can 
		// RP registrars in this case.
		
		ArrayList<String> roles = new ArrayList<String>();
		ArrayList<String> targets = new ArrayList<String>();
		
		roles.add("Translator");
		roles.add("RPRegistrar");
		roles.add("DelegatedRPRegistrar");
		
		// RPAuditors can review
		roles.add("RPAuditor");
		roles.add("DelegatedRPAuditor");
		
		targets.add(CarAdminUtils.idUnEscape(rpidin));
		targets.add(CarAdminUtils.idUnEscape(rhidin));
		
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

		String rhid = CarAdminUtils.idUnEscape(rhidin);
		String rpid = CarAdminUtils.idUnEscape(rpidin);
		
		ReturnedRPMetaInformation rrpmi = CarAdminUtils.getRelyingPartyMetaInformation(rhtype, rhid, rptype, rpid);
		
		ReturnedRPRequiredInfoItemList rpriil = CarAdminUtils.getRelyingPartyRequiredInfoItemList(rhtype, rhid, rptype, rpid);
		ReturnedRPOptionalInfoItemList rpoiil = CarAdminUtils.getRelyingPartyOptionalInfoItemList(rhtype, rhid, rptype, rpid);
		
		ArrayList<String> languages = CarAdminUtils.getSupportedLanguages();
		
		String displayname;
		String lang;
		
		if (req.getLocale() != null && req.getLocale().getLanguage() != null) {
			lang = req.getLocale().getLanguage();
			if (rrpmi.getDisplayname() == null)
				displayname = rrpmi.getRpidentifier().getRpid();
			else
				displayname = CarAdminUtils.localize(rrpmi.getDisplayname(), lang);
		} else {
			lang = languages.get(0);
			if (rrpmi.getDisplayname() == null)
				displayname = rrpmi.getRpidentifier().getRpid();
			else
				displayname = CarAdminUtils.localize(rrpmi.getDisplayname(), lang);
		}
		
		ArrayList<ReturnedRHMetaInformation> rhmil = CarAdminUtils.getAllDefinedResourceHolders();
		Collections.sort(rhmil,new ReturnedRHMetaInformationComparator());

		
		retval.addObject("activetab","rpregistration");
		retval.addObject("availablerhs",rhmil);
		retval.addObject("rrpmi",rrpmi);
		retval.addObject("rpriil",rpriil);
		retval.addObject("rpoiil",rpoiil);
		retval.addObject("languages",languages);
		retval.addObject("lang",lang);
		retval.addObject("displayname",displayname);
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		
		if (req.getParameter("state") != null && req.getParameter("state").equals("1")) {
			retval.addObject("successmsg","Successfully updated language mappings");
		} else if (req.getParameter("state") != null) {
			retval.addObject("failmsg","Failed to update langauge mappings");
		}
		
		CarAdminUtils.injectStrings(retval, new String[] { "rp_metainfo_label",
															"displayname_label",
															"missing_label",
															"description_label",
															"property_label",
															"update_label",
															"required_metainfo_label",
															"no_required_label",
															"reason_label",
															"optional_metainfo_label",
															"no_optional_label",
															"lang_report_heading",
															"top_heading",
															"sign_out",
															"top_logo_url"
															
		});
		return retval;
	}
}
