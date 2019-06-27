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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.icm.model.AllOtherInfoId;
import edu.internet2.consent.icm.model.AllOtherInfoTypeConst;
import edu.internet2.consent.icm.model.AllOtherInfoValueConst;
import edu.internet2.consent.icm.model.AllOtherValuesConst;
import edu.internet2.consent.icm.model.IcmAllOtherInfoReleaseStatement;
import edu.internet2.consent.icm.model.IcmDirectiveAllOtherValues;
import edu.internet2.consent.icm.model.IcmDirectiveOnValues;
import edu.internet2.consent.icm.model.IcmInfoReleasePolicy;
import edu.internet2.consent.icm.model.IcmInfoReleaseStatement;
import edu.internet2.consent.icm.model.IcmReleaseDirective;
import edu.internet2.consent.icm.model.InfoId;
import edu.internet2.consent.icm.model.RelyingPartyProperty;
import edu.internet2.consent.icm.model.ResourceHolderId;
import edu.internet2.consent.icm.model.UserProperty;
import edu.internet2.consent.icm.model.ValueObject;
import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.ReturnedInfoTypeList;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;

@Controller
public class AddMetaPolicyController {

	private String sconvo;
	private int convo;
	
	
	private String generateCSRFToken() {
		String foo = RandomStringUtils.random(32,true,true);
		String bar = Base64.encodeBase64URLSafeString(foo.getBytes());
		return bar;
	}
	
