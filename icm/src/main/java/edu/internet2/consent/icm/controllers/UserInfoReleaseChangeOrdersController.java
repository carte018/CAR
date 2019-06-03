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
package edu.internet2.consent.icm.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import edu.internet2.consent.icm.cfg.IcmConfig;
import edu.internet2.consent.icm.model.LogCriticality;
import edu.internet2.consent.icm.util.IcmUtility;

@Path("/user-info-release-change-orders")
public class UserInfoReleaseChangeOrdersController {
	// Class encapsulating methods for the user-info-release-change-orders endpoint in the ICM web service
	// These are pass-thru endpoints that call the corresponding endpoints in the COPSU and return
	// the results returned by the COPSU.
	//

	
	@SuppressWarnings("unused")
	private String caller = "";
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UserInfoReleaseChangeOrdersController.class);
	
	// Utility method for internal use only for generating responses in proper format.
	// We tack on the headers required for CORS with Swagger.io here automatically
	// We assume that the caller is setting both status code and entity, so we don't differentiate
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin", "http://editor.swagger.io").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	

	// Attempt to avoid remapping in the change order case.
	// We may not need any of our own modeling for this interface.
	
	// CORS for the Swagger editor, for development
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	@OPTIONS
	@Path("/{change_order}")
	public Response optionsRootById(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@GET
	@Path("/{change_order}")
	@Produces({"application/json"})
	public Response getRootById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("change_order") String change_order) {
		// Pass the savings to the COPSU and return the result
		IcmConfig config = IcmConfig.getInstance();
		
		String copsuHost = config.getProperty("copsu.server.name", true);
		String copsuPort = config.getProperty("copsu.serve.rport", true);
		
		StringBuilder sb = new StringBuilder();
			
		sb.append("/consent/v1/copsu/user-info-release-change-orders/");
		sb.append(change_order);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient, "GET", copsuHost, copsuPort, sb.toString(), request, null);
			
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			
			// Literal pass-thru in this case
			return buildResponse(Status.fromStatusCode(status),rbody);
			

		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0064",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		
	}
	@GET
	@Path("/")
	@Produces({"application/json"})
	public Response getRoot(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		// Pass the savings to the COPSU and return the result
		IcmConfig config = IcmConfig.getInstance();
		StringBuilder sb = new StringBuilder();
		
		String copsuHost = config.getProperty("copsu.server.name", true);
		String copsuPort = config.getProperty("copsu.server.port", true);
		
		sb.append("/consent/v1/copsu/user-info-release-change-orders");
		String queryString = request.getQueryString();
		sb.append("?");
		sb.append(queryString);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient, "GET",  copsuHost,  copsuPort,  sb.toString(), request, null);
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			return buildResponse(Status.fromStatusCode(status),rbody);
		
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0064", LogCriticality.error, e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	
	@POST
	@Path("/")
	@Produces({"application/json"})
	@Consumes({"application/json"})
	public Response postRoot(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// Pass along a POST request to the COPSU change order interface
		IcmConfig config = IcmConfig.getInstance();
		StringBuilder sb = new StringBuilder();
		String copsuHost = config.getProperty("copsu.server.name", true);
		String copsuPort = config.getProperty("copsu.server.port", true);
		
		sb.append("/consent/v1/copsu/user-info-release-change-orders");
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient, "POST", copsuHost, copsuPort, sb.toString(), request, entity);
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			return buildResponse(Status.fromStatusCode(status),rbody);
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0064", LogCriticality.error, e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);

		}
	}
	

}
