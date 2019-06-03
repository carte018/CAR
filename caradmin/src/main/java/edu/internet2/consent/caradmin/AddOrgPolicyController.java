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

import edu.internet2.consent.arpsi.model.AllOtherInfoId;
import edu.internet2.consent.arpsi.model.AllOtherInfoTypeConst;
import edu.internet2.consent.arpsi.model.AllOtherInfoValueConst;
import edu.internet2.consent.arpsi.model.AllOtherOrgInfoReleaseStatement;
import edu.internet2.consent.arpsi.model.AllOtherValuesConst;
import edu.internet2.consent.arpsi.model.InfoId;
import edu.internet2.consent.arpsi.model.OrgDirectiveAllOtherValues;
import edu.internet2.consent.arpsi.model.OrgDirectiveOnValues;
import edu.internet2.consent.arpsi.model.OrgInfoReleasePolicy;
import edu.internet2.consent.arpsi.model.OrgInfoReleaseStatement;
import edu.internet2.consent.arpsi.model.OrgReleaseDirective;
import edu.internet2.consent.arpsi.model.RelyingPartyProperty;
import edu.internet2.consent.arpsi.model.ResourceHolderId;
import edu.internet2.consent.arpsi.model.UserProperty;
import edu.internet2.consent.arpsi.model.ValueObject;
import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.ReturnedInfoTypeList;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;

@Controller
public class AddOrgPolicyController {
	
	@RequestMapping(value="/addorgpolicy/{rhtype}/{rhid:.+}",method=RequestMethod.POST)
	public ModelAndView handlePostAddOrgPolicy(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhidin) {
		String returl = "redirect:/orgpolicyview/"+rhtype+"/"+rhidin;
		ModelAndView retval = null;
		
		if (CarAdminUtils.init(req) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		if (req.getParameter("formname") != null && req.getParameter("formname").equals("newopform")) {
			OrgInfoReleasePolicy oirp = new OrgInfoReleasePolicy();
			if (req.getParameter("policydescription") != null) {
				oirp.setDescription(req.getParameter("policydescription"));
			} else {
				oirp.setDescription("");  // Must have a descripition of some sort
			}
			
			ResourceHolderId rhi = new ResourceHolderId();
			
			rhi.setRHType(rhtype);
			rhi.setRHValue(CarAdminUtils.idUnEscape(rhidin));
			
			oirp.setResourceHolderId(rhi);
			
			if (req.getParameter("rpstrategy") == null || req.getParameter("rpstrategy").equals("")) {
				// Failed to provide RP strategy -- error out
				retval = new ModelAndView(returl + "?state=0&component=addorgpolicy");
				return retval;
			} else {
				// Based on strategy, set up the policy parameters for RP selection
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
				
				// At this point matchProperty and MatchValue are sete for the policy
				ArrayList<RelyingPartyProperty> arpp = new ArrayList<RelyingPartyProperty>();
				RelyingPartyProperty rpp = new RelyingPartyProperty();
				rpp.setRpPropName(matchProperty);
				rpp.setRpPropValue(matchValue);
				arpp.add(rpp);
				
				oirp.setRelyingPartyPropertyArray(arpp);	
			}
		
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
				
				oirp.setUserPropertyArray(aup);
			}
			
			// Now for the info release statements
			ArrayList<OrgInfoReleaseStatement> airs = new ArrayList<OrgInfoReleaseStatement>();
			HashMap<String,OrgInfoReleaseStatement> hasone = new HashMap<String,OrgInfoReleaseStatement>();
			
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
				String basis = req.getParameter("basis_"+i);
				String aodirective = req.getParameter("directive_ao_"+i);
				String aobasis = req.getParameter("basis_ao_"+i);
				
				// Deal with the case in which there are multiple rows for the same attribute 
				// Current front-end implementation solves this a different way, but it's worthwhile 
				// in case that changes to include this rather cheap bit of code, just in case.
				
				
				
				OrgInfoReleaseStatement oirs = null;
				if (! hasone.containsKey(itype+":"+iid)) {
					oirs = new OrgInfoReleaseStatement();
					InfoId ii = new InfoId();
					ii.setInfoType(itype);
					ii.setInfoValue(iid);
					oirs.setInfoId(ii);
					hasone.put(itype+":"+iid, oirs);
				} else {
					oirs = hasone.get(itype+":"+iid);
				}
				
				if (oirs.getArrayOfOrgDirectiveOnValues() == null) {
					oirs.setArrayOfOrgDirectiveOnValues(new ArrayList<OrgDirectiveOnValues>());
				}
				
				OrgDirectiveOnValues odov = new OrgDirectiveOnValues();
				odov.setOrgPolicyBasis(basis);
				if (directive.equalsIgnoreCase("permit")) {
					odov.setOrgReleaseDirective(OrgReleaseDirective.permit);
				} else  {
					odov.setOrgReleaseDirective(OrgReleaseDirective.deny);
				}

				ArrayList<ValueObject> avo = new ArrayList<ValueObject>();
				ValueObject vo = new ValueObject();
				vo.setValue(values);
				avo.add(vo);
				odov.setValueObjectList(avo);
				
				oirs.getArrayOfOrgDirectiveOnValues().add(odov);

				// this is the "other way" mentioned above
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
						String subbasis = req.getParameter("basis_"+i+"_"+j);
						
						if (req.getParameter("deleted_"+i+"_"+j) != null && req.getParameter("deleted_"+i+"_"+j).equals("true")) {
							continue;  // skip deleted entries
						}
						
						OrgDirectiveOnValues subodov = new OrgDirectiveOnValues();
						subodov.setOrgPolicyBasis(subbasis);
						if (subdirective.equalsIgnoreCase("permit")) {
							subodov.setOrgReleaseDirective(OrgReleaseDirective.permit);
						} else  {
							subodov.setOrgReleaseDirective(OrgReleaseDirective.deny);
						}

						ArrayList<ValueObject> subavo = new ArrayList<ValueObject>();
						ValueObject subvo = new ValueObject();
						subvo.setValue(subvalues);
						subavo.add(subvo);
						subodov.setValueObjectList(subavo);
						
						oirs.getArrayOfOrgDirectiveOnValues().add(subodov);

					}
				}
				
				
				OrgDirectiveAllOtherValues oaov = new OrgDirectiveAllOtherValues();
				oaov.setAllOtherValuesConst(AllOtherValuesConst.allOtherValues);
				oaov.setOrgPolicyBasis(aobasis);
				if (aodirective.equalsIgnoreCase("permit")) {
					oaov.setOrgReleaseDirective(OrgReleaseDirective.permit);
				} else {
					oaov.setOrgReleaseDirective(OrgReleaseDirective.deny);
				}
				
