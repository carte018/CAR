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

import java.util.ArrayList;
import java.util.List;

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

import edu.internet2.consent.copsu.model.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.icm.cfg.IcmConfig;
import edu.internet2.consent.icm.model.ListOfUserReturnedPolicy;
import edu.internet2.consent.icm.model.UserAllOtherInfoReleaseStatement;
import edu.internet2.consent.icm.model.UserDirectiveAllOtherValues;
import edu.internet2.consent.icm.model.UserDirectiveOnValues;
import edu.internet2.consent.icm.model.UserInfoReleasePolicy;
import edu.internet2.consent.icm.model.UserInfoReleaseStatement;
import edu.internet2.consent.icm.model.UserPolicyMetadata;
import edu.internet2.consent.icm.model.UserReleaseDirective;
import edu.internet2.consent.icm.model.UserReturnedPolicy;
import edu.internet2.consent.icm.util.IcmUtility;
import edu.internet2.consent.icm.model.LogCriticality;

@Path("/user-info-release-policies")
public class UserInfoReleasePoliciesController {
	// Controller for exposing back-end COPSU policy operations
	//
	// Essentially pass the input to the back-end COPSU endpoint and 
	// then pass back the result from the COPSU endpoint
	// 
	// There are separate schemas for the COPSU and the ICM for user consent policies --
	// in the ICM what the COPSU refers to as simply "UserInfoReleasePolicy" becomes "UserInfoReleasePolicy".
	// We import the COPSU classes from the COPSU jar file and have our own schema of User* classes
	// which are not persisted by the ICM (only used as containers for WS calls) and exchanged with 
	// calling RH's.
	
	// Standard routines for handling authN and authZ

