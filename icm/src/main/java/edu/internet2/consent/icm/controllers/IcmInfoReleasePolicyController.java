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

import java.util.List;
import java.util.UUID;

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

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.icm.model.PolicyId;
import edu.internet2.consent.icm.cfg.IcmConfig;
import edu.internet2.consent.icm.model.IcmInfoReleasePolicy;
import edu.internet2.consent.icm.model.IcmInfoReleaseStatement;
import edu.internet2.consent.icm.model.IcmPolicyMetaData;
import edu.internet2.consent.icm.model.IcmReturnedPolicy;
import edu.internet2.consent.icm.model.ListOfIcmReturnedPolicy;
import edu.internet2.consent.icm.model.LogCriticality;
import edu.internet2.consent.icm.model.PolicyState;
import edu.internet2.consent.icm.model.RelyingPartyProperty;
import edu.internet2.consent.icm.model.SupersedingPolicyId;
import edu.internet2.consent.icm.model.UserId;
import edu.internet2.consent.icm.model.UserProperty;
import edu.internet2.consent.icm.util.IcmUtility;
import edu.internet2.consent.icm.util.OMSingleton;

@Path("/icm-info-release-policies")

public class IcmInfoReleasePolicyController {

	// Stub for caller tracking
	@SuppressWarnings("unused")
	private String caller = "";
	
	//@SuppressWarnings("unused")
	//private static final Log LOG = LogFactory.getLog(IcmInfoReleasePolicyController.class);
	
