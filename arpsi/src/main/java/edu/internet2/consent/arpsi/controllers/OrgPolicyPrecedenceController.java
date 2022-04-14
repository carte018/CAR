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
package edu.internet2.consent.arpsi.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.arpsi.cfg.ArpsiConfig;
import edu.internet2.consent.arpsi.model.ListOfReturnedPrecedenceObject;
import edu.internet2.consent.arpsi.model.LogCriticality;
import edu.internet2.consent.arpsi.model.OrgReturnedPolicy;
import edu.internet2.consent.arpsi.model.PATCH;
import edu.internet2.consent.arpsi.model.PrecedenceInstruction;
import edu.internet2.consent.arpsi.model.ReturnedPrecedenceObject;
import edu.internet2.consent.arpsi.util.ArpsiUtility;
import edu.internet2.consent.arpsi.util.OMSingleton;

import java.math.BigInteger;

@Path("/org-policy-precedence")
public class OrgPolicyPrecedenceController {
		@SuppressWarnings("unused")
		private String caller = "";
		//@SuppressWarnings("unused")
		//private static final Log LOG = LogFactory.getLog(OrgPolicyPrecedenceController.class);
		
		
		// Utility method for internal use only for generating responses in proper format.
		// We tack on the headers required for CORS with Swagger.io here automatically
		// We assume that the caller is setting both status code and entity, so we don't differentiate
		private Response buildResponse(Status code, String entity) {
			return Response.status(code).entity(entity).header("Access-Control-Allow-Origin", "http://editor.swagger.io").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
		}
		
