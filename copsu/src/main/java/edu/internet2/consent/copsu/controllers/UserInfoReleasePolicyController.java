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
package edu.internet2.consent.copsu.controllers;

import edu.internet2.consent.copsu.cfg.CopsuConfig;
// Import the whole model for now -- why not
import edu.internet2.consent.copsu.model.*;
import edu.internet2.consent.copsu.util.CopsuUtility;
import edu.internet2.consent.copsu.util.NewEntityUtilities;
import edu.internet2.consent.exceptions.CopsuInitializationException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.DELETE;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/*
 * This is the primary controller class responding to /user-info-release-policies requests in the API
 * We assume versioning is handled for the API at the container level -- in the reference impl. 
 * case, this is done via Tomcat, but any equivalent container mechanism should work equally well. 
 */
@Path("/user-info-release-policies")
public class UserInfoReleasePolicyController {

	// TODO:  When we choose an authN mechanism for 
	//        input requests we can pass along the authenticated identity here.
	@SuppressWarnings("unused")
	private String caller = "";
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UserInfoReleasePolicyController.class);
	
	// Utility method for internal use only for generating responses in proper format.
	// We tack on the headers required for CORS with Swagger.io here automatically
	// We assume that the caller is setting both status code and entity, so we don't differentiate
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin", "http://editor.swagger.io").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	
	
	// No authentication support at the moment, although we call a stub authorization check in
	// each endpoint.
	//
	
	
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@POST
	@Path("/")
	@Produces({"application/json"})
	@Consumes({"application/json"})
	public Response postRoot(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// Handle root POST requests.
		// On input, we expect (and validate) an infoReleasePolicy *as* the body of the request
		// We parse the policy into an InfoReleasePolicy object, then proceed as follows:
		// * Check the input policy for required parameters
		// *   - if any are missing, return a 400 (BAD_REQUEST) with the first missing parameter required
		// * Check to see if an existing policy for this (rh,rp,user) already exists in the persistence store
		// *   - if it does, fail with a 409, returning the URL of the existing policy in the message
		// * Assign new policyMetaData to the policy and wrap it into a ReturnedPolicy object
		// * Persist the ReturnedPolicy object
		// * Return a 200 with the ReturnedPolicy object as body
		// 
		// If the calling user does not have a newRPTemplate, we create one here to meet the standard of 
		// always having a new RP template when needed.
		//
		@SuppressWarnings("unused")
		CopsuConfig config = null;
		try {
			config = CopsuUtility.init("postPolicy", request, headers, null);
		} catch (CopsuInitializationException e) {
			return CopsuUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		ObjectMapper mapper = new ObjectMapper();
		InfoReleasePolicy inputPolicy = null;
		
		try {
			inputPolicy = mapper.readValue(entity,InfoReleasePolicy.class);
		} catch (JsonParseException e) {
			return CopsuUtility.locError(400,"ERR0005",LogCriticality.info);
			// Eventually make this an ErrorModel in JSON
		} catch (JsonMappingException e) {
			return CopsuUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return CopsuUtility.locError(400,  "ERR0007",LogCriticality.info);
		}
		
		// Now we have the input policy -- look for missing parameters
		
		if (inputPolicy == null) {
			return CopsuUtility.locError(400, "ERR0008", LogCriticality.info);
		}
		if (inputPolicy.getDescription() == null) {
			return CopsuUtility.locError(400, "ERR0009", LogCriticality.info);
		}
		if (inputPolicy.getUserId() == null) {
			return CopsuUtility.locError(400, "ERR0010", LogCriticality.info);
		}
		if (inputPolicy.getRelyingPartyId() == null) {
			return CopsuUtility.locError(400, "ERR0011", LogCriticality.info);
		}
		if (inputPolicy.getResourceHolderId() == null) {
			return CopsuUtility.locError(400, "ERR0012", LogCriticality.info);
		}
		if (inputPolicy.getWhileImAwayDirective() == null) {
			return CopsuUtility.locError(400,"ERR0013", LogCriticality.info);
		}
		if (inputPolicy.getAllOtherInfoReleaseStatement() == null) {
			return CopsuUtility.locError(400, "ERR0014", LogCriticality.info);
		}
		
		// Idempotent creation of a new RP template in case one does not exist yet -- once we see the user,
		// we create this early to make sure we always have one at the ready.
		try {
			NewEntityUtilities.createNewRPTemplateForUser(inputPolicy.getUserId());
		} catch (Exception e) {
			// ignore here -- if this fails, we're failsafe and we continue anyway
		}
		
		// It is allowed for the arrayOfInfoReleaseStatement to be empty
		
		// Check for an existing policy -- since this is a POST, having an existing policy with the same
		// parameters constitutes a conflict
		//
		
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			return CopsuUtility.locError(500, "ERR0018", LogCriticality.error);
		}
		
		// Query
		Query<ReturnedPolicy> checkQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=0",ReturnedPolicy.class);
		checkQuery.setParameter("userValue", inputPolicy.getUserId().getUserValue());
		checkQuery.setParameter("rpValue", inputPolicy.getRelyingPartyId().getRPvalue());
		checkQuery.setParameter("rhValue",  inputPolicy.getResourceHolderId().getRHValue());
		
		List<ReturnedPolicy> checkList = checkQuery.list();
		
		if (checkList != null && checkList.size() != 0) {
			// We got back at least one response -- time to bail out
			try {
				ReturnedPolicy conflict = checkList.get(0);
				return CopsuUtility.locError(409,"ERR0015",LogCriticality.error,"https://"+request.getServerName()+"/consent/v1/copsu/user-info-release-policies/"+conflict.getPolicyMetaData().getPolicyId().getBaseId());
			} finally {
				// leak protection
				sess.close();
			}
		}
		
		// Stay in the session, and now that we've validated, push to the backing store
		// Construct new policy metadata
		PolicyMetadata md = new PolicyMetadata();
		// Time stamp is now
		md.setCreateTime(System.currentTimeMillis());

		CreatorId creator = new CreatorId();
		creator.setCreatingUserType(inputPolicy.getUserId().getUserType());
		creator.setCreatingUserValue(inputPolicy.getUserId().getUserValue());
		md.setCreator(creator);
		md.setState(PolicyState.active);
		PolicyId pid = new PolicyId();
		pid.setVersion("1");
		// We must be certain here that our ID is not already in use, so...
		String unusedUUID = null;
		// In the very unlikely event that the UUID generator becomes caught in a
		// PRDN cycle somewhere, we only allow 100 tries to generate an unused UUID
		//
		int numtries=0;
		while (unusedUUID == null && numtries++ <= 100) {
			Query<ReturnedPolicy> collisionCheckQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId",ReturnedPolicy.class);
			unusedUUID = UUID.randomUUID().toString();
			collisionCheckQuery.setParameter("baseId",unusedUUID);
			List<ReturnedPolicy> cl = collisionCheckQuery.list();
			if (cl != null && ! cl.isEmpty()) {
				unusedUUID = null;
			}
		}
		if (unusedUUID == null) {
			// We failed in our mission to find an unused UUID
			if (sess != null) 
				sess.close();  // leak protection
			return CopsuUtility.locError(500, "ERR0045", LogCriticality.error);
		}
		pid.setBaseId(unusedUUID); // we use uuids for policy ids for now
		md.setPolicyId(pid);
		
		// Merge the md and the inputPolicy into a storable ReturnedPolicy
		ReturnedPolicy rp = new ReturnedPolicy();
		rp.setInfoReleasePolicy(inputPolicy);
		rp.setPolicyMetaData(md);
		
		// Store the result
		// leak protection
		Transaction tx = null;
		try {
			tx = sess.beginTransaction();
			sess.save(rp);
			//sess.getTransaction().commit();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			sess.close();
		}
		
		// Close the session (in this case, we're done for certain)
		//sess.close();
		
		// And return the rp object we saved
		try {
			return buildResponse(Status.CREATED,rp.toJSON());
		} catch (JsonProcessingException e) {
			return CopsuUtility.locError(500, "ERR0016", LogCriticality.error);
		}
		
	}

	@OPTIONS
	@Path("/{policy_id}")
	public Response optionsByPolicy(@Context HttpServletRequest request, @Context HttpHeaders headers,@PathParam("policy_id") String policy_id) 
	{
		return buildResponse(Status.OK,"");
	}
	
	@DELETE
	@Path("/{policy_id}")
	@Produces({"application/json"})
	public Response deleteByPolicyId(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id) {
		// Delete operation by policy ID
		// If the policy is found, we simply mark it as invalid.  No replacement, no other changes -- just
		// mark it as invalid.
		
		// Standard initialization processing
	
		// Configure
		@SuppressWarnings("unused")
		CopsuConfig config = null;
		try {
			config = CopsuUtility.init("deletePolicy", request, headers, null);
		} catch (CopsuInitializationException e) {
			return CopsuUtility.locError(500,"ERR0004", LogCriticality.error);
		}

		// Authorization verified
		
		if (policy_id == null || policy_id.equals("")) {
			// This should never happen, but if it does...
			return CopsuUtility.locError(400, "ERR0017", LogCriticality.error);
		}
		
		CopsuUtility.locLog("LOG0001",LogCriticality.debug,policy_id);
		
		// Verify driver for Hibernate
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			return CopsuUtility.locError(500, "ERR0018", LogCriticality.error);
		}
		Transaction tx = sess.beginTransaction();
		// leak protection
		try {
		// Inside the transaction, we:
		//  Retrieve the policy
		//  Change its state
		//  Commit the transaction and complete
		//
		// DELETE is a one-way ticket to inactivity
		// For administrative purposes (not part of the published API) we support a special
		// expungeOnDelete flag which, if passed with the value "true", will cause the deletion to 
		// be an actual deletion rather than an inactivation.  THIS MUST BE USED WITH GREAT CARE.
		//
		// Get the active version of the policy with the specified policy ID
		Query<ReturnedPolicy> policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId and policyMetaData.state = 0",ReturnedPolicy.class);
		policyQuery.setParameter("baseId", policy_id);
		
		List<ReturnedPolicy> resultList = (List<ReturnedPolicy>) policyQuery.list();
		if (resultList == null || resultList.isEmpty()) {
			CopsuUtility.locLog("LOG0002",LogCriticality.debug,policy_id);
			tx.rollback();
			sess.close();
			return CopsuUtility.locError(404, "ERR0019", LogCriticality.info);
		} else if (resultList.size() > 1) {
			// Should never happen, but...
			CopsuUtility.locLog("LOG0003",LogCriticality.debug,policy_id);
			tx.rollback();
			sess.close();
			return CopsuUtility.locError(409, "ERR0020", LogCriticality.error);
		} else {
			// Exactly one found -- delete it and move on
			CopsuUtility.locLog("LOG0004", LogCriticality.debug,policy_id);
			if (request.getParameter("expungeOnDelete") != null && request.getParameter("expungeOnDelete").equals("true")) {
				CopsuUtility.locLog("LOG0005", LogCriticality.info, policy_id);
				sess.delete(resultList.get(0));
				tx.commit();  // expunge the components of the policy
				sess.close();
				return buildResponse(Status.NO_CONTENT,"");
			}
			resultList.get(0).getPolicyMetaData().setState(PolicyState.inactive);
			tx.commit();
			sess.close();
			return buildResponse(Status.NO_CONTENT,"");
		}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException(e);  // re-throw
		} finally {
			if (sess.isOpen())
				sess.close();
		}
	}
	
	@PUT
	@Path("/{policy_id}")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response putByPolicyId(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id, String entity) {
		//
		// Put operation allows modification of an existing policy.
		// Our semantics actually dictate that this is a replacement operation rather than an update in place
		// We retrieve the specified policy, clone it, build a new version with a +1'd policy ID version, 
		// update the original to have state=inactive, then store both the old version and the new version
		// to the persistence layer.  Operation is always on the most recent (active) version.
		//
		// We return a 404 if the specified policy ID does not exist.
		// We return BAD_REQUEST if the specified user, RP, or RH are different from the retrieved version,
		// since those are immutables in the policy space.
		// On success, we return 200 with the full ReturnedPolicy object for the updated policy, which 
		// will include the new metadata values (version number, state, etc.).
		//
		
		@SuppressWarnings("unused")
		CopsuConfig config = null;
		try {
			config = CopsuUtility.init("putPolicy", request, headers, null);
		} catch (CopsuInitializationException e) {
			return CopsuUtility.locError(500,"ERR0004", LogCriticality.error);
		}

		ObjectMapper mapper = new ObjectMapper();  // to handle input JSON
		InfoReleasePolicy inputPolicy = null;
		
		try {
			inputPolicy = mapper.readValue(entity,  InfoReleasePolicy.class);
		} catch (JsonParseException j) {
			return CopsuUtility.locError(400, "ERR0005", LogCriticality.info);
		} catch (JsonMappingException j) {
			return CopsuUtility.locError(400, "ERR0006", LogCriticality.info);
		} catch (Exception e) {
			return CopsuUtility.locError(500, "ERR0007", LogCriticality.info);
		}
		
		// Input policy is now parsed into inputPolicy
		
		if (inputPolicy == null) {
			return CopsuUtility.locError(400, "ERR0008", LogCriticality.info);
		}
		
		// We require that the userId, RP, and RH match the values in the specified policy.
		// The type values are actually ignored on input, but the value values are checked.  Type values 
		// are simply copied through, since neither can be changed, but an errant type value is not an error
		// in this implementation, while a value mismatch is.
		//
		
		// Retrieve the policy in question from the persistence store
		//
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			return CopsuUtility.locError(500, "ERR0018", LogCriticality.error);
		}

		// We open a transaction since we'll need to make atomic changes here.
		Transaction tx = sess.beginTransaction();  // this will need to rollback or commit, depending
		// leak protection
		try {
		Query<ReturnedPolicy> uQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId=:baseId and policyMetaData.state=0",ReturnedPolicy.class);
		uQuery.setParameter("baseId",policy_id);
		
		List<ReturnedPolicy> originals = (List<ReturnedPolicy>) uQuery.list();
		
		if (originals == null || originals.isEmpty()) {
			// The specified policy ID was not found -- this is a fail
			CopsuUtility.locLog("LOG0006", LogCriticality.debug);
			tx.rollback();
			sess.close();
			return CopsuUtility.locError(404, "ERR0019", LogCriticality.info);
		}
		if (originals.size() > 1) {
			// This should never happen, but...
			CopsuUtility.locLog("LOG0007",LogCriticality.debug);
			tx.rollback();
			sess.close();
			return CopsuUtility.locError(409, "ERR0020",LogCriticality.error);
		}
		// we have a single match to move forward with
		//
		ReturnedPolicy original = originals.get(0);  // Note that changes to this one will be stored back
		
		// verify that userId, RP, and RH all match -- they are immutable in this endpoint
		//
		if (inputPolicy.getUserId() != null && inputPolicy.getUserId().getUserValue() != null && ! inputPolicy.getUserId().getUserValue().equals(original.getInfoReleasePolicy().getUserId().getUserValue())) {
			// UserId is immutable
			tx.rollback();
			sess.close();
			return CopsuUtility.locError(400, "ERR0021",LogCriticality.info,"userId");
		}
		if (inputPolicy.getRelyingPartyId() != null && inputPolicy.getRelyingPartyId().getRPvalue() != null && ! inputPolicy.getRelyingPartyId().getRPvalue().equals(original.getInfoReleasePolicy().getRelyingPartyId().getRPvalue())) {
			tx.rollback();
			sess.close();
			return CopsuUtility.locError(400, "ERR0021",LogCriticality.info,"relyingPartyId");
		}
		if (inputPolicy.getResourceHolderId() != null && inputPolicy.getResourceHolderId().getRHValue() != null && ! inputPolicy.getResourceHolderId().getRHValue().equals(original.getInfoReleasePolicy().getResourceHolderId().getRHValue())) {
			tx.rollback();
			sess.close();
			return CopsuUtility.locError(400, "ERR0021",LogCriticality.info,"resourceHolderId");
		}
		//
		// This is a valid request
		// The transaction amounts to:
		// Create a new object that matches the old object.
		// Set its version number to +1 the old object's version number
		// Set its state to State.active
		// Set its create time to now
		// Set the supersededBy value to null
		// Set the InfoReleasePolicy value to the input InfoReleasePolicy value
		// Set the old object's status to "inactive" (=1)
		// Set the old object's superseded-by to the new object identifier
		// commit the transaction
		// On any error, rollback transaction
		// Close transaction an dsession and return
		//
		
		// this is a possibly expensive cheat
		//
		ObjectMapper copier = new ObjectMapper();
		ReturnedPolicy newPolicy = new ReturnedPolicy();
		try {
			String serialized = original.toJSON();
			newPolicy = (ReturnedPolicy) copier.readValue(serialized.getBytes(), ReturnedPolicy.class);
		} catch (Exception e) {
			tx.rollback();
			sess.close();
			return CopsuUtility.locError(500, "ERR0022",LogCriticality.error);
		}
		// Before actually performing the update, check to see that there was a 
		// significant change in the policy.  Most PUTs of user policies in the wild
		// are merely replacing a policy with a duplicate policy of a later vintage. 
		// If this is the current case, rather than archiving the old and building a new
		// policy, we return without making a change.
		// Actual policies have both metadata and policy data.  For comparison purposes,
		// we are concerned only with policy data.
		//
		// And then, only with relevant policy data.
		//
		// It turns out that we can reasonably detect equivalence by comparing JSON representations
		
		if (original.getInfoReleasePolicy().toString().contentEquals(inputPolicy.toString())) {
			CopsuUtility.locLog("LOG0015", LogCriticality.debug, "Skipping update of duplicate user policy");
			tx.rollback();
			sess.close();
			return buildResponse(Status.OK,original.toJSON());
		}
		
		// Otherwise...
		
		// Set create time to now
		newPolicy.getPolicyMetaData().setCreateTime(System.currentTimeMillis());
		
		// Duplicate user into creator (in case there was a different creator before -- PUT is by the user)
		newPolicy.getPolicyMetaData().getCreator().setCreatingUserType(newPolicy.getInfoReleasePolicy().getUserId().getUserType());
		newPolicy.getPolicyMetaData().getCreator().setCreatingUserValue(newPolicy.getInfoReleasePolicy().getUserId().getUserValue());
		
		// Set version to version + 1
		newPolicy.getPolicyMetaData().getPolicyId().setVersion(String.valueOf(Integer.parseInt(newPolicy.getPolicyMetaData().getPolicyId().getVersion()) + 1));
		// Set state to active
		newPolicy.getPolicyMetaData().setState(PolicyState.active);
		// Clear superseded by
		newPolicy.getPolicyMetaData().setSupersededBy(null);
		// Set the inforeleasepolicy value
		newPolicy.setInfoReleasePolicy(inputPolicy);
		// And mark the old policy inactive
		original.getPolicyMetaData().setState(PolicyState.inactive);
		// And set its successor
		SupersedingPolicyId spi = new SupersedingPolicyId();
		spi.setSupersedingId(newPolicy.getPolicyMetaData().getPolicyId().getBaseId());
		spi.setSupersedingVersion(newPolicy.getPolicyMetaData().getPolicyId().getVersion());
		original.getPolicyMetaData().setSupersededBy(spi);
		// Save the new and update the old with a transaction commit
		try {
			sess.save(newPolicy);
			tx.commit();  // commit the save *and* the updates to original
		} catch (Exception e) {
			boolean recovered = false;
			for (int rcount = 0; rcount < 8; rcount ++) {
				try {
					tx.rollback();
					Thread.sleep(500);
					sess.save(newPolicy);
					tx.commit();
					recovered = true;
					break;
				} catch (Exception f) {
					// ignore here
				}
			}
			if (!recovered) {
				sess.close();
				throw new RuntimeException(e);
			}
		}
		
		// Close session and return success
		CopsuUtility.locLog("LOG0008",LogCriticality.info,newPolicy.getPolicyMetaData().getPolicyId().getBaseId(),newPolicy.getPolicyMetaData().getPolicyId().getBaseId());
		sess.close();
		try {
			return buildResponse(Status.OK,newPolicy.toJSON());
		} catch (JsonProcessingException e) {
			return CopsuUtility.locError(500, "ERR0016",LogCriticality.error);
		} 
		} catch(Exception e) {
			tx.rollback();
			throw new RuntimeException(e);  // re-throw
		} finally {
			if (sess.isOpen())
				sess.close();
		}
	}
	
	@GET
	@Path("/{policy_id}")
	@Produces({"application/json"})
	public Response getRootByPolicyId(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id) { 
		//
		// Request parameters
		
		String requestedCreatorId = null;
		String requestedVersion = null;
		
		// Start by getting a configuration instance, in case we need it
		@SuppressWarnings("unused")
		CopsuConfig config = null;
		try {
			config = CopsuUtility.init("getPolicy", request, headers, null);
		} catch (CopsuInitializationException e) {
			return CopsuUtility.locError(500, "ERR0004",LogCriticality.error);
		}

		
		// Now we are authorized
		if (policy_id == null || policy_id.equals("")) {
			return CopsuUtility.locError(400, "ERR0017",LogCriticality.info);
			// This should never happen
		}
		
		// Check for request parameters
		requestedCreatorId = request.getParameter("createdBy");
		requestedVersion = request.getParameter("policyVersion");
		
		CopsuUtility.locLog("LOG0009",LogCriticality.debug,policy_id,requestedVersion,requestedCreatorId);

		// Based on the request parameters, retrieve the relevant policy from the persistence store
		// If the policy does not exist in the persistence store, or if no matching policy is found,
		// we return a 404 response with an error body indicating that no matching policy was found.
		// If we have a version number, retrieve that version number (if it exists). 
		// If we have a creator, require that creator in the requested version.
		// If no version number provided, assume the "current" (active) version.
		//
		// Verify that we have the proper driver available
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			return CopsuUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		
		// Hackery for priorVersion as requestedVersion
		// When requested, get the current version and decrement by one for the prior
		// If there is no prior version (if this is version 1) we'll end up returning a 404
		//
		if (requestedVersion != null && (requestedVersion.equals("priorPolicy") || requestedVersion.equals("priorVersion"))) {
			Query<ReturnedPolicy> priorQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId and policyMetaData.state = 0",ReturnedPolicy.class);
			priorQuery.setParameter("baseId", policy_id);
			List<ReturnedPolicy> curList = (List<ReturnedPolicy>) priorQuery.list();
			if (curList != null && ! curList.isEmpty()) {
				requestedVersion = String.valueOf(Integer.parseInt(curList.get(0).getPolicyMetaData().getPolicyId().getVersion()) - 1);
			}
		}
		
		// If requestedVersion is "allPolicies" or "allVersions" (we treat them as equivalent here for backward compatibility), return the whole list -- this must be implemented inline
		// below, unfortunately.
		
		// Set up the query based on the input parameters
		Query<ReturnedPolicy> policyQuery = null;
		
		if (requestedCreatorId != null) {
			// This query requires a specific creator
			if (requestedVersion != null) {
				if (requestedVersion.equals("allPolicies") || requestedVersion.equals("allVersions")) {
					// return all the policies
					policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId and policyMetaData.creator.creatingUserValue = :creator",ReturnedPolicy.class);
					policyQuery.setParameter("baseId", policy_id);
					policyQuery.setParameter("creator", requestedCreatorId);
				} else {
					// And it requires a specific version
					policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId and policyMetaData.policyId.version = :version and policyMetaData.creator.creatingUserValue = :creator",ReturnedPolicy.class);
					policyQuery.setParameter("baseId", policy_id);
					policyQuery.setParameter("version", requestedVersion);
					policyQuery.setParameter("creator", requestedCreatorId);
				}
			} else {
				// no version, just creator
				// request active policy (policy state == 0)
				policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId and policyMetaData.state = 0 and policyMetaData.creator.creatingUserValue = :creator",ReturnedPolicy.class);
				policyQuery.setParameter("baseId", policy_id);
				policyQuery.setParameter("creator", requestedCreatorId);
			}
		} else {
			if (requestedVersion != null) {
				if (requestedVersion.equals("allPolicies") || requestedVersion.equals("allVersions")) {
						policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId",ReturnedPolicy.class);
						policyQuery.setParameter("baseId", policy_id);
				} else {
					// just a version number
					policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId and policyMetaData.policyId.version = :version",ReturnedPolicy.class);
					policyQuery.setParameter("baseId", policy_id);
					policyQuery.setParameter("version", requestedVersion);
				}
			} else {
				// No version, no creator -- just return the current policy
				policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId and policyMetaData.state = 0",ReturnedPolicy.class);
				policyQuery.setParameter("baseId",policy_id);
			}
		}
		
		// We have a query, so execute it.
		List<ReturnedPolicy> resultList = (List<ReturnedPolicy>) policyQuery.list();
		
		if (resultList == null || resultList.size() < 1) {
			// No results returned
			// Send back a 404 and log search failure
			// Close the session...
			sess.close();
			return CopsuUtility.locError(404, "ERR0019",LogCriticality.info);
		} else if (resultList.size() > 1 && (requestedVersion == null || ! (requestedVersion.equals("allPolicies") || requestedVersion.equals("allVersions")))) {
			// Odd -- more than one result found for a single version of the policy -- this should not be possible, but...
			// Return a 409 and log search conflict
			// Close the session...
			sess.close();
			return CopsuUtility.locError(409, "ERR0020",LogCriticality.error);
		} else {
			// Properly found exactly one result or all versions results for the allPolicies value
			// return it as JSON array
			CopsuUtility.locLog("LOG0010",LogCriticality.debug,policy_id,requestedVersion,requestedCreatorId);

			ListOfReturnedPolicy lorp = new ListOfReturnedPolicy();
			for (ReturnedPolicy l : resultList) {
				lorp.addPolicy(l);
			}
			try {
				return buildResponse(Status.OK,lorp.toJSON());
			} catch (JsonProcessingException j) {
				return CopsuUtility.locError(500, "ERR0016",LogCriticality.error);
			} finally {
				if (sess != null && sess.isOpen()) 
					sess.close();
			}
		}
	}
	
	// This behaves a bit differently than the policy request by ID (which may specify a non existent policy)
	//
	// This must always return a policy, even if it's a system-defined policy.
	// No 404 response is allowed by definition if at least a user is specified other than "allUsers".
	// Two policies are implemented in the event of a policy retrieval miss:
	//
	// We implement the creation of New RP templates as necessary to address this
	//
	@GET
	@Path("/")
	@Produces({"application/json"})
	public Response getRootByParameters(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		//
		// This is a search for policy matching a set of parameters passed in as query strings
		//
		// Required request parameters
		String requestedUser=null;   //  The user whose policy to retrieve
		String requestedRP=null;		//  The RP policy to retrieve for the user
		String requestedRH=null;		//  The RH the policy to retrieve must be for
		// Optional request parameters
		String requestedCreator=null;	// If provided, limit the policy return to policies with the specified creator ID
		String requestedState=null;		// If provided, limit the policy return to policies with the specified state
		String requestedRHType=null;		// If provided, limit the policy return to policies with the specified RH type
		
		// There is no requestedUserType since the COPSU does not implement user type selection in this method
		// There is no requestedRPType since the COPSU does not implement RP type selection in this method
		
		// We return a JSON array of returnedPolicy objects (converted from an ArrayList)
		
		// Get a configuration instance in case we need something
		@SuppressWarnings("unused")
		CopsuConfig config = null;
		try {
			config = CopsuUtility.init("getPolicy", request, headers, null);
		} catch (CopsuInitializationException e) {
			return CopsuUtility.locError(500, "ERR0004",LogCriticality.error);
		}

		// Now we are authorized
		
		// Check for required input parameters
		if (request.getParameter("user") == null || request.getParameter("user").equals("")) {
			return CopsuUtility.locError(400, "ERR0023",LogCriticality.info);
		} else {
			requestedUser = request.getParameter("user");
		}
		
		if (request.getParameter("relying-party") == null || request.getParameter("relying-party").equals("")) {
			return CopsuUtility.locError(400,"ERR0024",LogCriticality.info);
		} else {
			requestedRP = request.getParameter("relying-party");
		}
		
		if (request.getParameter("resource-holder") == null || request.getParameter("resource-holder").equals("")) {
			return CopsuUtility.locError(400, "ERR0025",LogCriticality.info);
		} else {
			requestedRH = request.getParameter("resource-holder");
		}
		
		// And gather non-required parameters
		if (request.getParameter("createdBy") != null && ! request.getParameter("createdBy").equals("")) {
			requestedCreator = request.getParameter("createdBy");
		}
		if (request.getParameter("state") != null && ! request.getParameter("state").equals("")) {
			// Check for invalid input
			ArrayList<String> validValues = new ArrayList<String>(Arrays.asList("active","inactive","all"));
			if (! validValues.contains(request.getParameter("state"))) {
				return buildResponse(Status.BAD_REQUEST,"{\"code\":\"400\",\"message\":\"'state' must be one of 'active','inactive', or 'all'\"}");
			} else {
				requestedState = request.getParameter("state");
			}
		}
		if (request.getParameter("resource-holder-type") != null && ! request.getParameter("resource-holder-type").equals("")) {
			requestedRHType = request.getParameter("resource-holder-type");
		}
		
		// Now we have parsed out parameters -- log the situation
		CopsuUtility.locLog("LOG0011",LogCriticality.debug,requestedUser,requestedRP,requestedRH);
		
		// And perform the search appropriate to the inputs
		// We return an array of JSON objects
		ListOfReturnedPolicy list = new ListOfReturnedPolicy();
		
		// Verify the driver for the Hibernate sesssion is active
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			return CopsuUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		
		// And setup the query based on input parameters
		Query<ReturnedPolicy> policyQuery = null;
		
		if (requestedState == null || requestedState.equals("active")) {
			// Just active policies
			if (requestedCreator != null && ! requestedCreator.equals("")) {
				// only specific creator, only active
				if (requestedUser.equals("allUsers")) {
					if (requestedRP.equals("allRPs")) {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.creator.creatingUserValue=:creator and policyMetaData.state=0",ReturnedPolicy.class);
						policyQuery.setParameter("rhValue", requestedRH);
						policyQuery.setParameter("creator",requestedCreator);
					} else {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.creator.creatingUserValue=:creator and policymetaData.state=0",ReturnedPolicy.class);
						policyQuery.setParameter("rpValue", requestedRP);
						policyQuery.setParameter("rhValue", requestedRH);
						policyQuery.setParameter("creator", requestedCreator);
					}
				} else if (requestedRP.equals("allRPs")) {
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.creator.cratingUserValue=:creator and policyMetaData.state=0",ReturnedPolicy.class);
					policyQuery.setParameter("userValue",requestedUser);
					policyQuery.setParameter("rhValue", requestedRH);
					policyQuery.setParameter("creator", requestedCreator);
				} else {
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.creator.creatingUserValue=:creator and policyMetaData.state=0",ReturnedPolicy.class);
					policyQuery.setParameter("userValue", requestedUser);
					policyQuery.setParameter("rpValue", requestedRP);
					policyQuery.setParameter("rhValue", requestedRH);
					policyQuery.setParameter("creator", requestedCreator);
				}
			} else {
				if (requestedUser.equals("allUsers")) {
					if (requestedRP.equals("allRPs")) {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=0",ReturnedPolicy.class);
						policyQuery.setParameter("rhValue", requestedRH);
					} else {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=0",ReturnedPolicy.class);
						policyQuery.setParameter("rpValue", requestedRP);
						policyQuery.setParameter("rhValue", requestedRH);
					}
				} else if (requestedRP.equals("allRPs")) {
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=0",ReturnedPolicy.class);
					policyQuery.setParameter("userValue", requestedUser);
					policyQuery.setParameter("rhValue", requestedRH);
				} else {
				// 	No creator restriction, only active
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=0",ReturnedPolicy.class);
					policyQuery.setParameter("userValue", requestedUser);
					policyQuery.setParameter("rpValue", requestedRP);
					policyQuery.setParameter("rhValue", requestedRH);
				}
			}
		} else if (requestedState.equals("inactive")) {
			// Just inactive policies
			if (requestedCreator != null && ! requestedCreator.equals("")) {
				// Only one creator, only inactive
				if (requestedUser.equals("allUsers")) {
					if (requestedRP.equals("allRPs")) {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=1 and policyMetaData.creator.creatingUserValue=:creator",ReturnedPolicy.class);
						policyQuery.setParameter("rhValue", requestedRH);
						policyQuery.setParameter("creator", requestedCreator);
					} else {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=1 and policyMetaData.creator.creatingUserValue=:creator",ReturnedPolicy.class);
						policyQuery.setParameter("rpValue", requestedRP);
						policyQuery.setParameter("rhValue", requestedRH);
						policyQuery.setParameter("creator", requestedCreator);
					}
				} else if (requestedRP.equals("allRPs")) {
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=1 and policyMetaData.creator.creatingUserValue=:creator",ReturnedPolicy.class);
					policyQuery.setParameter("userValue", requestedUser);
					policyQuery.setParameter("rhValue", requestedRH);
					policyQuery.setParameter("creator", requestedCreator);
				} else {
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=1 and policyMetaData.creator.creatingUserValue=:creator",ReturnedPolicy.class);
					policyQuery.setParameter("userValue",  requestedUser);
					policyQuery.setParameter("rpValue", requestedRP);
					policyQuery.setParameter("rhValue",  requestedRH);
					policyQuery.setParameter("creator",  requestedCreator);
				}
			} else {
				// only inactive
				if (requestedUser.equals("allUsers")) {
					if (requestedRP.equals("allRPs")) {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=1",ReturnedPolicy.class);
						policyQuery.setParameter("rhValue", requestedRH);
					} else {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=1", ReturnedPolicy.class);
						policyQuery.setParameter("rpValue", requestedRP);
						policyQuery.setParameter("rhValue", requestedRH);
					}
				} else if (requestedRP.equals("allRPs")) {
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=1", ReturnedPolicy.class);
					policyQuery.setParameter("userValue", requestedUser);
					policyQuery.setParameter("rhValue", requestedRH);
				} else {
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.state=1", ReturnedPolicy.class);
					policyQuery.setParameter("userValue", requestedUser);
					policyQuery.setParameter("rpValue", requestedRP);
					policyQuery.setParameter("rhValue", requestedRH);
				}
			}
		} else {
			// all policy states -- state = all
			if (requestedCreator != null && ! requestedCreator.equals("")) {
				if (requestedUser.equals("allUsers")) {
					if (requestedRP.equals("allRPs")) {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.creator.creatingUserValue=:creator", ReturnedPolicy.class);
						policyQuery.setParameter("rhValue", requestedRH);
						policyQuery.setParameter("creator", requestedCreator);
					} else {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.creator.creatingUserValue=:creator", ReturnedPolicy.class);
						policyQuery.setParameter("rpValue", requestedRP);
						policyQuery.setParameter("rhValue",requestedRH);
						policyQuery.setParameter("creator", requestedCreator);
					}
				} else if (requestedRP.equals("allRPs")) {
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.creator.creatingUserValue=:creator", ReturnedPolicy.class);
					policyQuery.setParameter("userValue",requestedUser);
					policyQuery.setParameter("rhValue", requestedRH);
					policyQuery.setParameter("creator",requestedCreator);
				} else {
					// Only one creator
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue and policyMetaData.creator.creatingUserValue=:creator", ReturnedPolicy.class);
					policyQuery.setParameter("userValue", requestedUser);
					policyQuery.setParameter("rpValue", requestedRP);
					policyQuery.setParameter("rhValue", requestedRH);
					policyQuery.setParameter("creator", requestedCreator);
				}
			} else {
				// All states, all creators
				if (requestedUser.equals("allUsers")) {
					if (requestedRP.equals("allRPs")) {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.resourceHolderId.RHValue=:rhValue", ReturnedPolicy.class);
						policyQuery.setParameter("rhValue", requestedRH);
					} else {
						policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue", ReturnedPolicy.class);
						policyQuery.setParameter("rpValue", requestedRP);
						policyQuery.setParameter("rhValue", requestedRH);
					}
				} else if (requestedRP.equals("allRPs")) {
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue", ReturnedPolicy.class);
					policyQuery.setParameter("userValue",  requestedUser);
					policyQuery.setParameter("rhValue", requestedRH);
				} else {
					policyQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.relyingPartyId.RPvalue=:rpValue and infoReleasePolicy.resourceHolderId.RHValue=:rhValue", ReturnedPolicy.class);
					policyQuery.setParameter("userValue",  requestedUser);
					policyQuery.setParameter("rpValue",  requestedRP);
					policyQuery.setParameter("rhValue",  requestedRH);
				}
			}
		}
		
		// We have a query constructed.  Execute it.
		List<ReturnedPolicy> resultList = (List<ReturnedPolicy>) policyQuery.list();
		
		if (resultList == null || resultList.size() < 1) {
			// We cannot 404 in this case -- we have to construct a policy from first principals and
			// use it to respond to the request.
			// First, check to see if the request was RP specific
			boolean isAllRPs = requestedRP.equals("allRPs");
			boolean isAllUsers = requestedUser.equals("allUsers");
			if (! isAllRPs && ! isAllUsers) {
				// If the request was RP specific
				// Check to see if the user has policies
				Query<ReturnedPolicy> userCheck = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue = :userValue",ReturnedPolicy.class);
				userCheck.setParameter("userValue", requestedUser);
				List<ReturnedPolicy> userCheckResult = (List<ReturnedPolicy>) userCheck.list();
				if (userCheckResult == null || userCheckResult.isEmpty()) {
					// Create a new user template for the user
					UserId u = new UserId();
					u.setUserType("user");  // Default user type
					u.setUserValue(requestedUser);
					NewEntityUtilities.createNewRPTemplateForUser(u);
					// And create a new RP policy from the user's new RP template
					RelyingPartyId rpi = new RelyingPartyId();
					rpi.setRPtype("relyingParty"); // default rp type
					rpi.setRPvalue(requestedRP);
					ResourceHolderId rhi = new ResourceHolderId();
					rhi.setRHType(requestedRHType==null?"resourceHolder":requestedRHType); // default to "resourceHolder"
					rhi.setRHValue(requestedRH);
					ReturnedPolicy createdPolicy = NewEntityUtilities.createNewRPPolicyFromNewRPTemplate(u,rpi,rhi);
					try {
						if (createdPolicy != null) {
							if (sess != null) {
								sess.close();
							}
							ListOfReturnedPolicy cpl = new ListOfReturnedPolicy();
							cpl.addPolicy(createdPolicy);
							return buildResponse(Status.OK, cpl.toJSON());
						} else {
							if (sess != null) {
								sess.close();
							}
							return CopsuUtility.locError(500, "ERR0026", LogCriticality.error);
						}
					} catch (Exception e) {
						if (sess != null) {
							sess.close();
						}
						return CopsuUtility.locError(500, "ERR0016", LogCriticality.error);
					}
				} else {
					// The user has policies, just not one for this RP
					Query<ReturnedPolicy> getNewRPTemplate = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue = :userValue and policyMetaData.state=0 and infoReleasePolicy.relyingPartyId.RPvalue = :rpValue", ReturnedPolicy.class);
					getNewRPTemplate.setParameter("userValue", requestedUser);
					getNewRPTemplate.setParameter("rpValue", NewRPTemplateValueConst.newRPTemplateValue.toString());
					List<ReturnedPolicy> returnedList = getNewRPTemplate.list();
					@SuppressWarnings("unused")
					ReturnedPolicy newRPTemplate = null;
					if (returnedList == null || returnedList.isEmpty()) {
						// no RP template -- create one
						UserId u = new UserId();
						u.setUserType("user");
						u.setUserValue(requestedUser);
						newRPTemplate = NewEntityUtilities.createNewRPTemplateForUser(u);
					} else {
						newRPTemplate = returnedList.get(0);  // Assume only one, or if not, use the first returned
					}
					UserId u = new UserId();
					u.setUserType("user");
					u.setUserValue(requestedUser);
					RelyingPartyId rpi = new RelyingPartyId();
					rpi.setRPtype("relyingParty");
					rpi.setRPvalue(requestedRP);
					ResourceHolderId rhi = new ResourceHolderId();
					rhi.setRHType(requestedRHType==null?"resourceHolder":requestedRHType);
					rhi.setRHValue(requestedRH);
					ReturnedPolicy rpPolicy = NewEntityUtilities.createNewRPPolicyFromNewRPTemplate(u,rpi,rhi);
					try {
						if (sess != null) {
							sess.close();
						}
						if (rpPolicy != null) {
							ListOfReturnedPolicy rpl = new ListOfReturnedPolicy();
							rpl.addPolicy(rpPolicy);
							return buildResponse(Status.OK, rpl.toJSON());
						} else {
							return CopsuUtility.locError(500, "ERR0027", LogCriticality.error);
						}
					} catch (Exception e) {
						return CopsuUtility.locError(500,"ERR0016", LogCriticality.error);
					}
				}
			} else if (!isAllUsers) {
				// Request was not RP specific and no matching policies found
				Query<ReturnedPolicy> checkUser = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue=:userValue and policyMetaData.state=0",ReturnedPolicy.class);
				checkUser.setParameter("userValue", requestedUser);
				List<ReturnedPolicy> rl = checkUser.list();
				if (rl == null || rl.isEmpty()) {
					// User has no policies
					UserId u = new UserId();
					u.setUserType("user");
					u.setUserValue(requestedUser);
					ReturnedPolicy createdTemplate = NewEntityUtilities.createNewRPTemplateForUser(u);
					try {
						if (sess != null) {
							sess.close();
						}
						if (createdTemplate != null) {
							ListOfReturnedPolicy rpl = new ListOfReturnedPolicy();
							rpl.addPolicy(createdTemplate);
							return buildResponse(Status.OK,rpl.toJSON());
						} else {
							return CopsuUtility.locError(500, "ERR0028", LogCriticality.error);
						}
					} catch (Exception e) {
						if (sess != null) {
							sess.close();
						}
						return CopsuUtility.locError(500, "ERR0016", LogCriticality.error);
					}
				} else {
					// User has policies, just not this one -- this case should be impossible.
					// Catching failed RP Template creations, though, on prior attempts is worthwhile
					// Verify that user doesn't have an RP template first, though, to be sure
					//
					Query<ReturnedPolicy> certainQuery = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue = :userValue and policyMetaData.state = 0 and infoReleasePolicy.relyingPartyId.RPvalue = :rpValue",ReturnedPolicy.class);
					certainQuery.setParameter("userValue", requestedUser);
					certainQuery.setParameter("rpValue", "newRPTemplateValue");
					List<ReturnedPolicy> testList = certainQuery.list();
					if (testList == null || testList.isEmpty()) {
						UserId u = new UserId();
						u.setUserType("user");
						u.setUserValue(requestedUser);
						ReturnedPolicy createdTemplate = NewEntityUtilities.createNewRPTemplateForUser(u);
						// Add to the list
						if (createdTemplate != null) {
							resultList.add(createdTemplate);
						}
					} else {
						resultList.add(testList.get(0));
					}
					// and return normally based on resultList
					for (ReturnedPolicy r : resultList) {
						list.addPolicy(r);
					}
					try {
						return buildResponse(Status.OK,list.toJSON());
					} catch (Exception e) {
						return CopsuUtility.locError(500, "ERR0016", LogCriticality.error);
					} finally {
						sess.close();
					}
				}
			} else {
				// If this is an allUsers request with no policies returned, 
				// we can do nothing but return a 404 to indicate that there 
				// are no policies for any users for that RP -- no new RP 
				// template can exist sans user...
				sess.close();
				return CopsuUtility.locError(404, "ERR0019", LogCriticality.info);
			}
		} else {
			// Build a response based on the JSON representation of the returned list
			for (ReturnedPolicy rp : resultList) {
				list.addPolicy(rp);
			}
			try {
				return buildResponse(Status.OK,list.toJSON());
			} catch (JsonProcessingException j) {
				return CopsuUtility.locError(500, "ERR0016", LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
}
