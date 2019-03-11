package edu.internet2.consent.caradmin;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestController {

	@RequestMapping(value="/test",method=RequestMethod.GET)
	public ModelAndView returnTest(HttpServletRequest req) {
		ModelAndView retval = new ModelAndView("errorPage");
		retval.addObject("message","Test Successful");
		return retval;
	}
}
