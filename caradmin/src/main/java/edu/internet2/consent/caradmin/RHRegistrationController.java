package edu.internet2.consent.caradmin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;
import edu.internet2.consent.arpsi.model.OrgReturnedPolicy;
import edu.internet2.consent.icm.model.IcmReturnedPolicy;
import edu.internet2.consent.icm.model.UserReturnedPolicy;
import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.InfoItemMode;
import edu.internet2.consent.informed.model.InternationalizedString;
import edu.internet2.consent.informed.model.LocaleString;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.ReturnedInfoItemMetaInformation;
import edu.internet2.consent.informed.model.ReturnedInfoTypeList;

@Controller
public class RHRegistrationController {
	
	@RequestMapping(value="/rhregistration",method=RequestMethod.POST)
	public ModelAndView handlePostRHRegistration(HttpServletRequest req) {
		//
		// Multiple possibilities 
		//
		int state = 0;
		String component = "none";
		
		if (CarAdminUtils.init(req) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		if (req.getParameter("formname").equals("newrhform")) {
			// this is a new RH request
			String rhtype = req.getParameter("rhtype");
			String rhid = req.getParameter("rhid");
			// Handle the display name as an internationalized string set
			InternationalizedString displayname = new InternationalizedString();
			ArrayList<LocaleString> displayals = new ArrayList<LocaleString>();
			for (int i = 1; i <= Integer.parseInt(req.getParameter("displaynamecount")); i++) {
				// For each displayname value pair
				LocaleString s = new LocaleString();
				s.setLocale(req.getParameter("displaynamelanguage"+i));
				s.setValue(req.getParameter("displaynamevalue"+i));
				displayals.add(s);
			}
			displayname.setLocales(displayals);
			// Handle the description the same way
			InternationalizedString description = new InternationalizedString();
			ArrayList<LocaleString> descriptionals = new ArrayList<LocaleString>();
			for (int i = 1; i <= Integer.parseInt(req.getParameter("descriptioncount")); i++) {
				// for each description value pair
				LocaleString s = new LocaleString();
				s.setLocale(req.getParameter("descriptionlanguage"+i));
				s.setValue(req.getParameter("descriptionvalue"+i));
				descriptionals.add(s);
			}
			description.setLocales(descriptionals);
			
			ReturnedRHMetaInformation rhmi = new ReturnedRHMetaInformation();
			rhmi.setDescription(description);
			rhmi.setDisplayname(displayname);
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(rhtype);
			rhi.setRhid(rhid);
			rhmi.setRhidentifier(rhi);
			CarAdminUtils.putRHMetaInformation(rhmi);
			state = 1;
			component = "createrh";
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("added new resource holder " + displayname.getLocales().get(0).getValue());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname").matches("rhinfo_edit_.*")) {
			// This is an edit update request for RH metainfo
			String rhtype = req.getParameter("rhtype");
			String rhid = req.getParameter("rhidentifier");
			// Collect the internationalized strings
			InternationalizedString displayname = new InternationalizedString();
			ArrayList<LocaleString> displayals = new ArrayList<LocaleString>();
			for (int i = 1; i <= Integer.parseInt(req.getParameter("displaynamecount")); i++) {
				LocaleString ls = new LocaleString();
				ls.setLocale(req.getParameter("displaynamelanguage_"+i));
				ls.setValue(req.getParameter("displaynamevalue_"+i));
				displayals.add(ls);
			}
			displayname.setLocales(displayals);
			InternationalizedString description = new InternationalizedString();
			ArrayList<LocaleString> descriptionals = new ArrayList<LocaleString>();
			for (int i = 1; i <= Integer.parseInt(req.getParameter("descriptioncount")); i++) {
				LocaleString ls = new LocaleString();
				ls.setLocale(req.getParameter("descriptionlanguage_"+i));
				ls.setValue(req.getParameter("descriptionvalue_"+i));
				descriptionals.add(ls);
			}
			description.setLocales(descriptionals);
			ReturnedRHMetaInformation rhmi = new ReturnedRHMetaInformation();
			rhmi.setDescription(description);
			rhmi.setDisplayname(displayname);
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(rhtype);
			rhi.setRhid(rhid);
			rhmi.setRhidentifier(rhi);
			CarAdminUtils.putRHMetaInformation(rhmi);
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated resource holder " + rhmi.getDisplayname().getLocales().get(0).getValue());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		} else if (req.getParameter("formname").matches("form[0-9].*")) {
			// This is an update to the iilist for the entity
			// Also now includes updates for (potentially) all the attributes in the iilist
			
			String rhtype = req.getParameter("rhtype");
			String rhid = req.getParameter("rhid");
			String formnumber = req.getParameter("formnumber");
			
			// Collect the list of infoitems
			ReturnedRHInfoItemList rriil = new ReturnedRHInfoItemList();
			RHIdentifier rhidentifier = new RHIdentifier();
			rhidentifier.setRhtype(rhtype);
			rhidentifier.setRhid(rhid);
			rriil.setRhidentifier(rhidentifier);
			ArrayList<InfoItemIdentifier> aiii = new ArrayList<InfoItemIdentifier>();
			for (int i = 1; i <= Integer.parseInt(req.getParameter("iicount")); i++) {
				if (req.getParameter("iitype"+i) != null && ! req.getParameter("iitype"+i).equalsIgnoreCase("")) {
					InfoItemIdentifier iii = new InfoItemIdentifier();
					iii.setIitype(req.getParameter("iitype"+i));
					iii.setIiid(req.getParameter("iiid"+i));
					aiii.add(iii);
					
					// while we're at it, process those that have changed
					CarAdminUtils.locError("ERR0033",formnumber,String.valueOf(i));
					if (req.getParameter("ii_modified_"+formnumber+"_"+i) != null && req.getParameter("ii_modified_"+formnumber+"_"+i).equals("true")) {
						// This one was modified, so update its ii metainfo while we're here
						CarAdminUtils.locError("ERR0034");
						ReturnedInfoItemMetaInformation riimi = new ReturnedInfoItemMetaInformation();
						RHIdentifier rhi = new RHIdentifier();
						rhi.setRhtype(rhtype);
						rhi.setRhid(rhid);
						riimi.setRhidentifier(rhi);
						riimi.setIiidentifier(iii);
						InternationalizedString dn = new InternationalizedString();
						ArrayList<LocaleString> locales = new ArrayList<LocaleString>();
						for (int j = 1; j <= Integer.parseInt(req.getParameter("ii_"+formnumber+"_"+i+"_dnlangcount")); j++) {
							LocaleString ls = new LocaleString();
							ls.setLocale(req.getParameter("ii_"+formnumber+"_"+i+"_dnlang_"+j));
							ls.setValue(req.getParameter("ii_"+formnumber+"_"+i+"_dnvalue_"+j));
							locales.add(ls);
						}
						dn.setLocales(locales);
						InternationalizedString desc = new InternationalizedString();
						ArrayList<LocaleString> locales2 = new ArrayList<LocaleString>();
						for (int j = 1 ; j <= Integer.parseInt(req.getParameter("ii_"+formnumber+"_"+i+"_desclangcount")); j++) {
							LocaleString ls = new LocaleString();
							ls.setLocale(req.getParameter("ii_"+formnumber+"_"+i+"_desclang_"+j));
							ls.setValue(req.getParameter("ii_"+formnumber+"_"+i+"_descvalue_"+j));
							locales2.add(ls);
						}
						desc.setLocales(locales2);
						riimi.setDisplayname(dn);
						riimi.setDescription(desc);
						riimi.setPresentationtype(req.getParameter("ii_"+formnumber+"_"+i+"_prestype"));
						riimi.setPolicytype(req.getParameter("ii_"+formnumber+"_"+i+"_policytype"));
						if (req.getParameter("ii_"+formnumber+"_"+i+"_asnd") != null && req.getParameter("ii_"+formnumber+"_"+i+"_asnd").equals("true"))
							riimi.setAsnd(true);
						if (req.getParameter("ii_"+formnumber+"_"+i+"_multivalued") != null && req.getParameter("ii_"+formnumber+"_"+i+"_multivalued").equals("true"))
							riimi.setMultivalued(true);
						if (req.getParameter("ii_"+formnumber+"_"+i+"_sensitivity") != null && req.getParameter("ii_"+formnumber+"_"+i+"_sensitivity").equals("true"))
							riimi.setSensitivity(true);
						CarAdminUtils.putIIMetaInformation(riimi);
						
					}
				}
			}
			rriil.setInfoitemlist(aiii);
			CarAdminUtils.putRHIIList(rriil);
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated information item info for " + rhid);
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		}
		String returl = "redirect:/rhregistration";
		if (state == 1) {
			returl = returl + "/?state=1";
			returl = returl + "&component="+component;
		} else if (state == 0) {
			returl = returl + "/?state=0";
			returl = returl + "&component="+component;
		}
		ModelAndView retval = new ModelAndView(returl);
		return retval;
	}
	
	@RequestMapping(value="/rhregistration/{rhtype}/{rhvalue}",method=RequestMethod.POST)
	public ModelAndView handlePostRHRegistrationByEntity(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhvalue") String rhvalue) {
		
		int state = 0;
		String component = "none";
		
		if (CarAdminUtils.init(req) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		// Behave differently based on the formname parameter passed to us
		if (req.getParameter("formname") != null && req.getParameter("formname").matches("^form_[^_]*_edit$")) {
			// POST of a language specific edit form -- sift out the language
			String chlang = req.getParameter("formname");
			chlang = chlang.replaceAll("^form_", "");
			chlang = chlang.replaceAll("_edit$","");
			// Get the RHMetaInformation we have now
			ReturnedRHMetaInformation rrhmi = CarAdminUtils.getResourceHolderMetaInformation(rhtype, CarAdminUtils.idUnEscape(rhvalue));
			
			// if this is a delete, handle that first
			if (req.getParameter("is_delete").equalsIgnoreCase("true")) {
				// Process delete
				LocaleString dls = new LocaleString();
				dls.setLocale(chlang);
				dls.setValue(req.getParameter("displayname"));
				rrhmi.getDisplayname().getLocales().remove(dls);
				LocaleString cls = new LocaleString();
				cls.setLocale(chlang);;
				cls.setValue(req.getParameter("description"));
				rrhmi.getDescription().getLocales().remove(cls);
				CarAdminUtils.putRHMetaInformation(rrhmi);  // actually update the rh metainfo
				
				ActivityStreamEntry ase = new ActivityStreamEntry();
				ase.setUser(req.getRemoteUser());
				ase.setType("admin");
				ase.setTimestamp(System.currentTimeMillis());
				ase.setOperation("deleted locale binding from resource holder " + rrhmi.getDisplayname().getLocales().get(0).getValue());
				try {
					CarAdminUtils.postActivityStreamEntry(ase);
				} catch (Exception e) {
					// ignore
				}
				return(new ModelAndView("redirect:/rhregistration/"+rhtype+"/"+rhvalue+"/?state=1&component=dellang"));
//				return retval;
			} 
			// Otherwise replace the values for displayname and description for this language (if it exists)
			boolean updated = false;
			for (LocaleString displs : rrhmi.getDisplayname().getLocales()) {
				if (displs.getLocale().equalsIgnoreCase(chlang)) {
					// Match
					updated = true;
					displs.setValue(req.getParameter("displayname"));
				}
			}
			if (! updated) {
				// We need to add another locale
				LocaleString nls = new LocaleString();
				nls.setLocale(chlang);
				nls.setValue(req.getParameter("displayname"));
				InternationalizedString is = rrhmi.getDisplayname();
				ArrayList<LocaleString> isl = (ArrayList<LocaleString>) is.getLocales();
				isl.add(nls);
				is.setLocales(isl);
				rrhmi.setDisplayname(is);
			}
			updated = false;
			for (LocaleString descls : rrhmi.getDescription().getLocales()) {
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
				InternationalizedString is = rrhmi.getDescription();
				ArrayList<LocaleString> isl = (ArrayList<LocaleString>) is.getLocales();
				isl.add(nls);
				is.setLocales(isl);
				rrhmi.setDescription(is);
			}
			
			CarAdminUtils.putRHMetaInformation(rrhmi);  // actually update the rh metainfo
			state = 1;
			component = "updatemetainfo";
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated resource holder metainformation for " + rrhmi.getDisplayname().getLocales().get(0).getValue());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
		//} else if (req.getParameter("formname") != null && req.getParameter("formname").matches("^form-add-ii-.*$")) {
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equals("additemform")) {
			// Add request from the add form -- create new info item of this type
			//String itype = req.getParameter("formname").replaceAll("^form-add-ii-","");
			String itype = req.getParameter("iitype");
			String id = req.getParameter("iiid");
			id = id.replaceAll(" ", "_");
			id = id.replaceAll(",", "_");
			String displaynamelang = req.getParameter("displaynamelang");
			String displaynamevalue = req.getParameter("displaynamevalue");
			String descriptionlang = req.getParameter("descriptionlang");
			String descriptionvalue = req.getParameter("descriptionvalue");
			String prestype = req.getParameter("prestype");
			String policytype = req.getParameter("policytype");
			boolean asnd = req.getParameter("asnd") != null;
			boolean multivalued = req.getParameter("multivalued") != null;
			boolean sensitivity = req.getParameter("sensitivity") != null;
			
			
			
			// Construct the object to add
			ReturnedInfoItemMetaInformation riimi = new ReturnedInfoItemMetaInformation();
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(rhtype);
			rhi.setRhid(CarAdminUtils.idUnEscape(rhvalue));
			riimi.setRhidentifier(rhi);
			
			// Check if we need to add a type for completeness
			ReturnedInfoTypeList ritl = CarAdminUtils.getInfoTypes(rhi);
		
			if (ritl == null) {
				ritl = new ReturnedInfoTypeList();
				ritl.setRhtype(rhtype);
				ritl.setRhvalue(CarAdminUtils.idUnEscape(rhvalue));
				ritl.setInfotypes(new ArrayList<String>());
			}
			
			if (ritl.getInfotypes() == null) {
				ritl.setInfotypes(new ArrayList<String>());
			}
			
			if (! ritl.getInfotypes().contains(itype)) {
				ritl.getInfotypes().add(itype);
				CarAdminUtils.putInfoTypes(ritl);
			}
		
			InfoItemIdentifier iii = new InfoItemIdentifier();
			iii.setIitype(itype);
			iii.setIiid(id);
			riimi.setIiidentifier(iii);
			InternationalizedString dname = new InternationalizedString();
			LocaleString ls = new LocaleString();
			ls.setLocale(displaynamelang);
			ls.setValue(displaynamevalue);
			ArrayList<LocaleString> als = new ArrayList<LocaleString>();
			als.add(ls);
			dname.setLocales(als);
			InternationalizedString cname = new InternationalizedString();
			LocaleString dls = new LocaleString();
			dls.setLocale(descriptionlang);
			dls.setValue(descriptionvalue);
			ArrayList<LocaleString> adls = new ArrayList<LocaleString>();
			adls.add(dls);
			cname.setLocales(adls);
			riimi.setDisplayname(dname);
			riimi.setDescription(cname);
			riimi.setPresentationtype(prestype);
			riimi.setPolicytype(policytype);
			riimi.setAsnd(asnd);
			riimi.setMultivalued(multivalued);
			riimi.setSensitivity(sensitivity);
			
			// and put it to the server
			CarAdminUtils.putIIMetaInformation(riimi);
			
			// and add it to the RH's info item list if not already there (adds are idempotent here)
			ReturnedRHInfoItemList rhiil = CarAdminUtils.getIiList(rhi);
			
			if (rhiil == null) {
				// No items yet -- build an empty container
				rhiil = new ReturnedRHInfoItemList();
				rhiil.setRhidentifier(rhi);
				rhiil.setInfoitemlist(new ArrayList<InfoItemIdentifier>());
			}
			if (! rhiil.getInfoitemlist().contains(iii))
				rhiil.getInfoitemlist().add(iii);
			CarAdminUtils.putRHIIList(rhiil);
			state = 1;
			component = "additem";
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("added new information item to resource holder " + rhi.getRhid());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
			
		} else if (req.getParameter("formname") != null && req.getParameter("formname").equals("form_rhlang_add")) {
			ReturnedRHMetaInformation rrhmi = CarAdminUtils.getResourceHolderMetaInformation(rhtype, CarAdminUtils.idUnEscape(rhvalue));
			LocaleString dls = new LocaleString();
			dls.setLocale(req.getParameter("lang"));
			dls.setValue(req.getParameter("displayname"));
			rrhmi.getDisplayname().getLocales().add(dls);
			LocaleString cls = new LocaleString();
			cls.setLocale(req.getParameter("lang"));
			cls.setValue(req.getParameter("description"));
			rrhmi.getDescription().getLocales().add(cls);
			CarAdminUtils.putRHMetaInformation(rrhmi);  // actually update the rh metainfo
			state = 1;
			component = "addlanguage";
		
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("added new locale binding to resource holder " + rrhmi.getDisplayname().getLocales().get(0).getValue());
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
//			return retval;
		} else if (req.getParameter("formname") != null && req.getParameter("formname").matches("^iiform_.*$")) {
			// This is an edit form for IIs of some type
			CarAdminUtils.locError("ERR0049","Called to update ii metainformation");
			String iitype = req.getParameter("formname");
			iitype = iitype.replaceAll("^iiform_", "");
			
			int iicount = Integer.parseInt(req.getParameter("iicount"));
			
			// For each ii in the current set, retrieve the current iimetainformation and, if values
			// differ, update it.
			// 
			// We avoid (minimally) expense of updates where possible.
			//
			
			for (int i = 1; i <= iicount; i++) {
				// Iterate the counter
				String iiname = req.getParameter("iid_"+i);
				RHIdentifier rhi = new RHIdentifier();
				rhi.setRhtype(rhtype);
				rhi.setRhid(CarAdminUtils.idUnEscape(rhvalue));
				InfoItemIdentifier iii = new InfoItemIdentifier();
				iii.setIitype(iitype);
				iii.setIiid(iiname);
				ReturnedInfoItemMetaInformation riimi = CarAdminUtils.getIIMetaInformation(rhi, iii);
				
				if (riimi == null) {
					riimi = new ReturnedInfoItemMetaInformation();
					riimi.setRhidentifier(rhi);
					riimi.setIiidentifier(iii);
				}
				// update if needed
				boolean needsupdate = false;
				if (riimi.getPresentationtype() == null || ! req.getParameter("iipresentationtype_"+i).equals(riimi.getPresentationtype())) {
					needsupdate=true;
					riimi.setPresentationtype(req.getParameter("iipresentationtype_"+i));
				}
				if (riimi.getPolicytype() == null || ! req.getParameter("iipolicytype_"+i).equals(riimi.getPolicytype())) {
					needsupdate=true;
					riimi.setPolicytype(req.getParameter("iipolicytype_"+i));
				}
				if ((req.getParameter("iiasnd_"+i) != null && req.getParameter("iiasnd_"+i).equals("true")) != riimi.getAsnd()) {
					needsupdate=true;
					riimi.setAsnd((req.getParameter("iiasnd_"+i) != null && req.getParameter("iiasnd_"+i).equals("true")));
					CarAdminUtils.locError("ERR0049","Setting asnd to "+riimi.getAsnd());
				}
				if ((req.getParameter("iimultivalued_"+i) != null && req.getParameter("iimultivalued_"+i).equals("true")) != riimi.getMultivalued()) {
					needsupdate=true;
					riimi.setMultivalued((req.getParameter("iimultivalued_"+i) != null && req.getParameter("iimultivalued_"+i).equals("true")));
					CarAdminUtils.locError("ERR0049","Setting multivalued to " + riimi.getMultivalued());
				}
				if ((req.getParameter("iisensitive_"+i) != null && req.getParameter("iisensitive_"+i).equals("true")) != riimi.getSensitivity()) {
					needsupdate=true;
					riimi.setSensitivity((req.getParameter("iisensitive_"+i) != null && req.getParameter("iisensitive_"+i).equals("true")));
					CarAdminUtils.locError("ERR0049","Setting sensitivitivy to " + riimi.getSensitivity());
				}
				
				if (needsupdate) {
					CarAdminUtils.putIIMetaInformation(riimi);
				}
			}
			state = 1;
			component = "updateii";
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("updated information item metainformation for " + CarAdminUtils.idUnEscape(rhvalue));
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
//			return retval;
		} else if (req.getParameter("formname") != null & req.getParameter("formname").equals("addiitype")) {
			// new II type to add
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(rhtype);
			rhi.setRhid(CarAdminUtils.idUnEscape(rhvalue));
			ReturnedInfoTypeList ritl = CarAdminUtils.getInfoTypes(rhi);
			if (ritl == null) {
				// urf -- initialize one
				ritl = new ReturnedInfoTypeList();
				ritl.setRhtype(rhtype);
				ritl.setRhvalue(CarAdminUtils.idUnEscape(rhvalue));
				ritl.setInfotypes(new ArrayList<String>());
				ReturnedRHInfoItemList riil = CarAdminUtils.getIiList(rhi);
				if (riil != null && riil.getInfoitemlist() != null) {
					for (InfoItemIdentifier i : riil.getInfoitemlist()) {
						if (! ritl.getInfotypes().contains(i.getIitype()))
							ritl.getInfotypes().add(i.getIitype());
					}
				}
			}
			if (req.getParameter("iitype") != null && ! ritl.getInfotypes().contains(req.getParameter("iitype"))){
				ritl.getInfotypes().add(req.getParameter("iitype"));
				CarAdminUtils.putInfoTypes(ritl);
			}
			
			ActivityStreamEntry ase = new ActivityStreamEntry();
			ase.setUser(req.getRemoteUser());
			ase.setType("admin");
			ase.setTimestamp(System.currentTimeMillis());
			ase.setOperation("added information item type for " + CarAdminUtils.idUnEscape(rhvalue));
			try {
				CarAdminUtils.postActivityStreamEntry(ase);
			} catch (Exception e) {
				// ignore
			}
//			return retval;
		}
		ModelAndView retval = new ModelAndView("redirect:/rhregistration/"+rhtype+"/"+rhvalue+"/?state=" + state + "&component=" + component);

		return retval;
	}

	@RequestMapping(value="/delregistration/{rhtype}/{rhvalue}",method=RequestMethod.POST)
	public ModelAndView handleDeleteRHRegistrationByEntity(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhvalue") String rhvalue) {
		ModelAndView retval = new ModelAndView("redirect:/rhregistration?state=1&component=delrh");
		
		if (CarAdminUtils.init(req) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		RHIdentifier rhi = new RHIdentifier();
		rhi.setRhtype(rhtype);
		rhi.setRhid(CarAdminUtils.idUnEscape(rhvalue));
		
		CarAdminUtils.archiveMetaInformation(rhi);
		
		ActivityStreamEntry ase = new ActivityStreamEntry();
		ase.setUser(req.getRemoteUser());
		ase.setType("admin");
		ase.setTimestamp(System.currentTimeMillis());
		ase.setOperation("archived resource holder " + CarAdminUtils.idUnEscape(rhvalue));
		try {
			CarAdminUtils.postActivityStreamEntry(ase);
		} catch (Exception e) {
			// ignore
		}
		return retval;
		
	}
	

	@RequestMapping(value="/rhregistration/{rhtype}/{rhvalue}",method=RequestMethod.GET)
	public ModelAndView handleGetRHRegistrationByEntity(HttpServletRequest req, @PathVariable("rhtype") String rhtype, @PathVariable("rhvalue") String rhvalue) {
		
		ModelAndView retval = new ModelAndView("RHRegistrationByRH");
		
		AdminConfig config = null;
		
		if ((config = CarAdminUtils.init(req)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		String rhv = CarAdminUtils.idEscape(rhvalue);
		
		// TODO:  Refactor this so that we pass in only one (not a list) and so that the vm expects only one (not a list)
		
		// Marshall the data
		ArrayList<ReturnedRHMetaInformation> rhmia = new ArrayList<ReturnedRHMetaInformation>();
		ArrayList<InjectedRHMetainformation> irhma = new ArrayList<InjectedRHMetainformation>();
		HashMap<InfoItemIdentifier,ReturnedInfoItemMetaInformation> ihash = new HashMap<InfoItemIdentifier,ReturnedInfoItemMetaInformation>();
		HashMap<String,HashMap<InfoItemIdentifier,ReturnedInfoItemMetaInformation>> phash = new HashMap<String,HashMap<InfoItemIdentifier,ReturnedInfoItemMetaInformation>>();
		ArrayList<ReturnedRPMetaInformation> armi = new ArrayList<ReturnedRPMetaInformation>(); 
		ArrayList<UserReturnedPolicy> rup = new ArrayList<UserReturnedPolicy>();
		int activerpct=0, userct=0;
		
		ReturnedRHMetaInformation rmi = CarAdminUtils.getResourceHolderMetaInformation(rhtype, rhv);
		if (rmi == null) {
			throw new RuntimeException("No Metainformation found for " + rhtype + ", " + rhvalue);
		}
		rhmia.add(rmi);
		
		for (ReturnedRHMetaInformation r : rhmia) {
			InjectedRHMetainformation i = new InjectedRHMetainformation();
			i.setRhtype(r.getRhidentifier().getRhtype());
			i.setRhidentifier(r.getRhidentifier().getRhid());
			i.setDisplayname(CarAdminUtils.localize(r.getDisplayname(), req.getLocale().getLanguage()));
			i.setDescription(CarAdminUtils.localize(r.getDescription(), req.getLocale().getLanguage()));
			i.setIilistmap(CarAdminUtils.getIiList(r.getRhidentifier()));
			if (i !=  null && i.getIilistmap() != null) {
				for (InfoItemIdentifier iii : i.getIilistmap().getInfoitemlist()) {
					// for each of the iis get the metainfo and add it to the map
					ReturnedInfoItemMetaInformation riimi = CarAdminUtils.getIIMetaInformation(r.getRhidentifier(),iii);
					ihash.put(iii, riimi);
					// And into the phash partitioned map as well
					if (phash.containsKey(iii.getIitype())) {
						phash.get(iii.getIitype()).put(iii, riimi);
					} else {
						HashMap<InfoItemIdentifier,ReturnedInfoItemMetaInformation> padd = new HashMap<InfoItemIdentifier,ReturnedInfoItemMetaInformation>();
						padd.put(iii, riimi);
						phash.put(iii.getIitype(),padd);
					}
				}
			}
			// In case there are added infotypes without any items yet, add them here
			RHIdentifier rhi = new RHIdentifier();
			rhi.setRhtype(rhtype);
			rhi.setRhid(rhvalue);
			ReturnedInfoTypeList ritl = CarAdminUtils.getInfoTypes(rhi);
			if (ritl != null && ritl.getInfotypes() != null) {
				for (String s : ritl.getInfotypes()) {
					if (! phash.containsKey(s)) {
						phash.put(s,new HashMap<InfoItemIdentifier,ReturnedInfoItemMetaInformation>());
					}
				}
			}
			i.setIdisplayname(r.getDisplayname());
			i.setIdescription(r.getDescription());
			irhma.add(i);
			
			ArrayList<ReturnedRPMetaInformation> arrmi = null;
			arrmi = CarAdminUtils.getAllRPsForRH(r.getRhidentifier().getRhtype(), r.getRhidentifier().getRhid());
			if (arrmi != null && ! arrmi.isEmpty()) {
				armi.addAll(arrmi);
			}
			ArrayList<String> armiValues = new ArrayList<String>();
			for (ReturnedRPMetaInformation rrpmi : armi) {
				armiValues.add(rrpmi.getRpidentifier().getRpid());
			}

			ArrayList<UserReturnedPolicy> aru = CarAdminUtils.getUserPoliciesForRH(r.getRhidentifier().getRhtype(),r.getRhidentifier().getRhid());
			if (aru != null && ! aru.isEmpty())
				rup.addAll(aru);

			HashSet<String> activerps = new HashSet<String>();
			HashSet<String> users = new HashSet<String>();
			if (rup != null) {
				HashMap<String,ReturnedRPMetaInformation> amap = new HashMap<String,ReturnedRPMetaInformation>();
				for (UserReturnedPolicy up : rup) {
					if (! amap.containsKey(up.getUserInfoReleasePolicy().getRelyingPartyId().getRPvalue())) {
						amap.put(up.getUserInfoReleasePolicy().getRelyingPartyId().getRPvalue(),CarAdminUtils.getRelyingPartyMetaInformation(r.getRhidentifier().getRhtype(),r.getRhidentifier().getRhid(),up.getUserInfoReleasePolicy().getRelyingPartyId().getRPtype(),up.getUserInfoReleasePolicy().getRelyingPartyId().getRPvalue()));
					}
					if (armiValues.contains(up.getUserInfoReleasePolicy().getRelyingPartyId().getRPvalue())) {
						if (! activerps.contains(up.getUserInfoReleasePolicy().getRelyingPartyId().getRPvalue()))
							activerps.add(up.getUserInfoReleasePolicy().getRelyingPartyId().getRPvalue());
						users.add(up.getUserInfoReleasePolicy().getUserId().getUserValue());
					}
				}
			}
			activerpct = activerps.size();
			userct = users.size();
			
			ArrayList<OrgReturnedPolicy> aorp = CarAdminUtils.getRHOrgInfoReleasePolicies(rmi.getRhidentifier().getRhtype(),rmi.getRhidentifier().getRhid());
			int ocount = 0;
			if (aorp != null) {
				ocount = aorp.size();
			}
			retval.addObject("orgpolct",ocount);
			ArrayList<IcmReturnedPolicy> airp = CarAdminUtils.getRHIcmInfoReleasePolicies(rmi.getRhidentifier().getRhtype(),rmi.getRhidentifier().getRhid());
			int icount = 0;
			if (airp != null) {
				icount = airp.size();
			}
			retval.addObject("metapolct",icount);

		}
		
		ArrayList<ReturnedRHMetaInformation> rhmil = CarAdminUtils.getAllDefinedResourceHolders();
		Collections.sort(rhmil,new ReturnedRHMetaInformationComparator());

		

		Collections.sort(irhma,new InjectedRHMetaInformationComparator());
		retval.addObject("injectedrhlist",irhma);
		retval.addObject("availablerhs",rhmil);
		retval.addObject("ihash",ihash);
		retval.addObject("phash",phash);
		retval.addObject("InfoItemMode",InfoItemMode.class);
		retval.addObject("Collections",Collections.class);
		retval.addObject("iicomparator",new InfoItemIdentifierComparator());
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("rhidtypes",CarAdminUtils.getSupportedRHIDTypes());
		retval.addObject("iitypes",CarAdminUtils.getSupportedIITypes());
		retval.addObject("languages",CarAdminUtils.getSupportedLanguages());
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		retval.addObject("armi",armi);
		retval.addObject("rup",rup);
		retval.addObject("activerpct",activerpct);
		retval.addObject("userct",userct);
		retval.addObject("activetab","rhregistration");
		retval.addObject("lang",req.getLocale().getLanguage());
		
		retval.addObject("logouturl",config.getProperty("logouturl",false));

		if (req.getParameter("state") != null && req.getParameter("state").equals("1")) {
			if (req.getParameter("component").equals("createrh"))
				retval.addObject("successmsg","Successfully created resource holder");
			else if (req.getParameter("component").equals("updateii"))
				retval.addObject("successmsg","Successfully updated information item(s)");
			else if (req.getParameter("component").equals("updatemetainfo"))
				retval.addObject("successmsg","Successfully updated metainformation");
			else if (req.getParameter("component").equals("additem"))
				retval.addObject("successmsg","Successfully added information item");
			else if (req.getParameter("component").equals("addlanguage"))
				retval.addObject("successmsg","Successfully added language mapping");
			else if (req.getParameter("component").equals("delrh"))
				retval.addObject("successmsg","Successfully archived resource holder");
			else if (req.getParameter("component").equals("dellang"))
				retval.addObject("successmsg","Successfully deleted language mapping");
		} else if (req.getParameter("state") != null) {
			if (req.getParameter("component") != null && req.getParameter("component").equals("createrh"))
				retval.addObject("failmsg","Failed to create resource holder");
		}
		return(retval);
		
	}
	@RequestMapping(value="/rhregistration",method=RequestMethod.GET)
	public ModelAndView handleGetRHRegistration(HttpServletRequest req) {
		
		ModelAndView retval;
		
		AdminConfig config = null;
		if ((config = CarAdminUtils.init(req)) == null) {
			ModelAndView eval = new ModelAndView("errorPage");
			eval.addObject("message","You are not authorized to access this service");
			return eval;
		}
		
		//TODO:  This is only for testing -- eventually, we should only use R?HRegistrationMain here regardless
		if (req.getParameter("full") != null && req.getParameter("full").equalsIgnoreCase("true")) {
			retval = new ModelAndView("RHRegistration");
		} else {
			retval = new ModelAndView("RHRegistrationMain");
		}
		
		// Marshall the list of Registered RHs first
		//
		// Get the /metainformation endpoint under /rhic in the informed API
		// and split the result into an array of RHMetaInformation objects
		// 
		// On the way, marshall a hashmap of the info item lists for the supported RHs
		//
		
		ArrayList<ReturnedRHMetaInformation> rhmia = CarAdminUtils.getAllDefinedResourceHolders();
		ArrayList<InjectedRHMetainformation> irhma = new ArrayList<InjectedRHMetainformation>();
		HashMap<InfoItemIdentifier,ReturnedInfoItemMetaInformation> ihash = new HashMap<InfoItemIdentifier,ReturnedInfoItemMetaInformation>();
		

		for (ReturnedRHMetaInformation r : rhmia) {
			InjectedRHMetainformation i = new InjectedRHMetainformation();
			i.setRhtype(r.getRhidentifier().getRhtype());
			i.setRhidentifier(r.getRhidentifier().getRhid());
			i.setDisplayname(CarAdminUtils.localize(r.getDisplayname(), req.getLocale().getLanguage()));
			i.setDescription(CarAdminUtils.localize(r.getDescription(), req.getLocale().getLanguage()));
			i.setIilistmap(CarAdminUtils.getIiList(r.getRhidentifier()));
			if (i !=  null && i.getIilistmap() != null) {
				for (InfoItemIdentifier iii : i.getIilistmap().getInfoitemlist()) {
					// for each of the iis get the metainfo and add it to the map
					ReturnedInfoItemMetaInformation riimi = CarAdminUtils.getIIMetaInformation(r.getRhidentifier(),iii);
					ihash.put(iii, riimi);
				}
			}
			i.setIdisplayname(r.getDisplayname());
			i.setIdescription(r.getDescription());
			irhma.add(i);
			
		}
		Collections.sort(rhmia,new ReturnedRHMetaInformationComparator());

		Collections.sort(irhma,new InjectedRHMetaInformationComparator());
		retval.addObject("injectedrhlist",irhma);
		retval.addObject("availablerhs",rhmia);
		retval.addObject("ihash",ihash);
		retval.addObject("InfoItemMode",InfoItemMode.class);
		//retval.addObject("authuser",req.getRemoteUser());
		retval.addObject("authuser",((String) req.getAttribute("eppn")).replaceAll(";.*$",""));
		retval.addObject("rhidtypes",CarAdminUtils.getSupportedRHIDTypes());
		retval.addObject("iitypes",CarAdminUtils.getSupportedIITypes());
		retval.addObject("languages",CarAdminUtils.getSupportedLanguages());
		retval.addObject("CarAdminUtils",CarAdminUtils.class);
		retval.addObject("activetab","rhregistration");
		retval.addObject("lang",req.getLocale().getLanguage());
		retval.addObject("logouturl",config.getProperty("logouturl", false));
		
		if (req.getParameter("state") != null && req.getParameter("state").equals("1")) {
			if (req.getParameter("component").equals("createrh"))
				retval.addObject("successmsg","Successfully created resource holder");
			else if (req.getParameter("component").equals("delrh"))
				retval.addObject("successmsg","Successfully archived resource holder");
		} else if (req.getParameter("state") != null) {
			if (req.getParameter("component") != null && req.getParameter("component").equals("createrh"))
				retval.addObject("failmsg","Failed to create resource holder");
		}

		return(retval);
	}
}