				oirs.setOrgDirectiveAllOtherValues(oaov);
			}
			
			// hasone now contians everything we need
			for(String key : hasone.keySet()) {
				airs.add(hasone.get(key));
			}
			
			oirp.setArrayOfInfoReleaseStatement(airs);
			
			// And (possibly) include an all other items directive -- else let it fall to null
			if (req.getParameter("all-other-items-directive") != null && ! req.getParameter("all-other-items-directive").equals("continue")) {
				// Add one
				AllOtherOrgInfoReleaseStatement aoirs = new AllOtherOrgInfoReleaseStatement();
				OrgDirectiveAllOtherValues odaov = new OrgDirectiveAllOtherValues();
				odaov.setAllOtherValuesConst(AllOtherValuesConst.allOtherValues);
				if (req.getParameter("all-other-items-directive").equalsIgnoreCase("PERMIT")) {
					odaov.setOrgReleaseDirective(OrgReleaseDirective.permit);
				} else {
					odaov.setOrgReleaseDirective(OrgReleaseDirective.deny);
				}
				odaov.setOrgPolicyBasis(req.getParameter("all-other-items-basis"));
				aoirs.setOrgDirectiveAllOtherValues(odaov);
				
				AllOtherInfoId aoi = new AllOtherInfoId();
				aoi.setAllOtherInfoType(AllOtherInfoTypeConst.allOtherInfoType);
				aoi.setAllOtherInfoValue(AllOtherInfoValueConst.allOtherInfoValue);
				aoirs.setAllOtherInfoId(aoi);
				
				oirp.setAllOtherOrgInfoReleaseStatement(aoirs);
			}
			
			// Now we're ready to send it to the database
			CarAdminUtils.postOrgInfoReleasePolicy(oirp);
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("added institutional policy '" + req.getParameter("policydescription") + "' to " + CarAdminUtils.idUnEscape(rhidin));
			CarAdminUtils.postActivityStreamEntry(ase);
			
			retval = new ModelAndView(returl + "?state=1&component=addorgpolicy");
			
			return retval;
			
		}
		
		
		
		// Remember to build retval with messaging
		return retval;
	}

	@RequestMapping(value="/addorgpolicy/{rhtype}/{rhid:.+}",method=RequestMethod.GET)
	public ModelAndView handleGetAddOrgPolicy(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhidin) {
		ModelAndView retval = new ModelAndView("AddOrgPolicy");
		AdminConfig config = null;
		
		if ((config = CarAdminUtils.init(req)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
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
		
		ReturnedRHMetaInformation rhmi = CarAdminUtils.getResourceHolderMetaInformation(rhtype, rhid);
		
		ArrayList<ReturnedRHMetaInformation> arrm = CarAdminUtils.getAllDefinedResourceHolders();
		Collections.sort(arrm,new ReturnedRHMetaInformationComparator());
		
		//retval.addObject("authuser",req.getRemoteUser());
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
		
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		
		if (req.getParameter("state") != null) {
			if (req.getParameter("state").equals("0")) {
				// failure
				if (req.getParameter("component").equals("addorgpolicy")) {
					retval.addObject("failmsg","Failed to add new institutional policy -- check input values");
				}
			} else if (req.getParameter("state").equals("1")) {
				// success
				if (req.getParameter("component").equals("addorgpolicy")) {
					retval.addObject("successmsg","Successfully added new institutional policy");
				}
			}
		}
					
		return retval;
	}
}