	// Utility method for internal use only for generating responses in proper format.
	// We tack on the headers required for CORS with Swagger.io here automatically
	// We assume that the caller is setting both status code and entity, so we don't differentiate
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin", "http://editor.swagger.io").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	@GET
	@Path("/healthcheck")
	public Response healthCheck(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		// We do a simple check against the database to verify that we have 
		// DB access, and then return based on that either 200 or 500.
		
		boolean healthy = false;  // unhealthy until proven otherwise
		
		Session sess = IcmUtility.getHibernateSession();
		
		if (sess == null) {
			return buildResponse(Status.INTERNAL_SERVER_ERROR,"No Session");
		}
		
		long c = 0;
		
		try {
			@SuppressWarnings("rawtypes")
			Query q =  sess.createSQLQuery("select 1 from dual");
			c =  ((Integer) q.uniqueResult()).longValue();
			if (c == 1) {
				healthy = true;
			}
		} catch (Exception e) {
			// ignore
			return buildResponse(Status.INTERNAL_SERVER_ERROR,"Exception: " + e.getMessage());
		} finally {
			if (sess != null) 
				sess.close();
		}
		
		if (healthy) 
			return buildResponse(Status.OK,"");
		else
			return buildResponse(Status.INTERNAL_SERVER_ERROR,"Check returned " + c);
		
	}
	
	
	// OPTIONS responder for CORS compliance for the "/" pathed endpoints
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@OPTIONS
	@Path("/{policy_id}")
	public Response optionsId(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	// Root POST handler -- creating new ICM policy (or returns a conflict if the policy already eixsts)
	// Similar to the ARPSI POST handler
	// Policy is constructed from input JSON then persisted in the data store via Hibernate
	
	@POST
	@Path("/")
	@Produces({"application/json"})
	@Consumes({"application/json"})
	public Response postRoot(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		
		// Like ARPSI policies, ICM policies are always added to the end of the priority queue and can be moved later.
		// Ordering storage means that we first save and then retrieve the object in order to get the proper 
		// ordering value, then handle insertion at the tail fo the list.
		
		// First, handle authorization
		@SuppressWarnings("unused")
		IcmConfig config = null;
		try {
			config = IcmUtility.init("postPolicy", request, headers, null);
		} catch (Exception e) {
			return IcmUtility.locError(500,"ERR0004");
		}

		// Map the input object into an icm policy
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		IcmInfoReleasePolicy inputPolicy = null;
		
		try {
			inputPolicy = mapper.readValue(entity, IcmInfoReleasePolicy.class);
		} catch (JsonParseException e) {
			return IcmUtility.locError(400, "ERR0005", LogCriticality.info);
		} catch (JsonMappingException e) {
			return IcmUtility.locError(400,"ERR0006", LogCriticality.info);
		} catch (Exception e) {
			return IcmUtility.locError(400, "ERR0007", LogCriticality.info);
		}
		
		// At this point, we have an input policy object to work with
		
		if (inputPolicy == null) {
			return IcmUtility.locError(400, "ERR0008", LogCriticality.info);
		}
		
		// Verify required fields in input policy
		
		if (inputPolicy.getDescription() == null || inputPolicy.getDescription().equals("")) {
			return IcmUtility.locError(400, "ERR0009", LogCriticality.info);
		}
		
		if (inputPolicy.getUserPropertyArray() == null || inputPolicy.getUserPropertyArray().isEmpty()) {
			return IcmUtility.locError(400, "ERR0048", LogCriticality.info);
		}
		
		if (inputPolicy.getRelyingPartyPropertyArray() == null || inputPolicy.getRelyingPartyPropertyArray().isEmpty()) {
			return IcmUtility.locError(400, "ERR0049", LogCriticality.info);
		}
		
		if (inputPolicy.getResourceHolderId() == null) {
			return IcmUtility.locError(400, "ERR0051", LogCriticality.info);
		}
		
		// One or both of an info release statement and/or an all other info statement is required here
		if ((inputPolicy.getArrayOfInfoReleaseStatement() == null || inputPolicy.getArrayOfInfoReleaseStatement().isEmpty()) && (inputPolicy.getAllOtherOrgInfoReleaseStatement() == null)) {
			return IcmUtility.locError(400, "ERR0050", LogCriticality.info);
		}
		
		// ICM policy, like ARPSI policy, is exclusively by fiat, so no need to create a separate policy.

		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) { 
			return IcmUtility.locError(500, "ERR0018");
		}
		
		// No duplicate check necessary in the ICM ,since like the ARPSI, it is legal for the ICM to carry the 
		// same policy in two locations within the hierarchy.
		
		// Build a new metadata for this policy
		
		IcmPolicyMetaData ipm = new IcmPolicyMetaData();
		
		// Create time gets to be now
		ipm.setCreateTime(System.currentTimeMillis());
		
		// Set Creator.  For now, we have no logged in user mechanism so we default to SYSTEM/ICM
		// TODO:  This needs to change when we get an authorization model in place.
		UserId creator = new UserId();
		creator.setUserType("SYSTEM");
		creator.setUserValue("ICM");
		ipm.setCreator(creator);
		
		// Set policy state to active
		ipm.setState(PolicyState.active);
		
		// Create a new policy id, with collision checking
		PolicyId pid = new PolicyId();
		pid.setVersion("1");  // new policies are always version 1
		String unusedUUID = null;
		while (unusedUUID == null) {
			Query<IcmReturnedPolicy> collisionCheckQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :checkval",IcmReturnedPolicy.class);
			unusedUUID = UUID.randomUUID().toString();
			collisionCheckQuery.setParameter("checkval", unusedUUID);
			List<IcmReturnedPolicy> irl = collisionCheckQuery.list();
			if (irl != null && ! irl.isEmpty()) {
				// Collision
				unusedUUID = null;
			}
		}
		
		//Set it into the pid
		pid.setBaseId(unusedUUID);
		ipm.setPolicyId(pid);
		
		// Prepare a storeable entity from the metadata and input policy
		IcmReturnedPolicy irp = new IcmReturnedPolicy();
		irp.setPolicy(inputPolicy);
		irp.setPolicyMetaData(ipm);
		
		// Now, acquire a priority for it by incrementing the max priority in the database
		// Transaction to avoid collisions
		
		Query<Long> maxQuery = sess.createQuery("select i.priority from IcmReturnedPolicy i where i.priority = (select max(ii.priority) from IcmReturnedPolicy ii where policyMetaData.state = 0)",Long.class);
		List<Long> li = maxQuery.list();
		long maxPriority = 0;
		if (li != null && ! li.isEmpty()) {
			maxPriority = li.get(0).longValue();
		}
		
		// At this point, maxPriority should be the maximum stored priority in the DB
		irp.setPriority(maxPriority + 1);
		Transaction tx = sess.beginTransaction();
		sess.save(irp);
		tx.commit();
		sess.close();
		
		try {
			try {
				IcmUtility.locLog("ERR1137","Created ICM policy: " + irp.getPolicyMetaData().getPolicyId().getBaseId() + ", version " + irp.getPolicyMetaData().getPolicyId().getVersion());
			} catch (Exception e) {
				// ignore - best effort logging
			}
			return buildResponse(Status.CREATED,irp.toJSON());
		} catch (JsonProcessingException e) {
			return IcmUtility.locError(500,"ERR0016");
		}
	}
	
