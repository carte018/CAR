package edu.internet2.consent.icm.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import edu.internet2.consent.icm.util.IcmUtility;

@Path("/org-info-release-policies")
public class OrgInfoReleasePoliciesController {

	// Reflection API from ICM into the ARPSI policy interface
	// Similar to the endpoints in the ICM for the COPSU -- essentially, take the input, refactor it
	// into the back-end naming convention, and send it through to the backend, then reap the response and
	// refactor it back into the ICM naming convention and return it to the caller.
	
	// Standard routines for handling authN and authZ

	@SuppressWarnings("unused")
	private String caller = "";
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(OrgInfoReleasePoliciesController.class);
	
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
	
	// PUT operation for modifying singular policy by ID
	// Send the request to the ARPSI and return the result 
	@PUT
	@Path("/{policy_id}")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response putRootById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id, String entity) {
		// Pass-thru for PUT operation for org-info-release-policies
		IcmConfig config = IcmConfig.getInstance();   
		String arpsiHost = config.getProperty("arpsi.server.name", true);
		String arpsiPort = config.getProperty("arpsi.server.port",true);
		
		StringBuilder sb = new StringBuilder();
	/*	sb.append("https://");
		sb.append(arpsiHost);
		sb.append(":");
		sb.append(arpsiPort); */
		
		sb.append("/consent/v1/arpsi/org-info-release-policies/");
		sb.append(policy_id);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient, "PUT", arpsiHost, arpsiPort, sb.toString(), request, entity);
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			return buildResponse(Status.fromStatusCode(status),rbody);
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0060",e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	// DELETE operation for deactivating specific policy by ID
	// Wrapper for the ARPSI DELETE routine -- we simply call it and return the result recast as a User* object
	
	@DELETE
	@Path("/{policy_id}")
	public Response deleteRootById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id) {
		// Pass along the savings
		IcmConfig config = IcmConfig.getInstance();
		StringBuilder sb = new StringBuilder();

		String arpsiHost = config.getProperty("arpsi.server.name", true);
		String arpsiPort = config.getProperty("arpsi.server.port", true);
		
		sb.append("/consent/v1/arpsi/org-info-release-policies/");
		sb.append(policy_id);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient, "DELETE", arpsiHost, arpsiPort, sb.toString(),request, null);
			int status = IcmUtility.extractStatusCode(response);
			if (status >= 300) {
				String rbody = IcmUtility.extractBody(response);
				return buildResponse(Status.fromStatusCode(status),rbody);
			} else {
				return buildResponse(Status.NO_CONTENT,"");
			}
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0060",e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	// GET operation for retrieving specific policy by ID
	// We retrieve the results from the ARPSI 
	//
	@GET
	@Path("/{policy_id}")
	@Produces({"application/json"})
	public Response getRootById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id) {
	// We retrieve the results from the ARPSI 
	//
		StringBuilder sb = new StringBuilder();
		IcmConfig config = IcmConfig.getInstance();
		String arpsiHost = config.getProperty("arpsi.server.name", true);
		String arpsiPort = config.getProperty("arpsi.server.port", true);
	
		sb.append("/consent/v1/arpsi/org-info-release-policies/");
		sb.append(policy_id);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient, "GET", arpsiHost, arpsiPort, sb.toString(), request, null);
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			return buildResponse(Status.fromStatusCode(status),rbody);
		} catch (Exception e) {
			return IcmUtility.locError(500,"ERR0060",e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	@GET
	@Path("/")
	@Produces({"application/json"})
	public Response getRootByParameters(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		// Given some input parameters, send them to the appropriate ARPSI service and return the result
		// converting object class identifiers along the way.
		//
		// construct the target URL from the inputs
		
		// testing
		// ClassLoader classLoader = UserInfoReleasePoliciesController.class.getClassLoader();
		// URL resource = classLoader.getResource("org/apache/http/message/BasicLineFormatter.class");
		// System.out.println(resource);
		//
		IcmConfig config = IcmConfig.getInstance();
		String arpsiHost = config.getProperty("arpsi.server.name", true);
		String arpsiPort = config.getProperty("arpsi.server.port", true); 
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/arpsi/org-info-release-policies");
		if (request.getQueryString() != null) {
			sb.append("?");
			sb.append(request.getQueryString());
		}
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient, "GET", arpsiHost, arpsiPort, sb.toString(), request, null);
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			return buildResponse(Status.fromStatusCode(status),rbody);
		} catch (Exception e) {
			return IcmUtility.locError(500,"ERR0060",e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
	
	@POST
	@Path("/")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response postRoot(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// Take the passed-in entity, convert it from a UserInfoReleasePolicy to an InfoReleasePolicy and
		// send it (serialized) to the back-end COPSU service, then retrieve the result and return it in 
		// the form of a UserReturnedPolicy rather than a ReturnedPolicy.
		//
		// First, construct the target URL at the back-end
		//
		IcmConfig config = IcmConfig.getInstance();
		String arpsiHost = config.getProperty("arpsi.server.name", true);
		String arpsiPort = config.getProperty("arpsi.server.port", true);
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/arpsi/org-info-release-policies");
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient, "POST", arpsiHost, arpsiPort, sb.toString(), request, entity);
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			return buildResponse(Status.fromStatusCode(status),rbody);
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0060",e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
}
