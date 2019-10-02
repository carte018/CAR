package demorp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {

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
		
		//  And hand off to the Velocity template
		
		return retval;
	}
}
