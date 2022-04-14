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

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

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

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.copsu.cfg.CopsuConfig;
import edu.internet2.consent.copsu.model.ChangeOrder;
import edu.internet2.consent.copsu.model.ChangeOrderMetaData;
import edu.internet2.consent.copsu.model.ListOfReturnedChangeOrder;
import edu.internet2.consent.copsu.model.LogCriticality;
import edu.internet2.consent.copsu.model.ReturnedChangeOrder;
import edu.internet2.consent.copsu.model.UserId;
import edu.internet2.consent.copsu.util.CopsuUtility;
import edu.internet2.consent.copsu.util.OMSingleton;
import edu.internet2.consent.exceptions.CopsuInitializationException;

@Path("/user-info-release-change-orders")
public class UserInfoReleaseChangeOrderController {

	@SuppressWarnings("unused")
	private String caller = "";
	//@SuppressWarnings("unused")
	//private static final Log LOG=LogFactory.getLog(UserInfoReleaseChangeOrderController.class);
	
	// Response builder
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin","http://editor.swagger.io").header("Access-Control-Allow-methods","GET, POST").header("Access-Control-Allow-Credentials","true").header("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	@GET
	@Path("/healthcheck")
	public Response healthCheck(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		// We do a simple check against the database to verify that we have 
		// DB access, and then return based on that either 200 or 500.
		
		boolean healthy = false;  // unhealthy until proven otherwise
		
		Session sess = CopsuUtility.getHibernateSession();
		
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
	
	// CORS
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	// GET a specific change order by id
	@GET
	@Path("/{change_order_id}")
	@Produces({"application/json"})
	public Response getRootById(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("change_order_id") String changeOrderId) {
		@SuppressWarnings("unused")
		CopsuConfig config = null;
		try {
			config = CopsuUtility.init("getChangeOrder", request, headers, null);
		} catch (CopsuInitializationException e) {
			return CopsuUtility.locError(500,"ERR0004");
		}
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			return CopsuUtility.locError(500, "ERR0018");
		}
		
		Query<ReturnedChangeOrder> coQuery = sess.createQuery("from ReturnedChangeOrder where changeOrderMetaData.changeOrderId = :changeId",ReturnedChangeOrder.class);
		coQuery.setParameter("changeId",changeOrderId);
		List<ReturnedChangeOrder> retlist = coQuery.list();
		
		if (retlist != null && ! retlist.isEmpty()) {
			try {
				// leak protection
				Response resp = buildResponse(Status.OK,retlist.get(0).toJSON());
				sess.close();
				return resp;
			} catch (Exception e) {
				sess.close();
				return CopsuUtility.locError(500, "ERR0044",e.getMessage());
			}
		} else {
			sess.close();
			return CopsuUtility.locError(404, "ERR0019",LogCriticality.info);
		}
	}
	
