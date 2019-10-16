package demorp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

@Controller
public class MainController {
	
	@RequestMapping(value="/ldap",method=RequestMethod.GET)
	public ModelAndView manageLDAP(HttpServletRequest req) {
		// Simply present the LDAP page
		ModelAndView retval = new ModelAndView("ldapform");
		retval.addObject("sitename","Test User Creation");
		retval.addObject("top_heading","Test User Creation");
		retval.addObject("logouturl","#");
		retval.addObject("authuser","New Demo User");
		
		retval.addObject("top_logo_url","/Amber-Icon.png");
		retval.addObject("sign_out"," ");
		return retval;
	}
	
	@RequestMapping(value="/ldap",method=RequestMethod.POST)
	public ModelAndView handleLDAPCreation(HttpServletRequest req) {
		// Create new users based on the input data
		ModelAndView retval = new ModelAndView("ldapsuccess");
		retval.addObject("sitename","Test Users Created");
		retval.addObject("top_heading","Test Users Created");
		retval.addObject("logouturl","#");
		
		String first = req.getParameter("initial");
		if (first != null && ! first.equalsIgnoreCase("")) {
			first = first.toLowerCase();
		} else  {
			first = "father";
		}
		
		String middle = req.getParameter("middle");
		if (middle != null && ! middle.equalsIgnoreCase("")) {
			middle = middle.toLowerCase();
		} else {
			middle = "guido";
		}
		
		String last = req.getParameter("lastname");
		if (last != null && ! last.equalsIgnoreCase("")) {
			last = last.toLowerCase();
			if (last.length() > 5)
				last = last.substring(0,5);
		} else {
			last = "sarducci";
		}
		
		String prefix = first + middle + last;
		
		Random rand = new Random();
		
		int uniqueid;
		uniqueid = rand.nextInt(1000);
		uniqueid = uniqueid + 880000;  // prefix starting with 88
		
		DirContext dirContext = null;
		try {
			String url = "ldap://ldapnode:389";
			String conntype = "simple";
			String AdminDn="CN=admin,dc=amber,dc=org";
			String password = "admin";
			Hashtable<String,String> environment = new Hashtable<String,String>();
			environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			environment.put(Context.PROVIDER_URL, url);
			environment.put(Context.SECURITY_AUTHENTICATION, conntype);
			environment.put(Context.SECURITY_PRINCIPAL,AdminDn);
			environment.put(Context.SECURITY_CREDENTIALS, password);
			
			dirContext = new InitialDirContext(environment);
		} catch (Exception e) {
			throw new RuntimeException("Failed to connect to LDAP: ", e);
		}
		
		// At this point we've got an LDAP connection...
		
		Attribute objectclass, cn, sn, mail, amberTitle, userPassword, eduPersonPrincipalName, eduPersonOrgDN, eduPersonOrgUnitDN, localDepartmentCode, uid, givenName, displayName, eduPersonAffiliation, eduPersonPrimaryAffiliation, localUniqueId, eduPersonOrcid,isMemberOf;
		
		// Create the faculty user
		String facuser = prefix + "-faculty";
		
		objectclass = new BasicAttribute("objectclass");
		objectclass.add("top");
		objectclass.add("person");
		objectclass.add("organizationalPerson");
		objectclass.add("inetOrgPerson");
		objectclass.add("eduPerson");
		objectclass.add("amberite");
		
		cn = new BasicAttribute("cn",first.toUpperCase() + " " + middle.toUpperCase() + " " + last);
		sn = new BasicAttribute("sn",last);
		givenName = new BasicAttribute("givenName",first.toUpperCase());
		mail = new BasicAttribute("mail",prefix + "-faculty@amber.org");
		amberTitle = new BasicAttribute("amberTitle","Professor");
		userPassword = new BasicAttribute("userPassword",facuser);
		eduPersonPrincipalName = new BasicAttribute("eduPersonPrincipalName",facuser + "@amber.org");
		eduPersonOrgDN = new BasicAttribute("eduPersonOrgDN","dc=amber,dc=org");
		eduPersonOrgUnitDN = new BasicAttribute("eduPersonOrgUnitDN","cn=public_policy,dc=amber,dc=org");
		localDepartmentCode = new BasicAttribute("localDepartmentCode","12130100");
		uid = new BasicAttribute("uid",facuser);
		displayName = new BasicAttribute("displayName","Professor " + req.getParameter("lastname"));
		eduPersonAffiliation = new BasicAttribute("eduPersonAffiliation");
		eduPersonAffiliation.add("faculty");
		eduPersonAffiliation.add("alum");
		eduPersonPrimaryAffiliation = new BasicAttribute("eduPersonPrimaryAffiliation","faculty");
		localUniqueId = new BasicAttribute("localUniqueId", String.valueOf(uniqueid));
		eduPersonOrcid = new BasicAttribute("eduPersonOrcid","9180-7863-"+uniqueid+"-0001");
		isMemberOf = new BasicAttribute("isMemberOf");
		isMemberOf.add("urn:mace:amber.org:groups:research:covert_project");
		isMemberOf.add("urn:mace:amber.org:groups:research:public_project");
		isMemberOf.add("urn:mace:amber.org:groups:faculty:tenured");
		isMemberOf.add("urn:mace:amber.org:groups:instructors");
		isMemberOf.add("urn:mace:amber.org:groups:payroll:employers");
		isMemberOf.add("urn:mace:amber.org:groups:payroll:employees");
		
		Attributes entry = new BasicAttributes();
		entry.put(objectclass);
		entry.put(cn);
		entry.put(sn);
		entry.put(givenName);
		entry.put(mail);
		entry.put(amberTitle);
		entry.put(userPassword);
		entry.put(eduPersonPrincipalName);
		entry.put(eduPersonOrgDN);
		entry.put(eduPersonOrgUnitDN);
		entry.put(localDepartmentCode);
		entry.put(uid);
		entry.put(displayName);
		entry.put(eduPersonAffiliation);
		entry.put(eduPersonPrimaryAffiliation);
		entry.put(localUniqueId);
		entry.put(eduPersonOrcid);
		entry.put(isMemberOf);
		
		String entryDN = "uid=" + facuser + ",ou=people,dc=amber,dc=org";
		try {
			dirContext.createSubcontext(entryDN,entry);
		} catch (Exception e) {
			throw new RuntimeException("Failed adding faculty object to LDAP: ", e);
		}
		
		uniqueid = rand.nextInt(1000);
		uniqueid = uniqueid + 880000;  // prefix starting with 88
		
		// Create the staff user
				String staffuser = prefix + "-staff";
				
				objectclass = new BasicAttribute("objectclass");
				objectclass.add("top");
				objectclass.add("person");
				objectclass.add("organizationalPerson");
				objectclass.add("inetOrgPerson");
				objectclass.add("eduPerson");
				objectclass.add("amberite");
				
				cn = new BasicAttribute("cn",first.toUpperCase() + " " + middle.toUpperCase() + " " + last);
				sn = new BasicAttribute("sn",last);
				givenName = new BasicAttribute("givenName",first.toUpperCase());
				mail = new BasicAttribute("mail",prefix + "-staff@amber.org");
				amberTitle = new BasicAttribute("amberTitle","ITPro");
				userPassword = new BasicAttribute("userPassword",staffuser);
				eduPersonPrincipalName = new BasicAttribute("eduPersonPrincipalName",staffuser + "@amber.org");
				eduPersonOrgDN = new BasicAttribute("eduPersonOrgDN","dc=amber,dc=org");
				eduPersonOrgUnitDN = new BasicAttribute("eduPersonOrgUnitDN","cn=shadows,dc=amber,dc=org");
				localDepartmentCode = new BasicAttribute("localDepartmentCode","20580100");
				uid = new BasicAttribute("uid",staffuser);
				displayName = new BasicAttribute("displayName",first.toUpperCase() + " " + req.getParameter("lastname"));
				eduPersonAffiliation = new BasicAttribute("eduPersonAffiliation");
				eduPersonAffiliation.add("staff");
				eduPersonPrimaryAffiliation = new BasicAttribute("eduPersonPrimaryAffiliation","staff");
				localUniqueId = new BasicAttribute("localUniqueId", String.valueOf(uniqueid));
				eduPersonOrcid = new BasicAttribute("eduPersonOrcid","9180-7863-"+uniqueid+"-0001");
				isMemberOf = new BasicAttribute("isMemberOf");
				isMemberOf.add("urn:mace:amber.org:groups:research:public_project");
				isMemberOf.add("urn:mace:amber.org:groups:payroll:employees");
				isMemberOf.add("urn:mace:amber.org:groups:it-support");
				
				entry = new BasicAttributes();
				entry.put(objectclass);
				entry.put(cn);
				entry.put(sn);
				entry.put(givenName);
				entry.put(mail);
				entry.put(amberTitle);
				entry.put(userPassword);
				entry.put(eduPersonPrincipalName);
				entry.put(eduPersonOrgDN);
				entry.put(eduPersonOrgUnitDN);
				entry.put(localDepartmentCode);
				entry.put(uid);
				entry.put(displayName);
				entry.put(eduPersonAffiliation);
				entry.put(eduPersonPrimaryAffiliation);
				entry.put(localUniqueId);
				entry.put(eduPersonOrcid);
				entry.put(isMemberOf);
				
				entryDN = "uid=" + staffuser + ",ou=people,dc=amber,dc=org";
				try {
					dirContext.createSubcontext(entryDN,entry);
				} catch (Exception e) {
					throw new RuntimeException("Failed adding staff object to LDAP: ", e);
				}
		
				uniqueid = rand.nextInt(1000);
				uniqueid = uniqueid + 880000;  // prefix starting with 88
				
				// Create the grad user
				String graduser = prefix + "-grad";
				
				objectclass = new BasicAttribute("objectclass");
				objectclass.add("top");
				objectclass.add("person");
				objectclass.add("organizationalPerson");
				objectclass.add("inetOrgPerson");
				objectclass.add("eduPerson");
				objectclass.add("amberite");
				
				cn = new BasicAttribute("cn",first.toUpperCase() + " " + middle.toUpperCase() + " " + last);
				sn = new BasicAttribute("sn",last);
				givenName = new BasicAttribute("givenName",first.toUpperCase());
				mail = new BasicAttribute("mail",prefix + "-grad@amber.org");
				amberTitle = new BasicAttribute("amberTitle","Graduate Assistant");
				userPassword = new BasicAttribute("userPassword",graduser);
				eduPersonPrincipalName = new BasicAttribute("eduPersonPrincipalName",graduser + "@amber.org");
				eduPersonOrgDN = new BasicAttribute("eduPersonOrgDN","dc=amber,dc=org");
				eduPersonOrgUnitDN = new BasicAttribute("eduPersonOrgUnitDN","cn=graduate_school,dc=amber,dc=org");
				localDepartmentCode = new BasicAttribute("localDepartmentCode","12180000");
				uid = new BasicAttribute("uid",graduser);
				displayName = new BasicAttribute("displayName",first.toUpperCase() + " " + req.getParameter("lastname"));
				eduPersonAffiliation = new BasicAttribute("eduPersonAffiliation");
				eduPersonAffiliation.add("student");
				eduPersonAffiliation.add("staff");
				eduPersonPrimaryAffiliation = new BasicAttribute("eduPersonPrimaryAffiliation","student");
				localUniqueId = new BasicAttribute("localUniqueId", String.valueOf(uniqueid));
				eduPersonOrcid = new BasicAttribute("eduPersonOrcid","9180-7863-"+uniqueid+"-0001");
				isMemberOf = new BasicAttribute("isMemberOf");
				isMemberOf.add("urn:mace:amber.org:groups:research:unicorn_project");
				isMemberOf.add("urn:mace:amber.org:groups:research:avalon_project");
				isMemberOf.add("urn:mace:amber.org:groups:payroll:employees");
				isMemberOf.add("urn:mace:amber.org:groups:gradstudents");
				isMemberOf.add("urn:mace:amber.org:groups:instructors");
				isMemberOf.add("urn:mace:amber.org:groups:courses:2019:fall:pubpol428_1");
				isMemberOf.add("urn:mace:amber.org:groups:courses:2019:fall:pubpol401_2");
				isMemberOf.add("urn:mace:amber.org:groups:courses:2019:fall:pubpol910_12");
				isMemberOf.add("urn:mace:amber.org:groups:courses:2019:fall:cps100_2L");
				isMemberOf.add("urn:mace:amber.org:groups:courses:2019:fall:research_101");
				
				
				entry = new BasicAttributes();
				entry.put(objectclass);
				entry.put(cn);
				entry.put(sn);
				entry.put(givenName);
				entry.put(mail);
				entry.put(amberTitle);
				entry.put(userPassword);
				entry.put(eduPersonPrincipalName);
				entry.put(eduPersonOrgDN);
				entry.put(eduPersonOrgUnitDN);
				entry.put(localDepartmentCode);
				entry.put(uid);
				entry.put(displayName);
				entry.put(eduPersonAffiliation);
				entry.put(eduPersonPrimaryAffiliation);
				entry.put(localUniqueId);
				entry.put(eduPersonOrcid);
				entry.put(isMemberOf);
				
				entryDN = "uid=" + graduser + ",ou=people,dc=amber,dc=org";
				try {
					dirContext.createSubcontext(entryDN,entry);
				} catch (Exception e) {
					throw new RuntimeException("Failed adding grad object to LDAP: ", e);
				}
		
				uniqueid = rand.nextInt(1000);
				uniqueid = uniqueid + 880000;  // prefix starting with 88
				
				// Create the ugrad user
				String ugraduser = prefix + "-ugrad";
				
				objectclass = new BasicAttribute("objectclass");
				objectclass.add("top");
				objectclass.add("person");
				objectclass.add("organizationalPerson");
				objectclass.add("inetOrgPerson");
				objectclass.add("eduPerson");
				objectclass.add("amberite");
				
				cn = new BasicAttribute("cn",first.toUpperCase() + " " + middle.toUpperCase() + " " + last);
				sn = new BasicAttribute("sn",last);
				givenName = new BasicAttribute("givenName",first.toUpperCase());
				mail = new BasicAttribute("mail",prefix + "-ugrad@amber.org");
				amberTitle = new BasicAttribute("amberTitle","Undergraduate");
				userPassword = new BasicAttribute("userPassword",ugraduser);
				eduPersonPrincipalName = new BasicAttribute("eduPersonPrincipalName",ugraduser + "@amber.org");
				eduPersonOrgDN = new BasicAttribute("eduPersonOrgDN","dc=amber,dc=org");
				eduPersonOrgUnitDN = new BasicAttribute("eduPersonOrgUnitDN","cn=mechanical_engineering,dc=amber,dc=org");
				localDepartmentCode = new BasicAttribute("localDepartmentCode","12171555");
				uid = new BasicAttribute("uid",ugraduser);
				displayName = new BasicAttribute("displayName",first.toUpperCase() + " " + req.getParameter("lastname"));
				eduPersonAffiliation = new BasicAttribute("eduPersonAffiliation");
				eduPersonAffiliation.add("student");
				eduPersonPrimaryAffiliation = new BasicAttribute("eduPersonPrimaryAffiliation","student");
				localUniqueId = new BasicAttribute("localUniqueId", String.valueOf(uniqueid));
				eduPersonOrcid = new BasicAttribute("eduPersonOrcid","9180-7863-"+uniqueid+"-0001");
				isMemberOf = new BasicAttribute("isMemberOf");
				isMemberOf.add("urn:mace:amber.org:groups:research:unicorn_project");
				isMemberOf.add("urn:mace:amber.org:groups:research:avalon_project");
				isMemberOf.add("urn:mace:amber.org:groups:courses:2019:fall:me128_1");
				isMemberOf.add("urn:mace:amber.org:groups:courses:2019:fall:me101_2");
				isMemberOf.add("urn:mace:amber.org:groups:courses:2019:fall:ce10_12");
				isMemberOf.add("urn:mace:amber.org:groups:courses:2019:fall:cps100_2L");
				isMemberOf.add("urn:mace:amber.org:groups:courses:2019:fall:writing_101");
				isMemberOf.add("urn:mace:amber.org:groups:undergraduates");
				
				
				entry = new BasicAttributes();
				entry.put(objectclass);
				entry.put(cn);
				entry.put(sn);
				entry.put(givenName);
				entry.put(mail);
				entry.put(amberTitle);
				entry.put(userPassword);
				entry.put(eduPersonPrincipalName);
				entry.put(eduPersonOrgDN);
				entry.put(eduPersonOrgUnitDN);
				entry.put(localDepartmentCode);
				entry.put(uid);
				entry.put(displayName);
				entry.put(eduPersonAffiliation);
				entry.put(eduPersonPrimaryAffiliation);
				entry.put(localUniqueId);
				entry.put(eduPersonOrcid);
				entry.put(isMemberOf);
				
				entryDN = "uid=" + ugraduser + ",ou=people,dc=amber,dc=org";
				try {
					dirContext.createSubcontext(entryDN,entry);
				} catch (Exception e) {
					throw new RuntimeException("Failed adding ugrad object to LDAP: ", e);
				}
				
				retval.addObject("facuser",facuser);
				retval.addObject("staffuser",staffuser);
				retval.addObject("graduser",graduser);
				retval.addObject("ugraduser",ugraduser);
				retval.addObject("top_logo_url","/Amber-Icon.png");
				retval.addObject("sign_out"," ");
				return(retval);
	}

