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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import edu.internet2.consent.arpsi.model.OrgReturnedPolicy;
import edu.internet2.consent.icm.model.UserReturnedPolicy;
import edu.internet2.consent.icm.model.IcmReturnedPolicy;
import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;

@Controller
public class DashboardController {

	@RequestMapping(value="/",method=RequestMethod.GET)
	public ModelAndView handleDashboardGet(HttpServletRequest req) {
		ModelAndView retval = new ModelAndView("Dashboard");
		AdminConfig config = null;
		if ((config = CarAdminUtils.init(req)) == null) {
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
		
		// First, discover the RHs.  In the process, set up RH stats
		ArrayList<ReturnedRHMetaInformation> rhmia = CarAdminUtils.getAllDefinedResourceHolders();

		ArrayList<InjectedDashboardInfo> iarray = new ArrayList<InjectedDashboardInfo>();
		HashMap<String,InjectedDashboardInfo> imap = new HashMap<String,InjectedDashboardInfo>();
		
		for (ReturnedRHMetaInformation ri : rhmia) {
			// for each of the RHs
			InjectedDashboardInfo idi = new InjectedDashboardInfo();

			idi.setRhtype(ri.getRhidentifier().getRhtype());
			idi.setRhid(ri.getRhidentifier().getRhid());
			
			idi.setDisplayname(ri.getDisplayname());
			idi.setDescription(ri.getDescription());
			
			// Now we need to compute some values for this RH
			// Start with the number of IIs supported by this RH
			
			ReturnedRHInfoItemList rril = CarAdminUtils.getIiList(ri.getRhidentifier());
			if (rril != null) {
				idi.setInfoitemcount(rril.getInfoitemlist().size());
			} else {
				idi.setInfoitemcount(0);
			}
			
			// And the number of RPs
			ArrayList<ReturnedRPMetaInformation> armi = CarAdminUtils.getAllRPsForRH(ri.getRhidentifier().getRhtype(),ri.getRhidentifier().getRhid());
			if (armi != null) {
				idi.setRpcount(armi.size());
			} else {
				idi.setRpcount(0);
			}
			
			// And get user policy information for this thing (ugh)
			ArrayList<UserReturnedPolicy> rup = CarAdminUtils.getUserPoliciesForRH(ri.getRhidentifier().getRhtype(),ri.getRhidentifier().getRhid());
			
			if (rup != null) {
				idi.setUserpolcount(rup.size());
			} else {
				idi.setUserpolcount(0);
			}
			// Compute the in-use RP count by populating a list of them
			HashSet<String> activerps = new HashSet<String>();
			HashSet<String> users = new HashSet<String>();
			if (rup != null) {
				for (UserReturnedPolicy up : rup) {
					activerps.add(up.getUserInfoReleasePolicy().getRelyingPartyId().getRPvalue());
					users.add(up.getUserInfoReleasePolicy().getUserId().getUserValue());
				}
				idi.setActiverpcount(activerps.size());
				// And the total user count
				idi.setUsercount(users.size());
			} else {
				idi.setActiverpcount(0);
				idi.setUsercount(0);
			}
			
			// Get the number of ARPSI policies currently active for the RH
			ArrayList<OrgReturnedPolicy> orp = CarAdminUtils.getArpsiPoliciesForRH(ri.getRhidentifier().getRhtype(),ri.getRhidentifier().getRhid());

			if (orp != null) {
				idi.setArpsipolcount(orp.size());
			} else {
				idi.setArpsipolcount(0);
			}
			
			// And the number of CARMA policies
			
			ArrayList<IcmReturnedPolicy> irp = CarAdminUtils.getIcmPoliciesForRH(ri.getRhidentifier().getRhtype(),ri.getRhidentifier().getRhid());
			
			if (irp != null) {
				idi.setCarmapolcount(irp.size());
			} else {
				idi.setCarmapolcount(0);;
			}
			
			// And add it
			iarray.add(idi);
			
			// And add it again
			imap.put(idi.getRhid(),idi);
		}		
		
		Collections.sort(iarray,new InjectedDashboardInfoComparator());
		
		ArrayList<ReturnedRHMetaInformation> availablerhs = CarAdminUtils.getAllDefinedResourceHolders();
		Collections.sort(availablerhs,new ReturnedRHMetaInformationComparator());
		
		HashMap<String,String> rhdispmap = new HashMap<String,String>();
		HashMap<String,String> rhorgcountmap = new HashMap<String,String>();
		HashMap<String,String> rhicmcountmap = new HashMap<String,String>();
		
		String lang = "en";
		if (req.getLocale() != null && req.getLocale().getLanguage() != null) {
			lang = req.getLocale().getLanguage();
		}

		for (ReturnedRHMetaInformation rmi : availablerhs) {
			rhdispmap.put(rmi.getRhidentifier().getRhid(),CarAdminUtils.localize(rmi.getDisplayname(),lang));
			ArrayList<OrgReturnedPolicy> aorp = CarAdminUtils.getRHOrgInfoReleasePolicies(rmi.getRhidentifier().getRhtype(),rmi.getRhidentifier().getRhid());
			int ocount = 0;
			if (aorp != null) {
				ocount = aorp.size();
			}
			ArrayList<IcmReturnedPolicy> airp = CarAdminUtils.getRHIcmInfoReleasePolicies(rmi.getRhidentifier().getRhtype(),rmi.getRhidentifier().getRhid());
			int icount = 0;
			if (airp != null) {
				icount = airp.size();
			}
			rhorgcountmap.put(rmi.getRhidentifier().getRhid(), String.valueOf(ocount));
			rhicmcountmap.put(rmi.getRhidentifier().getRhid(), String.valueOf(icount));
		}

		
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		retval.addObject("injected",iarray);
		retval.addObject("imap",imap);
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("activetab","dashboard");
		retval.addObject("rhdisp",rhdispmap);
		retval.addObject("rhorg",rhorgcountmap);
		retval.addObject("rhicm",rhicmcountmap);
		retval.addObject("rhlist",availablerhs);
		
		retval.addObject("lang",lang);
		retval.addObject("availablerhs",availablerhs);
		
		ArrayList<ActivityStreamEntry> aase = CarAdminUtils.getActivityStreamList("admin");
		retval.addObject("activitylist", aase);
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
		retval.addObject("sdf",sdf);
		retval.addObject("date",new Date());
		
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		
		CarAdminUtils.injectStrings(retval, new String[] { "grand_totals_heading",
														  "rh_total_heading",
														  "info_items_heading",
														  "rps_label",
														  "active_rps_label",
														  "users_label",
														  "policies_label",
														  "recent_label",
														  "rh_heading",
														  "opol_count_label",
														  "mpol_count_label",
														  "upol_count_label",
														  "back_link",
														  "top_heading",
														  "sign_out",
														  "top_logo_url"
		});
		return retval;
	}
}
