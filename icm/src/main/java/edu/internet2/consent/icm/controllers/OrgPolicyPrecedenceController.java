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
import javax.ws.rs.Path;
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
import org.apache.http.util.EntityUtils;

import edu.internet2.consent.icm.cfg.IcmConfig;
import edu.internet2.consent.icm.model.LogCriticality;
import edu.internet2.consent.icm.model.PATCH;
import edu.internet2.consent.icm.util.IcmHttpClientFactory;
import edu.internet2.consent.icm.util.IcmUtility;

@Path("/org-policy-precedence")
public class OrgPolicyPrecedenceController {
	// Standard folderol	
	
	// Reflection API from ICM into the ARPSI policy precedence interface
	// Similar to the endpoints in the ICM for the ARPSI policy -- essentially, take the input, refactor it
	// into the back-end naming convention, and send it through to the backend, then reap the response and
	// refactor it back into the ICM naming convention and return it to the caller.
	
	// Standard routines for handling authN and authZ
	// TODO:  When we choose an authN mechanism for 
	//        input requests we can pass along the authenticated identity here.
	@SuppressWarnings("unused")
	private String caller = "";
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(OrgPolicyPrecedenceController.class);
	
	// Utility method for internal use only for generating responses in proper format.
	// We tack on the headers required for CORS with Swagger.io here automatically
	// We assume that the caller is setting both status code and entity, so we don't differentiate
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin", "http://editor.swagger.io").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	// For the ARPSI, the mapping *should* be essentially null.
	
	// CORS for the Swagger editor, for development
	@OPTIONS
	@Path("/{policy_id}")
	public Response optionsRootId(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
			return buildResponse(Status.OK,"");
	}
	
	// CORS for the Swagger editor, for development
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	// Generic GET interface
	@GET
	@Path("/")
	@Produces({"application/json"})
	public Response getRoot(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		IcmConfig config = IcmConfig.getInstance();
		String arpsiHost = config.getProperty("arpsi.server.name", true);
		String arpsiPort = config.getProperty("arpsi.server.port", true); 
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/arpsi/org-policy-precedence");
		if (request.getQueryString() != null) {
			sb.append("?");
			sb.append(request.getQueryString());
		}

		//HttpClient httpClient = HttpClientBuilder.create().build();
		HttpClient httpClient = null;
		try {
			httpClient = IcmHttpClientFactory.getHttpsClient();
		} catch (Exception e) {
			// Log and create a raw client instead
			IcmUtility.locLog("ERR1136", LogCriticality.error,"Falling back to default HttpClient d/t failed client initialization");
			httpClient = HttpClientBuilder.create().build();
		}

		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient, "GET", arpsiHost, arpsiPort, sb.toString(), request, null);
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			return buildResponse(Status.fromStatusCode(status),rbody);

		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0064", LogCriticality.error, e.getMessage());
		} finally {
			EntityUtils.consumeQuietly(response.getEntity());
			HttpClientUtils.closeQuietly(response);
			//HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	@PATCH
	@Path("/")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response patchRoot(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		IcmConfig config = IcmConfig.getInstance();
		String arpsiHost = config.getProperty("arpsi.server.name", true);
		String arpsiPort = config.getProperty("arpsi.server.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/arpsi/org-policy-precedence");

		//HttpClient httpClient = HttpClientBuilder.create().build();
		HttpClient httpClient = null;
		try {
			httpClient = IcmHttpClientFactory.getHttpsClient();
		} catch (Exception e) {
			// Log and create a raw client instead
			IcmUtility.locLog("ERR1136", LogCriticality.error,"Falling back to default HttpClient d/t failed client initialization");
			httpClient = HttpClientBuilder.create().build();
		}

		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient,"PATCH", arpsiHost, arpsiPort, sb.toString(), request, entity);
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			return buildResponse(Status.fromStatusCode(status),rbody);
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0064", LogCriticality.error, e.getMessage());
		} finally {
			EntityUtils.consumeQuietly(response.getEntity());
			HttpClientUtils.closeQuietly(response);
			//HttpClientUtils.closeQuietly(httpClient);
		}
	}
}
