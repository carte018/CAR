package edu.internet2.consent.caradmin;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class CharSetFilter implements Filter {		 
	 
	    public void doFilter(
	      ServletRequest request, 
	      ServletResponse response, 
	      FilterChain next) throws IOException, ServletException {
	        request.setCharacterEncoding("UTF-8");
	        response.setContentType("text/html; charset=UTF-8");
	        response.setCharacterEncoding("UTF-8");
	        if (next != null) 
	        	next.doFilter(request, response);
	    }

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			// TODO Auto-generated method stub
		}

		@Override
		public void destroy() {
			// TODO Auto-generated method stub
			
		}
	 
	
}