		// Healthcheck support for decision controller
		@GET
		@Path("/healthcheck")
		public Response healthCheck(@Context HttpServletRequest request, @Context HttpHeaders headers) {
			// We do a simple check against the database to verify that we have 
			// DB access, and then return based on that either 200 or 500.
			
			boolean healthy = false;  // unhealthy until proven otherwise
			
			Session sess = ArpsiUtility.getHibernateSession();
			
			if (sess == null) {
				return buildResponse(Status.INTERNAL_SERVER_ERROR,"No Session");
			}
			
			long c = 0;
			
			try {
				@SuppressWarnings("rawtypes")
				Query q =  sess.createSQLQuery("select count(1) from dual");
				c =  ((BigInteger) q.uniqueResult()).longValue();
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
		
		// GET request for precedence information
		
		@GET
		@Path("/")
		public Response getRoot(@Context HttpServletRequest request, @Context HttpHeaders headers) {
			@SuppressWarnings("unused")
			ArpsiConfig config = null;
			try {
				config = ArpsiUtility.init("getPolicy", request, headers, null);
			} catch (Exception e) {
				return ArpsiUtility.locError(500,"ERR0004");
			}
		
			// Now we are authorized
			
			// Parse the request and generate the response appropriately. 
			// Three cases exist:
			//  (1) No arguments, in which case we return the entire database as a precedence list
			//  (2) rank is set to "first" and we return just the precedence=1 element in the list
			//  (3) policyIdList is set to a comma-separated list of policy baseIds we return the precedence for
			//
			
			// We implement (2), then (3), then (1)
			
			// Set up for hibernate request
			Session sess = ArpsiUtility.getHibernateSession();
			if (sess == null) {
				return ArpsiUtility.locError(500, "ERR0018");
			}

			ListOfReturnedPrecedenceObject lorp = new ListOfReturnedPrecedenceObject();
			
			// Case 2
			if (request.getParameter("rank") != null && request.getParameter("rank").equals("first")) {
				// Look for the rank=1 entry
				Query<OrgReturnedPolicy> rankQuery = sess.createQuery("from OrgReturnedPolicy where priority = 1 and policyMetaData.state = 0",OrgReturnedPolicy.class);
				List<OrgReturnedPolicy> retList = (List<OrgReturnedPolicy>) rankQuery.list();
				
				// Check for empty or > 1 response
				if (retList == null || retList.isEmpty()) {
					sess.close();
					return ArpsiUtility.locError(404,"ERR0019",LogCriticality.info);
				}
				if (retList.size() > 1) {
					sess.close();
					return ArpsiUtility.locError(409, "ERR0020",LogCriticality.info);
				}
				
				// Build a response object and return it
				ReturnedPrecedenceObject retobj = new ReturnedPrecedenceObject();
				
				
				retobj.setNumericRank(1);;
				retobj.setPolicyBaseId(retList.get(0).getPolicyMetaData().getPolicyId().getBaseId());
				retobj.setPolicyDescription(retList.get(0).getPolicy().getDescription());
				
				lorp.addObject(retobj);
				
				try {
					sess.close();
					return buildResponse(Status.OK,lorp.toJSON());
				} catch (Exception e) {
					return ArpsiUtility.locError(500, "ERR0016");
				}
			}
			
			// Case 3 -- policyIdList provided in argument
			if (request.getParameter("policyIdList") != null && ! request.getParameter("policyIdList").equals("")) {
				// Comma-separated list here
				String [] pids = request.getParameterValues("policyIdList");
				ArrayList<String> policyIds = new ArrayList<String>();
				for (String p : pids) {
					if (! p.contains(",")) {
						policyIds.add(p);
					} else {
						for (String p2 : p.split(",")) {
							policyIds.add(p2);
						}
					}
				}
				for (String policyId : policyIds) {
					Query<OrgReturnedPolicy> polQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.state = 0 and policyMetaData.policyId.baseId = :policyid",OrgReturnedPolicy.class);
					polQuery.setParameter("policyid", policyId);
					List<OrgReturnedPolicy> retList = (List<OrgReturnedPolicy>) polQuery.list();
					// Check for empty or > 1 response
					if (retList == null || retList.isEmpty()) {
						ArpsiUtility.locDebug("LOG0015",policyId);
						// This is loggable but non-fatal
						continue;
					}
					if (retList.size() > 1) {
						ArpsiUtility.locDebug("LOG0003",policyId);
						// Loggable but not fatal and we proceed
					}
				
					// Build a response object and add it
					ReturnedPrecedenceObject retobj = new ReturnedPrecedenceObject();
					
					
					retobj.setNumericRank(retList.get(0).getPriority());
					retobj.setPolicyBaseId(retList.get(0).getPolicyMetaData().getPolicyId().getBaseId());
					retobj.setPolicyDescription(retList.get(0).getPolicy().getDescription());
					
					lorp.addObject(retobj);
					
				}
				
				// Now the lorp contains the list
				// return it
				try {
					sess.close();
					return buildResponse(Status.OK,lorp.toJSON());
				} catch (Exception e) {
					return ArpsiUtility.locError(500, "ERR0016");
				}
			}
			
			// Case 1 - return all of them
			
			// Comma-separated list here
			Query<OrgReturnedPolicy> polQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.state = 0 order by priority",OrgReturnedPolicy.class);
			List<OrgReturnedPolicy> retList = (List<OrgReturnedPolicy>) polQuery.list();
			// Check for empty or > 1 response
			if (retList == null || retList.isEmpty()) {
				sess.close();
				return ArpsiUtility.locError(404, "ERR0019",LogCriticality.info);
			}
			for (OrgReturnedPolicy orp : retList) {
				// Build a response object and add it
				ReturnedPrecedenceObject retobj = new ReturnedPrecedenceObject();
				retobj.setNumericRank(orp.getPriority());
				retobj.setPolicyBaseId(orp.getPolicyMetaData().getPolicyId().getBaseId());
				retobj.setPolicyDescription(orp.getPolicy().getDescription());
				
				lorp.addObject(retobj);
			}
		
			// Now the lorp contains the list
			// return it
			try {
				sess.close();
				return buildResponse(Status.OK,lorp.toJSON());
			} catch (Exception e) {
				return ArpsiUtility.locError(500, "ERR0016");
			}
		}
		
		@SuppressWarnings("rawtypes")
		@PATCH
		@Path("/")
		public Response patchRoot(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
			// Provided a set of precedence instructions, apply them in the order provided
			// transactionally, with rollback of any failed updates.
			//
			// Reordering operations are the most expensive operations in the API, and should be used sparingly
			//
			// There are two operations:
			//     moveAfter - sets precedence(source) = precedence(target)+1 and increments all other 
			//                 precedences >= precedence(target)+1.  We allow open spaces (so no closing
			//				   decrement all precedences >= precedence(source) is required.
			//	    moveBefore - sets precedence(source) = precedence(target)-1 and increments all other 
			//					precedences >= precedence(target).
			//
			
			// Ob security and such
	
			@SuppressWarnings("unused")
			ArpsiConfig config = null;
			try {
				config = ArpsiUtility.init("patchPolicy", request, headers, null);
			} catch (Exception e) {
				return ArpsiUtility.locError(500,"ERR0004");
			}

			
			// Now we are authorized
			
			// Prepare our set of precedence instructions from the input
			//
			List<PrecedenceInstruction> piList = null;
			ListOfReturnedPrecedenceObject lorp = new ListOfReturnedPrecedenceObject();
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				piList = mapper.readValue(entity, new TypeReference<List<PrecedenceInstruction>>(){});
			} catch (JsonParseException e) {
				return ArpsiUtility.locError(400, "ERR005",LogCriticality.info);
			} catch (JsonMappingException e) {
				return ArpsiUtility.locError(400, "ERR006",LogCriticality.info);
			} catch (Exception e) {
				return ArpsiUtility.locError(500, "ERR007");
			}
			
			// Iterate over the list in a transaction
			Session sess = ArpsiUtility.getHibernateSession();
			if (sess == null) {
				return ArpsiUtility.locError(500, "ERR0018");
			}

			Transaction tx = sess.beginTransaction();
			
			for (PrecedenceInstruction pi : piList) {
				// For each precedence instruction provided
				// Retrieve the source and target objects, then update them
				String sourceId = pi.getPolicyToChange();
				String targetId = pi.getPolicy();
				
				Query<OrgReturnedPolicy> sourceQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.state = 0 and policyMetaData.policyId.baseId = :baseid",OrgReturnedPolicy.class);
				sourceQuery.setParameter("baseid",  sourceId);
				
				List<OrgReturnedPolicy> sourceList = sourceQuery.list();
				if (sourceList == null || sourceList.isEmpty()) {
					tx.rollback();
					sess.close();
					return ArpsiUtility.locError(400, "ERR0044",LogCriticality.info,sourceId);
				}
				OrgReturnedPolicy source = sourceList.get(0);
				
				Query<OrgReturnedPolicy> targetQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.state = 0 and policyMetaData.policyId.baseId = :baseid",OrgReturnedPolicy.class);
				targetQuery.setParameter("baseid", targetId);
				
				List<OrgReturnedPolicy> targetList = targetQuery.list();
				if (targetList == null || targetList.isEmpty()) {
					tx.rollback();
					sess.close();
					return ArpsiUtility.locError(400, "ERR0045",LogCriticality.info,targetId);
				}
				OrgReturnedPolicy target = targetList.get(0);
				
				// Switch over the operation
				
				boolean forward = (target.getPriority() > source.getPriority());  // true iff target later than source
				
				// Catch first for moving a onto a
				if (target.getReturnedPolicyIdentifier().equals(source.getReturnedPolicyIdentifier())) {
					tx.rollback();
					sess.close();
					return ArpsiUtility.locError(400, "ERR0046",LogCriticality.info);
				}
				
				// Grab the priorities
				long sourcePriority = source.getPriority();
				long targetPriority = target.getPriority();
				
				// And make a hole at the targetPriority.  Depending on the direction of the move, we 
				// make the hole differently, and depending on the move instruction, we make a hole above or
				// below the existing target.
				if (! forward) {  
				// this is a move up the list
				switch(pi.getOperation()) {
				case "moveAfter":
					// Perform a moveAfter operation
					//Query<OrgReturnedPolicy> afterUpdate = sess.createQuery("update OrgReturnedPolicy o set o.priority = o.priority + 1 where o.priority > :targetpriority and o.priority < :sourcepriority",OrgReturnedPolicy.class);
					Query afterUpdate = sess.createQuery("update OrgReturnedPolicy o set o.priority = o.priority + 1 where o.priority > :targetpriority and o.priority < :sourcepriority");
					afterUpdate.setParameter("targetpriority", targetPriority);
					afterUpdate.setParameter("sourcepriority",  sourcePriority);
					afterUpdate.executeUpdate();
					source.setPriority(targetPriority + 1);	// last in case of autocommit semantics
					break;
				case "moveBefore":
					// Perform a moveBefore operation
					//Query<OrgReturnedPolicy> beforeUpdate = sess.createQuery("update OrgReturnedPolicy o set o.priority = o.priority + 1 where o.priority >= :targetpriority and o.priority < :sourcepriority",OrgReturnedPolicy.class);
					Query beforeUpdate = sess.createQuery("update OrgReturnedPolicy o set o.priority = o.priority + 1 where o.priority >= :targetpriority and o.priority < :sourcepriority");
					beforeUpdate.setParameter("targetpriority", targetPriority);
					beforeUpdate.setParameter("sourcepriority",  sourcePriority);
					beforeUpdate.executeUpdate();
					source.setPriority(targetPriority);	// last in case of autocommit semantics
					break;
				default:
					// fail 
					tx.rollback();
					sess.close();
					return ArpsiUtility.locError(400, "ERR0047",LogCriticality.info,pi.getOperation());
				}
				} else {
					// this is a move to a lower level
					switch(pi.getOperation()) {
					case "moveAfter":
						// Perform a moveAfter operation
						//Query<OrgReturnedPolicy> afterUpdate = sess.createQuery("update OrgReturnedPolicy o set o.priority = o.priority - 1 where o.priority <= :targetpriority and o.priority > :sourcepriority",OrgReturnedPolicy.class);
						Query afterUpdate = sess.createQuery("update OrgReturnedPolicy o set o.priority = o.priority - 1 where o.priority <= :targetpriority and o.priority > :sourcepriority");
						afterUpdate.setParameter("targetpriority", targetPriority);
						afterUpdate.setParameter("sourcepriority",  sourcePriority);
						afterUpdate.executeUpdate();
						source.setPriority(targetPriority);	// last in case of autocommit semantics
						break;
					case "moveBefore":
						// Perform a moveBefore operation
						//Query<OrgReturnedPolicy> beforeUpdate = sess.createQuery("update OrgReturnedPolicy o set o.priority = o.priority - 1 where o.priority < :targetpriority and o.priority > :sourcepriority",OrgReturnedPolicy.class);
						Query beforeUpdate = sess.createQuery("update OrgReturnedPolicy o set o.priority = o.priority - 1 where o.priority < :targetpriority and o.priority > :sourcepriority");
						beforeUpdate.setParameter("targetpriority", targetPriority);
						beforeUpdate.setParameter("sourcepriority",  sourcePriority);
						beforeUpdate.executeUpdate();
						source.setPriority(targetPriority-1);	// last in case of autocommit semantics
						break;
					default:
						// fail 
						tx.rollback();
						sess.close();
						return ArpsiUtility.locError(400, "ERR0047",LogCriticality.info,pi.getOperation());
					}
				}
			}
			
			// If we get here, we've succeeded all the operations...
			// Commit the changes
			tx.commit();
			
			// And return the success
			// Iterate over the input requests retrieving the final state objects
			// and using them to construct the lorp
			//
			tx = sess.beginTransaction();  // start a new transaction
			for (PrecedenceInstruction pi2 : piList) {
				ReturnedPrecedenceObject r = new ReturnedPrecedenceObject();
				Query<OrgReturnedPolicy> rQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.state = 0 and policyMetaData.policyId.baseId = :changepolicy",OrgReturnedPolicy.class);
				rQuery.setParameter("changepolicy", pi2.getPolicyToChange());
				List<OrgReturnedPolicy> lr = (List<OrgReturnedPolicy>)rQuery.list();
				r.setNumericRank(lr.get(0).getPriority());
				r.setPolicyBaseId(pi2.getPolicyToChange());
				r.setPolicyDescription(lr.get(0).getPolicy().getDescription());
				lorp.addObject(r);
			}
			tx.commit();
			try {
				sess.close();
				try {
					ArpsiUtility.locLog("LOG0021","Policy Reordering Request Processed: " + lorp.toJSON());
				} catch (Exception e) {
					// ignore - log at best effort
				}
				return buildResponse(Status.OK,lorp.toJSON());
			} catch (Exception e) {
				return ArpsiUtility.locError(500, "ERR0016");
			}
		
		}
}