	@RequestMapping(value="/demo/{appname}",method=RequestMethod.GET)
	public ModelAndView handleDashboardGet(HttpServletRequest req,  @PathVariable("appname") String appname) {
		//
		// Simple process of selecting a VM based on URI and then stuffing what we received
		// into it.  The bulk of interesting processing actually happens in the Velocity 
		// template after that.
		//
		ModelAndView retval = null;
		
		if (appname.equalsIgnoreCase("contentrus")) {
			retval = new ModelAndView("contentrus");
			retval.addObject("sitename","Content-R-Us");
			retval.addObject("top_heading","Content-R-Us");
			// hack for sliced bread demo -- parameterize later
			retval.addObject("logouturl","/contentrus/Shibboleth.sso/Logout?return=https://idms-carsb-dev-01.oit.duke.edu:9443/idp/profile/Logout");
		} else if (appname.equalsIgnoreCase("randsrus")) {
			retval = new ModelAndView("randsrus");
			retval.addObject("sitename","Research-R-Us");
			retval.addObject("top_heading","Research-R-Us");
			retval.addObject("logouturl","/randsrus/Shibboleth.sso/Logout?return=https://idms-carsb-dev-01.oit.duke.edu:9443/idp/profile/Logout");
		} else if (appname.equalsIgnoreCase("scholars")) {
			retval = new ModelAndView("scholars");
			retval.addObject("sitename","Scholarly Garage");
			retval.addObject("top_heading","Scholarly Garage");
			retval.addObject("logouturl","/scholars/Shibboleth.sso/Logout?return=https://idms-carsb-dev-01.oit.duke.edu:9443/idp/profile/Logout");
		} else if (appname.equalsIgnoreCase("payroll")) {
			retval = new ModelAndView("payroll");
			retval.addObject("sitename","Peanuts:  Your Payroll Site");
			retval.addObject("top_heading","Peanuts");
			retval.addObject("logouturl","/payroll/Shibboleth.sso/Logout?return=https://idms-carsb-dev-01.oit.duke.edu:9443/idp/profile/Logout");
		}
		
		retval.addObject("top_logo_url","/Pattern_In_Rebma.png");
		
		if (req.getAttribute("displayName") != null) {
			retval.addObject("authuser",req.getAttribute("displayName"));
		} else {
			retval.addObject("authuser",req.getRemoteUser());
		}
		
		retval.addObject("sign_out","sign out " + retval.getModel().get("authuser"));
		
		// And stuff the attributes we received into the context
		
		ArrayList<String> anames = new ArrayList<String>();
		anames.add("amberTitle");
		anames.add("cn");
		anames.add("departmentCode");
		anames.add("displayName");
		anames.add("eduPersonOrcid");
		anames.add("eduPersonOrgDN");
		anames.add("eduPersonOrgUnitDN");
		anames.add("eduPersonPrimaryAffiliation");
		anames.add("eduPersonPrincipalName");
		anames.add("eduPersonScopedAffiliation");
		anames.add("eduPersonTargetedID");
		anames.add("eduPersonUniqueId");
		anames.add("isMemberOf");
		anames.add("mail");
		anames.add("sn");
		
		HashMap<String,Object> map = new HashMap<String,Object>();
		
		for (String n : anames) {
			if (req.getAttribute(n) != null) {
				map.put(n,req.getAttribute(n));
			}
		}
		
		retval.addObject("map",map);
		retval.addObject("anames",anames);
		
		if (map.get("eduPersonOrgDN") != null) {
			retval.addObject("odn","true");
		}
		
		// Special case faculty affiliation
		if (map.get("eduPersonScopedAffiliation") != null && ((String) map.get("eduPersonScopedAffiliation")).contains("faculty")) {
			retval.addObject("isfaculty","true");
		}
		
		// Special case researcher case based on research groups
		if (map.get("isMemberOf") != null && ((String) map.get("isMemberOf")).contains(":research:")) {
			retval.addObject("isresearch","true");
		}
		
		
		//  And hand off to the Velocity template
		
		return retval;
	}
}
