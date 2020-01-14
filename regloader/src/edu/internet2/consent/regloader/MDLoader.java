/*
 * Copyright 20xx - 20xx Duke University
    
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
package edu.internet2.consent.regloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.opensaml.core.config.Configuration;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.idpdisco.DiscoveryResponse;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.ext.saml2mdrpi.RegistrationInfo;
import org.opensaml.saml.ext.saml2mdui.Description;
import org.opensaml.saml.ext.saml2mdui.DisplayName;
import org.opensaml.saml.ext.saml2mdui.Logo;
import org.opensaml.saml.ext.saml2mdui.PrivacyStatementURL;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.impl.ExtensionsImpl;
import org.w3c.dom.Element;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.InfoItemValueList;
import edu.internet2.consent.informed.model.InternationalizedString;
import edu.internet2.consent.informed.model.LocaleString;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.RPIdentifier;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPOptionalInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRPProperty;
import edu.internet2.consent.informed.model.ReturnedRPRequiredInfoItemList;
import edu.internet2.consent.informed.model.ReturnedValueMetaInformation;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;

public class MDLoader {

	// Main class for the MDloader CLI
	// Load metadata out of a SAML metadata file and turn it into
	// RP registration information
	
	// Just provides a main method as entry point for parsing
	// the command line and initiating processing
	
	public static void main(String[] args) {
		
		// Basic syntax is:
		//    $0 -mdfile <filename> -rhtype <type> -rhid <id> -conf <cf file> 
		//
		// With no other arguments, we load the entire metadata file
		// parsing it down to component RPs and processing each of 
		// them as an RP to the specified RH.  -conf specifies a config file
		// containing credentials and reference URLs for making changes via the 
		// CAR API.
		//
		//
		// If the specified RH does not exist in CAR, we fail.
		// If the mdfile does not exist, we fail.
		// If the mdfile does not parse properly, we fail.
		//
		// If everything works properly, for each RP specified in the 
		// metadata we consume, we either:
		//
		//  (1) If the RP does not already exist, create an RP for the
		//      specified RH in the CAR instance referenced by the config file
		//  (2) If the RP already exists, we update the components of it in 
		//	    the CAR world that are feasible to update.
		//
		// When complete, we kick the CAR cache  to force updates.
		//
		// Additional (optional) flags include:
		//
		//	-rpid <entityId>
		//		to restrict the load to a single RP by entity Id
		//	-dryrun
		//		instead of actually updating, report on what would be updated
		//		(basically, show each RP as it is and as it will be)
		//  -replace
		//		ignore existing data and simply replace the RP in place
		//		overwriting any previous data and any changes made in the UI
		//		already.
		//
		//  -onlynew
		//	    import new entities from the MD without touching already registered
		//      entities
		//
		// Additionally, we need to specify via the command line how we interact with the ICM
		//
		// Alternatively, we read this from the conf file specified
		// -icmhost
		//		icm hostname for constructing ICM-dependent URLs
		// -icmport
		//		ditto for the port number (usually 443)
		// -icmuser
		//		identifier to use when authenticating to the ICM
		//
		// -icmcred
		//		credential to use when authenticating to the ICM (password for icmuser)
		//
		
		
		String mdfilename = null;
		String rhtype = null;
		String rhid = null;
		String cfilename = null;
		String rpid = null;
		String icmhost = null;
		String icmport = null;
		String icmuser = null;
		String icmcred = null;
		boolean dryrun = false;
		boolean replace = false;
		boolean onlynew = false;
		
		// Parse command line arguments
		
		for (int i = 0 ; i < args.length; i ++) {
			if (args[i].equals("-mdfile")) {
				mdfilename = args[++i];
				System.out.println("Filename: " + mdfilename);
				continue;
			}
			if (args[i].equals("-rhtype")) {
				rhtype = args[++i];
				continue;
			}
			if (args[i].equals("-rhid")) {
				rhid = args[++i];
				continue;
			}
			if (args[i].equals("-conf")) {
				cfilename = args[++i];
				continue;
			}
			if (args[i].equals("-rpid")) {
				rpid = args[++i];
				continue;
			}
			if (args[i].equals("-dryrun")) {
				dryrun = true;
				continue;
			}
			if (args[i].equals("-replace")) {
				replace = true;
				continue;
			}
			if (args[i].equals("-onlynew")) {
				onlynew = true;
				continue;
			}
			if (args[i].equals("-icmhost")) {
				icmhost = args[++i];
				continue;
			}
			if (args[i].equals("-icmport")) {
				icmport = args[++i];
				continue;
			}
			if (args[i].equals("-icmuser")) {
				icmuser = args[++i];
				continue;
			}
			if (args[i].equals("-icmcred")) {
				icmcred = args[++i];
				continue;
			}
			
		}
		try {
			InitializationService.initialize();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		HashSet<String> attributenames = new HashSet<String>();

		FilesystemMetadataResolver fsmp = loadMDFile(mdfilename);
		BasicParserPool bpp = new BasicParserPool();
		try {
			fsmp.setId("myresolver");
			fsmp.setRequireValidMetadata(true);
			bpp.initialize();
			fsmp.setParserPool(bpp);
			fsmp.initialize();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ArrayList<String> entities = new ArrayList<String>();

		HashMap<String,ReturnedRPMetaInformation> mdloaded = new HashMap<String,ReturnedRPMetaInformation>();
		HashMap<String,HashMap<String,ArrayList<String>>> optionaliis = new HashMap<String,HashMap<String,ArrayList<String>>>();
		HashMap<String,HashMap<String,ArrayList<String>>> requirediis = new HashMap<String,HashMap<String,ArrayList<String>>>();
		
		
		Iterator<EntityDescriptor> it = fsmp.iterator();
		while (it.hasNext()) {
						
			// Foreach entity in the metadata blob...
			ReturnedRPMetaInformation rrpmi = new ReturnedRPMetaInformation();
			
			EntityDescriptor ed = it.next();
			String entity = ed.getEntityID();
			
			if (ed.getIDPSSODescriptor(SAMLConstants.SAML20P_NS) != null) {
				// Ignore this -- it's an IDP entry
				System.out.println(entity + " is an IDP -- ignoring");
				continue;
			} 
			if (ed.getAttributeAuthorityDescriptor(SAMLConstants.SAML20P_NS) != null) {
				// ignore attribute authorities
				System.out.println(entity + " is an Attribute Authority -- skipping");
				continue;
			}
			if (ed.getSPSSODescriptor(SAMLConstants.SAML20P_NS) == null && ed.getSPSSODescriptor(SAMLConstants.SAML11P_NS) == null) {
				// Ignore this -- it's something odd
				System.out.println(entity + " seems to be something we don't understand");
				continue;
			}
			
			// This must be an SP description
						
			// We assume since we're parsing SAML metadata that we can use entityId as our identifier type in all cases
			RPIdentifier rpi = new RPIdentifier();
			rpi.setRptype("entityId");
			rpi.setRpid(entity);

			rrpmi.setRpidentifier(rpi);
			
			RHIdentifier rhi = new RHIdentifier();
			
			rhi.setRhtype(rhtype);
			rhi.setRhid(rhid);
			
			rrpmi.setRhidentifier(rhi);
			
			// Default default showagain to true for now
			
			rrpmi.setDefaultshowagain("true");
			
			// Meaningful SP Extension values
			HashMap<String,String> spdisplayname = new HashMap<String,String>();
			HashMap<String,String> spdescription = new HashMap<String,String>();
			String infourl = "";
			String privacyurl = "";
			String logourl = "";
			HashMap<String,ArrayList<String>> requiredattrs = new HashMap<String,ArrayList<String>>();
			HashMap<String,ArrayList<String>> optionalattrs = new HashMap<String,ArrayList<String>>();
			
			
			
			// We care about Extensions belonging to the entity, and 
			// about the SPSSODescriptor and its extensions
			
			Extensions mainEx = ed.getExtensions();
			SPSSODescriptor spd = ed.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
			
			if (spd == null) {
				continue;  // we elide non SAML2 entities in this case.
			}
			Extensions spx = spd.getExtensions();
			
			if (spx == null) {
				entities.add(ed.getEntityID()); // add to the entity list anyway
				mdloaded.put(rrpmi.getRpidentifier().getRpid(),rrpmi);
				continue;  // if no extensions, no extensions
			}
			
			// We have more to parse
			
			InternationalizedString displayname = new InternationalizedString();
			ArrayList<LocaleString> displaylocales = new ArrayList<LocaleString>();
			displayname.setLocales(displaylocales);

			InternationalizedString description = new InternationalizedString();
			ArrayList<LocaleString> descriptionlocales = new ArrayList<LocaleString>();
			description.setLocales(descriptionlocales);
			
			ArrayList<ReturnedRPProperty> arrpp = new ArrayList<ReturnedRPProperty>();
			
			
			
			for (XMLObject spschild : spx.getOrderedChildren()) {
				if (spschild instanceof DiscoveryResponse) {
					continue;  // we don't care about DiscoveryResponse objects
				}
				else if (spschild instanceof UIInfo) {
					for (XMLObject uichild : spschild.getOrderedChildren()) {
						if (uichild instanceof DisplayName) {
							spdisplayname.put(((DisplayName) uichild).getXMLLang(),((DisplayName) uichild).getValue());
							LocaleString l = new LocaleString();
							l.setLocale(((DisplayName) uichild).getXMLLang());
							l.setValue(((DisplayName)uichild).getValue());
							displayname.getLocales().add(l);
							System.out.println("DisplayName:  " + ((DisplayName)uichild).getXMLLang() + " = " + ((DisplayName)uichild).getValue());;
						} else if (uichild instanceof Description) {
							spdescription.put(((Description) uichild).getXMLLang(),((Description) uichild).getValue());
							LocaleString l = new LocaleString();
							l.setLocale(((Description)uichild).getXMLLang());
							l.setValue(((Description)uichild).getValue());
							description.getLocales().add(l);
							System.out.println("Description: " + ((Description)uichild).getXMLLang() + " = " + ((Description)uichild).getValue());							
						} else if (uichild instanceof PrivacyStatementURL) {
							privacyurl = ((PrivacyStatementURL) uichild).getValue();
							System.out.println("PrivacyURL: " + ((PrivacyStatementURL)uichild).getXMLLang() + " = " + ((PrivacyStatementURL)uichild).getValue());
						} else if (uichild instanceof Logo) {
							logourl = ((Logo)uichild).getURL();
							System.out.println("Logo: " + ((Logo)uichild).getXMLLang() + " = " + ((Logo)uichild).getURL());
						}
					}
					// Sometimes we don't have any information
					if (displayname.getLocales().isEmpty()) {
						LocaleString l = new LocaleString();
						l.setLocale("en");
						l.setValue(entity);
						displayname.getLocales().add(l);
					}
					if (description.getLocales().isEmpty()) {
						description = displayname;
					}
					rrpmi.setDisplayname(displayname);
					rrpmi.setDescription(description);
					rrpmi.setPrivacyurl(privacyurl);
					rrpmi.setIconurl(logourl);
					
				}
			}
			
			for (XMLObject spdchild : spd.getOrderedChildren()) {
				 if (spdchild instanceof AttributeConsumingService) {
					 System.out.println("FOUND ATTRIBUTECONSUMINGSERVICE");
						for (XMLObject acschild : spdchild.getOrderedChildren()) {
							if (acschild instanceof RequestedAttribute) {
								String handle = ((RequestedAttribute)acschild).getFriendlyName();
								if (handle == null || handle.equals("")) {
									handle = ((RequestedAttribute)acschild).getName();
								}
								if ("true".equals(((RequestedAttribute)acschild).getUnknownAttributes().get("isRequired"))) {
									ArrayList<String> vals = new ArrayList<String>();
									if (((RequestedAttribute)acschild).getAttributeValues() == null || ((RequestedAttribute)acschild).getAttributeValues().isEmpty()) {
										vals.add(".*");
									} else {
										for (XMLObject v : ((RequestedAttribute)acschild).getAttributeValues()) {
											if (v instanceof AttributeValue) {
												vals.add(((AttributeValue)v).getDOM().getTextContent());
											}
										}
									}
									requiredattrs.put(handle, vals);
								} else {
									ArrayList<String> vals = new ArrayList<String>();
									if (((RequestedAttribute)acschild).getAttributeValues() == null || ((RequestedAttribute)acschild).getAttributeValues().isEmpty()) {
										vals.add(".*");
									} else {
										for (XMLObject v : ((RequestedAttribute)acschild).getAttributeValues()) {
											if (v instanceof AttributeValue) {
												vals.add(((AttributeValue)v).getDOM().getTextContent());
											}
										}
									}
									optionalattrs.put(handle, vals);
								}
							}
						}
					}
			}
			
			requirediis.put(entity, requiredattrs);
			optionaliis.put(entity, optionalattrs);
			
//			System.out.println("     Required Attrs: " );
//			for (String h : requiredattrs.keySet()) {
//				System.out.println("       Attr: " + h);
//				for (String i : requiredattrs.get(h)) {
				//	System.out.println("          " + h + " = " + i);
//				}
//			}
//			System.out.println("     Optional Attrs: " );
//			for (String h : optionalattrs.keySet()) {
				// System.out.println("       Attr: " + h);
//				for (String i : optionalattrs.get(h)) {
				//	System.out.println("          " + h + " = " + i);
//				}
//			}
			
			
			// Parse out extensions related to RegistrationAuthority or Attributes
			// Attributes in this case should be tags like entity-category and assurance-certification
			for (XMLObject mainchild : mainEx.getOrderedChildren()) {
				if (mainchild instanceof RegistrationInfo) {
					System.out.println("Registration Authority: " + ((RegistrationInfo) mainchild).getRegistrationAuthority());
					ReturnedRPProperty rrpp = new ReturnedRPProperty();
					rrpp.setRppropertyname("RegistrationAuthority");
					rrpp.setRppropertyvalue(((RegistrationInfo) mainchild).getRegistrationAuthority());
					arrpp.add(rrpp);
				} else if (mainchild instanceof EntityAttributes) {
					ArrayList<Attribute> attributes = new ArrayList<Attribute>(((EntityAttributes) mainchild).getAttributes());
					for (Attribute a : attributes) {
						for (XMLObject v : a.getAttributeValues()) {
							if (v instanceof AttributeValue) {
								ReturnedRPProperty rrpp = new ReturnedRPProperty();
								if (a.getName() != null && ! a.getName().equals("")) {
									//System.out.println("     "+a.getName()+" = " + ((AttributeValue)v).toString());
									attributenames.add(a.getName());
									rrpp.setRppropertyname(a.getName());
									rrpp.setRppropertyvalue(((AttributeValue)v).toString());
									
								}
								if (a.getFriendlyName() != null && ! a.getFriendlyName().equals("")) {
									//System.out.println("     "+a.getFriendlyName() + " = " + ((AttributeValue)v).toString());;
									rrpp.setRppropertyname(a.getFriendlyName());
									rrpp.setRppropertyvalue(((AttributeValue)v).toString());
								}
								arrpp.add(rrpp);
							} else if (v instanceof XSAnyImpl){
								ReturnedRPProperty rrpp = new ReturnedRPProperty();
								if (a.getName() != null && ! a.getName().equals("")) {
									//System.out.println("     "+a.getName()+" = "+((XSAnyImpl)v).getTextContent());
									attributenames.add(a.getName());
									rrpp.setRppropertyname(a.getName());
									rrpp.setRppropertyvalue(((XSAnyImpl)v).getTextContent());
								}
								if (a.getFriendlyName() != null && ! a.getFriendlyName().equals("")) {
									//System.out.println("     "+a.getFriendlyName()+" = "+((XSAnyImpl)v).getTextContent());
									rrpp.setRppropertyname(a.getFriendlyName());
									rrpp.setRppropertyvalue(((XSAnyImpl)v).getTextContent());
								}
								arrpp.add(rrpp);
							}
						}
					}
				}
			}
			
			rrpmi.setRpproperties(arrpp);
			mdloaded.put(rrpmi.getRpidentifier().getRpid(),rrpmi);

			System.out.println("SP " + ed.getEntityID());
			
				
			entities.add(ed.getEntityID());
		}

		// Dump information from the relevant informed content hive for comparison
		//
		
		HashMap<String,ReturnedRPMetaInformation> hmrp = retrieveCurrentRPRegistrations(icmhost,icmport,rhtype,rhid,icmuser,icmcred);
		HashMap<String,ReturnedRPRequiredInfoItemList> hmrrii = retrieveRequiredInfoItems(icmhost,icmport,rhtype,rhid,icmuser,icmcred);
		HashMap<String,ReturnedRPOptionalInfoItemList> hmroii = retrieveOptionalInfoItems(icmhost,icmport,rhtype,rhid,icmuser,icmcred);
		
		if (hmrp == null) {
			System.out.println("retrieveCurrentRPRegistration("+icmhost+","+icmport+","+rhtype+","+rhid+","+icmuser+","+icmcred+") returned null");
		} else {
			System.out.println("retrieveCurrentRPRegistration("+icmhost+","+icmport+","+rhtype+","+rhid+","+icmuser+","+icmcred+") returned " + hmrp.size() + " entries");
		}

		// At this point, we have the list of entity IDs in the metadata in 
		// "entities", and:
		//
		// mdloaded contains a hash from entity -> ReturnedRPMetaInformation
		// requirediis contains a hash from entity -> ArrayList<String(value)>
		// optionaliis contains a hash from entity -> ArrayList<String(value)>
		// 
		// while for informed content database entities, we have:
		//
		// hmrp contains a hash from entity -> ReturnedRPMetaIformation
		// hmrrii contains hash from entity -> ReturnedRequiredIIList
		// hmroii contains hash from entity -> ReutrnedOptionalIIList
		//
		// We now use this information to:
		//
		// * if dryrun, only report what would be done, don't actually do it
		// * find entries in metadata not in database and create them in database
		// * if not onlynew, find entries in both places and merge them
		
		// For testing, everything forced to dryrun
		// dryrun = true;  
		
		// Start by finding new entries
		for (String e : entities) {
			System.out.println("Operating on RP: " + e);
			// for every SP entity ID in the metadata being imported
			if (rpid != null && ! rpid.equals("")) {
				if (! e.equals(rpid)) {
					continue;   // ignore all others if rpid is set on command line
				}
			}
			if (! hmrp.containsKey(e)) {
				// this is a new one
				System.out.println("Create: " + e);
				if (mdloaded.get(e).getDisplayname() != null && mdloaded.get(e).getDisplayname().getLocales() != null && ! mdloaded.get(e).getDisplayname().getLocales().isEmpty())
					System.out.println("     Displayname: " + mdloaded.get(e).getDisplayname().getLocales().get(0).getValue() + "("+mdloaded.get(e).getDisplayname().getLocales().get(0).getLocale()+")");
				if (mdloaded.get(e).getDescription() != null && mdloaded.get(e).getDescription() != null && ! mdloaded.get(e).getDescription().getLocales().isEmpty())
					System.out.println("     Description: " + mdloaded.get(e).getDescription().getLocales().get(0).getValue() + "(" + mdloaded.get(e).getDescription().getLocales().get(0).getLocale()+")");
				System.out.println("     PrivacyURL: " + mdloaded.get(e).getPrivacyurl());
				System.out.println("     IconURL: " + mdloaded.get(e).getIconurl());
				System.out.println("     Properties:");
				if (mdloaded.get(e).getRpproperties() != null) {
					for (ReturnedRPProperty r : mdloaded.get(e).getRpproperties()) {
						System.out.println("          " + r.getRppropertyname() + " = " + r.getRppropertyvalue());
					}
				}
				if (requirediis.containsKey(e)) {
					System.out.println("     Required Attrs:");
					for (String a : requirediis.get(e).keySet()) {
						String vp = "";
						for (String v : requirediis.get(e).get(a)) {
							vp = vp + v + ",";
						}
						vp = vp.replaceAll(",$", "");
						System.out.println("          "+a+" = " + vp);
					}
				}
				if (optionaliis.containsKey(e)) {
					System.out.println("     Optional Attrs:");
					for (String a : optionaliis.get(e).keySet()) {
						String vp = "";
						for (String v : optionaliis.get(e).get(a)) {
							vp = vp + v + ",";
						}
						vp = vp.replaceAll(",$", "");
						System.out.println("         "+a+" = " + vp);
					}
				}
				if (! dryrun) {
					registerRelyingParty(icmhost,icmport,rhtype,rhid,mdloaded.get(e).getRpidentifier().getRptype(),mdloaded.get(e).getRpidentifier().getRpid(),icmuser,icmcred,mdloaded.get(e));
					System.out.println("CREATION COMPLETE: " + e);
					
				/* RGC - having no attributes is not a crime... anymore */
				/*	if ((!requirediis.containsKey(e) || requirediis.get(e).isEmpty()) && (!optionaliis.containsKey(e) || optionaliis.get(e).isEmpty())) {
						// No attributes at all.  For purposes of registration, add transientid as optional
						HashMap<String,ArrayList<String>> force = new HashMap<String,ArrayList<String>>();
						ArrayList<String> forcev = new ArrayList<String>();
						forcev.add(".*");
						force.put("transientId", forcev);
						optionaliis.put(e,force);
						System.out.println("Forcing transientid for registration of " + e);;
					} */
					if (requirediis != null && requirediis.containsKey(e)) {
						registerRequiredItems(icmhost,icmport,rhtype,rhid,mdloaded.get(e).getRpidentifier().getRptype(),mdloaded.get(e).getRpidentifier().getRpid(),icmuser,icmcred,requirediis.get(e));
						System.out.println("REQUIRED IIS REGISTERED: " + e);
					} else {
						System.out.println("NO REQUIRED IIS FOR: " +e);
					}
					if (optionaliis != null & optionaliis.containsKey(e)) {
						registerOptionalItems(icmhost,icmport,rhtype,rhid,mdloaded.get(e).getRpidentifier().getRptype(),mdloaded.get(e).getRpidentifier().getRpid(),icmuser,icmcred,optionaliis.get(e));
						System.out.println("OPTIONAL IIS REGISTERED: " + e);
					} else {
						System.out.println("NO OPTIONAL IIS FOR: " + e);
					}
					// dryrun = true;	// comment to allow full run post-testing
				} else {
					System.out.println("Would run: registerRelyingParty("+icmhost+","+icmport+","+rhtype+","+rhid+","+mdloaded.get(e).getRpidentifier().getRptype()+","+mdloaded.get(e).getRpidentifier().getRpid()+","+icmuser+","+icmcred+","+mdloaded.get(e).toString());
				}
			}
		}
	}
	
	private static FilesystemMetadataResolver loadMDFile(String filename) {
		FilesystemMetadataResolver retval = null;
		try {
			File infile = new File(filename);
			retval = new FilesystemMetadataResolver(infile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return retval;
	}
	
	private static String idEscape(String value) {
		// Convert all embedded slashes to !s 
		return value.replaceAll("/", "!").replaceAll(" ", "%20").replaceAll("\\|", "%7C");
	}
	
	private static String idUnEscape(String value) {
		// Convert all embedded "!" to "/"
		return value.replaceAll("!", "/").replaceAll("%20"," ").replaceAll("%7C", "|");
	}
	
	private static String buildAuthorizationHeader(String basicUser,String basicCred) {
		
		String authString = basicUser + ":" + basicCred;
		byte [] encodedString = Base64.encodeBase64(authString.getBytes());
		String retval = "Basic " + new String(encodedString);
		
		return retval;
	}
	
	private static void registerOptionalItems(String icmhost, String icmport, String rhtype, String rhid, String rptype, String rpid, String icmuser, String icmcred, HashMap<String,ArrayList<String>> optional) {
		
		// Given framing info and a map from iinames to lists of ii values, construct a ReturnedRequiredIIList and inject it
		// 
		ReturnedRPOptionalInfoItemList rrpriil = new ReturnedRPOptionalInfoItemList();
		
		// Populate the rrpriil
		RHIdentifier rhi = new RHIdentifier();
		rhi.setRhtype(rhtype);
		rhi.setRhid(rhid);
		rrpriil.setRhidentifier(rhi);

		RPIdentifier rpi = new RPIdentifier();
		rpi.setRptype(rptype);
		rpi.setRpid(rpid);
		rrpriil.setRpidentifier(rpi);
		
		rrpriil.setState("active");
		
		// Since we're operating over SAML metadata, we assume ii types are always "attribute"
				
		ArrayList<InfoItemValueList> rlist = new ArrayList<InfoItemValueList>();
		
		for (String i : optional.keySet()) {
			InfoItemValueList iivl = new InfoItemValueList();
			InfoItemIdentifier iid = new InfoItemIdentifier();
			iid.setIitype("attribute");
			iid.setIiid(i);
			iivl.setInfoitemidentifier(iid);
			ArrayList<String> vlist = new ArrayList<String>();
			vlist.addAll(optional.get(i));
			iivl.setValuelist(vlist);
			
			// Assume source item = item
			iivl.setSourceitemname(i);
			
			// And leave reason empty, since reasons are not in metadata at this time
			rlist.add(iivl);
		}
		rrpriil.setOptionallist(rlist);
		
		ObjectMapper om = new ObjectMapper();
		
		String json = null;
		
		try {
			json = om.writeValueAsString(rrpriil);
		} catch (Exception e) {
			throw new RuntimeException("Failed serializing required ii list");
		}
		
		String icmurl = "https://" + icmhost + ":" + icmport + "/consent/v1/informed/rpic/optionaliilist/" + rhtype + "/" + idEscape(rhid) + "/" + rptype + "/" + idEscape(rpid);
		
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPut putRequest = new HttpPut(icmurl);
		putRequest.addHeader("accept","application/json");
		putRequest.addHeader("Content-Type","application/json");
		putRequest.addHeader(HttpHeaders.AUTHORIZATION,buildAuthorizationHeader(icmuser,icmcred));
		
		String sendEntity = json;
		StringEntity send = null;
		try {
			send = new StringEntity(sendEntity);
		} catch (Exception e) {
			throw new RuntimeException("Failed sending json to create " + rpid,e);
		}
		send.setContentType("application/json");
		putRequest.setEntity(send);
		
		try {
			httpClient.execute(putRequest);
		} catch (Exception e) {
			throw new RuntimeException("Failed PUTing RP metainformation",e);
		}
		// ignore result -- just run blind
	}
	
	private static void registerRequiredItems(String icmhost, String icmport, String rhtype, String rhid, String rptype, String rpid, String icmuser, String icmcred, HashMap<String,ArrayList<String>> required) {
		
		// Given framing info and a map from iinames to lists of ii values, construct a ReturnedRequiredIIList and inject it
		// 
		ReturnedRPRequiredInfoItemList rrpriil = new ReturnedRPRequiredInfoItemList();
		
		// Populate the rrpriil
		RHIdentifier rhi = new RHIdentifier();
		rhi.setRhtype(rhtype);
		rhi.setRhid(rhid);
		rrpriil.setRhidentifier(rhi);

		RPIdentifier rpi = new RPIdentifier();
		rpi.setRptype(rptype);
		rpi.setRpid(rpid);
		rrpriil.setRpidentifier(rpi);
		
		rrpriil.setState("active");
		
		
		// Since we're operating over SAML metadata, we assume ii types are always "attribute"
				
		ArrayList<InfoItemValueList> rlist = new ArrayList<InfoItemValueList>();
		
		for (String i : required.keySet()) {
			InfoItemValueList iivl = new InfoItemValueList();
			InfoItemIdentifier iid = new InfoItemIdentifier();
			iid.setIitype("attribute");
			iid.setIiid(i);
			iivl.setInfoitemidentifier(iid);
			ArrayList<String> vlist = new ArrayList<String>();
			vlist.addAll(required.get(i));
			iivl.setValuelist(vlist);
			
			// Assume source item = item
			iivl.setSourceitemname(i);
			
			// And leave reason empty, since reasons are not in metadata at this time
			rlist.add(iivl);
		}
		
		rrpriil.setRequiredlist(rlist);
		
		ObjectMapper om = new ObjectMapper();
		
		String json = null;
		
		try {
			json = om.writeValueAsString(rrpriil);
		} catch (Exception e) {
			throw new RuntimeException("Failed serializing required ii list");
		}
		
		String icmurl = "https://" + icmhost + ":" + icmport + "/consent/v1/informed/rpic/requirediilist/" + rhtype + "/" + idEscape(rhid) + "/" + rptype + "/" + idEscape(rpid);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPut putRequest = new HttpPut(icmurl);
		putRequest.addHeader("accept","application/json");
		putRequest.addHeader("Content-Type","application/json");
		putRequest.addHeader(HttpHeaders.AUTHORIZATION,buildAuthorizationHeader(icmuser,icmcred));
		
		String sendEntity = json;
		StringEntity send = null;
		try {
			send = new StringEntity(sendEntity);
		} catch (Exception e) {
			throw new RuntimeException("Failed sending json to create " + rpid,e);
		}
		send.setContentType("application/json");
		putRequest.setEntity(send);
		
		try {
			httpClient.execute(putRequest);
		} catch (Exception e) {
			throw new RuntimeException("Failed PUTing RP metainformation",e);
		}
		// ignore result -- just run blind
	}
	
	private static void registerRelyingParty(String icmhost, String icmport, String rhtype, String rhid, String rptype, String rpid, String icmuser, String icmcred, ReturnedRPMetaInformation rrpmi) {
		
		// Given framing info and a ReturnedRPMetaInformation object, register the RP in the specified CAR instance by 
		// PUTing a JSON representation of the object into the informed content endpoint
		//
		ObjectMapper om = new ObjectMapper();
		String json = null;
		try {
			json = om.writeValueAsString(rrpmi);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize RP Metainformation",e);
		}
		
		String icmurl = "https://" + icmhost + ":" + icmport + "/consent/v1/informed/rpic/metainformation/" + rhtype + "/" + idEscape(rhid) + "/" + rptype + "/" + idEscape(rpid);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPut putRequest = new HttpPut(icmurl);
		putRequest.addHeader("accept","application/json");
		putRequest.addHeader("Content-Type","application/json");
		putRequest.addHeader(HttpHeaders.AUTHORIZATION,buildAuthorizationHeader(icmuser,icmcred));
		
		String sendEntity = json;
		StringEntity send = null;
		try {
			send = new StringEntity(sendEntity);
		} catch (Exception e) {
			throw new RuntimeException("Failed sending json to create " + rpid,e);
		}
		send.setContentType("application/json");
		putRequest.setEntity(send);
		
		try {
			httpClient.execute(putRequest);
		} catch (Exception e) {
			throw new RuntimeException("Failed PUTing RP metainformation",e);
		}
		// ignore result -- just run blind
	}
	
	private static HashMap<String,ReturnedRPRequiredInfoItemList> retrieveRequiredInfoItems(String icmhost, String icmport, String rhtype, String rhid, String icmuser, String icmcred) {
		
		HashMap<String,ReturnedRPRequiredInfoItemList> retval = new HashMap<String,ReturnedRPRequiredInfoItemList>();

		String icmurl = "https://"+icmhost+":"+icmport+"/consent/v1/informed/rpic/requirediilist/" + rhtype + "/" + idEscape(rhid);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet(icmurl);
		
		getRequest.addHeader("accept","application/json");;
		String authzheader = buildAuthorizationHeader(icmuser,icmcred);
		getRequest.addHeader(HttpHeaders.AUTHORIZATION,authzheader);
		HttpResponse response = null;
		BufferedReader br = null;
		try {
			response = httpClient.execute(getRequest);
			// No matter what, we get the body of the response back
			br = new BufferedReader(new InputStreamReader((response.getEntity().getContent()),StandardCharsets.UTF_8));
			StringBuilder rsb = new StringBuilder();
			String body = null;
			while((body = br.readLine()) != null) {
				rsb.append(body);
			}
			String rbody = rsb.toString();
			
			// If the response was an error, return the body with the error code
			if (response.getStatusLine().getStatusCode() >= 300) {
				// cache the failure too, if failure was a 404
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				// System.out.println("Response from ICM was: " + rbody);
				return null;  // on error, just return nothing
			}
			//Otherwise, deserialize the response
//			System.out.println("Deserializing: " + rbody);
			ObjectMapper om = new ObjectMapper();
			List<ReturnedRPRequiredInfoItemList> lmi = om.readValue(rbody, new TypeReference<List<ReturnedRPRequiredInfoItemList>>(){});
			ArrayList<ReturnedRPRequiredInfoItemList> arpmi = new ArrayList<ReturnedRPRequiredInfoItemList>();
			if (lmi != null) {
				arpmi.addAll(lmi);
			} else {
				System.out.println("lmi is null");
			}
			
			if(arpmi == null) {
				return null;
			}
			
			// Walk the returned list and build the hashmap
			for (ReturnedRPRequiredInfoItemList rpmi : arpmi) {
				retval.put(rpmi.getRpidentifier().getRpid(), rpmi);
			}
			return retval;
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			//HttpClientUtils.closeQuietly(httpClient);
			if (br != null) {
				try {
					br.close();
				} catch (Exception ign) {
					// ignore
				}
			}
		}
	}
	
	private static HashMap<String,ReturnedRPOptionalInfoItemList> retrieveOptionalInfoItems(String icmhost, String icmport, String rhtype, String rhid, String icmuser, String icmcred) {
		HashMap<String,ReturnedRPOptionalInfoItemList> retval = new HashMap<String,ReturnedRPOptionalInfoItemList>();

		String icmurl = "https://"+icmhost+":"+icmport+"/consent/v1/informed/rpic/optionaliilist/" + rhtype + "/" + idEscape(rhid);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet(icmurl);
		
		getRequest.addHeader("accept","application/json");;
		String authzheader = buildAuthorizationHeader(icmuser,icmcred);
		getRequest.addHeader(HttpHeaders.AUTHORIZATION,authzheader);
		HttpResponse response = null;
		BufferedReader br = null;
		try {
			response = httpClient.execute(getRequest);
			// No matter what, we get the body of the response back
			br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			StringBuilder rsb = new StringBuilder();
			String body = null;
			while((body = br.readLine()) != null) {
				rsb.append(body);
			}
			String rbody = rsb.toString();
			
			// If the response was an error, return the body with the error code
			if (response.getStatusLine().getStatusCode() >= 300) {
				// cache the failure too, if failure was a 404
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				// System.out.println("Response from ICM was: " + rbody);
				return null;  // on error, just return nothing
			}
			//Otherwise, deserialize the response
//			System.out.println("Deserializing: " + rbody);
			ObjectMapper om = new ObjectMapper();
			List<ReturnedRPOptionalInfoItemList> lmi = om.readValue(rbody, new TypeReference<List<ReturnedRPOptionalInfoItemList>>(){});
			ArrayList<ReturnedRPOptionalInfoItemList> arpmi = new ArrayList<ReturnedRPOptionalInfoItemList>();
			if (lmi != null) {
				arpmi.addAll(lmi);
			} else {
				System.out.println("lmi is null");
			}
			
			if(arpmi == null) {
				return null;
			}
			
			// Walk the returned list and build the hashmap
			for (ReturnedRPOptionalInfoItemList rpmi : arpmi) {
				retval.put(rpmi.getRpidentifier().getRpid(), rpmi);
			}
			return retval;
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			//HttpClientUtils.closeQuietly(httpClient);
			if (br != null) {
				try {
					br.close();
				} catch (Exception ign) {
					// ignore
				}
			}
		}
	}
	private static HashMap<String,ReturnedRPMetaInformation> retrieveCurrentRPRegistrations(String icmhost, String icmport, String rhtype, String rhid, String icmuser, String icmcred) {
		
		HashMap<String,ReturnedRPMetaInformation> retval = new HashMap<String,ReturnedRPMetaInformation>();

		String icmurl = "https://"+icmhost+":"+icmport+"/consent/v1/informed/rpic/metainformation/" + rhtype + "/" + idEscape(rhid);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet(icmurl);
		
		getRequest.addHeader("accept","application/json");
		String authzheader = buildAuthorizationHeader(icmuser,icmcred);
		getRequest.addHeader(HttpHeaders.AUTHORIZATION,authzheader);
		
		// And send the request
		HttpResponse response = null;
		BufferedReader br = null;
		try {
			response = httpClient.execute(getRequest);
			// No matter what, we get the body of the response back
			br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			StringBuilder rsb = new StringBuilder();
			String body = null;
			while((body = br.readLine()) != null) {
				rsb.append(body);
			}
			String rbody = rsb.toString();
			
			// If the response was an error, return the body with the error code
			if (response.getStatusLine().getStatusCode() >= 300) {
				// cache the failure too, if failure was a 404
				try {
					EntityUtils.consumeQuietly(response.getEntity());
				} catch (Exception x) {
					// ignore
				}
				// System.out.println("Response from ICM was: " + rbody);
				return null;  // on error, just return nothing
			}
			//Otherwise, deserialize the response
//			System.out.println("Deserializing: " + rbody);
			ObjectMapper om = new ObjectMapper();
			List<ReturnedRPMetaInformation> lmi = om.readValue(rbody, new TypeReference<List<ReturnedRPMetaInformation>>(){});
			ArrayList<ReturnedRPMetaInformation> arpmi = new ArrayList<ReturnedRPMetaInformation>();
			if (lmi != null) {
				arpmi.addAll(lmi);
			} else {
				System.out.println("lmi is null");
			}
			
			if(arpmi == null) {
				return null;
			}
			
			// Walk the returned list and build the hashmap
			for (ReturnedRPMetaInformation rpmi : arpmi) {
				retval.put(rpmi.getRpidentifier().getRpid(), rpmi);
			}
			return retval;
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			return null;  // on error, just fail
		} finally {
			HttpClientUtils.closeQuietly(response);
			//HttpClientUtils.closeQuietly(httpClient);
			if (br != null) {
				try {
					br.close();
				} catch (Exception ign) {
					// ignore
				}
			}
		}		
	}
}