	@SuppressWarnings("unused")
	private String caller = "";
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UserInfoReleasePoliciesController.class);
	
	// Utility method for internal use only for generating responses in proper format.
	// We tack on the headers required for CORS with Swagger.io here automatically
	// We assume that the caller is setting both status code and entity, so we don't differentiate

	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin", "http://editor.swagger.io").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	
	// Mapping utility methods.  We take one of our UserReturnedPolicy objects as input and return a 
	// COPSU ReturnedPolicy object and vice-versa.
	
	@SuppressWarnings("static-access")
	private UserReturnedPolicy convertToIcmUserReturnedPolicy(edu.internet2.consent.copsu.model.ReturnedPolicy rp) {
		// Deep copy from one of theirs to one of ours in the wrapper
		UserReturnedPolicy retval = new UserReturnedPolicy();
		// Metadata
		UserPolicyMetadata upm = new UserPolicyMetadata();
		// Policy ID (required)
		edu.internet2.consent.icm.model.PolicyId pi = new edu.internet2.consent.icm.model.PolicyId();
		pi.setBaseId(rp.getPolicyMetaData().getPolicyId().getBaseId());
		pi.setVersion(rp.getPolicyMetaData().getPolicyId().getVersion());
		upm.setPolicyId(pi);
		// Creator (required)
		edu.internet2.consent.icm.model.CreatorId cr = new edu.internet2.consent.icm.model.CreatorId();
		cr.setCreatingUserType(rp.getPolicyMetaData().getCreator().getCreatingUserType());
		cr.setCreatingUserValue(rp.getPolicyMetaData().getCreator().getCreatingUserValue());
		upm.setCreator(cr);
		// CreateTime (required)
		upm.setCreateTime(rp.getPolicyMetaData().getCreateTime());
		// State (required)
		upm.setState(rp.getPolicyMetaData().getState().valueOf(edu.internet2.consent.icm.model.PolicyState.class,rp.getPolicyMetaData().getState().toString()));
		// Superseded by (optional)
		if (rp.getPolicyMetaData().getSupersededBy() != null) {
			edu.internet2.consent.icm.model.SupersedingPolicyId si = new edu.internet2.consent.icm.model.SupersedingPolicyId();
			si.setSupersedingId(rp.getPolicyMetaData().getSupersededBy().getSupersedingId());
			si.setSupersedingVersion(rp.getPolicyMetaData().getSupersededBy().getSupersedingVersion());
			upm.setSupersededBy(si);
		}
		// Change order (optional)
		if (rp.getPolicyMetaData().getChangeOrder() != null) {
			upm.setChangeOrder(rp.getPolicyMetaData().getChangeOrder());
		}
		
		// Convert the other
		UserInfoReleasePolicy uir = convertToIcmUserPolicy(rp.getInfoReleasePolicy());
		
		// and build the meta object
		retval.setPolicyMetaData(upm);
		retval.setUserInfoReleasePolicy(uir);
		return retval;
		
	}
	
	@SuppressWarnings("static-access")
	private UserInfoReleasePolicy convertToIcmUserPolicy(edu.internet2.consent.copsu.model.InfoReleasePolicy irp) {
		// Deep copy from one of theirs to one of ours.
		//
		UserInfoReleasePolicy retval = new UserInfoReleasePolicy();
		// Description (required)
		retval.setDescription(irp.getDescription());
		// UserId (required)
		edu.internet2.consent.icm.model.UserId ui = new edu.internet2.consent.icm.model.UserId();
		ui.setUserType(irp.getUserId().getUserType());
		ui.setUserValue(irp.getUserId().getUserValue());
		retval.setUserId(ui);
		// RelyingPartyId (required)
		edu.internet2.consent.icm.model.RelyingPartyId rpi = new edu.internet2.consent.icm.model.RelyingPartyId();
		rpi.setRPtype(irp.getRelyingPartyId().getRPtype());
		rpi.setRPvalue(irp.getRelyingPartyId().getRPvalue());
		retval.setRelyingPartyId(rpi);
		// ResourceHolderId (required)
		edu.internet2.consent.icm.model.ResourceHolderId rhi = new edu.internet2.consent.icm.model.ResourceHolderId();
		rhi.setRHType(irp.getResourceHolderId().getRHType());
		rhi.setRHValue(irp.getResourceHolderId().getRHValue());
		retval.setResourceHolderId(rhi);
		// whileImAwayDirective (required)
		retval.setWhileImAwayDirective(irp.getWhileImAwayDirective().valueOf(edu.internet2.consent.icm.model.WhileImAwayDirective.class,irp.getWhileImAwayDirective().toString()));
		// userAllOtherInfoReleaseStatement (required)
		UserAllOtherInfoReleaseStatement uaoirs = new UserAllOtherInfoReleaseStatement();
		edu.internet2.consent.icm.model.AllOtherInfoId aoi = new edu.internet2.consent.icm.model.AllOtherInfoId();
		aoi.setAllOtherInfoType(irp.getAllOtherInfoReleaseStatement().getAllOtherInfoId().getAllOtherInfoType().valueOf(edu.internet2.consent.icm.model.AllOtherInfoTypeConst.class,irp.getAllOtherInfoReleaseStatement().getAllOtherInfoId().getAllOtherInfoType().toString()));
		aoi.setAllOtherInfoValue(irp.getAllOtherInfoReleaseStatement().getAllOtherInfoId().getAllOtherInfoValue().valueOf(edu.internet2.consent.icm.model.AllOtherInfoValueConst.class,irp.getAllOtherInfoReleaseStatement().getAllOtherInfoId().getAllOtherInfoValue().toString()));
		uaoirs.setAllOtherInfoId(aoi);
		UserDirectiveAllOtherValues udao = new UserDirectiveAllOtherValues();
		udao.setUserReleaseDirective(irp.getAllOtherInfoReleaseStatement().getDirectiveAllOtherValues().getReleaseDirective().valueOf(UserReleaseDirective.class,irp.getAllOtherInfoReleaseStatement().getDirectiveAllOtherValues().getReleaseDirective().toString()));
		udao.setAllOtherValues(edu.internet2.consent.icm.model.AllOtherValuesConst.allOtherValues);
		uaoirs.setUserDirectiveAllOtherValues(udao);
		retval.setUserAllOtherInfoReleaseStatement(uaoirs);
		// arrayOfInfoReleaseStatement (optional)
		ArrayList<UserInfoReleaseStatement> al = new ArrayList<UserInfoReleaseStatement>();
		if (irp.getArrayOfInfoReleaseStatement() != null && ! irp.getArrayOfInfoReleaseStatement().isEmpty()) {
			// deep copy
			for (InfoReleaseStatement irs : irp.getArrayOfInfoReleaseStatement()) {
				// copy with type conversion
				UserInfoReleaseStatement uirs = new UserInfoReleaseStatement();
				// InfoId (required)
				edu.internet2.consent.icm.model.InfoId ii = new edu.internet2.consent.icm.model.InfoId();
				ii.setInfoType(irs.getInfoId().getInfoType());
				ii.setInfoValue(irs.getInfoId().getInfoValue());
				uirs.setInfoId(ii);
				// UDirectiveAllOtherValues (required)
				UserDirectiveAllOtherValues udav = new UserDirectiveAllOtherValues();
				udav.setUserReleaseDirective(irs.getDirectiveAllOtherValues().getReleaseDirective().valueOf(UserReleaseDirective.class,irs.getDirectiveAllOtherValues().getReleaseDirective().toString()));
				udav.setAllOtherValues(edu.internet2.consent.icm.model.AllOtherValuesConst.allOtherValues);
				uirs.setUserDirectiveAllOtherValues(udav);
				//Persistence (optional)
				if (irs.getPersistence() != null) {
					uirs.setPersistence(irs.getPersistence());
				}
				uirs.setArrayOfDirectiveOnValues(new ArrayList<UserDirectiveOnValues>());
				// ArrayOfDirectiveOnValues (optional)
				if (irs.getArrayOfDirectiveOnValues() != null && ! irs.getArrayOfDirectiveOnValues().isEmpty()) {
					for (DirectiveOnValues dov : irs.getArrayOfDirectiveOnValues()) {
						UserDirectiveOnValues udov = new UserDirectiveOnValues();
						udov.setValuesList(new ArrayList<edu.internet2.consent.icm.model.ValueObject>());
						udov.setUserReleaseDirective(dov.getReleaseDirective().valueOf(edu.internet2.consent.icm.model.UserReleaseDirective.class,dov.getReleaseDirective().toString()));
						for (ValueObject vo : dov.getValueObjectList()) {
							edu.internet2.consent.icm.model.ValueObject nvo = new edu.internet2.consent.icm.model.ValueObject();
							nvo.setValue(vo.getValue());
							udov.getValueObjectList().add(nvo);
						}
						uirs.getArrayOfDirectiveOnValues().add(udov);
					}
				}
				al.add(uirs);
			}
			retval.setArrayOfInfoReleaseStatement(al);
		}
		return retval;
	}
	
	@SuppressWarnings("static-access")
	private edu.internet2.consent.copsu.model.InfoReleasePolicy convertToCopsuPolicy(UserInfoReleasePolicy uirp) {
		InfoReleasePolicy retval = new InfoReleasePolicy();
		
		// Deep copy between the objects without reference overlap
		// Doing this ourselves gives us explicit control as the property names differ
		//
		// In this case, we convert the UserInfoReleasePolicy we may receive on input to an InfoReleasePolicy
		// suitable for input to the COPSU
		// Description (required)
		retval.setDescription(uirp.getDescription());
		// UserId (required)
		UserId ui = new UserId();
		retval.setUserId(ui);
		retval.getUserId().setUserType(uirp.getUserId().getUserType());
		retval.getUserId().setUserValue(uirp.getUserId().getUserValue());
		// RelyingPartyId (required)
		RelyingPartyId ri = new RelyingPartyId();
		retval.setRelyingPartyId(ri);
		retval.getRelyingPartyId().setRPtype(uirp.getRelyingPartyId().getRPtype());
		retval.getRelyingPartyId().setRPvalue(uirp.getRelyingPartyId().getRPvalue());
		// ResourceHolderID (required)
		ResourceHolderId hi = new ResourceHolderId();
		retval.setResourceHolderId(hi);
		if (uirp.getResourceHolderId() != null) {
			retval.getResourceHolderId().setRHType(uirp.getResourceHolderId().getRHType());
		} else {
			retval.getResourceHolderId().setRHType("resourceHolder");
		}
		retval.getResourceHolderId().setRHValue(uirp.getResourceHolderId().getRHValue());
		// whileImAwayDirective (required)
		retval.setWhileImAwayDirective(uirp.getWhileImAwayDirective().valueOf(WhileImAwayDirective.class,uirp.getWhileImAwayDirective().toString()));
		//allOtherInfoReleaseStatement (required)
		AllOtherInfoReleaseStatement aoirse = new AllOtherInfoReleaseStatement();
		retval.setAllOtherInfoReleaseStatement(aoirse);
		AllOtherInfoId aoi = new AllOtherInfoId();
		aoi.setAllOtherInfoType(uirp.getUserAllOtherInfoReleaseStatement().getAllOtherInfoId().getAllOtherInfoType().valueOf(AllOtherInfoTypeConst.class,uirp.getUserAllOtherInfoReleaseStatement().getAllOtherInfoId().getAllOtherInfoType().toString()));
		aoi.setAllOtherInfoValue(uirp.getUserAllOtherInfoReleaseStatement().getAllOtherInfoId().getAllOtherInfoValue().valueOf(AllOtherInfoValueConst.class,uirp.getUserAllOtherInfoReleaseStatement().getAllOtherInfoId().getAllOtherInfoValue().toString()));
		retval.getAllOtherInfoReleaseStatement().setAllOtherInfoId(aoi);
		DirectiveAllOtherValues dao = new DirectiveAllOtherValues();
		dao.setReleaseDirective(uirp.getUserAllOtherInfoReleaseStatement().getUserDirectiveAllOtherValues().getUserReleaseDirective().valueOf(ReleaseDirective.class,uirp.getUserAllOtherInfoReleaseStatement().getUserDirectiveAllOtherValues().getUserReleaseDirective().toString()));
		dao.setAllOtherValues(AllOtherValuesConst.allOtherValues);
		retval.getAllOtherInfoReleaseStatement().setDirectiveAllOtherValues(dao);
		// arrayOfInfoReleaseStatement (optional)
		if (uirp.getArrayOfInfoReleaseStatement() != null && ! uirp.getArrayOfInfoReleaseStatement().isEmpty()) {
			// deep copy the array with type conversion.  Urf.
			ArrayList<InfoReleaseStatement> fairse = new ArrayList<InfoReleaseStatement>();
			retval.setArrayOfInfoReleaseStatement(fairse);
			for (UserInfoReleaseStatement u : uirp.getArrayOfInfoReleaseStatement()) {
				// For each element in the list...
				// Since the type structure differs, we have to do this the (very) long way around
				//
				InfoReleaseStatement irs = new InfoReleaseStatement();
				// InfoID (required)
				InfoId ii = new InfoId();
				ii.setInfoType(u.getInfoId().getInfoType());
				ii.setInfoValue(u.getInfoId().getInfoValue());
				irs.setInfoId(ii);
				// DirectiveAllOtherValues (required)
				DirectiveAllOtherValues daov = new DirectiveAllOtherValues();
				daov.setReleaseDirective(u.getUserDirectiveAllOtherValues().getUserReleaseDirective().valueOf(ReleaseDirective.class,u.getUserDirectiveAllOtherValues().getUserReleaseDirective().toString()));
				daov.setAllOtherValues(AllOtherValuesConst.allOtherValues);
				irs.setDirectiveAllOtherValues(daov);
				// Persistence (optional)
				if (u.getPersistence() != null) {
					irs.setPersistence(u.getPersistence());
				}
				// ArrayOfDirectiveOnValues (optional)
				// Deep means deep.  Sigh.
				if (u.getArrayOfDirectiveOnValues() != null && ! u.getArrayOfDirectiveOnValues().isEmpty()) {
					// we have some
					ArrayList<DirectiveOnValues> aodve = new ArrayList<DirectiveOnValues>();
					irs.setArrayOfDirectiveOnValues(aodve);
					for (UserDirectiveOnValues ud : u.getArrayOfDirectiveOnValues()) {
						DirectiveOnValues dov = new DirectiveOnValues();
						ArrayList<ValueObject> vol = new ArrayList<ValueObject>();
						dov.setValuesList(vol);
						dov.setReleaseDirective(ud.getUserReleaseDirective().valueOf(ReleaseDirective.class,ud.getUserReleaseDirective().toString()));
						for (edu.internet2.consent.icm.model.ValueObject vo : ud.getValueObjectList()) {
							edu.internet2.consent.copsu.model.ValueObject nvo = new edu.internet2.consent.copsu.model.ValueObject();
							nvo.setValue(vo.getValue());
							dov.getValueObjectList().add(nvo);
						}
						irs.getArrayOfDirectiveOnValues().add(dov);
					}
				}
				retval.getArrayOfInfoReleaseStatement().add(irs);
			}
		}
		return retval;
	}
	
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
	// Send the request to the COPSU and return the result repackaged as a User* object
	@PUT
	@Path("/{policy_id}")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response putRootById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id, String entity) {
		// Pass-thru for PUT operation for user-info-release-policies
		IcmConfig config = IcmConfig.getInstance();
		String copsuHost = config.getProperty("copsu.server.name", true);
		String copsuPort = config.getProperty("copsu.server.port",true);
		
		StringBuilder sb = new StringBuilder();
		sb.append("/consent/v1/copsu/user-info-release-policies/");
		sb.append(policy_id);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			// First, translate Icm object to Copsu object for pass-thru
			ObjectMapper om = new ObjectMapper();
			UserInfoReleasePolicy uirp = om.readValue(entity,  UserInfoReleasePolicy.class);
			InfoReleasePolicy irp = convertToCopsuPolicy(uirp);
			String sendEntity = irp.toJSON();
			
			response = IcmUtility.forwardRequest(httpClient, "PUT", copsuHost, copsuPort, sb.toString(), request, sendEntity);
			
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			
			// On error, send status code and body from COPSU
			if (status >= 300)
				return buildResponse(Status.fromStatusCode(status),rbody);
			
			// Otherwise, deserialize, convert, re-serialize, and return
			
			ReturnedPolicy r = om.readValue(rbody,  ReturnedPolicy.class);
			UserReturnedPolicy ur = convertToIcmUserReturnedPolicy(r);
			return buildResponse(Status.fromStatusCode(status),ur.toJSON());
			
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0056",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
		
	// DELETE operation for deactivating specific policy by ID
	// Wrapper for the COPSU routine -- we simply call it and return the result recast as a User* object
	
	@DELETE
	@Path("/{policy_id}")
	public Response deleteRootById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id) {
		// Pass along the savings
		IcmConfig config = IcmConfig.getInstance();
		StringBuilder sb = new StringBuilder();
		
		String copsuHost = config.getProperty("copsu.server.name", true);
		String copsuPort = config.getProperty("copsu.server.port", true);
		
		sb.append("/consent/v1/copsu/user-info-release-policies/");
		sb.append(policy_id);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient,"DELETE", copsuHost, copsuPort, sb.toString(), request, null);
			int status = IcmUtility.extractStatusCode(response);
			
			// on error, return code and returned body
			if (status >= 300) {
				String rbody = IcmUtility.extractBody(response);
				return buildResponse(Status.fromStatusCode(status),rbody);
			}
			
			// otherwise, return an empty response because there's no content to return
			return buildResponse(Status.NO_CONTENT,"");
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0056",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
				

	// GET operation for retrieving specific policy by ID
	// We retrieve the results from the COPSU and rebrand them to the caller as User* objects
	//
	@GET
	@Path("/{policy_id}")
	@Produces({"application/json"})
	public Response getRootById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id) {
	// We retrieve the results from the COPSU and rebrand them to the caller as User* objects.
	//
		
				
		StringBuilder sb = new StringBuilder();
		IcmConfig config = IcmConfig.getInstance();
		String copsuHost = config.getProperty("copsu.server.name", true);
		String copsuPort = config.getProperty("copsu.server.port", true); 
		sb.append("/consent/v1/copsu/user-info-release-policies/");
		sb.append(policy_id);
		
		String rbody = null;
		int status;
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		try {
			response = IcmUtility.forwardRequest(httpClient,"GET",copsuHost,copsuPort,sb.toString(),request,null);
			rbody = IcmUtility.extractBody(response);
			status = IcmUtility.extractStatusCode(response);
			if (status >= 300)
				return buildResponse(Status.fromStatusCode(status),rbody);
			ObjectMapper om = new ObjectMapper();
			List<ReturnedPolicy> lr = om.readValue(rbody, new TypeReference<List<ReturnedPolicy>>() {});
			ListOfUserReturnedPolicy ulr = new ListOfUserReturnedPolicy();
			for (ReturnedPolicy l : lr) {
				ulr.addPolicy(convertToIcmUserReturnedPolicy(l));
			}
			
			return buildResponse(Status.fromStatusCode(response.getStatusLine().getStatusCode()),ulr.toJSON());
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0056",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}

	@GET
	@Path("/")
	@Produces({"application/json"})
	public Response getRootByParameters(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		// Given some input parameters, send them to the appropriate COPSU service and return the result
		// converting object class identifiers along the way.
		//
		// construct the target URL from the inputs
		
		IcmConfig config = IcmConfig.getInstance();
		String copsuHost = config.getProperty("copsu.server.name", true);
		String copsuPort = config.getProperty("copsu.server.port", true); 
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/copsu/user-info-release-policies");
		if (request.getQueryString() != null) {
			sb.append("?");
			sb.append(request.getQueryString());
		}
		// Now we have the target:  send the request to the COPSU
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			response = IcmUtility.forwardRequest(httpClient, "GET", copsuHost, copsuPort, sb.toString(), request, null);
			
			// No matter what, we get the body of the response back
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			
			// On error, return error code + body
			if (status >= 300) 
				return buildResponse(Status.fromStatusCode(status),rbody);
			
			// otherwise, deserialize, convert to an ICM object, and return it re-serialized
			ObjectMapper om = new ObjectMapper();
			List<ReturnedPolicy> lr = om.readValue(rbody, new TypeReference<List<ReturnedPolicy>>() {});
			ListOfUserReturnedPolicy ulr = new ListOfUserReturnedPolicy();
			for (ReturnedPolicy l : lr) {
				ulr.addPolicy(convertToIcmUserReturnedPolicy(l));
			}
			return buildResponse(Status.fromStatusCode(status),ulr.toJSON());
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0056",LogCriticality.error,e.getMessage());
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
		String copsuHost = config.getProperty("copsu.server.name", true);
		String copsuPort = config.getProperty("copsu.server.port", true);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("/consent/v1/copsu/user-info-release-policies");
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		
		try {
			
			// Convert input to COPSU form
			ObjectMapper om = new ObjectMapper();
			UserInfoReleasePolicy uirp = om.readValue(entity,  UserInfoReleasePolicy.class);
			InfoReleasePolicy irp = convertToCopsuPolicy(uirp);
			String sendEntity = irp.toJSON();
			
			// send it
			response = IcmUtility.forwardRequest(httpClient, "POST", copsuHost, copsuPort, sb.toString(), request, sendEntity);
			
			// Process the response
			int status = IcmUtility.extractStatusCode(response);
			String rbody = IcmUtility.extractBody(response);
			
			if (status >= 300) 
				return buildResponse(Status.fromStatusCode(status),rbody);
			
			ReturnedPolicy rp = om.readValue(rbody,ReturnedPolicy.class);
			UserReturnedPolicy urp = convertToIcmUserReturnedPolicy(rp);
			return buildResponse(Status.fromStatusCode(status),urp.toJSON());
			
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0056",LogCriticality.error,e.getMessage());
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
}
