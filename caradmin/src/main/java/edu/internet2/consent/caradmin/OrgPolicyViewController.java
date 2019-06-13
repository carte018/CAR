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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import edu.internet2.consent.arpsi.model.OrgReturnedPolicy;
import edu.internet2.consent.icm.model.IcmReturnedPolicy;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;

@Controller
public class OrgPolicyViewController {
	
    private String sconvo;
    private int convo;


    private String generateCSRFToken() {
            String foo = RandomStringUtils.random(32,true,true);
            String bar = Base64.encodeBase64URLSafeString(foo.getBytes());
            return bar;
    }

	@RequestMapping(value="/orgpolicyview",method=RequestMethod.GET)
	public ModelAndView GetOrgPolicyView(HttpServletRequest req) {
		ModelAndView retval = new ModelAndView("OrgPolicyView");
		//
		// We need to collect the list of RH metainfo objects to inject into the UI template
		ArrayList<ReturnedRHMetaInformation> rhmil = CarAdminUtils.getAllDefinedResourceHolders();
		HashMap<String,String> rhdispmap = new HashMap<String,String>();
		HashMap<String,String> rhorgcountmap = new HashMap<String,String>();
		HashMap<String,String> rhicmcountmap = new HashMap<String,String>();
		
		AdminConfig config = null;
		
		if ((config = CarAdminUtils.init(req)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message",CarAdminUtils.getLocalComponent("unauthorized_msg"));
			return eval;
		}
		
		String lang = req.getLocale().getLanguage();
		if (lang == null) {
			lang = "en"; // default to English for now
		}
		for (ReturnedRHMetaInformation rmi : rhmil) {
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
		
		Collections.sort(rhmil,new ReturnedRHMetaInformationComparator());
		retval.addObject("rhlist",rhmil);
		retval.addObject("availablerhs",rhmil);
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("rhdisp",rhdispmap);
		retval.addObject("rhorg",rhorgcountmap);
		retval.addObject("rhicm",rhicmcountmap);
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		retval.addObject("activetab","policies");
		retval.addObject("lang",req.getLocale().getLanguage());
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		
		CarAdminUtils.injectStrings(retval, new String[] { "rh_heading",
															"opol_count_label",
															"mpol_count_label",
															"top_heading",
															"sign_out",
															"top_logo_url"
															
		});
		return retval;
		
	}
	
	@RequestMapping(value="/orgpolicyview/{rhtype}/{rhid:.+}",method=RequestMethod.POST)
	public ModelAndView handlePostOrgPolicyViewByRH(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhid) {

		//	ModelAndView retval = new ModelAndView("redirect:"+rhid+"?success=Policy Order Updated");
			ModelAndView retval = new ModelAndView("redirect:"+req.getRequestURL().append("?success=Policy Order Updated").toString());
			
			if (CarAdminUtils.init(req) == null) {
				ModelAndView eval = new ModelAndView("errorPage");
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

			
			if (req.getParameter("formid") != null && req.getParameter("formid").equals("metapolmover")) {
				// This is a reordering of meta policies
				ReorderRequest rr = new ReorderRequest();
				rr.setPolicyToChange(req.getParameter("movedid"));
				if (req.getParameter("order") != null && req.getParameter("order").equals("before")) {
					rr.setOperation("moveBefore");
					rr.setPolicy(req.getParameter("beforeid"));
				} else {
					rr.setOperation("moveAfter");
					rr.setPolicy(req.getParameter("afterid"));
				}
				CarAdminUtils.locError("ERR0063",LogCriticality.debug,rr.getPolicyToChange(),rr.getOperation(),rr.getPolicy());
				CarAdminUtils.postIcmPolicyPrecedence(rr);
				return(retval);
			} else if (req.getParameter("formid") != null && req.getParameter("formid").equals("orgpolmover")) {
				// This is a reordering of org policies
				ReorderRequest rr = new ReorderRequest();
				rr.setPolicyToChange(req.getParameter("movedid"));;
				if (req.getParameter("order") != null && req.getParameter("order").equals("before")) {
					rr.setOperation("moveBefore");;
					rr.setPolicy(req.getParameter("beforeid"));
				} else {
					rr.setOperation("moveAfter");;
					rr.setPolicy(req.getParameter("afterid"));
				}
				CarAdminUtils.postOrgPolicyPrecedence(rr);
				return(retval);
			} else if (req.getParameter("formid") != null && req.getParameter("formid").equals("orgpoldelete")) {
				// delete an orgpolicy
				RHIdentifier rhi = new RHIdentifier();
				rhi.setRhid(rhid);
				rhi.setRhtype(rhtype);
				String targ = req.getParameter("baseid");
				CarAdminUtils.archiveOrgPolicy(rhi, targ);
				return(retval);
			} else if (req.getParameter("formid") != null && req.getParameter("formid").equals("metapoldelete")) {
				// delete a metapolicy
				RHIdentifier rhi = new RHIdentifier();
				rhi.setRhid(rhid);
				rhi.setRhtype(rhtype);
				String targ = req.getParameter("baseid");
				CarAdminUtils.archiveMetaPolicy(rhi,targ);
				return(retval);
			} else {
				return(retval);
			}
	}		
	
	@RequestMapping(value="/orgpolicyview/{rhtype}/{rhid:.+}", method=RequestMethod.GET)
	public ModelAndView handleGetOrgPolicyViewByRH(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhid) {
		ModelAndView retval = new ModelAndView("OrgPolicyViewByRH");
		AdminConfig config = null;
		
		if ((config = CarAdminUtils.init(req)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
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
        
		// This is the view-by-RH tool, so start by collecting the RH metainfo we need
		//
		ReturnedRHMetaInformation rhmi = CarAdminUtils.getResourceHolderMetaInformation(rhtype, CarAdminUtils.idUnEscape(rhid));
		String lang = req.getLocale().getLanguage();
		if (lang == null) {
			lang = "en";  // default to English for now
		}
		

		String rhdisplayname = CarAdminUtils.localize(rhmi.getDisplayname(),lang);


		// And retrieve the full set of org-info-release-policies for this RH (which may be very large)
		// The request work lives in CarAdminUtils here...
		
		ArrayList<OrgReturnedPolicy> policyset = CarAdminUtils.getRHOrgInfoReleasePolicies(rhtype,CarAdminUtils.idUnEscape(rhid));

		// And sort the policyset array in the order of precedence application
		if (policyset != null && ! policyset.isEmpty())
			Collections.sort(policyset,new OrgPolicyComparator());
		
		ArrayList<ReturnedRHMetaInformation> rhmil = CarAdminUtils.getAllDefinedResourceHolders();
		Collections.sort(rhmil,new ReturnedRHMetaInformationComparator());

		ArrayList<IcmReturnedPolicy> icmset = CarAdminUtils.getRHIcmInfoReleasePolicies(rhtype, CarAdminUtils.idUnEscape(rhid));
		
		if (icmset != null && ! icmset.isEmpty())
			Collections.sort(icmset,new IcmPolicyComparator());
		
		retval.addObject("rhdisplayname",rhdisplayname);
		retval.addObject("language",lang);
		retval.addObject("lang",lang);
		retval.addObject("availablerhs",rhmil);
		retval.addObject("rhmi",rhmi);
		retval.addObject("policies",policyset);
		retval.addObject("icmpolicies",icmset);
		retval.addObject("daterec",new Date());
		retval.addObject("formatrec",new SimpleDateFormat());
		retval.addObject("rhidtypes",CarAdminUtils.getSupportedRHIDTypes());
		retval.addObject("iitypes",CarAdminUtils.getSupportedIITypes());
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("activetab","rhregistration");
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		retval.addObject("listsortingdnd","1");
		retval.addObject("logouturl",config.getProperty("logouturl", false));

		
		if (req.getParameter("state") != null) {
			if (req.getParameter("state").equals("0")) {
				// failure
				if (req.getParameter("component").equals("addmetapolicy")) {
					retval.addObject("failmsg","Failed to add new meta policy -- check input values");
				} else if (req.getParameter("component").equals("addorgpolicy")) {
					retval.addObject("failmsg","Failed to add new institutional policy -- check input values");
				} else if (req.getParameter("component").equals("updateorgpolicy")) {
					retval.addObject("failmsg","Failed to update institutional policy");
				} else if (req.getParameter("component").equals("updatemetapolicy")) {
					retval.addObject("failmsg","Failed to update meta policy");
				}
			} else if (req.getParameter("state").equals("1")) {
				// success
				if (req.getParameter("component").equals("addmetapolicy")) {
					retval.addObject("successmsg","Successfully added new meta policy");
				} else if (req.getParameter("component").equals("addorgpolicy")) {
					retval.addObject("successmsg","Successfully added new org policy");
				} else if (req.getParameter("component").equals("updateorgpolicy")) {
					retval.addObject("successmsg","Successfully updated org policy");
				} else if (req.getParameter("component").equals("updatemetapolicy")) {
					retval.addObject("successmsg","Successfully updated meta policy");
				}
			}
		} else if (req.getParameter("success") != null) {
			retval.addObject("successmsg",req.getParameter("success"));
		}
		
		CarAdminUtils.injectStrings(retval, new String[] { "managing_heading",
															"instructions_body_orgpol_view",
															"add_orgpol_label",
															"add_mpol_label",
															"instpol_heading",
															"metainfo_heading",
															"created_label",
															"status_label",
															"pid_label",
															"version_label",
															"affected_heading",
															"property_label",
															"matching_label",
															"target_heading",
															"directives_heading",
															"item_type_heading",
															"item_id_heading",
															"directive_heading",
															"values_heading",
															"basis_heading",
															"other_values_label",
															"all_values_label",
															"unspecified_label",
															"any_request_label",
															"mpol_count_label",
															"top_heading",
															"sign_out",
															"top_logo_url"
															
		});
		return retval;
		
	}
}
