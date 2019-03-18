package edu.internet2.consent.icm.controllers;

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

import edu.internet2.consent.icm.model.IcmReturnedPolicy;
import edu.internet2.consent.icm.model.ListOfReturnedPrecedenceObject;
import edu.internet2.consent.icm.model.LogCriticality;
import edu.internet2.consent.icm.model.PATCH;
import edu.internet2.consent.icm.model.PrecedenceInstruction;
import edu.internet2.consent.icm.model.ReturnedPrecedenceObject;
import edu.internet2.consent.icm.util.IcmUtility;
import edu.internet2.consent.icm.cfg.IcmConfig;

@Path("/icm-policy-precedence")
public class IcmPolicyPrecedenceController {

	String caller = "";
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(IcmPolicyPrecedenceController.class);
	
	// Utility method for internal use only for generating responses in proper format.
	// We tack on the headers required for CORS with Swagger.io here automatically
	// We assume that the caller is setting both status code and entity, so we don't differentiate
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin", "http://editor.swagger.io").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	// OPTIONS responder for CORS compliance for the "/" pathed endpoints
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}

	@GET
	@Path("/")
	public Response getRoot(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		@SuppressWarnings("unused")
		IcmConfig config = null;
		try {
			config = IcmUtility.init("getPrecedence", request, headers, null);
		} catch (Exception e) {
			return IcmUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Now we are authorized
		
		// Parse request and respond accordingly
		//
		// Three possible scenarios:
		// 		No arguments, so we return the entire database as a precedence list
		//		Rank is set to "first" so we just return the single priority = 1 item
		//		policyIdList is set to comma-separated list of baseids and we return precedence for just those
		//
		
		// Hibernate
		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018", LogCriticality.error);
		}
		
		ListOfReturnedPrecedenceObject lorp = new ListOfReturnedPrecedenceObject();
		
		// Degenerate case
		if (request.getParameter("rank") != null && request.getParameter("rank").equals("first")) {
			Query<IcmReturnedPolicy> rankQuery = sess.createQuery("from IcmReturnedPolicy where priority = 1 and policyMetaData.state = 0",IcmReturnedPolicy.class);
			List<IcmReturnedPolicy> retList = (List<IcmReturnedPolicy>) rankQuery.list();
			
			if (retList == null || retList.isEmpty()) {
				sess.close();
				return IcmUtility.locError(404, "ERR0019", LogCriticality.info);
			}
			if (retList.size() > 1) {
				sess.close();
				return IcmUtility.locError(409, "ERR0020", LogCriticality.info);
			}
			
			ReturnedPrecedenceObject retobj = new ReturnedPrecedenceObject();
			
			retobj.setNumericRank(1);
			retobj.setPolicyBaseId(retList.get(0).getPolicyMetaData().getPolicyId().getBaseId());
			retobj.setPolicyDescription(retList.get(0).getPolicy().getDescription());
			
			lorp.addObject(retobj);
			
			try {
				sess.close();
				return buildResponse(Status.OK,lorp.toJSON());
			} catch (Exception e) {
				return IcmUtility.locError(500, "ERR0016", LogCriticality.error);
			}
		}
		
		// policyIdList provided
		
		if (request.getParameter("policyIdList") != null && ! request.getParameter("policyIdList").equals("")) {
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
				Query<IcmReturnedPolicy> polQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.state = 0 and policyMetaData.policyId.baseId = :baseid",IcmReturnedPolicy.class);
				polQuery .setParameter("baseid", policyId);
				List<IcmReturnedPolicy> retList = (List<IcmReturnedPolicy>) polQuery.list();
				if (retList == null || retList.isEmpty()) {
					// non-terminal search failure
					continue;
				}
				if (retList.size() > 1) {
					// do nothing now -- later, possibly log or...
				}
				
				ReturnedPrecedenceObject retobj = new ReturnedPrecedenceObject();
				
				retobj.setNumericRank(retList.get(0).getPriority());
				retobj.setPolicyBaseId(retList.get(0).getPolicyMetaData().getPolicyId().getBaseId());
				retobj.setPolicyDescription(retList.get(0).getPolicy().getDescription());
				
				lorp.addObject(retobj);
			}
			try {
				sess.close();
				return buildResponse(Status.OK,lorp.toJSON());
			} catch (Exception e) {
				return IcmUtility.locError(500, "ERR0016", LogCriticality.error);
			}
		}
		
		// Case where we just return them all
		Query<IcmReturnedPolicy> polQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.state = 0 order by priority",IcmReturnedPolicy.class);
		List<IcmReturnedPolicy> retList = (List<IcmReturnedPolicy>) polQuery.list();
		
		if (retList == null || retList.isEmpty()) {
			sess.close();
			return IcmUtility.locError(404, "ERR0019", LogCriticality.error);
		}
		
		for (IcmReturnedPolicy irp : retList) {
			ReturnedPrecedenceObject retobj = new ReturnedPrecedenceObject();
			retobj.setNumericRank(irp.getPriority());
			retobj.setPolicyBaseId(irp.getPolicyMetaData().getPolicyId().getBaseId());
			retobj.setPolicyDescription(irp.getPolicy().getDescription());
			
			lorp.addObject(retobj);
			
		}
		
		try {
			sess.close();
			return buildResponse(Status.OK,lorp.toJSON());
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0016", LogCriticality.error);
		}
	}
	
	@PATCH
	@Path("/")
	public Response patchRoot(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		
		// Given a set of precedence instructions, apply them in order with rollback
		// Reordering is an expensive operation, and should not be performed lightly.
		//
		@SuppressWarnings("unused")
		IcmConfig config = null;
		try {
			config = IcmUtility.init("patchPrecedence", request, headers, null);
		} catch (Exception e) {
			return IcmUtility.locError(500,"ERR0004", LogCriticality.error);
		}		
		
		List<PrecedenceInstruction>piList = null;
		ListOfReturnedPrecedenceObject lorp = new ListOfReturnedPrecedenceObject();
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			piList = mapper.readValue(entity,  new TypeReference<List<PrecedenceInstruction>>(){});
		} catch (JsonParseException e) {
			return IcmUtility.locError(400, "ERR0005", LogCriticality.info);
		} catch (JsonMappingException e) {
			return IcmUtility.locError(400,"ERR0006", LogCriticality.info);
		} catch (Exception e) {
			return IcmUtility.locError(400, "ERR0007", LogCriticality.info);
		}
		
		// Hibernate

		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		Transaction tx = (Transaction) sess.beginTransaction();
		
		for (PrecedenceInstruction pi : piList) {
			String sourceId = pi.getPolicyToChange();
			String targetId = pi.getPolicy();
			
			Query<IcmReturnedPolicy> sourceQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.state = 0 and policyMetaData.policyId.baseId = :baseid",IcmReturnedPolicy.class);
			sourceQuery.setParameter("baseid",  sourceId);
			
			List<IcmReturnedPolicy> sourceList = sourceQuery.list();
			
			if (sourceList == null || sourceList.isEmpty()) {
				tx.rollback();
				sess.close();
				return IcmUtility.locError(400, "ERR0044",LogCriticality.info,sourceId);
			}
			
			IcmReturnedPolicy source = sourceList.get(0);
			
			Query<IcmReturnedPolicy> targetQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.state = 0 and policyMetaData.policyId.baseId = :baseid",IcmReturnedPolicy.class);
			targetQuery.setParameter("baseid",  targetId);
			
			List<IcmReturnedPolicy> targetList = targetQuery.list();
			if (targetList == null || targetList.isEmpty()) {
				tx.rollback();
				sess.close();
				return IcmUtility.locError(400, "ERR0045",LogCriticality.info,targetId);
			}
			
			IcmReturnedPolicy target = targetList.get(0);
			
			boolean forward = (target.getPriority() > source.getPriority());  // directionality
			
			if (target.getReturnedPolicyIdentifier().equals(source.getReturnedPolicyIdentifier())) {
				tx.rollback();
				sess.close();
				return IcmUtility.locError(400, "ERR0046", LogCriticality.info);
			}
			
			long sourcePriority = source.getPriority();
			long targetPriority = target.getPriority();
			
			// We make  holes differently depending on directionality detected above
			
			if (! forward) {
				switch(pi.getOperation()) {
				case "moveAfter":
					Query<IcmReturnedPolicy> afterUpdate = sess.createQuery("update IcmReturnedPolicy o set o.priority = o.priority + 1 where o.priority > :targetp and o.priority < :sourcep",IcmReturnedPolicy.class);
					afterUpdate.setParameter("targetp",targetPriority);
					afterUpdate.setParameter("sourcep",  sourcePriority);
					afterUpdate.executeUpdate();
					source.setPriority(targetPriority + 1);
					break;
				case "moveBefore":
					Query<IcmReturnedPolicy> beforeUpdate = sess.createQuery("update IcmReturnedPolicy o set o.priority = o.priority + 1 where o.priority >= :targetp and o.priority < :sourcep",IcmReturnedPolicy.class);
					beforeUpdate.setParameter("targetp",  targetPriority);
					beforeUpdate.setParameter("sourcep",  sourcePriority);
					beforeUpdate.executeUpdate();
					source.setPriority(targetPriority);
					break;
				default:
					tx.rollback();
					sess.close();
					return IcmUtility.locError(400, "ERR0047", LogCriticality.info, pi.getOperation());
				}
			} else {
				// other direction
				switch(pi.getOperation()) {
				case "moveAfter":
					Query<IcmReturnedPolicy> afterUpdate = sess.createQuery("update IcmReturnedPolicy o set o.priority = o.priority - 1 where o.priority <= :targetp and o.priority > :sourcep",IcmReturnedPolicy.class);
					afterUpdate.setParameter("targetp",  targetPriority);
					afterUpdate.setParameter("sourcep",  sourcePriority);
					afterUpdate.executeUpdate();
					source.setPriority(targetPriority);
					break;
				case "moveBefore":
					Query<IcmReturnedPolicy> beforeUpdate = sess.createQuery("update IcmReturnedPolicy o set o.priority = o.priority - 1 where o.priority < :targetp and o.priority > :sourcep",IcmReturnedPolicy.class);
					beforeUpdate.setParameter("targetp", targetPriority);
					beforeUpdate.setParameter("sourcep", sourcePriority);
					beforeUpdate.executeUpdate();
					source.setPriority(targetPriority-1);
					break;
				default:
					tx.rollback();
					sess.close();
					return IcmUtility.locError(400, "ERR0047", LogCriticality.info, pi.getOperation());
				}
			}
		}
		
		// Now commit
		tx.commit();
		
		// And return
		
		tx = (Transaction) sess.beginTransaction();
		for (PrecedenceInstruction pi2 : piList) {
			ReturnedPrecedenceObject r = new ReturnedPrecedenceObject();
			Query<IcmReturnedPolicy> rQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.state = 0 and policyMetaData.policyId.baseId = :baseid",IcmReturnedPolicy.class);
			rQuery.setParameter("baseid", pi2.getPolicyToChange());
			List<IcmReturnedPolicy> lr = (List<IcmReturnedPolicy>)rQuery.list();
			r.setNumericRank(lr.get(0).getPriority());
			r.setPolicyBaseId(pi2.getPolicyToChange());
			r.setPolicyDescription(lr.get(0).getPolicy().getDescription());
			lorp.addObject(r);
		}

		tx.commit();
		
		try {
			sess.close();
			return buildResponse(Status.OK,lorp.toJSON());
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0016", LogCriticality.error);
		}
	}
}
