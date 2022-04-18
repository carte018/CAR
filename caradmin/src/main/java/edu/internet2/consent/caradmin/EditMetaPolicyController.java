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

import edu.internet2.consent.arpsi.model.OrgReturnedPolicy;
import edu.internet2.consent.icm.model.IcmAllOtherInfoReleaseStatement;
import edu.internet2.consent.icm.model.IcmDirectiveAllOtherValues;
import edu.internet2.consent.icm.model.IcmDirectiveOnValues;
import edu.internet2.consent.icm.model.IcmInfoReleasePolicy;
import edu.internet2.consent.icm.model.IcmInfoReleaseStatement;
import edu.internet2.consent.icm.model.IcmReleaseDirective;
import edu.internet2.consent.icm.model.IcmReturnedPolicy;
import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.ReturnedInfoTypeList;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;

@Controller
public class EditMetaPolicyController {

    private String sconvo;
    private int convo;


    private String generateCSRFToken() {
            String foo = RandomStringUtils.random(32,true,true);
            String bar = Base64.encodeBase64URLSafeString(foo.getBytes());
            return bar;
    }

	
	@RequestMapping(value="/editmetapolicy/{pid}",method=RequestMethod.POST)
	public ModelAndView handlePostEditMetaPolicy(HttpServletRequest req, @PathVariable("pid") String pid) {

		String rhtype = req.getParameter("rhtype");
		String rhid = CarAdminUtils.idEscape(req.getParameter("rhid"));
		
		AdminConfig config = null;
		
		// Only Policy Admins may submit edits to policies
		
		// We need to dereference the pid to apply authz restrictions
		
		IcmReturnedPolicy orp = CarAdminUtils.getIcmInfoReleasePolicyById(pid);

		if (orp == null) {
			// invalid request
			return new ModelAndView("redirect:/orgpolicyview/?failmessage=Requested Policy Not Found");
		}
		
		// Restrict to Policy Admins
		
		ArrayList<String> roles = new ArrayList<String>();
		ArrayList<String> targets = new ArrayList<String>();
		
		roles.add("PolicyAdmin");
		roles.add("DelegatedPolicyAdmin");
		
		targets.add(orp.getPolicy().getResourceHolderId().getRHValue())
		;
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

		
		if (! pid.equals(req.getParameter("policyid"))) {
			return new ModelAndView("redirect:/orgpolicyview/"+rhtype+"/"+rhid+"/?state=0&component=updatemetapolicy");
		}
		
		
		String returl = "redirect:/orgpolicyview/"+rhtype+"/"+rhid;
		
		ModelAndView retval = null;
		
		if (req.getParameter("formname") != null && req.getParameter("formname").equals("editmpform")) {
			IcmInfoReleasePolicy oirp = new IcmInfoReleasePolicy();
			if (req.getParameter("policydescription") != null) {
				oirp.setDescription(req.getParameter("policydescription"));
			} else {
				oirp.setDescription("");  // Must have a description of some sort
			}
			
			edu.internet2.consent.icm.model.ResourceHolderId rhi = new edu.internet2.consent.icm.model.ResourceHolderId();
			
			rhi.setRHType(rhtype);
			rhi.setRHValue(rhid);
			
			oirp.setResourceHolderId(rhi);
			
			if (req.getParameter("rpstrategy") == null || req.getParameter("rpstrategy").equals("")) {
				// Failed to provide RP strategy -- error out
				retval = new ModelAndView(returl + "?state=0&component=updatemetapolicy");
				return retval;
			} else {
				// Based on strategy, set up the policy parameters for RP selection
				String matchProperty = null;
				String matchValue = null;
				if (req.getParameter("rpstrategy").equals("allRPs")) {
					if (req.getParameter("specrptype1") == null || req.getParameter("specrptype1").equals("")) {
						// Failed to provide rp type in all rps
						retval = new ModelAndView(returl+"?state=0&component=updatemetapolicy");
						return retval;
					} else {
						matchProperty = req.getParameter("specrptype1");
						matchValue = "^.*$";  // match any value
					}
				} else if (req.getParameter("rpstrategy").equals("matchedRPs")) {
					if (req.getParameter("rpproperty") == null || req.getParameter("rpproperty").equals("") || req.getParameter("rpmatch") == null) {
						// Failed to provide rp property to match
						retval = new ModelAndView(returl + "?state=0&component=updatemetapolicy");
						return retval;
					} else {
						matchProperty = req.getParameter("rpproperty");
						matchValue = req.getParameter("rpmatch");  // this may be empty -- functionally irrelevant in that case, but legal
					}
				} else if (req.getParameter("rpstrategy").equals("oneRP")) {
					if (req.getParameter("specrptype") == null || req.getParameter("specrptype").equals("") || req.getParameter("specrpid") == null || req.getParameter("specrpid").equals("")) {
						// Failed to provide rp specifier
						retval = new ModelAndView(returl + "?state=0&component=updatemetapolicy");
						return retval;
					} else {
						matchProperty = req.getParameter("specrptype");
						matchValue = req.getParameter("specrpid");
					}
				}
				
				// At this point matchProperty and MatchValue are set for the policy
				ArrayList<edu.internet2.consent.icm.model.RelyingPartyProperty> arpp = new ArrayList<edu.internet2.consent.icm.model.RelyingPartyProperty>();
				edu.internet2.consent.icm.model.RelyingPartyProperty rpp = new edu.internet2.consent.icm.model.RelyingPartyProperty();
				rpp.setRpPropName(matchProperty);
				rpp.setRpPropValue(matchValue);
				arpp.add(rpp);
				
				oirp.setRelyingPartyPropertyArray(arpp);	
			}
		
			if (req.getParameter("userstrategy") == null || req.getParameter("userstrategy").equals("")) {
				//fail
				retval = new ModelAndView(returl + "?state=0&component=updatemetapolicy");
				return retval;
			} else {
				// Similar process for managing user strategy for this policy
				String matchProperty = null;
				String matchValue = null;
				if (req.getParameter("userstrategy").equals("allUsers")) {
					if (req.getParameter("userproperty1") == null || req.getParameter("userproperty1").equals("")) {
						// Fail
						retval = new ModelAndView(returl + "?state=0&component=updatemetapolicy");
						return retval;
					} else {
						matchProperty = req.getParameter("userproperty1");
						matchValue = "^.*$";
					}
				} else if (req.getParameter("userstrategy").equals("matchedUsers")) {
					if (req.getParameter("userproperty") == null || req.getParameter("userproperty").equals("") || req.getParameter("usermatch") == null || req.getParameter("usermatch").equals("")) {
						// Fail
						retval = new ModelAndView(returl + "?state=0&component=updatemetapolicy");
						return retval;
					} else {
						matchProperty = req.getParameter("userproperty");
						matchValue = req.getParameter("usermatch");
					}
				}
				
				ArrayList<edu.internet2.consent.icm.model.UserProperty> aup = new ArrayList<edu.internet2.consent.icm.model.UserProperty>();
				edu.internet2.consent.icm.model.UserProperty up = new edu.internet2.consent.icm.model.UserProperty();
				up.setUserPropName(matchProperty);
				up.setUserPropValue(matchValue);
				aup.add(up);
				
				oirp.setUserPropertyArray(aup);
			}
			
			// Now for the info release statements
			ArrayList<IcmInfoReleaseStatement> airs = new ArrayList<IcmInfoReleaseStatement>();
			HashMap<String,IcmInfoReleaseStatement> hasone = new HashMap<String,IcmInfoReleaseStatement>();
			
			if (req.getParameter("directivecount") == null || req.getParameter("directivecount").equals("")) {
				// fail
				retval = new ModelAndView(returl + "?state=0&component=updatemetapolicy");
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
				
				
				
				IcmInfoReleaseStatement oirs = null;
				if (! hasone.containsKey(itype+":"+iid)) {
					oirs = new IcmInfoReleaseStatement();
					edu.internet2.consent.icm.model.InfoId ii = new edu.internet2.consent.icm.model.InfoId();
					ii.setInfoType(itype);
					ii.setInfoValue(iid);
					oirs.setInfoId(ii);
					hasone.put(itype+":"+iid, oirs);
				} else {
					oirs = hasone.get(itype+":"+iid);
				}
				
				if (oirs.getArrayOfIcmDirectiveOnValues() == null) {
					oirs.setArrayOfIcmDirectiveOnValues(new ArrayList<IcmDirectiveOnValues>());
				}
				
				IcmDirectiveOnValues odov = new IcmDirectiveOnValues();
				if (directive.equalsIgnoreCase("copsu")) {
					odov.setIcmReleaseDirective(IcmReleaseDirective.COPSU);
				} else  {
					odov.setIcmReleaseDirective(IcmReleaseDirective.ARPSI);
				}

				ArrayList<edu.internet2.consent.icm.model.ValueObject> avo = new ArrayList<edu.internet2.consent.icm.model.ValueObject>();
				edu.internet2.consent.icm.model.ValueObject vo = new edu.internet2.consent.icm.model.ValueObject();
				vo.setValue(values);
				avo.add(vo);
				odov.setValueObjectList(avo);
				
				oirs.getArrayOfIcmDirectiveOnValues().add(odov);

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
						@SuppressWarnings("unused")
						String subbasis = req.getParameter("basis_"+i+"_"+j);
						
						if (req.getParameter("deleted_"+i+"_"+j) != null && req.getParameter("deleted_"+i+"_"+j).equals("true")) {
							continue;  // skip deleted entries
						}
						
						IcmDirectiveOnValues subodov = new IcmDirectiveOnValues();
						if (subdirective.equalsIgnoreCase("COPSU")) {
							subodov.setIcmReleaseDirective(IcmReleaseDirective.COPSU);
						} else  {
							subodov.setIcmReleaseDirective(IcmReleaseDirective.ARPSI);
						}

						ArrayList<edu.internet2.consent.icm.model.ValueObject> subavo = new ArrayList<edu.internet2.consent.icm.model.ValueObject>();
						edu.internet2.consent.icm.model.ValueObject subvo = new edu.internet2.consent.icm.model.ValueObject();
						subvo.setValue(subvalues);
						subavo.add(subvo);
						subodov.setValueObjectList(subavo);
						
						oirs.getArrayOfIcmDirectiveOnValues().add(subodov);

					}
				}
				
				
				IcmDirectiveAllOtherValues oaov = new IcmDirectiveAllOtherValues();
				oaov.setAllOtherValuesConst(edu.internet2.consent.icm.model.AllOtherValuesConst.allOtherValues);
				if (aodirective.equalsIgnoreCase("COPSU")) {
					oaov.setIcmReleaseDirective(IcmReleaseDirective.COPSU);
				} else {
					oaov.setIcmReleaseDirective(IcmReleaseDirective.ARPSI);
				}
				
				oirs.setIcmDirectiveAllOtherValues(oaov);
			}
			