	// GET operation - search for change orders
	//
	@GET
	@Path("/")
	@Produces({"application/json"})
	public Response getRootByParameters(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// Handle GET requests for the root resource
		// These are treated as searches, with constraints AND'd in the URL arguments
		//
		// Response body contains an array of JSON serialized ReturnedChangeOrder objects
		//
		String requestedChangeOrderType = null;
		String requestedCreatedBy = null;
		String requestedCreatedAfter = null;
		String requestedCreatedBefore = null;

		@SuppressWarnings("unused")
		CopsuConfig config = null;
		try {
			config = CopsuUtility.init("getChangeOrder", request, headers, null);
		} catch (CopsuInitializationException e) {
			return CopsuUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// Now we can do the work
		
		requestedChangeOrderType = request.getParameter("changeOrderType");
		requestedCreatedBy = request.getParameter("createdBy");
		requestedCreatedAfter = request.getParameter("createdAfter");
		requestedCreatedBefore = request.getParameter("createdBefore");
		
		StringBuilder sb = new StringBuilder();
		sb.append("from ReturnedChangeOrder where changeOrderMetaData.changeOrderId is not null");  // null request
		if (requestedChangeOrderType != null && ! requestedChangeOrderType.equals("")) {
			sb.append(" and changeOrder.changeOrderType= :cotype");
		}
		if (requestedCreatedBy != null && ! requestedCreatedBy.equals("")) {
			sb.append(" and changeOrderMetaData.creator.userValue = :creator");
		}
		if (requestedCreatedBefore != null && ! requestedCreatedBefore.equals("")) {
			sb.append(" and changeOrderMetaData.createTime < :before");
		}
		if (requestedCreatedAfter != null && ! requestedCreatedAfter.equals("")) {
			sb.append(" and changeOrderMetaData.createTime > :after");
		}
		
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			return CopsuUtility.locError(500, "ERR0018");
		}
		
		Query<ReturnedChangeOrder> coQuery = sess.createQuery(sb.toString(),ReturnedChangeOrder.class);
		if (sb.toString().contains(":cotype")) {
			coQuery.setParameter("cotype", requestedChangeOrderType);
		}
		if (sb.toString().contains(":creator")) {
			coQuery.setParameter("creator", requestedCreatedBy);
		}
		if (sb.toString().contains(":before")) {
			coQuery.setParameter("before", Long.parseLong(requestedCreatedBefore));
		}
		if (sb.toString().contains(":after")) {
			coQuery.setParameter("after", Long.parseLong(requestedCreatedAfter));
		}
		
		List<ReturnedChangeOrder> returnedList = coQuery.list();
		
		ListOfReturnedChangeOrder list = new ListOfReturnedChangeOrder();
		if (returnedList == null || returnedList.isEmpty()) {
			sess.close();
			return CopsuUtility.locError(404, "ERR0019",LogCriticality.info);
		}
		for (ReturnedChangeOrder ro : returnedList) {
			list.addChangeOrder(ro);
		}
		try {
			return buildResponse(Status.OK,list.toJSON());
		} catch (JsonProcessingException j) {
			return CopsuUtility.locError(500, "ERR0016");
		} finally {
			sess.close();
		}
	}
	
	// POST operation.
	// Create a new change order and apply it to the database.
	//
	@POST
	@Path("/")
	@Produces({"application/json"})
	@Consumes({"application/json"})
	public Response postRoot(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// Handle POST requests
		@SuppressWarnings("unused")
		CopsuConfig config = null;
		try {
			config = CopsuUtility.init("postChangeOrder", request, headers, null);
		} catch (CopsuInitializationException e) {
			return CopsuUtility.locError(500,"ERR0004");
		}
		
		// Input is a JSON serialized ChangeOrder
		// We deserialize it into a ChangeOrder object, then:
		//   * store it to the database
		//   * activate it in the database
		//
		// Application of arbitrarily complex change orders may be arbitrarily expensive in large databases.
		// Judicious use is advised.
		//
		// Internally automated change orders are *not* handled via this interface -- they are handled via a separate
		// process.
		//
		// Externally automated change orders (such as those which might occur due to external triggers for policy reversion)
		// are handled via this interface.
		//
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		ChangeOrder inputChangeOrder = null;
		
		try {
			inputChangeOrder = mapper.readValue(entity, ChangeOrder.class);
		} catch (JsonParseException e) {
			return CopsuUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return CopsuUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return CopsuUtility.locError(500, "ERR0007");
		}
		
		// Otherwise, we mapped the input JSON to a change order
		
		if (inputChangeOrder == null) {
			return CopsuUtility.locError(400, "ERR0008",LogCriticality.info);
		}
		
		// And it's not empty.  Verify that it has the required fields
		
		if (inputChangeOrder.getDescription() == null) {
			return CopsuUtility.locError(400, "ERR0009",LogCriticality.info);
		}
		if (inputChangeOrder.getChangeOrderType() == null) {
			return CopsuUtility.locError(400, "ERR0038",LogCriticality.info);
		}
		if (inputChangeOrder.getUserIdArray() == null) {
			return CopsuUtility.locError(400, "ERR0010",LogCriticality.info);
		}
		if (inputChangeOrder.getRelyingPartyIdArray() == null) {
			return CopsuUtility.locError(400,"ERR0011",LogCriticality.info);
		}
		if (inputChangeOrder.getPolicyIdArray() == null) {
			return CopsuUtility.locError(400, "ERR0017",LogCriticality.info);
		}
		if (inputChangeOrder.getResourceHolderId() == null) {
			return CopsuUtility.locError(400,"ERR0012",LogCriticality.info);
		}
		
		// The rest is optional, but we must have at least one of either info release statements or all other info release statement
		if ((inputChangeOrder.getArrayOfInfoReleaseStatement()==null || inputChangeOrder.getArrayOfInfoReleaseStatement().isEmpty()) && (inputChangeOrder.getAllOtherInfoReleaseStatement() == null)) {
			return CopsuUtility.locError(400, "ERR0039",LogCriticality.info);
		}
		
		// And there is a constraint on the change order type and the list of values offered
		switch(inputChangeOrder.getChangeOrderType()) {
		case createPolicyFromNewUserConfig:
			return CopsuUtility.locError(400, "ERR0040",LogCriticality.info);
		case fixErrorInPolicy:
			if (inputChangeOrder.getUserIdArray().isEmpty() || inputChangeOrder.getUserIdArray().size() > 1 || inputChangeOrder.getRelyingPartyIdArray().isEmpty() || inputChangeOrder.getRelyingPartyIdArray().size() > 1) {
				return CopsuUtility.locError(400,"ERR0041",LogCriticality.info);
			}
			break;
		case changePoliciesAboutSpecificRP:
			// Only one RP allowed
			if (inputChangeOrder.getRelyingPartyIdArray().isEmpty() || inputChangeOrder.getRelyingPartyIdArray().size() > 1 || inputChangeOrder.getRelyingPartyIdArray().get(0).equals("allRPs")) {
				return CopsuUtility.locError(400, "ERR0042",LogCriticality.info);
			}
			break;
		}
		
		// This is a valid change order
		// construct metadata for it
		// For now, we don't have a value in the caller from the headers, so we have a default case to fall back to
		//
		ChangeOrderMetaData comd = new ChangeOrderMetaData();
		UserId defaultUser = new UserId();
		defaultUser.setUserType("user");
		defaultUser.setUserValue("anonymousAdmin");
		comd.setCreator(defaultUser);
		comd.setCreateTime(System.currentTimeMillis());
		comd.setChangeOrderId(UUID.randomUUID().toString());
		
		// And construct a new ReturnedChangeOrder for the purpose
		ReturnedChangeOrder rco = new ReturnedChangeOrder();
		rco.setChangeOrderMetaData(comd);
		rco.setChangeOrder(inputChangeOrder);
		
		// And persist the change order into the database
		// We need the change order to be present in the database before applying it
		
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			return CopsuUtility.locError(500, "ERR0018");
		}

		
		// There is no possibility of conflict, unless somehow we have overlapping randomized UUID values.
		// Query for that just in case.  We do this rather than the more expensive iteration until finding a 
		// unique UUID because failure of an administrative change at this level is less tragic than failure of 
		// a policy store operation for a user policy.
		//
		
