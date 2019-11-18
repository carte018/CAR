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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.model.AdminRoleMapping;

@Controller
public class ManageAdminRolesController {

	// Thin UI for managing admin roles in the database
	//
	// Superadmins and admins listed in the config file can manage roles here.
	//
	
	@RequestMapping(value="/manageadmins",method=RequestMethod.GET)
	public ModelAndView getManageAdmins(HttpServletRequest req) {
		
		ModelAndView retval = new ModelAndView("AdminRoles");
		
		// Check authorization
		
		AdminConfig config = null;
		
		// First check for superadmin status via the init routine, 
		// then (failing that) get a config instance and check for
		// config admin rights.
		
		boolean authorized = false;
		
		ArrayList<String> roles = new ArrayList<String>();
		roles.add("superadmin");
		
		config = CarAdminUtils.init(req,roles,null);
		
		if (config == null) {
			// See if there's an override
			config = AdminConfig.getInstance();
			String admineppns = null;
			admineppns = config.getProperty("AdminEPPNs", false);
			if (admineppns != null) {
				String eppn = ((String) req.getAttribute("eppn")).replaceAll(";.*$","");
				String remote = req.getRemoteUser();
				String[] ae = admineppns.split(",");
				for (String a : ae) {
					if ((eppn != null && a.equals(eppn)) || (remote != null && a.contentEquals(remote))) {
						authorized=true;
					}
				}
			}
		} else {
			authorized = true;
		}
		
		if (! authorized) {
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
			err.addObject("logouturl","/Shibboleth.sso/Logout");  // config failure precludesusing config'd logouturl
			CarAdminUtils.injectStrings(err, new String[] { 
					  "top_heading",
					  "sign_out",
					  "top_logo_url"
			});
			err.addObject("message",CarAdminUtils.getLocalComponent("unauthorized_msg"));
			return err;
		}
		
		// We're authorized -- inject the current information and return the template
		
		String lang = "en";
		if (req.getLocale() != null && req.getLocale().getLanguage() != null) {
			lang = req.getLocale().getLanguage();
		}
		
		ArrayList<AdminRoleMapping> arm = new ArrayList<AdminRoleMapping>();  // null protect
		
		ArrayList<AdminRoleMapping> rs = CarAdminUtils.getAdminRoles(null,null,null);  // get all admin roles
		
		arm.addAll(rs);
		
		retval.addObject("arm",arm);
		
		// Convenience for the template
		ArrayList<AdminRoleMapping> rhreg = new ArrayList<AdminRoleMapping>();
		ArrayList<AdminRoleMapping> delrhreg = new ArrayList<AdminRoleMapping>();
		ArrayList<AdminRoleMapping> rpreg = new ArrayList<AdminRoleMapping>();
		ArrayList<AdminRoleMapping> delrpreg = new ArrayList<AdminRoleMapping>();
		ArrayList<AdminRoleMapping> poladmin = new ArrayList<AdminRoleMapping>();
		ArrayList<AdminRoleMapping> delpoladmin = new ArrayList<AdminRoleMapping>();
		ArrayList<AdminRoleMapping> translator = new ArrayList<AdminRoleMapping>();
		ArrayList<AdminRoleMapping> superadmin = new ArrayList<AdminRoleMapping>();
		
		ArrayList<AdminRoleMapping> rhauditor = new ArrayList<AdminRoleMapping>();
		ArrayList<AdminRoleMapping> delrhauditor = new ArrayList<AdminRoleMapping>();
		ArrayList<AdminRoleMapping> rpauditor = new ArrayList<AdminRoleMapping>();
		ArrayList<AdminRoleMapping> delrpauditor = new ArrayList<AdminRoleMapping>();
		
		
		for (AdminRoleMapping a : arm) {
			if (a.getRoleName().contentEquals("superadmin"))
				superadmin.add(a);
			else if (a.getRoleName().contentEquals("RHRegistrar"))
				rhreg.add(a);
			else if (a.getRoleName().contentEquals("DelegatedRHRegistrar"))
				delrhreg.add(a);
			else if (a.getRoleName().contentEquals("RPRegistrar"))
				rpreg.add(a);
			else if (a.getRoleName().contentEquals("DelegatedRPRegistrar"))
				delrpreg.add(a);
			else if (a.getRoleName().contentEquals("PolicyAdmin"))
				poladmin.add(a);
			else if (a.getRoleName().contentEquals("DelegatedPolicyAdmin"))
				delpoladmin.add(a);
			else if (a.getRoleName().contentEquals("Translator")) 
				translator.add(a);
			else if (a.getRoleName().contentEquals("RHAuditor"))
				rhauditor.add(a);
			else if (a.getRoleName().contentEquals("DelegatedRHAuditor"))
				delrhauditor.add(a);
			else if (a.getRoleName().contentEquals("RPAuditor"))
				rpauditor.add(a);
			else if (a.getRoleName().contentEquals("DelegatedRPAuditor"))
				delrpauditor.add(a);
			
		}
		
		retval.addObject("superadmin",superadmin);
		retval.addObject("rhreg",rhreg);
		retval.addObject("delrhreg",delrhreg);
		retval.addObject("rpreg",rpreg);
		retval.addObject("delrpreg",delrpreg);
		retval.addObject("poladmin",poladmin);
		retval.addObject("delpoladmin",delpoladmin);
		retval.addObject("translator",translator);
		retval.addObject("rhauditor",rhauditor);
		retval.addObject("delrhauditor",delrhauditor);
		retval.addObject("rpauditor",rpauditor);
		retval.addObject("delrpauditor",delrpauditor);
		
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
	
		return retval;
	}
	
