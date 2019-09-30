package demorp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {

	@RequestMapping(value="/demo/{appname}",method=RequestMethod.GET)
	public ModelAndView handleDashboardGet(HttpServletRequest req,  @PathParam("appname") String appname) {
		//
		// Simple process of selecting a VM based on URI and then stuffing what we received
		// into it.  The bulk of interesting processing actually happens in the Velocity 
		// template after that.
		//
		ModelAndView retval = null;
		
		if (appname.equalsIgnoreCase("contentrus")) {
			retval = new ModelAndView("contentrus");
		} else if (appname.equalsIgnoreCase("randsrus")) {
			retval = new ModelAndView("randsrus");
		} else if (appname.equalsIgnoreCase("scholars")) {
			retval = new ModelAndView("scholars");
		} else if (appname.equalsIgnoreCase("payroll")) {
			retval = new ModelAndView("payroll");
		}
		
		// And stuff the attributes we received into the context
		
		Enumeration<String> names = req.getAttributeNames();
		ArrayList<String> anames = new ArrayList<String>();
		HashMap<String,Object> map = new HashMap<String,Object>();
		
		while (names.hasMoreElements()) {
			String n = names.nextElement();
			map.put(n,req.getAttribute(n));
			anames.add(n);
		}
		
		retval.addObject("map",map);
		retval.addObject("anames",anames);
		
		if (map.get("eduPersonOrgDN") != null) {
			retval.addObject("odn","true");
		}
		
		//  And hand off to the Velocity template
		
		return retval;
	}
}