	@RequestMapping(value="/addmetapolicy/{rhtype}/{rhid:.+}",method=RequestMethod.POST)
	public ModelAndView handlePostAddMetaPolicy(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhidin) {
		String returl = "redirect:/orgpolicyview/"+rhtype+"/"+rhidin;
		ModelAndView retval = null;
		// Only PAs can add policies
		
		ArrayList<String> roles = new ArrayList<String>();
		ArrayList<String> targets = new ArrayList<String>();
		
		roles.add("PolicyAdmin");
		roles.add("DelegatedPolicyAdmin");
		targets.add(CarAdminUtils.idUnEscape(rhidin));
		if (CarAdminUtils.init(req) == null) {
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
		
		if (req.getParameter("formname") != null && req.getParameter("formname").equals("newopform")) {
			IcmInfoReleasePolicy iirp = new IcmInfoReleasePolicy();
			
			if (req.getParameter("policydescription") != null) {
				iirp.setDescription(req.getParameter("policydescription"));
			} else {
				iirp.setDescription("");  // Must have a descripition of some sort
			}
			
			ResourceHolderId rhi = new ResourceHolderId();
			rhi.setRHType(rhtype);
			rhi.setRHValue(CarAdminUtils.idUnEscape(rhidin));
			
			iirp.setResourceHolderId(rhi);
			
			if (req.getParameter("rpstrategy") == null || req.getParameter("rpstrategy").equals("")) {
				// Failed to provide RP strategy -- error out
				retval = new ModelAndView(returl + "?state=0&component=addorgpolicy");
				return retval;
			} else {
				// Dependent on settings, establish an RP match string
				String matchProperty = null;
				String matchValue = null;
				if (req.getParameter("rpstrategy").equals("allRPs")) {
					if (req.getParameter("specrptype1") == null || req.getParameter("specrptype1").equals("")) {
						// Failed to provide rp type in all rps
						retval = new ModelAndView(returl+"?state=0&component=addorgpolicy");
						return retval;
					} else {
						matchProperty = req.getParameter("specrptype1");
						matchValue = "^.*$";  // match any value
					}
				} else if (req.getParameter("rpstrategy").equals("matchedRPs")) {
					if (req.getParameter("rpproperty") == null || req.getParameter("rpproperty").equals("") || req.getParameter("rpmatch") == null) {
						// Failed to provide rp property to match
						retval = new ModelAndView(returl + "?state=0&component=addorgpolicy");
						return retval;
					} else {
						matchProperty = req.getParameter("rpproperty");
						matchValue = req.getParameter("rpmatch");  // this may be empty -- functionally irrelevant in that case, but legal
					}
				} else if (req.getParameter("rpstrategy").equals("oneRP")) {
					if (req.getParameter("specrptype") == null || req.getParameter("specrptype").equals("") || req.getParameter("specrpid") == null || req.getParameter("specrpid").equals("")) {
						// Failed to provide rp specifier
						retval = new ModelAndView(returl + "?state=0&component=addorgpolicy");
						return retval;
					} else {
						matchProperty = req.getParameter("specrptype");
						matchValue = req.getParameter("specrpid");
					}
				}
				
				// And apply to the policy
				ArrayList<RelyingPartyProperty> arpp = new ArrayList<RelyingPartyProperty>();
				RelyingPartyProperty rpp = new RelyingPartyProperty();
				rpp.setRpPropName(matchProperty);
				rpp.setRpPropValue(matchValue);
				arpp.add(rpp);
				
				iirp.setRelyingPartyPropertyArray(arpp);
				
			}
			
			// Now user strategy
			
			if (req.getParameter("userstrategy") == null || req.getParameter("userstrategy").equals("")) {
				//fail
				retval = new ModelAndView(returl + "?state=0&component=addorgpolicy");
				return retval;
			} else {
				// Similar process for managing user strategy for this policy
				String matchProperty = null;
				String matchValue = null;
				if (req.getParameter("userstrategy").equals("allUsers")) {
					if (req.getParameter("userproperty1") == null || req.getParameter("userproperty1").equals("")) {
						// Fail
						retval = new ModelAndView(returl + "?state=0&component=addorgpolicy");
						return retval;
					} else {
						matchProperty = req.getParameter("userproperty1");
						matchValue = "^.*$";
					}
				} else if (req.getParameter("userstrategy").equals("matchedUsers")) {
					if (req.getParameter("userproperty") == null || req.getParameter("userproperty").equals("") || req.getParameter("usermatch") == null || req.getParameter("usermatch").equals("")) {
						// Fail
						retval = new ModelAndView(returl + "?state=0&component=addorgpolicy");
						return retval;
					} else {
						matchProperty = req.getParameter("userproperty");
						matchValue = req.getParameter("usermatch");
					}
				}
				
				ArrayList<UserProperty> aup = new ArrayList<UserProperty>();
				UserProperty up = new UserProperty();
				up.setUserPropName(matchProperty);
				up.setUserPropValue(matchValue);
				aup.add(up);
				
				iirp.setUserPropertyArray(aup);
			}
			
			// And the relevant info release statements.
			// This is complex.
			
			ArrayList<IcmInfoReleaseStatement> aiirs = new ArrayList<IcmInfoReleaseStatement>();
			HashMap<String,IcmInfoReleaseStatement> hasone = new HashMap<String,IcmInfoReleaseStatement>();
			
			if (req.getParameter("directivecount") == null || req.getParameter("directivecount").equals("")) {
				// fail
				retval = new ModelAndView(returl + "?state=0&component=addorgpolicy");
				return retval;
			}
			
			int directivecount = Integer.parseInt(req.getParameter("directivecount"));
			for (int i = 1; i <= directivecount; i++) {
				String itype = req.getParameter("itype_"+i);
				String iid = req.getParameter("iid_"+i);
				String directive = req.getParameter("directive_"+i);
				String values = req.getParameter("values_"+i);
				String aodirective = req.getParameter("directive_ao_"+i);
				
				// Deal with the case in which there are multiple rows for the same attribute 
				// Current front-end implementation solves this a different way, but it's worthwhile 
				// in case that changes to include this rather cheap bit of code, just in case.
				
				IcmInfoReleaseStatement irs = null;
				if (! hasone.containsKey(itype+":"+iid)) {
					irs = new IcmInfoReleaseStatement();
					InfoId ii = new InfoId();
					ii.setInfoType(itype);
					ii.setInfoValue(iid);
					irs.setInfoId(ii);
					hasone.put(itype+":"+iid, irs);
				} else {
					irs = hasone.get(itype+":"+iid);
				}
				
				if (irs.getArrayOfIcmDirectiveOnValues() == null) {
					irs.setArrayOfIcmDirectiveOnValues((List<IcmDirectiveOnValues>) new ArrayList<IcmDirectiveOnValues>());
				}
				
				IcmDirectiveOnValues idov = new IcmDirectiveOnValues();
				if (directive.equals("COPSU")) {
					idov.setOrgReleaseDirective(IcmReleaseDirective.COPSU);
				} else {
					idov.setOrgReleaseDirective(IcmReleaseDirective.ARPSI);
				}
				
				ArrayList<ValueObject> avo = new ArrayList<ValueObject>();
				ValueObject vo = new ValueObject();
				vo.setValue(values);
				avo.add(vo);
				idov.setValueObjectList(avo);
				
				irs.getArrayOfIcmDirectiveOnValues().add(idov);
				
				// If there are subitems for this item, include them in the directive on values
				//
				
				if (req.getParameter("addcount_"+i) != null && Integer.parseInt(req.getParameter("addcount_"+i)) > 0) {
					// Repeat the process for each subelement that has not been flagged as deleted in the form
					for (int j = 1; j <= Integer.parseInt(req.getParameter("addcount_"+i)); j++) {
						@SuppressWarnings("unused")
						String subitype = req.getParameter("itype_"+i+"_"+j);
						@SuppressWarnings("unused")
						String subiid = req.getParameter("iid_"+i+"_"+j);
						String subdirective = req.getParameter("directive_"+i+"_"+j);
						String subvalues = req.getParameter("values_"+i+"_"+j);
						
						if (req.getParameter("deleted_"+i+"_"+j) != null && req.getParameter("deleted_"+i+"_"+j).equals("true")) {
							continue;  // skip deleted entries
						}
						IcmDirectiveOnValues subidov = new IcmDirectiveOnValues();
						if (subdirective.equals("COPSU")) {
							subidov.setOrgReleaseDirective(IcmReleaseDirective.COPSU);
						} else {
							subidov.setOrgReleaseDirective(IcmReleaseDirective.ARPSI);
						}
						
						ArrayList<ValueObject> subavo = new ArrayList<ValueObject>();
						ValueObject subvo = new ValueObject();
						subvo.setValue(subvalues);
						subavo.add(subvo);
						subidov.setValueObjectList(subavo);
						
						irs.getArrayOfIcmDirectiveOnValues().add(subidov);
						
					}
				}
				
				IcmDirectiveAllOtherValues idaov = new IcmDirectiveAllOtherValues();
				idaov.setAllOtherValuesConst(AllOtherValuesConst.allOtherValues);
				if (aodirective.equals("COPSU")) {
					idaov.setOrgReleaseDirective(IcmReleaseDirective.COPSU);
				} else {
					idaov.setOrgReleaseDirective(IcmReleaseDirective.ARPSI);
				}
				
				irs.setIcmDirectiveAllOtherValues(idaov);
				
			}
			
			// Reap from hasone hash
			for(String key : hasone.keySet()) {
				aiirs.add(hasone.get(key));
				CarAdminUtils.locError("ERR0058",LogCriticality.debug,String.valueOf(hasone.get(key).getArrayOfIcmDirectiveOnValues().size()));
			}
			
			CarAdminUtils.locError("ERR0057",LogCriticality.debug,String.valueOf(hasone.keySet().size()));
			ObjectMapper om = new ObjectMapper();
			try {
				CarAdminUtils.locError("ERR0060",LogCriticality.debug,om.writeValueAsString(aiirs));
			} catch (Exception e) {
					// ignore
			}
			iirp.setArrayOfInfoReleaseStatement(aiirs);
			
			if (req.getParameter("all-other-items-directive") != null && ! req.getParameter("all-other-items-directive").equals("continue")) {
				// Add one
				IcmAllOtherInfoReleaseStatement aoirs = new IcmAllOtherInfoReleaseStatement();
				IcmDirectiveAllOtherValues idaov = new IcmDirectiveAllOtherValues();
				idaov.setAllOtherValuesConst(AllOtherValuesConst.allOtherValues);
				if (req.getParameter("all-other-items-directive").equals("COPSU")) {
					idaov.setOrgReleaseDirective(IcmReleaseDirective.COPSU);
				} else {
					idaov.setOrgReleaseDirective(IcmReleaseDirective.ARPSI);
				}
				
				aoirs.setIcmDirectiveAllOtherValues(idaov);
				
				AllOtherInfoId aoi = new AllOtherInfoId();
				aoi.setAllOtherInfoType(AllOtherInfoTypeConst.allOtherInfoType);
				aoi.setAllOtherInfoValue(AllOtherInfoValueConst.allOtherInfoValue);
				aoirs.setAllOtherInfoId(aoi);
				
				iirp.setAllOtherOrgInfoReleaseStatement(aoirs);
			}
			// And send it to the database
			try {
				CarAdminUtils.locError("ERR0059",LogCriticality.debug,om.writeValueAsString(iirp));
			} catch (Exception e) {
				// ignore
			}
			CarAdminUtils.postIcmInfoReleasePolicy(iirp);
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("added meata policy '" + req.getParameter("policydescription") + "' to " + CarAdminUtils.idUnEscape(rhidin));
			CarAdminUtils.postActivityStreamEntry(ase);
			
			retval = new ModelAndView(returl + "?state=1&component=addorgpolicy");
			
			return retval;
		}
		return retval;
	}
	
	@RequestMapping(value="/addmetapolicy/{rhtype}/{rhid:.+}",method=RequestMethod.GET)
	public ModelAndView handleGetAddOrgPolicy(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhidin) {
		ModelAndView retval = new ModelAndView("AddMetaPolicy");
		AdminConfig config = null;

		// Only policy admins (and delegated policy admins) can add policies to an RH
		
		ArrayList<String> roles = new ArrayList<String>();
		ArrayList<String> targets = new ArrayList<String>();
		
		roles.add("PolicyAdmin");
		roles.add("DelegatedPolicyAdmin");
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
		
		String rhid = CarAdminUtils.idUnEscape(rhidin);
		
		RHIdentifier rhi = new RHIdentifier();
		rhi.setRhtype(rhtype);;
		rhi.setRhid(rhid);
		
		// Get a list of the relevant RH's supported item types
		ReturnedInfoTypeList supptypes = CarAdminUtils.getInfoTypes(rhi);
		
		HashMap<String,ArrayList<String>> typeidmap = new HashMap<String,ArrayList<String>>();
		
		for (String type : supptypes.getInfotypes()) {
			ArrayList<String> val = new ArrayList<String>();
			ReturnedRHInfoItemList idlist = CarAdminUtils.getIiList(rhi);
			for (InfoItemIdentifier iii : idlist.getInfoitemlist()) {
				if (iii.getIitype().equals(type))
					val.add(iii.getIiid());
			}
			typeidmap.put(type,val);
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
		
		ReturnedRHMetaInformation rhmi = CarAdminUtils.getResourceHolderMetaInformation(rhtype, rhid);
		
		ArrayList<ReturnedRHMetaInformation> arrm = CarAdminUtils.getAllDefinedResourceHolders();
		Collections.sort(arrm,new ReturnedRHMetaInformationComparator());
		
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("availablerhs",arrm);
		retval.addObject("activetab","rhregistration");
		retval.addObject("typeidmap",typeidmap);
		retval.addObject("supptypes",supptypes);
		retval.addObject("languages",CarAdminUtils.getSupportedLanguages());
		retval.addObject("lang",req.getLocale().getLanguage());
		
		retval.addObject("rhmi",rhmi);
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		
		ArrayList<String> supportedRPTypes = new ArrayList<String>();
		ArrayList<ReturnedRPMetaInformation> rps = CarAdminUtils.getAllRPsForRH(rhtype, rhid);
		ArrayList<String> rpids = new ArrayList<String>();
		for (ReturnedRPMetaInformation rpmi : rps) {
			if (! supportedRPTypes.contains(rpmi.getRpidentifier().getRptype()))
				supportedRPTypes.add(rpmi.getRpidentifier().getRptype());
			rpids.add(rpmi.getRpidentifier().getRpid());
		}
		
		retval.addObject("supportedrptypes",supportedRPTypes);
		retval.addObject("rpids",rpids);
		
		retval.addObject("logouturl",config.getProperty("logouturl",false));
		
		if (req.getParameter("state") != null) {
			if (req.getParameter("state").equals("0")) {
				// failure
				if (req.getParameter("component").equals("addmetapolicy")) {
					retval.addObject("failmsg","Failed to add new meta policy -- check input values");
				}
			} else if (req.getParameter("state").equals("1")) {
				// success
				if (req.getParameter("component").equals("addmetapolicy")) {
					retval.addObject("successmsg","Successfully added new meta policy");
				}
			}
		}
		CarAdminUtils.injectStrings(retval, new String[] { "instructions_heading",
															"instructions_body_metapol_add",
															"policy_descr_label",
															"rps_label",
															"apply_heading",
															"all_rps_label",
															"all_rps2_label",
															"rps_with_label",
															"some_rps_label",
															"specific_rp_label",
															"one_rp_label",
															"users_any_label",
															"all_users_label",
															"users_with_label",
															"some_users_label",
															"item_type_heading",
															"item_id_heading",
															"directives_heading",
															"directive_heading",
															"values_heading",
															"basis_heading",
															"other_values_label",
															"add_item_label",
															"all_other_heading",
															"create_policy_label",
															"matching_label",
															"top_heading",
															"sign_out",
															"top_logo_url"});
															
		return retval;
	}
}