		Query<ReturnedChangeOrder> checkQuery = sess.createQuery("from ReturnedChangeOrder where changeOrderMetaData.changeOrderId = :changeId",ReturnedChangeOrder.class);
		checkQuery.setParameter("changeId", comd.getChangeOrderId());
		List<ReturnedChangeOrder> checkList =  checkQuery.list();
		if (! checkList.isEmpty()) {
			// Collision
			sess.close();
			return CopsuUtility.locError(409, "ERR0043",LogCriticality.info);
		}
		
		// We know we have a unique change Order number now
		// Save the policy to the back-end
		//
		Transaction tx = sess.beginTransaction();
		// leak protection
		try {
		sess.save(rco);
		// Regardless of the outcome below, we save the change order -- it just may never be used anywhere
		tx.commit();
		sess.close();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException(e); // rethrow
		} finally {
			if (sess.isOpen())
				sess.close();
		}
		try {
			// call the change order's apply() method.  If it fails for any reason, roll back the change order
			// in the database.
			rco.apply();
		} catch (Exception e) {
			// If there is an exception during the application, we must roll back the transaction
			// We assume here that the apply() method in the returned change order has already rolled back any changes it made based on its exception
			// And return a failure
			return CopsuUtility.locError(500, "ERR0037", "Change order application failure: " + e.getMessage());
		}
		try {
			try {
				CopsuUtility.locLog("LOG0015","Change order " + rco.getChangeOrderMetaData().getChangeOrderId() + " applied");
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
					CopsuUtility.locDebug("LOG0015","Change order text: " + rco.toJSON());
			} catch (Exception e) {
				// ignore -- best effort logging
			}
			return buildResponse(Status.OK,rco.toJSON());
		} catch (Exception e) {
			// JSON parsing error
			return CopsuUtility.locError(500, "ERR0016");
		}
	} 
}