			// hasone now contians everything we need
			for(String key : hasone.keySet()) {
				airs.add(hasone.get(key));
			}
			
			oirp.setArrayOfInfoReleaseStatement(airs);
			
			// And (possibly) include an all other items directive -- else let it fall to null
			if (req.getParameter("all-other-items-directive") != null && ! req.getParameter("all-other-items-directive").equals("continue")) {
				// Add one
				IcmAllOtherInfoReleaseStatement aoirs = new IcmAllOtherInfoReleaseStatement();
				IcmDirectiveAllOtherValues odaov = new IcmDirectiveAllOtherValues();
				odaov.setAllOtherValuesConst(edu.internet2.consent.icm.model.AllOtherValuesConst.allOtherValues);
				if (req.getParameter("all-other-items-directive").equalsIgnoreCase("COPSU")) {
					odaov.setIcmReleaseDirective(IcmReleaseDirective.COPSU);
				} else {
					odaov.setIcmReleaseDirective(IcmReleaseDirective.ARPSI);
				}
				aoirs.setIcmDirectiveAllOtherValues(odaov);
				
				edu.internet2.consent.icm.model.AllOtherInfoId aoi = new edu.internet2.consent.icm.model.AllOtherInfoId();
				aoi.setAllOtherInfoType(edu.internet2.consent.icm.model.AllOtherInfoTypeConst.allOtherInfoType);
				aoi.setAllOtherInfoValue(edu.internet2.consent.icm.model.AllOtherInfoValueConst.allOtherInfoValue);
				aoirs.setAllOtherInfoId(aoi);
				
				oirp.setAllOtherOrgInfoReleaseStatement(aoirs);
			}
			
