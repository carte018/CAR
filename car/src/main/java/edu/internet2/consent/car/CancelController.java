package edu.internet2.consent.car;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CancelController {

	private String sconvo = null;
	
	@RequestMapping(value="/cancel",method=RequestMethod.GET)
	public ModelAndView canceler(HttpServletRequest req) {
		HttpSession sess = req.getSession(false);
		if (sess != null && req.getParameter("conversation") != null) {
			sconvo = req.getParameter("conversation");
			sess.removeAttribute(sconvo + ":" + "returntourl");
			sess.removeAttribute(sconvo + ":" + "csrftoken");
		}
		CarConfig config = CarConfig.getInstance();
		String cancelurl = config.getProperty("cancelURL", false);
		if (cancelurl != null && ! cancelurl.contentEquals("")) 
			return new ModelAndView("redirect:"+cancelurl);
		else
			return new ModelAndView("redirect:https://www.duke.edu");
	}
}