	@RequestMapping(value="/manageadmins",method=RequestMethod.POST)
	public ModelAndView postManageAdmins(HttpServletRequest req) {
		
		ModelAndView retval = new ModelAndView("redirect:/manageadmins");
		
		// authorization checks
		
		AdminConfig config = null;
		
		// First check for superadmin status via the init routine, 
		// then (failing that) get a config instance and check for
		// config admin rights.
		
		boolean authorized = false;
		
		ArrayList<String> roles = new ArrayList<String>();
		roles.add("superadmin");
		
		config = CarAdminUtils.init(req,roles,null);
		
		if (config == null) {
			// See if there's an override
			config = AdminConfig.getInstance();
			String admineppns = null;
			admineppns = config.getProperty("AdminEPPNs", false);
			if (admineppns != null) {
				String eppn = ((String) req.getAttribute("eppn")).replaceAll(";.*$","");
				String remote = req.getRemoteUser();
				String[] ae = admineppns.split(",");
				for (String a : ae) {
					if ((eppn != null && a.equals(eppn)) || (remote != null && a.contentEquals(remote))) {
						authorized=true;
					}
				}
			}
		} else {
			authorized = true;
		}
		
		if (! authorized) {
			ModelAndView err = new ModelAndView("errorPage");
			err.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
			err.addObject("logouturl","/Shibboleth.sso/Logout");  // config failure precludesusing config'd logouturl
			CarAdminUtils.injectStrings(err, new String[] { 
					  "top_heading",
					  "sign_out",
					  "top_logo_url"
			});
			err.addObject("message",CarAdminUtils.getLocalComponent("unauthorized_msg"));
			return err;
		}
		
		// We are authorized
		
		// Determine the nature of the request being made -- add or delete, and to what
		// roletype is one of "superadmin", "RHRegistrar", "DelegatedRHRegistrar", 
		// "RPRegistrar","DelegatedRPRegistrar","PolicyAdmin","DelegatedPolicyAdmin", or
		// "Translator".  Anything else will be ignored.
		//
		// Now also with "RHAuditor", "DelegatedRHAuditor", "RPAuditor", and "DelegatedRPAuditor"
		//
		
		String roletype = req.getParameter("roletype");
		
		// Handle each case separately
		
		String prefix = "";
		if (roletype.contentEquals("superadmin"))
			prefix = "super";
		else if (roletype.contentEquals("RHRegistrar"))
			prefix = "rhr";
		else if (roletype.contentEquals("DelegatedRHRegistrar"))
			prefix = "drhr";
		else if (roletype.contentEquals("RPRegistrar"))
			prefix = "rpr";
		else if (roletype.contentEquals("DelegatedRPRegistrar"))
			prefix = "drpr";
		else if (roletype.contentEquals("PolicyAdmin"))
			prefix="pa";
		else if (roletype.contentEquals("DelegatedPolicyAdmin"))
			prefix="dpa";
		else if (roletype.contentEquals("Translator"))
			prefix="trans";
		else if (roletype.contentEquals("RHAuditor"))
			prefix="rha";
		else if (roletype.contentEquals("DelegatedRHAuditor"))
			prefix="drha";
		else if (roletype.contentEquals("RPAuditor"))
			prefix="rpa";
		else if (roletype.contentEquals("DelegatedRPAuditor"))
			prefix="drpa";
		else
			return retval;  // this is an invalid request
		
		// Now we have the name and the prefix -- we can process add or delete
		
		if (req.getParameter(prefix+"_is_add").contentEquals("1")) {
			// this is an add
			AdminRoleMapping arm = new AdminRoleMapping();
			arm.setSubject(req.getParameter(prefix+"_subject_add"));
			arm.setTarget(req.getParameter(prefix+"_target_add"));
			arm.setRoleName(roletype);
			CarAdminUtils.postAdminRoleMapping(arm);
			return retval;
		} else {
			// This may be a delete
			if (req.getParameter("ctr") == null) 
				return retval;   // bad response
			
			int counter = Integer.parseInt(req.getParameter("ctr"));
			for (int i = 1; i <= counter; i++) {
				if (req.getParameter(prefix+"_is_delete_"+counter).contentEquals("1")) {
					// This is the delete -- perform and return
					String pid = req.getParameter(prefix+"_id_"+counter);
					CarAdminUtils.deleteAdminRoleMapping(Long.parseLong(pid));
					return retval; // no sense looking any further now
				}
			}
		}
		return retval;

	}
}
