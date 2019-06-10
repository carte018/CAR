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
	
	@RequestMapping(value="/rplocalereview/{rhtype}/{rhid}/{rptype}/{rpid}",method=RequestMethod.POST)
	public ModelAndView handlePostRPLocaleReview(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhidin, @PathVariable("rptype") String rptype, @PathVariable("rpid") String rpidin) {
		
		ModelAndView retvals = new ModelAndView("redirect:/rplocalereview/"+rhtype+"/"+rhidin+"/"+rptype+"/"+rpidin+"/?state=1");
		@SuppressWarnings("unused")
		ModelAndView retvalf = new ModelAndView("redirect:/rplocalereview/"+rhtype+"/"+rhidin+"/"+rptype+"/"+rpidin+"/?state=0");
		String rhid = CarAdminUtils.idUnEscape(rhidin);
		String rpid = CarAdminUtils.idUnEscape(rpidin);
		
		if (CarAdminUtils.init(req) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message",CarAdminUtils.getLocalComponent("unauthorized_msg"));
			return eval;
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
		if ((config = CarAdminUtils.init(req)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message",CarAdminUtils.getLocalComponent("unauthorized_msg"));
			return eval;
		}
		
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
			displayname = CarAdminUtils.localize(rrpmi.getDisplayname(), lang);
		} else {
			lang = languages.get(0);
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
															"lang_report_heading"
															
		});
		return retval;
	}
}