			// Now we're ready to send it to the database
			CarAdminUtils.putIcmInfoReleasePolicy(oirp, pid);
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated institutional policy '" + req.getParameter("policydescription") + "' to " + rhid);
			CarAdminUtils.postActivityStreamEntry(ase);
			
			retval = new ModelAndView(returl + "?state=1&component=updateorgpolicy");
			
			return retval;
		}
		return retval;
	}
	
	@RequestMapping(value="/editmetapolicy/{pid}",method=RequestMethod.GET)
	public ModelAndView handleEditMetaPolicy(HttpServletRequest req, @PathVariable("pid") String pid) {
		
		ModelAndView retval = new ModelAndView("EditMetaPolicy");
		AdminConfig config = null;
		
		
		// Only Policy Admins can even get to the edit page for policies
		
		ArrayList<String> roles = new ArrayList<String>();
		ArrayList<String> targets = new ArrayList<String>();
		
		roles.add("PolicyAdmin");
		roles.add("DelegatedPolicyAdmin");
		
		// We must dereference the pid to determine what policy we're editing
				
		IcmReturnedPolicy orp = CarAdminUtils.getIcmInfoReleasePolicyById(pid);

		// If we have been asked for a non-exist policy, return to the main page with an error.
		if (orp == null) {
			return new ModelAndView("redirect:/orgpolicyview/?failmessage=Requested Policy Not Found");
		}
		
		// Otherwise, restrict delegated admins to the associated RH
		targets.add(orp.getPolicy().getResourceHolderId().getRHValue());
		
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

        // Produce an edit page
		
		RHIdentifier rhi = new RHIdentifier();
		String rhid = orp.getPolicy().getResourceHolderId().getRHValue();
		String rhtype = orp.getPolicy().getResourceHolderId().getRHType();
		rhi.setRhid(rhid);
		rhi.setRhtype(rhtype);
		
		
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
		
		// And pass in the actual policy document
		
		retval.addObject("policytoedit",orp);
		// And pre-compute some things to make the display simpler (and avoid too much velocity)
		// Some tourist information about the policy statement
		int dircount = orp.getPolicy().getArrayOfInfoReleaseStatement().size();
		retval.addObject("dircount",dircount);

		// We perform an unrolling operation and fold the policy itself into a local structure to
		// simplify presentation in the edit interface.  Since there are multiple layers of nesting,
		// and since the presentation is complex at best, it's saner to do the unrolling work here
		// than to embed it in the VTL, where it'll be bound up in HTML.
		//
		HashMap<String,UnrolledMetaDirective> unrolled = new HashMap<String,UnrolledMetaDirective>();
		HashMap<String,AllOtherMetaDirective> aaod = new HashMap<String,AllOtherMetaDirective>();
		HashMap<String,String> addcounts = new HashMap<String,String>();
		
		int oirscounter = 1;
		
		for (IcmInfoReleaseStatement oirs : orp.getPolicy().getArrayOfInfoReleaseStatement()) {
			int instancecounter = 0;

			edu.internet2.consent.icm.model.InfoId ii = oirs.getInfoId();
			String daov = oirs.getIcmDirectiveAllOtherValues().getIcmReleaseDirective().toString();
			
			if (oirs.getArrayOfIcmDirectiveOnValues() == null || oirs.getArrayOfIcmDirectiveOnValues().isEmpty()) {
				// Construct a faux entry for display purposes using .* as the value and the relevant ii information
				UnrolledMetaDirective ud = new UnrolledMetaDirective();
				
				ud.setDirective(daov);
				ud.setRowid(String.valueOf(oirscounter));
				ud.setIitype(ii.getInfoType());
				ud.setIiid(ii.getInfoValue());
				ud.setValue(".*");
				unrolled.put(ud.getRowid(), ud);
				instancecounter += 1;
			} else {
				for (IcmDirectiveOnValues odov : oirs.getArrayOfIcmDirectiveOnValues()) {
					String directive = odov.getIcmReleaseDirective().toString();
					for (edu.internet2.consent.icm.model.ValueObject value : odov.getValueObjectList()) {
						String v = value.getValue();
						// 	Now we're at the bottom of the iteration -- populate and add to the list
						UnrolledMetaDirective ud = new UnrolledMetaDirective();
						if (instancecounter == 0) {
							ud.setRowid(String.valueOf(oirscounter));
						} else {
							ud.setRowid(String.valueOf(oirscounter) + "_" + String.valueOf(instancecounter));
						}
						ud.setDirective(directive);
						ud.setValue(v);
						ud.setIitype(ii.getInfoType());
						ud.setIiid(ii.getInfoValue());
						unrolled.put(ud.getRowid(),ud);
						instancecounter += 1;
					}
				}
			}
			addcounts.put(String.valueOf(oirscounter), String.valueOf(instancecounter-1));
			AllOtherMetaDirective aod = new AllOtherMetaDirective();
			aod.setRowid(String.valueOf(oirscounter));
			aod.setDirective(daov);
			aod.setIiid(ii.getInfoValue());
			aod.setIitype(ii.getInfoType());
			aaod.put(aod.getRowid(),aod);
			
			oirscounter += 1;
			
		}

		ArrayList<String> ukeys = new ArrayList<String>();
		ukeys.addAll(unrolled.keySet());
		Collections.sort(ukeys);
		
		retval.addObject("ukeys",ukeys);
		retval.addObject("unrolled",unrolled);
		retval.addObject("aaod",aaod);
		retval.addObject("addcounts",addcounts);
		
		// To enable sorting of the keysets when displaying
		retval.addObject("Collections",Collections.class);
		retval.addObject("Integer",Integer.class);
		retval.addObject("String",String.class);
		
		// RP strategy information

		boolean allrps = false;
		String allrpType = "";
		String allrpValue = "";
		boolean somerps = false;
		ArrayList<edu.internet2.consent.icm.model.RelyingPartyProperty> somerparray = new ArrayList<edu.internet2.consent.icm.model.RelyingPartyProperty>();
		boolean onerp = false;
		String onerpType = "";
		String onerpValue = "";
		
		for (edu.internet2.consent.icm.model.RelyingPartyProperty rpp : orp.getPolicy().getRelyingPartyPropertyArray()) {
			// Iterate through the rpps and determine if we're allrps, somerps, or onerp
			if (supportedRPTypes.contains(rpp.getRpPropName()) && rpp.getRpPropValue().equals("^.*$")) {
				// this is an all RPs case, whether that was how it was specified originally or not
				allrps=true;
				allrpType = rpp.getRpPropName();
				allrpValue = rpp.getRpPropValue();
				break;  // end looping, since we have a winner
			} 
			else if (supportedRPTypes.contains(rpp.getRpPropName()) && rpids.contains(rpp.getRpPropValue())) {
				// this is a specific (singular) RP designation (whether it was entered that way or not)
				onerp = true;
				onerpType = rpp.getRpPropName();
				onerpValue = rpp.getRpPropValue();
				break;  // end looping, since we have a winner
				
			} else {
				// this is the remainder case, so it's about some rps only.  We don't break here, since there may be mulitple clauses
				somerps = true;
				somerparray.add(rpp);
			}
		}
		retval.addObject("allrps",allrps);
		retval.addObject("allrpType",allrpType);
		retval.addObject("allrpValue",allrpValue);
		retval.addObject("somerps",somerps);
		retval.addObject("somerparray",somerparray);
		retval.addObject("onerp",onerp);
		retval.addObject("onerpType",onerpType);
		retval.addObject("onerpValue",onerpValue);
		// User strategy information
		// Here, we have no proscribed identifier, so any ^.*$ identifier is taken to be all users
		boolean allusers = false;
		boolean someusers = false;
		String allusersType = "";
		ArrayList<edu.internet2.consent.icm.model.UserProperty> someusersarray = new ArrayList<edu.internet2.consent.icm.model.UserProperty>();
		for (edu.internet2.consent.icm.model.UserProperty up : orp.getPolicy().getUserPropertyArray()) {
			if (up.getUserPropValue().equals("^.*$") || up.getUserPropValue().equals(".*")) {
				// this is an all users case
				allusers = true;
				allusersType = up.getUserPropName();
				break;
			} else {
				// Otherwise, this is one of possibly multiple user properties
				someusers = true;
				someusersarray.add(up);
			}
		}
		retval.addObject("allusers",allusers);
		retval.addObject("someusers",someusers);
		retval.addObject("allusersType",allusersType);
		retval.addObject("someusersarray",someusersarray);
		
		
		if (req.getParameter("state") != null) {
			if (req.getParameter("state").equals("0")) {
				// failure
				if (req.getParameter("component").equals("addorgpolicy")) {
					retval.addObject("failmsg",CarAdminUtils.getLocalComponent("add_failed_msg"));
				}
			} else if (req.getParameter("state").equals("1")) {
				// success
				if (req.getParameter("component").equals("addorgpolicy")) {
					retval.addObject("successmsg",CarAdminUtils.getLocalComponent("add_succeeded_msg"));
				}
			}
		}
		
		CarAdminUtils.injectStrings(retval, new String[] { "meta_policy_label",
															"version_label",
															"instructions_heading",
															"instructions_body_metapol_edit",
															"policy_descr_label",
															"rps_label",
															"apply_heading",
															"all_rps_label",
															"all_rps2_label",
															"rps_with_label",
															"matching_label",
															"some_rps_label",
															"specific_rp_label",
															"one_rp_label",
															"users_any_label",
															"all_users_label",
															"users_with_label",
															"some_users_label",
															"directives_heading",
															"item_type_heading",
															"item_id_heading",
															"directive_heading",
															"values_heading",
															"basis_heading",
															"other_values_label",
															"add_item_label",
															"all_other_heading",
															"update_policy_label",
															"top_heading",
															"sign_out",
															"top_logo_url"
															
		});
		return retval;
	}
}
