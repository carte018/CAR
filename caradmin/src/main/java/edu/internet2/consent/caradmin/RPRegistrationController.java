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

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.InfoItemValueList;
import edu.internet2.consent.informed.model.InternationalizedString;
import edu.internet2.consent.informed.model.LocaleString;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.RPIdentifier;
import edu.internet2.consent.informed.model.ReturnedInfoTypeList;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPOptionalInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRPProperty;
import edu.internet2.consent.informed.model.ReturnedRPRequiredInfoItemList;

@Controller
public class RPRegistrationController {
	
	@RequestMapping(value="/rpdelregistration/{rhtype}/{rhid}/{rptype}/{rpid}",method=RequestMethod.POST)
	public ModelAndView handlePostDeleteRPRegistration(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhidin, @PathVariable("rptype") String rptype, @PathVariable("rpid") String rpidin) {
		ModelAndView retval = new ModelAndView("redirect:/rpregistration/?state=1&component=rparchive");
		
		if (CarAdminUtils.init(req) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		String rhid = CarAdminUtils.idUnEscape(rhidin);
		String rpid = CarAdminUtils.idUnEscape(rpidin);
		
		RHIdentifier rhi = new RHIdentifier();
		rhi.setRhtype(rhtype);
		rhi.setRhid(rhid);
		RPIdentifier rpi = new RPIdentifier();
		rpi.setRptype(rptype);;
		rpi.setRpid(rpid);
		
		CarAdminUtils.archiveRPMetaInformation(rhi, rpi);
		
		ActivityStreamEntry ase = new ActivityStreamEntry();
		ase.setUser(req.getRemoteUser());
		ase.setType("admin");
		ase.setTimestamp(System.currentTimeMillis());
		ase.setOperation("archived relying party " + rpi.getRpid());
		try {
			CarAdminUtils.postActivityStreamEntry(ase);
		} catch (Exception e) {
			// ignore
		}
		
		return retval;
	}
	@RequestMapping(value="/rpregistration/{rhtype}/{rhid}/{rptype}/{rpid}",method=RequestMethod.POST)
	public ModelAndView handlePostRPEditRegistration(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhid, @PathVariable("rptype") String rptype, @PathVariable("rpid") String rpid) {
		
		// Multiple possibilities
		
		String component = null;
		int state = 0;
		
		if (CarAdminUtils.init(req) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("rpdisplayname_edit_form")) {
			// Update the displayname InternationalizedString value
			InternationalizedString newval = new InternationalizedString();
			ArrayList<LocaleString> al = new ArrayList<LocaleString>();
			for (int i = 1; i <= Integer.parseInt(req.getParameter("rpdisplaynamecount")); i++) {
				LocaleString ls = new LocaleString();
				ls.setLocale(req.getParameter("rpdisplaynamelanguage_"+i));
				ls.setValue(req.getParameter("rpdisplaynamevalue_"+i));
				al.add(ls);
			}
			newval.setLocales(al);
			// And call the updater
			CarAdminUtils.updateRPDisplayName(rhtype,rhid,rptype,rpid,newval);
			component = "rpmi";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated relying party metainformation for " + rpid);
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		}  else if (req.getParameter("formname") != null && req.getParameter("formname").equals("form_rplang_add")) {
			ReturnedRPMetaInformation rrpmi = CarAdminUtils.getRelyingPartyMetaInformation(rhtype, CarAdminUtils.idUnEscape(rhid),rptype,CarAdminUtils.idUnEscape(rpid));
			LocaleString dls = new LocaleString();
			dls.setLocale(req.getParameter("lang"));
			dls.setValue(req.getParameter("displayname"));
			//
			// Since IDMS-4191, displayname and description may be null initially
			//
			if (rrpmi.getDisplayname() == null) 
				rrpmi.setDisplayname(new InternationalizedString());
			if (rrpmi.getDisplayname().getLocales() == null)
				rrpmi.getDisplayname().setLocales(new ArrayList<LocaleString>());
			rrpmi.getDisplayname().getLocales().add(dls);
			LocaleString cls = new LocaleString();
			cls.setLocale(req.getParameter("lang"));
			cls.setValue(req.getParameter("description"));
			if (rrpmi.getDescription() == null)
				rrpmi.setDescription(new InternationalizedString());
			if (rrpmi.getDescription().getLocales() == null)
				rrpmi.getDescription().setLocales(new ArrayList<LocaleString>());
			rrpmi.getDescription().getLocales().add(cls);
			CarAdminUtils.putRelyingPartyMetaInformation(rrpmi);  // actually update the rp metainfo
			component = "rpmi";
			state=1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("added relying party metainformation locale binding for " + rrpmi.getDisplayname().getLocales().get(0).getValue());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").matches("^form_[^_]*_edit$")) {
			// POST of a language specific edit form -- sift out the language
			String chlang = req.getParameter("formname");
			chlang = chlang.replaceAll("^form_", "");
			chlang = chlang.replaceAll("_edit$","");
			// Get the RPMetaInformation we have now
			ReturnedRPMetaInformation rrpmi = CarAdminUtils.getRelyingPartyMetaInformation(rhtype, CarAdminUtils.idUnEscape(rhid),rptype,CarAdminUtils.idUnEscape(rpid));
			
			// if this is a delete, handle that first
			if (req.getParameter("is_delete").equalsIgnoreCase("true")) {
				// Process delete
				LocaleString dls = new LocaleString();
				dls.setLocale(chlang);
				dls.setValue(req.getParameter("displayname"));
				rrpmi.getDisplayname().getLocales().remove(dls);
				LocaleString cls = new LocaleString();
				cls.setLocale(chlang);;
				cls.setValue(req.getParameter("description"));
				rrpmi.getDescription().getLocales().remove(cls);
				CarAdminUtils.putRelyingPartyMetaInformation(rrpmi);  // actually update the rp metainfo
				component = "rpmi";
				state = 1;
				
				ActivityStreamEntry ase = new ActivityStreamEntry();
				ase.setUser(req.getRemoteUser());
				ase.setType("admin");
				ase.setTimestamp(System.currentTimeMillis());
				ase.setOperation("deleted locale binding from relying party metainformation for " + rrpmi.getDisplayname().getLocales().get(0).getValue());
				try {
					CarAdminUtils.postActivityStreamEntry(ase);
				} catch (Exception e) {
					// ignore
				}
				return new ModelAndView("redirect:/rpregistration/"+rhtype+"/"+CarAdminUtils.idEscape(rhid)+"/"+rptype+"/"+CarAdminUtils.idEscape(rpid)+"/?component=" + component + "&state=" + state);
			}
			// Otherwise replace the values for displayname and description for this language (if it exists)
			boolean updated = false;
			//
			// NPE protection
			//
			if (rrpmi.getDisplayname() != null && rrpmi.getDisplayname().getLocales() != null)
				for (LocaleString displs : rrpmi.getDisplayname().getLocales()) {
					if (displs.getLocale().equalsIgnoreCase(chlang)) {
					// 		Match
						updated = true;
						displs.setValue(req.getParameter("displayname"));
					}
				}
			if (! updated) {
				// We need to add another locale
				LocaleString nls = new LocaleString();
				nls.setLocale(chlang);
				nls.setValue(req.getParameter("displayname"));
				InternationalizedString is = rrpmi.getDisplayname();
				// NPE protection
				if (is == null) 
					is = new InternationalizedString();
				ArrayList<LocaleString> isl = (ArrayList<LocaleString>) is.getLocales();
				// NPE protection
				if (isl == null)  
					isl = new ArrayList<LocaleString>();
				isl.add(nls);
				is.setLocales(isl);
				rrpmi.setDisplayname(is);
			}
			updated = false;
			// NPE protection
			if (rrpmi.getDescription() != null && rrpmi.getDescription().getLocales() != null)
				for (LocaleString descls : rrpmi.getDescription().getLocales()) {
					if (descls.getLocale().equalsIgnoreCase(chlang)) {
						// Match
						updated = true;
						descls.setValue(req.getParameter("description"));
					}
				}
			
			if (! updated) {
				// We need to add another locale
				LocaleString nls = new LocaleString();
				nls.setLocale(chlang);
				nls.setValue(req.getParameter("description"));
				InternationalizedString is = rrpmi.getDescription();
				// NPE protection
				if (is == null) 
					is = new InternationalizedString();
				ArrayList<LocaleString> isl = (ArrayList<LocaleString>) is.getLocales();
				if (isl == null) 
					isl = new ArrayList<LocaleString>();
				isl.add(nls);
				is.setLocales(isl);
				rrpmi.setDescription(is);
			}
			
			CarAdminUtils.putRelyingPartyMetaInformation(rrpmi);  // actually update the rp metainfo
			component = "rpmi";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated locale bindings for relying party metainformation for " + rrpmi.getDisplayname().getLocales().get(0).getValue());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("newrpform")) {
			// Add the RP to the list
			ReturnedRPMetaInformation rpmi = new ReturnedRPMetaInformation();
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(rhtype);
			rhi.setRhid(CarAdminUtils.idUnEscape(rhid));
			rpmi.setRhidentifier(rhi);
			RPIdentifier rpi = new RPIdentifier();
			rpi.setRptype(rptype);
			rpi.setRpid(CarAdminUtils.idUnEscape(rpid));
			rpmi.setRpidentifier(rpi);
			InternationalizedString displayval = new InternationalizedString();
			LocaleString ls = new LocaleString();
			ls.setLocale(req.getParameter("displaynamelang_1"));
			ls.setValue(req.getParameter("displaynamevalue_1"));
			ArrayList<LocaleString> als1 = new ArrayList<LocaleString>();
			als1.add(ls);
			displayval.setLocales(als1);
			InternationalizedString descrval = new InternationalizedString();
			LocaleString ls2 = new LocaleString();
			ls2.setLocale(req.getParameter("descriptionlang_1"));
			ls2.setValue(req.getParameter("descriptionvalue_1"));
			ArrayList<LocaleString> als2 = new ArrayList<LocaleString>();
			als2.add(ls2);
			descrval.setLocales(als2);
			rpmi.setDescription(descrval);
			rpmi.setDisplayname(displayval);
			CarAdminUtils.postRPMetaInformation(rpmi);
			component = "newrp";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("added new relying party to " + CarAdminUtils.idUnEscape(rhid));
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("rpdescription_edit_form")) {
			// Update the description InternationalizedString value
			InternationalizedString newval = new InternationalizedString();
			ArrayList<LocaleString> al = new ArrayList<LocaleString>();
			for (int i = 1; i <= Integer.parseInt(req.getParameter("rpdescriptioncount")); i++) {
				LocaleString ls = new LocaleString();
				ls.setLocale(req.getParameter("rpdescriptionlanguage_"+i));
				ls.setValue(req.getParameter("rpdescriptionvalue_"+i));
				al.add(ls);
			}
			newval.setLocales(al);
			// And update
			CarAdminUtils.updateRPDescription(rhtype,rhid,rptype,rpid,newval);
			component = "rpmi";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated relying party description for " + rpid);
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
			
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("ic_edit_form")) {
			// Do all of them at once -- combined form submission
			String newicon = req.getParameter("iconurl");
			CarAdminUtils.updateRPIconUrl(rhtype,rhid,rptype,rpid,newicon);
			String newpriv = req.getParameter("privacyurl");
			CarAdminUtils.updateRPPrivacyUrl(rhtype, rhid, rptype, rpid, newpriv);
			String newshow = req.getParameter("defaultshowagain");
			CarAdminUtils.updateDefaultShowAgain(rhtype,rhid,rptype,rpid,newshow);
			component = "rpic";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated informed content for relying party " + rpid);
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("iconurldiv_edit_form")) {
			// update the icon url
			String newval = req.getParameter("iconurl");
			CarAdminUtils.updateRPIconUrl(rhtype,rhid,rptype,rpid,newval);
			component = "rpic";
			state=1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated icon url for " + rpid);
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("privacyurldiv_edit_form")) {
			// update the privacy url
			String newval = req.getParameter("privacyurl");
			CarAdminUtils.updateRPPrivacyUrl(rhtype,rhid,rptype,rpid,newval);
			component = "rpic";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated privacy url for " + rpid);
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("defaultshowagaindiv_edit_form")) {
			// update showagain
			String newval = req.getParameter("defaultshowagain");
			CarAdminUtils.updateDefaultShowAgain(rhtype,rhid,rptype,rpid,newval);
			component = "rpic";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated default transparency for " + rpid);
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("rppropertiesform")) {
			// update rpproperties list
			ReturnedRPMetaInformation rrpmi = new ReturnedRPMetaInformation();
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(req.getParameter("rhtype"));
			rhi.setRhid(req.getParameter("rhid"));
			rrpmi.setRhidentifier(rhi);
			RPIdentifier rpi = new RPIdentifier();
			rpi.setRptype(req.getParameter("rptype"));
			rpi.setRpid(req.getParameter("rpid"));
			rrpmi.setRpidentifier(rpi);
			ArrayList<ReturnedRPProperty> arpp = new ArrayList<ReturnedRPProperty>();
			for (int i=1; i<=Integer.parseInt(req.getParameter("rppropertycount")); i++) {
				if (req.getParameter("propertyname_"+i) != null) {
					ReturnedRPProperty r = new ReturnedRPProperty();
					r.setRppropertyname(req.getParameter("propertyname_"+i));
					r.setRppropertyvalue(req.getParameter("propertyvalue_"+i));
					arpp.add(r);
				}
			}
			rrpmi.setRpproperties(arpp);
			CarAdminUtils.updateRPProperties(rrpmi);
			component = "rpprops";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			if (rrpmi != null && rrpmi.getDisplayname() != null) {
				ase.setOperation("updated relying party properties for " + rrpmi.getDisplayname().getLocales().get(0).getValue());
			} else {
				ase.setOperation("updated relying party properties");
			}
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("additemform")) {
			// Add an item request, generalized
			
			if (req.getParameter("required") != null && req.getParameter("required").equals("required") && req.getParameter("values") != null && req.getParameter("values").length() > 0) {
				// required add
				ReturnedRPRequiredInfoItemList rrpil = CarAdminUtils.getRelyingPartyRequiredInfoItemList(rhtype, rhid, rptype, rpid);
				
				if (rrpil == null || rrpil.getRequiredlist() == null) {
					// First time through -- create
					rrpil = new ReturnedRPRequiredInfoItemList();
					RHIdentifier rhi = new RHIdentifier();
					rhi.setRhtype(req.getParameter("rhtype"));
					rhi.setRhid(req.getParameter("rhid"));
					rrpil.setRhidentifier(rhi);
					RPIdentifier rpi = new RPIdentifier();
					rpi.setRptype(req.getParameter("rptype"));
					rpi.setRpid(req.getParameter("rpid"));
					rrpil.setRpidentifier(rpi);
					ArrayList<InfoItemValueList> aiv = new ArrayList<InfoItemValueList>();
					rrpil.setRequiredlist(aiv);
				}
				
				// Now update with the new one
				InfoItemValueList ivl = new InfoItemValueList();
				InfoItemIdentifier iii = new InfoItemIdentifier();
				iii.setIitype(req.getParameter("iitype"));
				iii.setIiid(req.getParameter("iiid"));
				ivl.setInfoitemidentifier(iii);
				
				ivl.setSourceitemname(req.getParameter("sourceii"));
				
				InternationalizedString is = new InternationalizedString();
				LocaleString ls = new LocaleString();
				ls.setLocale(req.getParameter("reasonlang"));
				ls.setValue(req.getParameter("reason"));
				ArrayList<LocaleString> als = new ArrayList<LocaleString>();
				als.add(ls);
				is.setLocales(als);
				ivl.setReason(is);
				
				ArrayList<String> a = new ArrayList<String>();
				for (String s : req.getParameter("values").split(",")) {
					if (s.equalsIgnoreCase("All Values")) {
						a.add(".*");
					} else {
						a.add(s);
					}
				}
				ivl.setValuelist(a);
				
				rrpil.getRequiredlist().add(ivl);
				
				CarAdminUtils.putRPRequiredInfoItemList(rrpil);
				component = "additem";
				state = 1;
				
				ActivityStreamEntry ase = new ActivityStreamEntry();
				ase.setUser(req.getRemoteUser());
				ase.setType("admin");
				ase.setTimestamp(System.currentTimeMillis());
				ase.setOperation("added required information itemt to " + rrpil.getRpidentifier().getRpid());
				try {
					CarAdminUtils.postActivityStreamEntry(ase);
				} catch (Exception e) {
					// ignore
				}
			} else if (req.getParameter("required") != null && req.getParameter("required").equals("optional") && req.getParameter("values") != null && req.getParameter("values").length() > 0) {
				// required add
				ReturnedRPOptionalInfoItemList ropil = CarAdminUtils.getRelyingPartyOptionalInfoItemList(rhtype, rhid, rptype, rpid);
				
				if (ropil == null || ropil.getOptionallist() == null) {
					// First time through -- create
					ropil = new ReturnedRPOptionalInfoItemList();
					RHIdentifier rhi = new RHIdentifier();
					rhi.setRhtype(req.getParameter("rhtype"));
					rhi.setRhid(req.getParameter("rhid"));
					ropil.setRhidentifier(rhi);
					RPIdentifier rpi = new RPIdentifier();
					rpi.setRptype(req.getParameter("rptype"));
					rpi.setRpid(req.getParameter("rpid"));
					ropil.setRpidentifier(rpi);
					ArrayList<InfoItemValueList> aiv = new ArrayList<InfoItemValueList>();
					ropil.setOptionallist(aiv);
				}
				
				// Now update with the new one
				InfoItemValueList ivl = new InfoItemValueList();
				InfoItemIdentifier iii = new InfoItemIdentifier();
				iii.setIitype(req.getParameter("iitype"));
				iii.setIiid(req.getParameter("iiid"));
				ivl.setInfoitemidentifier(iii);
				
				ivl.setSourceitemname(req.getParameter("sourceii"));
				
				InternationalizedString is = new InternationalizedString();
				LocaleString ls = new LocaleString();
				ls.setLocale(req.getParameter("reasonlang"));
				ls.setValue(req.getParameter("reason"));
				ArrayList<LocaleString> als = new ArrayList<LocaleString>();
				als.add(ls);
				is.setLocales(als);
				ivl.setReason(is);
				
				ArrayList<String> a = new ArrayList<String>();
				for (String s : req.getParameter("values").split(",")) {
					if (s.equalsIgnoreCase("All Values")) {
						a.add(".*");
					} else {
						a.add(s);
					}
				}
				ivl.setValuelist(a);
				
				ropil.getOptionallist().add(ivl);
				
				CarAdminUtils.putRPOptionalInfoItemList(ropil);
				component = "additem";
				state = 1;
				
				ActivityStreamEntry ase = new ActivityStreamEntry();
				ase.setUser(req.getRemoteUser());
				ase.setType("admin");
				ase.setTimestamp(System.currentTimeMillis());
				ase.setOperation("added optional info item to " + ropil.getRpidentifier().getRpid());
				try {
					CarAdminUtils.postActivityStreamEntry(ase);
				} catch (Exception e) {
					// ignore
				}
			} else {
				// Nothing to do so just return for now
				component = "additem";
				state = 0;
			}
			
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("requirediiform")) {
			ReturnedRPRequiredInfoItemList rrpil = new ReturnedRPRequiredInfoItemList();
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(req.getParameter("rhtype"));
			rhi.setRhid(req.getParameter("rhid"));
			rrpil.setRhidentifier(rhi);
			RPIdentifier rpi = new RPIdentifier();
			rpi.setRptype(req.getParameter("rptype"));
			rpi.setRpid(req.getParameter("rpid"));
			rrpil.setRpidentifier(rpi);
			
			ArrayList<InfoItemValueList> rlist = new ArrayList<InfoItemValueList>();
			for (int i=1; i<=Integer.parseInt(req.getParameter("requiredcount")); i++) {
				if (req.getParameter("requirediitype_"+i) != null) {
					InfoItemValueList iivl = new InfoItemValueList();
					InfoItemIdentifier iii = new InfoItemIdentifier();
					iii.setIitype(req.getParameter("requirediitype_"+i));
					iii.setIiid(req.getParameter("requirediivalue_"+i));
					iivl.setInfoitemidentifier(iii);
					iivl.setSourceitemname(req.getParameter("requirediisource_"+i));
					ArrayList<String> vlist = new ArrayList<String>();
					for (int j=1; j<=Integer.parseInt(req.getParameter("requirediivaluecount_"+i)); j++) {
						vlist.add(req.getParameter("requirediivalrex_"+j+"_"+i));
					}
					iivl.setValuelist(vlist);
					InternationalizedString is = new InternationalizedString();
					ArrayList<LocaleString> locales = new ArrayList<LocaleString>();
					for (int k=1; k<=Integer.parseInt(req.getParameter("requirediireasoncount_"+i));k++) {
						LocaleString ls = new LocaleString();
						ls.setLocale(req.getParameter("requirediireasonlanguage_"+k+"_"+i));
						ls.setValue(req.getParameter("requirediireasonvalue_"+k+"_"+i));
						locales.add(ls);
					}
					is.setLocales(locales);
					iivl.setReason(is);
					rlist.add(iivl);
				}
			}
			rrpil.setRequiredlist(rlist);
			CarAdminUtils.updateRequiredList(rrpil);
			component = "rpiis";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated required information items for " + rrpil.getRpidentifier().getRpid());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equalsIgnoreCase("optionaliiform")) {
			ReturnedRPOptionalInfoItemList rrpil = new ReturnedRPOptionalInfoItemList();
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(req.getParameter("rhtype"));
			rhi.setRhid(req.getParameter("rhid"));
			rrpil.setRhidentifier(rhi);
			RPIdentifier rpi = new RPIdentifier();
			rpi.setRptype(req.getParameter("rptype"));
			rpi.setRpid(req.getParameter("rpid"));
			rrpil.setRpidentifier(rpi);
			
			ArrayList<InfoItemValueList> rlist = new ArrayList<InfoItemValueList>();
			for (int i=1; i<=Integer.parseInt(req.getParameter("optionalcount")); i++) {
				if (req.getParameter("optionaliitype_"+i) != null) {
					InfoItemValueList iivl = new InfoItemValueList();
					InfoItemIdentifier iii = new InfoItemIdentifier();
					iii.setIitype(req.getParameter("optionaliitype_"+i));
					iii.setIiid(req.getParameter("optionaliivalue_"+i));
					iivl.setInfoitemidentifier(iii);
					iivl.setSourceitemname(req.getParameter("optionaliisource_"+i));
					ArrayList<String> vlist = new ArrayList<String>();
					for (int j=1; j<=Integer.parseInt(req.getParameter("optionaliivaluecount_"+i)); j++) {
						vlist.add(req.getParameter("optionaliivalrex_"+j+"_"+i));
					}
					iivl.setValuelist(vlist);
					InternationalizedString is = new InternationalizedString();
					ArrayList<LocaleString> locales = new ArrayList<LocaleString>();
					for (int k=1; k<=Integer.parseInt(req.getParameter("optionaliireasoncount_"+i));k++) {
						LocaleString ls = new LocaleString();
						ls.setLocale(req.getParameter("optionaliireasonlanguage_"+k+"_"+i));
						ls.setValue(req.getParameter("optionaliireasonvalue_"+k+"_"+i));
						locales.add(ls);
					}
					is.setLocales(locales);
					iivl.setReason(is);
					rlist.add(iivl);
				}
			}
			rrpil.setOptionallist(rlist);
			CarAdminUtils.updateOptionalList(rrpil);
			component = "rpiis";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated optional informatio items for " + rrpil.getRpidentifier().getRpid());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").matches("^form-required-.*$")) {
			// New required info item form (with all the items)
			
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(req.getParameter("rhtype"));
			rhi.setRhid(req.getParameter("rhid"));
			
			RPIdentifier rpi = new RPIdentifier();
			rpi.setRptype(req.getParameter("rptype"));
			rpi.setRpid(req.getParameter("rpid"));
			
			String iitype = req.getParameter("iitype");
			String defaultlanguage = req.getParameter("defaultlanguage");
			
			int iicount = Integer.parseInt(req.getParameter("itemcount"));
			
			// Iterate through the items by number, processing the edits (every POST updates every item at this point)

			ReturnedRPRequiredInfoItemList orig = CarAdminUtils.getRelyingPartyRequiredInfoItemList(req.getParameter("rhtype"), req.getParameter("rhid"), req.getParameter("rptype"), req.getParameter("rpid"));

			// Index the original list's Reason data for later retrieval
			// The data model is optimized for retrieval, not modification
			
			HashMap<String,HashMap<String,LocaleString>> folded = new HashMap<String,HashMap<String,LocaleString>>();

			for (InfoItemValueList iivl : orig.getRequiredlist()) {
				// for each infoitem
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
				folded.put(iivl.getInfoitemidentifier().getIiid(),inner);
			}
			
			
			ReturnedRPRequiredInfoItemList rpril = new ReturnedRPRequiredInfoItemList();
			
			rpril.setRhidentifier(rhi);
			rpril.setRpidentifier(rpi);

			ArrayList<InfoItemValueList> aivl = new ArrayList<InfoItemValueList>();
			
			for (int i = 1; i <= iicount; i++) {
				InfoItemIdentifier iii = new InfoItemIdentifier();
				iii.setIitype(iitype);
				iii.setIiid(req.getParameter("iid_"+i));

				InfoItemValueList ivl = new InfoItemValueList();
				
				ivl.setInfoitemidentifier(iii);
				
				// Update the incumbent reason data for the current language
				folded.get(iii.getIiid()).get(defaultlanguage).setValue(req.getParameter("reason_"+i));
				// And use it for the new reason data
				InternationalizedString is = new InternationalizedString();
				ArrayList<LocaleString> als = new ArrayList<LocaleString>();
				for (String k : folded.get(iii.getIiid()).keySet()) {
					als.add(folded.get(iii.getIiid()).get(k));
				}
				is.setLocales(als);
				ivl.setReason(is);
				
				// Split and process the value list
				// Special case "All values"
				ArrayList<String> avl = new ArrayList<String>();
				
				if (req.getParameter("values_"+i).contains("All values")) {
					avl.add(".*");
				} else {
					String[] parts = req.getParameter("values_"+i).split(",");
					for (String p : parts) {
						avl.add(p);
					}
				}
				ivl.setValuelist(avl);
				
				ivl.setSourceitemname(req.getParameter("sourceitemname_"+i));
				
				// Only add if the entry is not marked for deletion
				if (req.getParameter("deleted_"+i) == null || req.getParameter("deleted_"+i).equals("0"))
					aivl.add(ivl);
				

			}
			
			// Now, add any required items that are of different item types
			for (InfoItemValueList nvl : orig.getRequiredlist()) {
				if (! nvl.getInfoitemidentifier().getIitype().equals(iitype)) {
					aivl.add(nvl);
				}
			}
			
			rpril.setRequiredlist(aivl);
			
			CarAdminUtils.putRPRequiredInfoItemList(rpril);
			component = "rpiis";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated required information item list for " + rpril.getRpidentifier().getRpid());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").matches("^form-optional-.*$")) {
			// New required info item form (with all the items)
			
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(req.getParameter("rhtype"));
			rhi.setRhid(req.getParameter("rhid"));
			
			RPIdentifier rpi = new RPIdentifier();
			rpi.setRptype(req.getParameter("rptype"));
			rpi.setRpid(req.getParameter("rpid"));
			
			String iitype = req.getParameter("iitype");
			String defaultlanguage = req.getParameter("defaultlanguage");
			
			int iicount = Integer.parseInt(req.getParameter("itemcount"));
			
			// Iterate through the items by number, processing the edits (every POST updates every item at this point)

			ReturnedRPOptionalInfoItemList orig = CarAdminUtils.getRelyingPartyOptionalInfoItemList(req.getParameter("rhtype"), req.getParameter("rhid"), req.getParameter("rptype"), req.getParameter("rpid"));

			// Index the original list's Reason data for later retrieval
			// The data model is optimized for retrieval, not modification
			
			HashMap<String,HashMap<String,LocaleString>> folded = new HashMap<String,HashMap<String,LocaleString>>();

			for (InfoItemValueList iivl : orig.getOptionallist()) {
				// for each infoitem
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
				folded.put(iivl.getInfoitemidentifier().getIiid(),inner);
			}
			
			
			ReturnedRPOptionalInfoItemList rpoil = new ReturnedRPOptionalInfoItemList();
			
			rpoil.setRhidentifier(rhi);
			rpoil.setRpidentifier(rpi);

			ArrayList<InfoItemValueList> aivl = new ArrayList<InfoItemValueList>();
			
			for (int i = 1; i <= iicount; i++) {
				InfoItemIdentifier iii = new InfoItemIdentifier();
				iii.setIitype(iitype);
				iii.setIiid(req.getParameter("iid_"+i));

				InfoItemValueList ivl = new InfoItemValueList();
				
				ivl.setInfoitemidentifier(iii);
				
				// Update the incumbent reason data for the current language
				folded.get(iii.getIiid()).get(defaultlanguage).setValue(req.getParameter("reason_"+i));
				// And use it for the new reason data
				InternationalizedString is = new InternationalizedString();
				ArrayList<LocaleString> als = new ArrayList<LocaleString>();
				for (String k : folded.get(iii.getIiid()).keySet()) {
					als.add(folded.get(iii.getIiid()).get(k));
				}
				is.setLocales(als);
				ivl.setReason(is);
				
				// Split and process the value list
				// Special case "All values"
				ArrayList<String> avl = new ArrayList<String>();
				
				if (req.getParameter("values_"+i).contains("All values")) {
					avl.add(".*");
				} else {
					String[] parts = req.getParameter("values_"+i).split(",");
					for (String p : parts) {
						avl.add(p);
					}
				}
				ivl.setValuelist(avl);
				
				ivl.setSourceitemname(req.getParameter("sourceitemname_"+i));
				
				// Only add if the entry is not marked for deletion
				if (req.getParameter("deleted_"+i) == null || req.getParameter("deleted_"+i).equals("0"))
				aivl.add(ivl);

			}
			
			// And take care of iis of other types in the optional list not updated in this POST
			for (InfoItemValueList nvl : orig.getOptionallist()) {
				if (! nvl.getInfoitemidentifier().getIitype().equals(iitype)) {
					aivl.add(nvl);
				}
			}
			
			rpoil.setOptionallist(aivl);
			
			CarAdminUtils.putRPOptionalInfoItemList(rpoil);
			component = "rpiis";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated optional information items for " + rpoil.getRpidentifier().getRpid());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname") != null && req.getParameter("formname").matches("^form-add-required-.*$")) {
			// Add a required item
			
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(req.getParameter("rhtype"));
			rhi.setRhid(req.getParameter("rhid"));
			
			RPIdentifier rpi = new RPIdentifier();
			rpi.setRptype(req.getParameter("rptype"));
			rpi.setRpid(req.getParameter("rpid"));
			
			@SuppressWarnings("unused")
			String iitype = req.getParameter("iitype");
			@SuppressWarnings("unused")
			String defaultlanguage = req.getParameter("defaultlanguage");
			
			ReturnedRPRequiredInfoItemList rriil = CarAdminUtils.getRelyingPartyRequiredInfoItemList(req.getParameter("rhtype"), req.getParameter("rhid"), req.getParameter("rptype"), req.getParameter("rpid"));
			
			if (rriil == null) {
				// Create one if none exists yet
				rriil = new ReturnedRPRequiredInfoItemList();
				
				rriil.setRhidentifier(rhi);
				rriil.setRpidentifier(rpi);
				rriil.setRequiredlist(new ArrayList<InfoItemValueList>());
			}
			
			// Add new one 
			
			InfoItemValueList ivl = new InfoItemValueList();
			InfoItemIdentifier iii = new InfoItemIdentifier();
			iii.setIitype(req.getParameter("iitype"));
			iii.setIiid(req.getParameter("identifier"));
			ivl.setInfoitemidentifier(iii);
			
			ivl.setSourceitemname(req.getParameter("sourceid"));
			
			InternationalizedString is = new InternationalizedString();
			LocaleString ls = new LocaleString();
			ArrayList<LocaleString> als = new ArrayList<LocaleString>();
			ls.setLocale(req.getParameter("reasonlang"));
			ls.setValue(req.getParameter("reasonval"));
			als.add(ls);
			is.setLocales(als);
			ivl.setReason(is);
			
			ArrayList<String> vl = new ArrayList<String>();
			if (req.getParameter("value").equalsIgnoreCase("All values")) {
				// just do all values
				vl.add(".*");
			} else {
				String[] parts = req.getParameter("value").split(",");
				for (String s : parts) {
					vl.add(s);
				}
			}
			
			ivl.setValuelist(vl);
			
			rriil.getRequiredlist().add(ivl);
			
			CarAdminUtils.putRPRequiredInfoItemList(rriil);
			component = "additem";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("added required information item for " + rriil.getRpidentifier().getRpid());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}

		}  else if (req.getParameter("formname") != null && req.getParameter("formname").matches("^form-add-optional-.*$")) {
			// Add a required item
			
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(req.getParameter("rhtype"));
			rhi.setRhid(req.getParameter("rhid"));
			
			RPIdentifier rpi = new RPIdentifier();
			rpi.setRptype(req.getParameter("rptype"));
			rpi.setRpid(req.getParameter("rpid"));
			
			@SuppressWarnings("unused")
			String iitype = req.getParameter("iitype");
			@SuppressWarnings("unused")
			String defaultlanguage = req.getParameter("defaultlanguage");
			
			ReturnedRPOptionalInfoItemList roiil = CarAdminUtils.getRelyingPartyOptionalInfoItemList(req.getParameter("rhtype"), req.getParameter("rhid"), req.getParameter("rptype"), req.getParameter("rpid"));
			
			if (roiil == null) {
				// Create one if none exists yet
				roiil = new ReturnedRPOptionalInfoItemList();
				
				roiil.setRhidentifier(rhi);
				roiil.setRpidentifier(rpi);
				roiil.setOptionallist(new ArrayList<InfoItemValueList>());
			}
			
			// Add new one 
			
			InfoItemValueList ivl = new InfoItemValueList();
			InfoItemIdentifier iii = new InfoItemIdentifier();
			iii.setIitype(req.getParameter("iitype"));
			iii.setIiid(req.getParameter("identifier"));
			ivl.setInfoitemidentifier(iii);
			
			ivl.setSourceitemname(req.getParameter("sourceid"));
			
			InternationalizedString is = new InternationalizedString();
			LocaleString ls = new LocaleString();
			ArrayList<LocaleString> als = new ArrayList<LocaleString>();
			ls.setLocale(req.getParameter("reasonlang"));
			ls.setValue(req.getParameter("reasonval"));
			als.add(ls);
			is.setLocales(als);
			ivl.setReason(is);
			
			ArrayList<String> vl = new ArrayList<String>();
			if (req.getParameter("value").equalsIgnoreCase("All values")) {
				// just do all values
				vl.add(".*");
			} else {
				String[] parts = req.getParameter("value").split(",");
				for (String s : parts) {
					vl.add(s);
				}
			}
			
			ivl.setValuelist(vl);
			
			roiil.getOptionallist().add(ivl);
			
			CarAdminUtils.putRPOptionalInfoItemList(roiil);
			component = "additem";
			state = 1;
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("added optional information item for " + roiil.getRpidentifier().getRpid());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}

		}
			
		return new ModelAndView("redirect:/rpregistration/"+rhtype+"/"+CarAdminUtils.idEscape(rhid)+"/"+rptype+"/"+CarAdminUtils.idEscape(rpid)+"/?component="+component+"&state="+state);
	}
	
	@RequestMapping(value="/rpregistration/{rhtype}/{rhid}/{rptype}/{rpid}",method=RequestMethod.GET)
	public ModelAndView handleGetRPEditRegistration(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhid, @PathVariable("rptype") String rptype, @PathVariable("rpid") String rpid) {
		ModelAndView retval = new ModelAndView("RPRegistrationEdit");
		
		AdminConfig config = null;
		if ((config = CarAdminUtils.init(req)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		// Grab the RH metainformation for the RH in question
		ReturnedRHMetaInformation rhmi = CarAdminUtils.getResourceHolderMetaInformation(rhtype,rhid);
		String lang = req.getLocale().getLanguage();

		String rhdisplayname = CarAdminUtils.localize(rhmi.getDisplayname(), lang);
		
		// And grab the current metainfo for the RP
		
		ReturnedRPMetaInformation rpmi = CarAdminUtils.getRelyingPartyMetaInformation(rhtype,rhid,rptype,rpid);
		if (rpmi == null) {
			throw new RuntimeException("getRelyingPartyMetaInformation returned null for"+rhtype+","+rhid+","+rptype+","+rpid);
		}
		String rpdisplayname = null;
		if (rpmi.getDisplayname() != null)
			rpdisplayname = CarAdminUtils.localize(rpmi.getDisplayname(), lang);
		else
			rpdisplayname = rpmi.getRpidentifier().getRpid();
		
		String rpdescription = null;
		if (rpmi.getDescription() != null)
			rpdescription = CarAdminUtils.localize(rpmi.getDescription(), lang);
		else
			rpdescription = rpmi.getRpidentifier().getRpid();
		
		// Get the list of iis associated with this rp's rh
		RHIdentifier rhi = new RHIdentifier();
		rhi.setRhtype(rhtype);
		rhi.setRhid(rhid);
		
		ReturnedRHInfoItemList rhiil = CarAdminUtils.getIiList(rhi);
		ReturnedInfoTypeList ritl = CarAdminUtils.getInfoTypes(rhi);  // get infotypes available from this RH
		
		// And grab the required and optional lists for the RP
		
		ReturnedRPOptionalInfoItemList rpoii = CarAdminUtils.getRelyingPartyOptionalInfoItemList(rhtype, rhid, rptype, rpid);
		
		// Fold into a hash by type for display purposes
		// And build a plist for the aggregated optional/required types in use
		ArrayList<String> usediit = new ArrayList<String>();
		HashMap<String,ReturnedRPOptionalInfoItemList> rpohash = new HashMap<String,ReturnedRPOptionalInfoItemList>();
		if (rpoii != null) {
			for (InfoItemValueList iivl : rpoii.getOptionallist()) {
				if (!rpohash.containsKey(iivl.getInfoitemidentifier().getIitype())) {
				// 	represents a new type of optional ii build out new list
					ReturnedRPOptionalInfoItemList newrpoil = new ReturnedRPOptionalInfoItemList();
					ArrayList<InfoItemValueList> newiivla = new ArrayList<InfoItemValueList>();
					newrpoil.setOptionallist(newiivla);
					rpohash.put(iivl.getInfoitemidentifier().getIitype(),newrpoil);
				}
				// regardless
				rpohash.get(iivl.getInfoitemidentifier().getIitype()).getOptionallist().add(iivl);
				if (! usediit.contains(iivl.getInfoitemidentifier().getIitype())) {
					usediit.add(iivl.getInfoitemidentifier().getIitype());
				}
			}
		}
		
		ReturnedRPRequiredInfoItemList rprii = CarAdminUtils.getRelyingPartyRequiredInfoItemList(rhtype, rhid, rptype, rpid);
		HashMap<String,ReturnedRPRequiredInfoItemList> rprhash = new HashMap<String,ReturnedRPRequiredInfoItemList>();
		if (rprii != null) {
			for (InfoItemValueList iivl : rprii.getRequiredlist()) {
				if (! rprhash.containsKey(iivl.getInfoitemidentifier().getIitype())) {
					ReturnedRPRequiredInfoItemList newrpril = new ReturnedRPRequiredInfoItemList();
					ArrayList<InfoItemValueList> newiivla= new ArrayList<InfoItemValueList>();
					newrpril.setRequiredlist(newiivla);
					rprhash.put(iivl.getInfoitemidentifier().getIitype(), newrpril);
				}
			// 	regardless
				rprhash.get(iivl.getInfoitemidentifier().getIitype()).getRequiredlist().add(iivl);
				if (! usediit.contains(iivl.getInfoitemidentifier().getIitype())) {
					usediit.add(iivl.getInfoitemidentifier().getIitype());
				}
			}
		}	
		// Pre-compute the in-use infoitems and pass them to the template for simplicity -- this is just easier to do here
		// than in the browser context.
		ArrayList<String> inuse = new ArrayList<String>();
		if (rpoii != null)
			for (InfoItemValueList i : rpoii.getOptionallist()) {
				inuse.add(i.getInfoitemidentifier().getIiid());
			}
		else {
			rpoii = new ReturnedRPOptionalInfoItemList();
			rpoii.setOptionallist(new ArrayList<InfoItemValueList>());
		}
			
		if (rprii != null)
			for (InfoItemValueList i : rprii.getRequiredlist()) {
				inuse.add(i.getInfoitemidentifier().getIiid());
			}
		else {
			rprii = new ReturnedRPRequiredInfoItemList();
			rprii.setRequiredlist(new ArrayList<InfoItemValueList>());
		}
		
		ArrayList<ReturnedRHMetaInformation> rhmil = CarAdminUtils.getAllDefinedResourceHolders();
		Collections.sort(rhmil,new ReturnedRHMetaInformationComparator());

		
		// inject
		
		retval.addObject("rhmi",rhmi);
		retval.addObject("availablerhs",rhmil);
		retval.addObject("rhdisplayname",rhdisplayname);
		retval.addObject("rpmi",rpmi);
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		retval.addObject("language",lang);
		retval.addObject("rpdisplayname",rpdisplayname);
		retval.addObject("rpdescription",rpdescription);
		retval.addObject("rhiil",rhiil);
		retval.addObject("rpoii",rpoii);
		retval.addObject("rprii",rprii);
		retval.addObject("inuse",inuse);
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("languages",CarAdminUtils.getSupportedLanguages());
		retval.addObject("iitypes",CarAdminUtils.getSupportedIITypes());
		retval.addObject("rhiitypes",ritl.getInfotypes());
		retval.addObject("usediit",usediit);
		retval.addObject("rpohash",rpohash);
		retval.addObject("rprhash",rprhash);
		retval.addObject("activetab","rpregistration");
		retval.addObject("lang",lang);
		
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		
		if (req.getParameter("component") != null && req.getParameter("state") != null) {
			if (req.getParameter("state").equals("1")) {
				// success
				if (req.getParameter("component").equals("rpmi"))
					retval.addObject("successmsg","Successfully updated relying party metainformation");
				else if (req.getParameter("component").equals("rpic"))
					retval.addObject("successmsg","Successfully updated relying party informed conent");
				else if (req.getParameter("component").equals("rpprops"))
					retval.addObject("successmsg","Successfully updated relying party properties");
				else if (req.getParameter("component").equals("additem"))
					retval.addObject("successmsg","Successfully added information item request");
				else if (req.getParameter("component").equals("rpiis"))
					retval.addObject("successmsg","Successfully updated information item properties");
				else if (req.getParameter("component").equals("newrp"))
					retval.addObject("successmsg","Successfully added new relying party");
				else if (req.getParameter("component").equals("rpcreate"))
					retval.addObject("successmsg","Successfully added new relying party");
			} else {
				if (req.getParameter("component").equals("rpmi"))
					retval.addObject("failmsg","Failed to update relying party metainformation");
				else if (req.getParameter("component").equals("rpic"))
					retval.addObject("failmsg","Failed to update relying party informed content");
				else if (req.getParameter("component").equals("rpprops")) 
					retval.addObject("failmsg","Failed to update relying party properties");
				else if (req.getParameter("component").equals("additem"))
					retval.addObject("failmsg","Failed to add information item request");
				else if (req.getParameter("component").equals("rpiis"))
					retval.addObject("failmsg","Failed to update information item properties");
				else if (req.getParameter("component").equals("newrp"))
					retval.addObject("failmsg","Failed to add new relying party");
				else if (req.getParameter("component").equals("rpcreate"))
					retval.addObject("failmsg","Failed to add new relying party");
			}
		}

		return retval;
	}

	@RequestMapping(value="/rpregistration/{rhtype}/{rhid}",method=RequestMethod.POST)
	public	ModelAndView handlePostRPRegistrationByRH(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhid) {
		ModelAndView retval = new ModelAndView("redirect:#");
		
		if (CarAdminUtils.init(req) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		ReturnedRPMetaInformation rpmi = new ReturnedRPMetaInformation();
		
		RHIdentifier rhi = new RHIdentifier();
		rhi.setRhid(CarAdminUtils.idUnEscape(rhid));
		rhi.setRhtype(rhtype);
		rpmi.setRhidentifier(rhi);
		
		RPIdentifier rpi = new RPIdentifier();
		rpi.setRptype(req.getParameter("rptype"));
		rpi.setRpid(req.getParameter("rpid"));
		rpmi.setRpidentifier(rpi);
		
		ReturnedRPProperty rrpp = new ReturnedRPProperty();
		ArrayList<ReturnedRPProperty> arrpp = new ArrayList<ReturnedRPProperty>();
		rrpp.setRppropertyname("addedby");
		rrpp.setRppropertyvalue(req.getRemoteUser());
		arrpp.add(rrpp);
		rpmi.setRpproperties(arrpp);
		
		InternationalizedString displayname = new InternationalizedString();
		ArrayList<LocaleString> locales = new ArrayList<LocaleString>();
		for (int i = 1; i <= Integer.parseInt(req.getParameter("displaynamecount")); i++) {
			LocaleString ls = new LocaleString();
			ls.setLocale(req.getParameter("displaynamelang_"+i));
			ls.setValue(req.getParameter("displaynamevalue_"+i));
			locales.add(ls);
		}
		displayname.setLocales(locales);
		rpmi.setDisplayname(displayname);
		
		InternationalizedString description = new InternationalizedString();
		ArrayList<LocaleString> descs = new ArrayList<LocaleString>();
		for (int i = 1; i <= Integer.parseInt(req.getParameter("descriptioncount")); i++) {
			LocaleString ls = new LocaleString();
			ls.setLocale(req.getParameter("descriptionlang_"+i));
			ls.setValue(req.getParameter("descriptionvalue_"+i));
			descs.add(ls);
		}
		description.setLocales(descs);
		rpmi.setDescription(description);
		
		rpmi.setDefaultshowagain("true");  // default new RPs to showagain -- changeable on edit

		CarAdminUtils.postRPMetaInformation(rpmi); // store the metainformation
		ReturnedRPOptionalInfoItemList rpoii = new ReturnedRPOptionalInfoItemList();
		rpoii.setRhidentifier(rhi);
		rpoii.setRpidentifier(rpi);
		rpoii.setOptionallist(new ArrayList<InfoItemValueList>()); // empty list
		CarAdminUtils.putRPOptionalInfoItemList(rpoii);
		
		ReturnedRPRequiredInfoItemList rprii = new ReturnedRPRequiredInfoItemList();
		rprii.setRhidentifier(rhi);
		rprii.setRpidentifier(rpi);
		rprii.setRequiredlist(new ArrayList<InfoItemValueList>()); // empty list
		CarAdminUtils.putRPRequiredInfoItemList(rprii);
		
		ActivityStreamEntry ase = new ActivityStreamEntry();
		ase.setUser(req.getRemoteUser());
		ase.setType("admin");
		ase.setTimestamp(System.currentTimeMillis());
		ase.setOperation("added new relying party to " + CarAdminUtils.idUnEscape(rhid));
		try {
			CarAdminUtils.postActivityStreamEntry(ase);
		} catch (Exception e) {
			// ignore
		}
		retval = new ModelAndView("redirect:"+req.getParameter("rptype")+"/"+CarAdminUtils.idEscape(req.getParameter("rpid"))+"/");
		return(retval);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/rpregistration/{rhtype}/{rhid}",method=RequestMethod.GET)
	public ModelAndView handleGetRPRegistrationByRH(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhid") String rhid) {
		
		ModelAndView retval = new ModelAndView("RPRegistrationList");
		
		AdminConfig config = null;
		if ((config = CarAdminUtils.init(req)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		// Here, we've been handed an RH and asked to return a page listing all the RPs for that 
		// RH, along with a way to add a new one from scratch.
		// We only include the information necessary to list the RP at this point -- click-thru for editing data about it
		//
		
		// Collect the RP list
		//
		CarAdminUtils.locError("ERR0014",LogCriticality.debug,rhid);
		ArrayList<ReturnedRPMetaInformation> arpmi = CarAdminUtils.getAllRPsForRH(rhtype, rhid);
		if (arpmi != null) 
			CarAdminUtils.locError("ERR0015",LogCriticality.debug,String.valueOf(arpmi.size()));
		else {
			CarAdminUtils.locError("ERR0015",LogCriticality.debug,"0");
			arpmi = new ArrayList<ReturnedRPMetaInformation>();
		}
		
		// For this purpose, rather than inject the entire object into the list display, we inject an array of smaller
		// objects that are more manageable -- we save injecting the full data for individual RP manipulations.
		//
		ArrayList<InjectedRPMetaInformation> irpm = new ArrayList<InjectedRPMetaInformation>();
		
		for (ReturnedRPMetaInformation r : arpmi) {
			InjectedRPMetaInformation i = new InjectedRPMetaInformation();
			i.setRhtype(r.getRhidentifier().getRhtype());
			i.setRhid(r.getRhidentifier().getRhid());
			i.setRptype(r.getRpidentifier().getRptype());
			i.setRpid(r.getRpidentifier().getRpid());
			if (r.getDisplayname() != null) {
				i.setDisplayname(CarAdminUtils.localize(r.getDisplayname(), req.getLocale().getLanguage()));
			} else {
				i.setDisplayname(r.getRpidentifier().getRpid());
			}
			//CarAdminUtils.locError("ERR0016",i.getRhtype(),i.getRhid(),i.getRptype(),i.getRpid(),i.getDisplayname());
			irpm.add(i);
		}
		Collections.sort(irpm);
		
		ArrayList<ReturnedRHMetaInformation> rhmil = CarAdminUtils.getAllDefinedResourceHolders();
		Collections.sort(rhmil,new ReturnedRHMetaInformationComparator());

		retval.addObject("locale",req.getLocale().getLanguage());
		retval.addObject("lang",req.getLocale().getLanguage());
		retval.addObject("availablerhs",rhmil);
		retval.addObject("rpmetalist",irpm);
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		try {
			ObjectMapper om = new ObjectMapper();
			retval.addObject("JSON",om.writeValueAsString(irpm));
		} catch (Exception e) {
			CarAdminUtils.locError("ERR0018",LogCriticality.error);
			// continue, but searches will fail
		}
		
		// In support of "add new" functionality
		ReturnedRHMetaInformation rrh = CarAdminUtils.getResourceHolderMetaInformation(rhtype, rhid);
		retval.addObject("rhmeta",rrh);
		String lang = req.getLocale().getLanguage();
		String rhdisplayname = null;
		if (rrh.getDisplayname() != null)
			rhdisplayname = CarAdminUtils.localize(rrh.getDisplayname(), lang);
		else
			rhdisplayname = rrh.getRhidentifier().getRhid();
		retval.addObject("rhdisplayname",rhdisplayname);
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("rpidtypes",CarAdminUtils.getSupportedRPIDTypes());
		retval.addObject("languages",CarAdminUtils.getSupportedLanguages());
		retval.addObject("activetab","rpregistration");
		retval.addObject("logouturl",config.getProperty("logouturl",false));
		
		if (req.getParameter("state") != null && req.getParameter("state").equals("1")) {
			if (req.getParameter("componet").equals("rparchive")) {
				retval.addObject("successmsg","Successfully archived relying party");
			}
		} else if (req.getParameter("state") != null && req.getParameter("state").equals("0")) {
			if (req.getParameter("component").equals("rparchive")) {
				retval.addObject("failmsg","Failed to archive relying party");
			}
		}
		return (retval);
	}
	@RequestMapping(value="/rpregistration-old",method=RequestMethod.GET)
	public ModelAndView handleGetRPRegistration(HttpServletRequest req) {
		
		ModelAndView retval = new ModelAndView("RPRegistrationMain");
		AdminConfig config = null;
		
		if ((config = CarAdminUtils.init(req)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		// For RP registration main page, return a queue of links to RHs whose RPs can be regsitered
		// RP registration is then a per-RH operation with /rpregistration/{type}/{id}
		//
		ArrayList<ReturnedRHMetaInformation> rhmia = CarAdminUtils.getAllDefinedResourceHolders();

		Collections.sort(rhmia,new ReturnedRHMetaInformationComparator());
		retval.addObject("rhmetalist",rhmia);
		retval.addObject("availablerhs",rhmia);
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		String locale = req.getLocale().getLanguage();
		retval.addObject("locale",locale);
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("activetab","rpregistration");
		retval.addObject("lang",locale);
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		return(retval);
	}
	
	@RequestMapping(value="/rpregistration",method=RequestMethod.POST)
	public ModelAndView handlePostNewRP(HttpServletRequest req) {
		String pre = req.getParameter("rhidentification");
		String[] parts = pre.split("/",2);
		String rhtype = parts[0];
		String rhid = parts[1];
		String rptype = req.getParameter("rptype");
		String rpid = req.getParameter("rpid");
		
		ModelAndView retval = new ModelAndView("redirect:/rpregistration/"+rhtype+"/"+CarAdminUtils.idEscape(rhid)+"/"+rptype+"/"+CarAdminUtils.idEscape(rpid)+"/?state=1&component=rpcreate");
		
		if (CarAdminUtils.init(req) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		// Add the RP to the list
		ReturnedRPMetaInformation rpmi = new ReturnedRPMetaInformation();
		RHIdentifier rhi = new RHIdentifier();
		rhi.setRhtype(rhtype);
		rhi.setRhid(CarAdminUtils.idUnEscape(rhid));
		rpmi.setRhidentifier(rhi);
		RPIdentifier rpi = new RPIdentifier();
		rpi.setRptype(rptype);
		rpi.setRpid(CarAdminUtils.idUnEscape(rpid));
		rpmi.setRpidentifier(rpi);
		InternationalizedString displayval = new InternationalizedString();
		LocaleString ls = new LocaleString();
		ls.setLocale(req.getParameter("displaynamelang_1"));
		ls.setValue(req.getParameter("displaynamevalue_1"));
		ArrayList<LocaleString> als1 = new ArrayList<LocaleString>();
		als1.add(ls);
		displayval.setLocales(als1);
		InternationalizedString descrval = new InternationalizedString();
		LocaleString ls2 = new LocaleString();
		ls2.setLocale(req.getParameter("descriptionlang_1"));
		ls2.setValue(req.getParameter("descriptionvalue_1"));
		ArrayList<LocaleString> als2 = new ArrayList<LocaleString>();
		als2.add(ls2);
		descrval.setLocales(als2);
		rpmi.setDescription(descrval);
		rpmi.setDisplayname(displayval);
		CarAdminUtils.postRPMetaInformation(rpmi);
		
		ActivityStreamEntry ase = new ActivityStreamEntry();
		ase.setUser(req.getRemoteUser());
		ase.setType("admin");
		ase.setTimestamp(System.currentTimeMillis());
		ase.setOperation("added new relying party to " + CarAdminUtils.idUnEscape(rhid));
		try {
			CarAdminUtils.postActivityStreamEntry(ase);
		} catch (Exception e) {
			// ignore
		}
		return retval;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/rpregistration",method=RequestMethod.GET)
	public ModelAndView handleGetRPRegistrationNew(HttpServletRequest req) {
		
		ModelAndView retval = new ModelAndView("RPRegistrationNew");
		
		AdminConfig config = null;
		if ((config = CarAdminUtils.init(req)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		ArrayList<ReturnedRHMetaInformation> rhmia = CarAdminUtils.getAllDefinedResourceHolders();
		
		// Iterate over the RHs and populate an injectable list
		
		ArrayList<InjectedRPMetaInformation> airpmi = new ArrayList<InjectedRPMetaInformation>();
		ArrayList<String> rhlist = new ArrayList<String>();
		HashMap<String,String> displist = new HashMap<String,String>();
		
		for (ReturnedRHMetaInformation rhmi : rhmia) {
			rhlist.add(rhmi.getRhidentifier().getRhtype() + "/" + rhmi.getRhidentifier().getRhid());
			String dname = CarAdminUtils.localize(rhmi.getDisplayname(), req.getLocale().getLanguage());
			displist.put(rhmi.getRhidentifier().getRhid(), dname);
			// For each RH, get the list of RPs
			ArrayList<ReturnedRPMetaInformation> arpmi = CarAdminUtils.getAllRPsForRH(rhmi.getRhidentifier().getRhtype(), rhmi.getRhidentifier().getRhid());
			if (arpmi == null) {
				continue;
			}
			for (ReturnedRPMetaInformation r : arpmi) {
				InjectedRPMetaInformation i = new InjectedRPMetaInformation();
				i.setRhtype(r.getRhidentifier().getRhtype());
				i.setRhid(r.getRhidentifier().getRhid());
				i.setRptype(r.getRpidentifier().getRptype());
				i.setRpid(r.getRpidentifier().getRpid());
				if (r.getDisplayname() != null) {
					i.setDisplayname(CarAdminUtils.localize(r.getDisplayname(), req.getLocale().getLanguage()));
				} else {
					i.setDisplayname(r.getRpidentifier().getRpid());
				}
				airpmi.add(i);
			}
		}
		Collections.sort(airpmi);
		
		Collections.sort(rhmia,new ReturnedRHMetaInformationComparator());

		retval.addObject("airpmi",airpmi);
		retval.addObject("availablerhs",rhmia);
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		String locale = req.getLocale().getLanguage();
		retval.addObject("locale",locale);
		retval.addObject("lang",locale);
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("rhlist",rhlist);
		retval.addObject("displist",displist);
		retval.addObject("activetab","rpregistration");
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		ObjectMapper om = new ObjectMapper();
		try {
			retval.addObject("JSON",om.writeValueAsString(airpmi));
		} catch (Exception e) {
			retval.addObject("JSON","{}");
		}
		
		try {
			retval.addObject("MAP",om.writeValueAsString(displist));
		} catch (Exception e) {
			retval.addObject("MAP","{}");
		}
		
		retval.addObject("rpidtypes",CarAdminUtils.getSupportedRPIDTypes());
		retval.addObject("languages",CarAdminUtils.getSupportedLanguages());
		
		if (req.getParameter("state") != null && req.getParameter("state").equals("1")) {
			if (req.getParameter("component").equals("rparchive")) {
				retval.addObject("successmsg","Successfully archived relying party");
			} else if (req.getParameter("component").equals("rpcreate")) {
				retval.addObject("successmsg","Successfully created relying party");
			}
		} else if (req.getParameter("state") != null && req.getParameter("state").equals("0")) {
			if (req.getParameter("component").equals("rparchive")) {
				retval.addObject("failmsg","Failed to archive relying party");
			} else if (req.getParameter("component").equals("rpcreate")) {
				retval.addObject("failmsg","Failed to create relying party");
			}
		}
		return(retval);
	}
}
