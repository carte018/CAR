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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;

@Controller
public class SystemSettingsController {

	@RequestMapping(value="/systemsettings",method=RequestMethod.GET)
	public ModelAndView getSystemSettings(HttpServletRequest req) {
		
		AdminConfig config = AdminConfig.getInstance();  // no authz needed here
		
		// Nothing interesting here -- just bake out the template, which is 
		// currently entirely static content.
		ModelAndView retval = new ModelAndView("SystemSettings");
		
		String lang = "en";
		if (req.getLocale() != null && req.getLocale().getLanguage() != null) {
			lang = req.getLocale().getLanguage();
		}
		
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("activetab","systemsettings");
		
		retval.addObject("lang",lang);
				
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		
		CarAdminUtils.injectStrings(retval, new String[] {"back_link",
														  "top_heading",
														  "sign_out",
														  "top_logo_url"
		});
		
		return retval;
	}
	
	@RequestMapping(value="/systemsettings/translatorview",method=RequestMethod.GET)
	public ModelAndView getTranslatorView(HttpServletRequest req) {
		
		ModelAndView retval = new ModelAndView("TranslatorSelector");
		
		AdminConfig config = AdminConfig.getInstance();  // no authz needed here
		
		String lang = "en";
		if (req.getLocale() != null && req.getLocale().getLanguage() != null) {
			lang = req.getLocale().getLanguage();
		}
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("activetab","systemsettings");
		
		retval.addObject("lang",lang);
				
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		
		CarAdminUtils.injectStrings(retval, new String[] {"back_link",
														  "top_heading",
														  "sign_out",
														  "top_logo_url"
		});
		
		// Inject a map from RH identifier to ArrayList of RP identifiers
		// suitable for building a dynamic selector to allow a Translator to
		// work on translating strings for any RH or RP under an RH.
		// Magic happens in the JavaScript -- here we're just providing 
		// working data from the persistence layer.
		
		HashMap<ReturnedRHMetaInformation,ArrayList<ReturnedRPMetaInformation>> rhrpmap = new HashMap<ReturnedRHMetaInformation,ArrayList<ReturnedRPMetaInformation>>();
		HashMap<String,String[]> irdplist = new HashMap<String,String[]>();
		
		// Retrieve the list of RHs we know about in this instance:
		
		ArrayList<ReturnedRHMetaInformation> rhmil = CarAdminUtils.getAllDefinedResourceHolders();
		
		for (ReturnedRHMetaInformation rh : rhmil) {
			ArrayList<ReturnedRPMetaInformation> rpmil = CarAdminUtils.getAllRPsForRH(rh.getRhidentifier().getRhtype(), rh.getRhidentifier().getRhid());
			if (rpmil == null) 
				rpmil = new ArrayList<ReturnedRPMetaInformation>();  // non-nullify
			
			rhrpmap.put(rh, rpmil);
			
			ArrayList<String> urlfrags = new ArrayList<String>();
			
			for (ReturnedRPMetaInformation rpmi : rpmil) {
				urlfrags.add(CarAdminUtils.idEscape(rpmi.getRpidentifier().getRptype()) + "/" + CarAdminUtils.idEscape(rpmi.getRpidentifier().getRpid())+"|"+((rpmi.getDisplayname()==null)?rpmi.getRpidentifier().getRpid():CarAdminUtils.localize(rpmi.getDisplayname(),lang)));
			}
			Collections.sort(urlfrags);
			irdplist.put(rh.getRhidentifier().getRhtype()+"|"+rh.getRhidentifier().getRhid(),urlfrags.toArray(new String[0]));
		}
		
		retval.addObject("rhrpmap",rhrpmap);
		//ObjectMapper om = new ObjectMapper();
		ObjectMapper om = OMSingleton.getInstance().getOm();
		try {
			retval.addObject("irdplist",om.writeValueAsString(irdplist));
		} catch (Exception e) {
			// ignore
		}
		
		
		return retval;
		
	}
}