	// GET for a single policy by baseId
	
	@GET
	@Path("/{policy_id}")
	@Produces({"application/json"})
	public Response getRootById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id) {

		
		// Get a specific Icm policy by baseId value
		@SuppressWarnings("unused")
		IcmConfig config = null;
		try {
			config = IcmUtility.init("getPolicy", request, headers, null);
		} catch (Exception e) {
			return IcmUtility.locError(500,"ERR0004");
		}


		// Now that we're authorized
		
		String requestedCreatorId = null;
		String requestedVersion = null;
		
		if (policy_id == null || policy_id.equals("")) {
			// Fail
			return IcmUtility.locError(500, "ERR0008");
		}
		
		// Check for request parameters
		
		requestedCreatorId = request.getParameter("createdBy");
		requestedVersion = request.getParameter("policyVersion");
		
		// Hibernate

		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018");
		}
		
		// Determine the prior policy version number in case it's needed
		
		if (requestedVersion != null && (requestedVersion.equals("priorPolicy") || requestedVersion.equals("priorVersion"))) {
			Query<IcmReturnedPolicy> priorQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.state = 0",IcmReturnedPolicy.class);
			priorQuery.setParameter("baseid", policy_id);
			List<IcmReturnedPolicy> curList = (List<IcmReturnedPolicy>) priorQuery.list();
			if (curList != null && ! curList.isEmpty()) {
				requestedVersion = String.valueOf(Integer.parseInt(curList.get(0).getPolicyMetaData().getPolicyId().getVersion()) - 1);
			}
		}
		
		// Set up the query
		Query<IcmReturnedPolicy> policyQuery = null;
		
		if (requestedCreatorId != null) {
			if (requestedVersion != null) {
				if (requestedVersion.equals("allPolicies") || requestedVersion.equals("allVersions")) {
					policyQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.creator.userValue = :creator",IcmReturnedPolicy.class);
					policyQuery.setParameter("baseid", policy_id);
					policyQuery.setParameter("creator", requestedCreatorId);
				} else {
					policyQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.policyId.version = :version and policyMetaData.creator.userValue = :creator",IcmReturnedPolicy.class);
					policyQuery.setParameter("baseid", policy_id);
					policyQuery.setParameter("version",  requestedVersion);
					policyQuery.setParameter("creator",requestedCreatorId);
				}
			} else {
				policyQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.state = 0 and policyMetaData.creator.userValue = :creator",IcmReturnedPolicy.class);
				policyQuery.setParameter("baseid", policy_id);
				policyQuery.setParameter("creator",requestedCreatorId);
			}
		} else {
			if (requestedVersion != null) {
				if (requestedVersion.equals("allPolicies") || requestedVersion.equals("allVersions")) {
					policyQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :baseid",IcmReturnedPolicy.class);
					policyQuery.setParameter("baseid", policy_id);
				} else {
					policyQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.policyId.version = :version",IcmReturnedPolicy.class);
					policyQuery.setParameter("baseid", policy_id);
					policyQuery.setParameter("version",  requestedVersion);
				}
			} else {
				policyQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.state = 0",IcmReturnedPolicy.class);
				policyQuery.setParameter("baseid",  policy_id);
			}
		}
		
		// Execute the query
		List<IcmReturnedPolicy> resultList = (List<IcmReturnedPolicy>) policyQuery.list();
		
		if (resultList == null || resultList.size() < 1) {
			// return 404
			sess.close();
			return IcmUtility.locError(404, "ERR0019", LogCriticality.info);
		} else if (resultList.size() > 1 && (requestedVersion == null || ! (requestedVersion.equals("allPolicies") || requestedVersion.equals("allVersions")))) {
			sess.close();
			return IcmUtility.locError(409, "ERR0020", LogCriticality.info);
		} else {
			try {
				ListOfIcmReturnedPolicy lirp = new ListOfIcmReturnedPolicy();
				for (IcmReturnedPolicy i : resultList) {
					lirp.addPolicy(i);
				}
				IcmUtility.locLog("ERR1137","Returning ICM Policy: " + policy_id);
				return buildResponse(Status.OK,lirp.toJSON());
			} catch (JsonProcessingException j) {
				return IcmUtility.locError(500, "ERR0016");
			} finally {
				if (sess != null) 
					sess.close();
			}
		}
	}
	
	@GET
	@Path("/")
	@Produces({"application/json"})
	@Consumes({"application/json"})
	public Response getRoot(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		
		// policy search endpoint
		
		String requestedUserType = null;
		String requestedUser = null;
		String requestedRPType = null;
		String requestedRP = null;
		
				
		// Default to "ignore" (which is equivalent to "all" in this case)
		
		String requestedInfoItem = null;
		String requestedInfoItemType = null;
		
		// Get configuration
		@SuppressWarnings("unused")
		IcmConfig config = null;
		try {
			config = IcmUtility.init("getPolicy", request, headers, null);
		} catch (Exception e) {
			return IcmUtility.locError(500,"ERR0004");
		}


		// Authorized now
		
		ListOfIcmReturnedPolicy retval = new ListOfIcmReturnedPolicy();
		
		// Hibernate
		
		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018");
		}
		
		sess.setHibernateFlushMode(FlushMode.MANUAL);   // we are read only
		
		// this is the same trickiness that ensues with the ARPSI based on policy interpretation.
		
		StringBuilder queryBuilder = new StringBuilder();
		
		// Start with a base query taking state into consideration
		
		if (request.getParameter("state") != null && request.getParameter("state").equalsIgnoreCase("all")) {
			queryBuilder.append("from IcmReturnedPolicy ");
		} else if (request.getParameter("state") != null && request.getParameter("state").equalsIgnoreCase("inactive")) {
			queryBuilder.append("from IcmReturnedPolicy where policyMetaData.state = 1 "); // inactive
		} else {
			queryBuilder.append("from IcmReturnedPolicy where policyMetaData.state = 0 ");
		}
				
		// Take into consideration requested RH
		if (request.getParameter("resource-holder") == null || request.getParameter("resource-holder-type") == null) {
			sess.close();
			return IcmUtility.locError(400, "ERR0025",LogCriticality.info);
		}
		queryBuilder.append("AND policy.resourceHolderId.RHValue = :rhvalue AND policy.resourceHolderId.RHType = :rhtype");
		boolean createdByLimit = false;
		if (request.getParameter("createdBy") != null && ! request.getParameter("createdBy").equals("")) {
			queryBuilder.append(" AND policyMetaData.creator.userValue = :creator");
		}
		String queryString = queryBuilder.toString();
		
		Query<IcmReturnedPolicy> searchQuery = sess.createQuery(queryString,IcmReturnedPolicy.class);
		searchQuery.setParameter("rhvalue",  request.getParameter("resource-holder"));
		searchQuery.setParameter("rhtype", request.getParameter("resource-holder-type"));
		if (createdByLimit) {
			searchQuery.setParameter("creator",  request.getParameter("createdBy"));
		}
		
		// and retrieve the list of possible matches
		
		
		List<IcmReturnedPolicy> resultList = null;
		
		try {
			resultList = (List<IcmReturnedPolicy>) searchQuery.list();
		} catch (Exception e) {
			sess.close();
			return IcmUtility.locError(500, "ERR0052");
		} 
		
		
		// With the (possibly long) list of possible matches, sieve the results
		
		if (request.getParameter("user") != null && request.getParameter("user-type") != null && ! request.getParameter("user").equals("") && ! request.getParameter("user-type").equals("")) {
			requestedUser = request.getParameter("user");
			requestedUserType = request.getParameter("user-type");
		}
		if (request.getParameter("relying-party") != null & request.getParameter("relying-party-type") != null && ! request.getParameter("relying-party").equals("") && ! request.getParameter("relying-party-type").equals("")) {
			requestedRP = request.getParameter("relying-party");
			requestedRPType = request.getParameter("relying-party-type");
		}
		if (request.getParameter("info-item") != null && request.getParameter("info-item-type") != null && ! request.getParameter("info-item").equals("") && ! request.getParameter("info-item-type").equals("")) {
			requestedInfoItem = request.getParameter("info-item");
			requestedInfoItemType = request.getParameter("info-item-type");
		}
		
		for (IcmReturnedPolicy irp : resultList) {
			boolean userMatch = (requestedUser == null);
			boolean rpMatch = (requestedRP == null);
			boolean iiMatch = (requestedInfoItem == null);
			
			if (! userMatch) {
				for (UserProperty up : irp.getPolicy().getUserPropertyArray()) {
					if (requestedUserType.equalsIgnoreCase(up.getUserPropName()) && requestedUser.matches(up.getUserPropValue()))
						userMatch = true;
				}
			}
			if (! rpMatch) {
				for (RelyingPartyProperty rp : irp.getPolicy().getRelyingPartyPropertyArray()) {
					if (requestedRPType.equalsIgnoreCase(rp.getRpPropName()) && requestedRP.matches(rp.getRpPropValue())) 
						rpMatch = true;
				}
			}
			if (! iiMatch) {
				for (IcmInfoReleaseStatement iirs : irp.getPolicy().getArrayOfInfoReleaseStatement()) {
					if (requestedInfoItemType.equalsIgnoreCase(iirs.getInfoId().getInfoType()) && requestedInfoItem.matches(iirs.getInfoId().getInfoValue()))
						iiMatch = true;
				}
			}
			
			if (iiMatch && rpMatch && userMatch) {
				retval.addPolicy(irp);;
			}
		}
		
		
		if (retval.getContained().isEmpty()) {
			// 404
			sess.close();
			return IcmUtility.locError(404, "ERR0019",LogCriticality.info);
		} else {
			// 200 result
			try {
				try {
					IcmUtility.locDebug("ERR1137","Returning " + retval.getContained().size() + " ICM policies from search request");
				} catch (Exception e) {
					// ignore - best effort logging
				}
				return buildResponse(Status.OK,retval.toJSON());
			} catch (Exception e) {
				return IcmUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
	}
	
	@PUT
	@Path("/{policy_id}")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response putById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id, String entity) {
		// Replace a policy with a new version (which may substantially different)
		
		// Semantics say that PUTing a new policy to replace an old results in the old policy being deactivated and the new
		// policy receiving a +1 version with the same policy baseId as before.

		@SuppressWarnings("unused")
		IcmConfig config = null;
		try {
			config = IcmUtility.init("putPolicy", request, headers, null);
		} catch (Exception e) {
			return IcmUtility.locError(500,"ERR0004");
		}
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		IcmInfoReleasePolicy inputPolicy = null;
		
		try {
			inputPolicy = mapper.readValue(entity,  IcmInfoReleasePolicy.class);
		} catch (JsonParseException j) {
			return IcmUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException j) {
			return IcmUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0007");
		}
		
		// Now we have the input policy
		
		// Verify that the RH matches the RH of the target policy

		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018");
		}
		
		// Start a transaction
		Transaction tx = sess.beginTransaction();
	
		Query<IcmReturnedPolicy> uQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.state = 0",IcmReturnedPolicy.class);
		uQuery.setParameter("baseid",  policy_id);
		
		List<IcmReturnedPolicy> originals = (List<IcmReturnedPolicy>) uQuery.list();
		
		if (originals == null || originals.isEmpty()) {
			tx.rollback();
			sess.close();
			return IcmUtility.locError(404, "ERR0019",LogCriticality.info);
		}
		if (originals.size() > 1) {
			tx.rollback();
			sess.close();
			return IcmUtility.locError(409, "ERR0020",LogCriticality.info);
		}
		
		IcmReturnedPolicy original = originals.get(0);
		
		if (inputPolicy.getResourceHolderId() != null && inputPolicy.getResourceHolderId().getRHValue() != null && ! inputPolicy.getResourceHolderId().getRHValue().equals(original.getPolicy().getResourceHolderId().getRHValue())) {
			tx.rollback();
			sess.close();
			return IcmUtility.locError(400, "ERR0021",LogCriticality.info,"ResourceHolderId");
		}
		
		//ObjectMapper copier = new ObjectMapper();
		ObjectMapper copier = OMSingleton.getInstance().getOm();
		
		IcmReturnedPolicy newPolicy = new IcmReturnedPolicy();
		
		try {
			String serialized = original.toJSON();
			newPolicy = (IcmReturnedPolicy) copier.readValue(serialized.getBytes(),  IcmReturnedPolicy.class);
		} catch (Exception e) {
			tx.rollback();
			sess.close();
			return IcmUtility.locError(500,"ERR0022");
		}
		
		// Update createTime
		newPolicy.getPolicyMetaData().setCreateTime(System.currentTimeMillis());
		
		// Increment version number
		newPolicy.getPolicyMetaData().getPolicyId().setVersion(String.valueOf(Integer.parseInt(newPolicy.getPolicyMetaData().getPolicyId().getVersion()) + 1));
		
		// State is active
		newPolicy.getPolicyMetaData().setState(PolicyState.active);
		
		// Clear supersededBy value
		newPolicy.getPolicyMetaData().setSupersededBy(null);
		
		// Insert
		
		newPolicy.setPolicy(inputPolicy);
		
		// mark the old as inactive
		original.getPolicyMetaData().setState(PolicyState.inactive);
		
		// And note the successor
		SupersedingPolicyId spi = new SupersedingPolicyId();
		spi.setSupersedingId(newPolicy.getPolicyMetaData().getPolicyId().getBaseId());
		spi.setSupersedingVersion(newPolicy.getPolicyMetaData().getPolicyId().getVersion());
		original.getPolicyMetaData().setSupersededBy(spi);
		
		// And copy precedence
		newPolicy.setPriority(original.getPriority());
		
		// And save the new
		sess.save(newPolicy);
		tx.commit();
				
		try {
			try {
				IcmUtility.locLog("ERR1137","Updated ICM policy: " + policy_id);
			} catch (Exception e) {
				// ignore - best effort log
			}
			return buildResponse(Status.OK,newPolicy.toJSON());
		} catch (JsonProcessingException e) {
			return IcmUtility.locError(500,"ERR0016");
		} finally {
			sess.close();
		}
	}
	@DELETE
	@Path("/{policy_id}")
	@Produces({"application/json"})
	public Response deleteById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id) {
	
		// Delete a policy (which in our semantics means deactive its currently-active version)
		@SuppressWarnings("unused")
		IcmConfig config = null;
		try {
			config = IcmUtility.init("deletePolicy", request, headers, null);
		} catch (Exception e) {
			return IcmUtility.locError(500,"ERR0004");
		}
		

		// Authorization verified.
		if (policy_id == null || policy_id.equals("")) {
			return IcmUtility.locError(400,"ERR0017",LogCriticality.info);
		}
		
		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018");
		}
		Transaction tx = sess.beginTransaction();
		
		Query<IcmReturnedPolicy> policyQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.state = 0",IcmReturnedPolicy.class);
		policyQuery.setParameter("baseid", policy_id);
		List<IcmReturnedPolicy> resultList = (List<IcmReturnedPolicy>) policyQuery.list();
		if (resultList == null || resultList.isEmpty()) {
			tx.rollback();
			sess.close();
			return IcmUtility.locError(404, "ERR0019",LogCriticality.info);
		} else if (resultList.size() > 1) {
			tx.rollback();
			sess.close();
			return IcmUtility.locError(409, "ERR0020",LogCriticality.info);
		} else {
			// delete it
			if (request.getParameter("expungeOnDelete") != null && request.getParameter("expungeOnDelete").equals("true")) {
				sess.delete(resultList.get(0));
				tx.commit();
				sess.close();
				return buildResponse(Status.NO_CONTENT,"");
			}
			resultList.get(0).getPolicyMetaData().setState(PolicyState.inactive);
			tx.commit();
			sess.close();
			IcmUtility.locLog("ERR1137","Deleted ICM policy: " + policy_id);
			return buildResponse(Status.NO_CONTENT,"");
		}
	}
}
