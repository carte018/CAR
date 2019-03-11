package edu.internet2.consent.arpsi.controllers;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.arpsi.cfg.ArpsiConfig;
import edu.internet2.consent.arpsi.model.ListOfOrgReturnedPolicy;
import edu.internet2.consent.arpsi.model.OrgInfoReleasePolicy;
import edu.internet2.consent.arpsi.model.OrgInfoReleaseStatement;
import edu.internet2.consent.arpsi.model.OrgPolicyMetaData;
import edu.internet2.consent.arpsi.model.OrgReturnedPolicy;
import edu.internet2.consent.arpsi.model.PolicyId;
import edu.internet2.consent.arpsi.model.PolicyState;
import edu.internet2.consent.arpsi.model.RelyingPartyProperty;
import edu.internet2.consent.arpsi.model.SupersedingPolicyId;
import edu.internet2.consent.arpsi.model.UserId;
import edu.internet2.consent.arpsi.model.UserProperty;
import edu.internet2.consent.arpsi.util.ArpsiUtility;

@Path("/org-info-release-policies")
public class OrgInfoReleasePolicyController {
	@SuppressWarnings("unused")
	private String caller = "";
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(OrgInfoReleasePolicyController.class);
	
	
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
	
	@OPTIONS
	@Path("/{policy_id}")
	public Response optionsId(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	// Root POST handler -- creates a new policy (or returns a conflict if the policy already exists).
	// This is similar to the root POST handler in the other services.
	// Policy is constructed from input JSON, then persisted to the data store via Hibernate
	
	@POST
	@Path("/")
	@Produces({"application/json"})
	@Consumes({"application/json"})
	public Response postRoot(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		//
		// Org policies are always added to the end of the hierarchy. They can then be moved as desired.
		//
		// Ordering storage means that we must first save and then retrieve the object in order to get the 
		// proper value of the generated key to use in the ordering list.  We then have to handle insertion at 
		// the list tail.
		//
		// First, handle authorization
		@SuppressWarnings("unused")
		ArpsiConfig config = null;
		try {
			config = ArpsiUtility.init("postPolicy", request, headers, null);
		} catch (Exception e) {
			return ArpsiUtility.locError(500,"ERR0004");
		}
		
		// Map the input into an org policy 
		
		ObjectMapper mapper = new ObjectMapper();
		OrgInfoReleasePolicy inputPolicy = null;
		
		try {
			inputPolicy = mapper.readValue(entity, OrgInfoReleasePolicy.class);
		} catch(JsonParseException e) {
			return ArpsiUtility.locError(400, "ERR0005");
		} catch(JsonMappingException e) {
			return ArpsiUtility.locError(400, "ERR0006");
		} catch (Exception e) {
			return ArpsiUtility.locError(400, "ERR0007");
		}
		
		// If we make it here, we have the input policy in inputPolicy
		
		if (inputPolicy == null) {
			return ArpsiUtility.locError(400, "ERR0008");
		}
		
		// Verify that the input contains the required fields according to the API spec
		
		if (inputPolicy.getDescription() == null || inputPolicy.getDescription().equals("")) {
			return ArpsiUtility.locError(400, "ERR0009");
		}
		if (inputPolicy.getUserPropertyArray() == null || inputPolicy.getUserPropertyArray().isEmpty()) {
			return ArpsiUtility.locError(400, "ERR0048");
		}
		if (inputPolicy.getRelyingPartyPropertyArray() == null || inputPolicy.getRelyingPartyPropertyArray().isEmpty()) {
			return ArpsiUtility.locError(400, "ERR0049");
		}
		if (inputPolicy.getResourceHolderId() == null) {
			return ArpsiUtility.locError(400,"ERR0012");
		}
		
		// We also require one or both of an info release statement or an all other info statement
		if ((inputPolicy.getArrayOfInfoReleaseStatement() == null || inputPolicy.getArrayOfInfoReleaseStatement().isEmpty()) && (inputPolicy.getAllOtherOrgInfoReleaseStatement() == null)) {
			return ArpsiUtility.locError(400, "ERR0050");
		}
		
		// there are no defaults to manage creation of here as there are in the COPSU -- everything here is 
		// by fiat or absent.
		
		// Establish a Hibernate session to operate on this stuff
		Session sess = ArpsiUtility.getHibernateSession();
		if (sess == null) {
			return ArpsiUtility.locError(500, "ERR0018");
		}
		
		// It is actually perfectly legal to have multiple copies of the exact same policy in the ARPSI, useful
		// in the event that ordering is such that you want to apply a policy both before and after another 
		// policy (although that seems an unlikely scenario, really, involving interlocking policies).
		//
		// Hence, no check for duplication is done here
		//
		// We need to construct a new metadata blob for this policy before submitting it to the database
		//
		OrgPolicyMetaData opm = new OrgPolicyMetaData();
		// Set createTime to now
		opm.setCreateTime(System.currentTimeMillis());
		
		// Set creator.  For now, we have no logged in user, so we default to SYSTEM/ARPSI
		// TODO:  This needs to change when we get an authorization/authentication strategy around the ARPSI
		UserId creator = new UserId();
		creator.setUserType("SYSTEM");
		creator.setUserValue("ARPSI");
		opm.setCreator(creator);
		
		// Set policy state
		opm.setState(PolicyState.active);
		
		// Create a new policy id
		PolicyId pid = new PolicyId();
		pid.setVersion("1");  // always 1 to start with
		String unusedUUID = null;
		while (unusedUUID == null) {
			Query<OrgReturnedPolicy> collisionCheckQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :baseid",OrgReturnedPolicy.class);
			unusedUUID = UUID.randomUUID().toString();
			collisionCheckQuery.setParameter("baseid", unusedUUID);
			List<OrgReturnedPolicy> orl = collisionCheckQuery.list();
			if (orl != null && ! orl.isEmpty()) {
				// Collided
				unusedUUID = null;
			}
			
		}
		
		// set policy id
		pid.setBaseId(unusedUUID);
		opm.setPolicyId(pid);
		
		// Merge the metadata and the inputPolicy into a storeable OrgReturnedPolicy object
		OrgReturnedPolicy orp = new OrgReturnedPolicy();
		orp.setPolicy(inputPolicy);
		orp.setPolicyMetaData(opm);
		// and set the priority by getting the top priority
		// We do this inside a transaction that's coupled with the creation in order to guard against
		// collisions
		Query<Long> maxquery = sess.createQuery("select o.priority from OrgReturnedPolicy o where o.priority = (select max(oo.priority) from OrgReturnedPolicy oo)",Long.class);
		List<Long> li = maxquery.list();
		long maxpriority = 0;
		if (li != null && ! li.isEmpty()) {
			maxpriority = li.get(0).longValue();
		}
		// Now, maxpriority is the maximum priority value stored.  
		// Use it, save the policy, and commit the transaction
		// TODO: deal with potential thread collisions here with more explicit pessimistic locking to avoid
		// priority collisions.
		orp.setPriority(maxpriority + 1);
		Transaction tx = sess.beginTransaction();
		sess.save(orp);
		tx.commit();
		sess.close();
		try {
			return buildResponse(Status.OK,orp.toJSON());
		} catch (JsonProcessingException e) {
			return ArpsiUtility.locError(500, "ERR0016");
		}
	}
	
	@GET
	@Path("/{policy_id}")
	@Produces({"application/json"})
	public Response getRootById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id) {
		// Get a specific org policy by policy baseId value
		// Get configuration
		@SuppressWarnings("unused")
		ArpsiConfig config = null;
		try {
			config = ArpsiUtility.init("getPolicy", request, headers, null);
		} catch (Exception e) {
			return ArpsiUtility.locError(500,"ERR0004");
		}

		// Now we are authorized

		String requestedCreatorId = null;
		String requestedVersion = null;
		
		if (policy_id == null || policy_id.equals("")) {
			// Fail
			return ArpsiUtility.locError(500, "ERR0017");
		}
		
		// Check for request parameters
		requestedCreatorId = request.getParameter("createdBy");
		requestedVersion = request.getParameter("policyVersion");
		
		// Set up for hibernate request
		Session sess = ArpsiUtility.getHibernateSession();
		if (sess == null) {
			return ArpsiUtility.locError(500,"ERR0018");
		}
		
		// Determination of prior policy version number
		
		if (requestedVersion != null && (requestedVersion.equals("priorPolicy") || requestedVersion.equals("priorVersion"))) {
			Query<OrgReturnedPolicy> priorQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.state = 0",OrgReturnedPolicy.class);
			priorQuery.setParameter("baseid",policy_id);
			List<OrgReturnedPolicy> curList = (List<OrgReturnedPolicy>) priorQuery.list();
			if (curList != null && !curList.isEmpty()) {
				requestedVersion = String.valueOf(Integer.parseInt(curList.get(0).getPolicyMetaData().getPolicyId().getVersion()) - 1);
			}
		}
		
		// Set up the query
		
		Query<OrgReturnedPolicy> policyQuery = null;
		
		if (requestedCreatorId != null) {
			if (requestedVersion != null) {
				if (requestedVersion.equals("allPolicies") || requestedVersion.equals("allVersions")) {
					policyQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.creator.userValue = :uservalue",OrgReturnedPolicy.class);
					policyQuery.setParameter("baseid", policy_id);
					policyQuery.setParameter("uservalue",  requestedCreatorId);
				} else {
					policyQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.policyId.version = :version and policyMetaData.creator.userValue = :uservalue",OrgReturnedPolicy.class);
					policyQuery.setParameter("baseid", policy_id);
					policyQuery.setParameter("version", requestedVersion);
					policyQuery.setParameter("uservalue",  requestedCreatorId);
				}
			} else {
				policyQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.state = 0 and policyMetaData.creator.userValue = :uservalue",OrgReturnedPolicy.class);
				policyQuery.setParameter("baseid",  policy_id);
				policyQuery.setParameter("uservalue",  requestedCreatorId);
			}
		} else {
			if (requestedVersion != null) {
				if (requestedVersion.equals("allPolicies") || requestedVersion.equals("allVersions")) {
					policyQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :baseid",OrgReturnedPolicy.class);
					policyQuery.setParameter("baseid",  policy_id);
				} else {
					policyQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.policyId.version = :version",OrgReturnedPolicy.class);
					policyQuery.setParameter("baseid", policy_id);
					policyQuery.setParameter("version", requestedVersion);
				}
			} else {
				policyQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.state = 0",OrgReturnedPolicy.class);
				policyQuery.setParameter("baseid",  policy_id);
			}
		}
		
		// Execute the query
		List<OrgReturnedPolicy> resultList = (List<OrgReturnedPolicy>) policyQuery.list();
		
		if (resultList == null || resultList.size() < 1) {
			// return a 404
			sess.close();
			return ArpsiUtility.locError(404, "ERR0019");
		} else if (resultList.size() > 1 && (requestedVersion == null || !(requestedVersion.equals("allPolicies") || requestedVersion.equals("allVersions")))) {
			sess.close();
			return ArpsiUtility.locError(409, "ERR0020");
		} else {
			ListOfOrgReturnedPolicy lorp = new ListOfOrgReturnedPolicy();
			for (OrgReturnedPolicy o : resultList) {
				lorp.addPolicy(o);
			}
			try {
				return buildResponse(Status.OK,lorp.toJSON());
			} catch (JsonProcessingException j) {
				return ArpsiUtility.locError(500,"ERR0016");
			} finally {
				if (sess != null) {
					sess.close();
				}
			}
		}
	}
	
	@GET
	@Path("/")
	@Produces({"application/json"})
	public Response getRoot(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		//
		// This amounts to a policy search endpoint -- given a set of constraints in the arguments to the URL
		// retrieve policies that meet the criteria.
		// 
		// Unless otherwise specified, we only retrieve active policies
		
		// Default to "all" for each of the parameters
		
		String requestedUserType = null;
		String requestedUser = null;
		String requestedRPType = null;
		String requestedRP = null;
		
				
		// Default to "ignore" (which is equivalent to "all" in this case)
		
		String requestedInfoItem = null;
		String requestedInfoItemType = null;
		
		// Get configuration
		@SuppressWarnings("unused")
		ArpsiConfig config = null;
		try {
			config = ArpsiUtility.init("getPolicy", request, headers, null);
		} catch (Exception e) {
			return ArpsiUtility.locError(500,"ERR0004");
		}

		
		// Now we are authorized
		
		// None of the input parameters are required
		ListOfOrgReturnedPolicy retval = new ListOfOrgReturnedPolicy();
		
		Session sess = ArpsiUtility.getHibernateSession();
		if (sess == null) {
			return ArpsiUtility.locError(500, "ERR0018");
		}
		sess.setHibernateFlushMode(FlushMode.MANUAL);  // we are read only
		
		// Now, construct a query to match the input parameters
		// This is an incremental and somewhat tricky process.
		// Because in the ARPSI we cannot rely on matches against values in the back-end 
		// directly for properties that are treated as regexes, we need to actually retrieve 
		// every possible matching policy and then sieve the results for the real matches.
		
		StringBuilder queryBuilder = new StringBuilder();
		
		// Start with a base query taking state into consideration
		if (request.getParameter("state") != null && request.getParameter("state").equalsIgnoreCase("all")) {
			queryBuilder.append("from OrgReturnedPolicy ");
		} else if (request.getParameter("state") != null && request.getParameter("state").equalsIgnoreCase("inactive")) {
			queryBuilder.append("from OrgReturnedPolicy where policyMetaData.state = 1 "); // inactive
		} else {
			queryBuilder.append("from OrgReturnedPolicy where policyMetaData.state = 0 "); // active
		}
		
		// Take into consideration the RH (which is a unique value)
		if (request.getParameter("resource-holder") == null || request.getParameter("resource-holder-type") == null) {
			sess.close();
			return ArpsiUtility.locError(400, "ERR0051");
		}
		queryBuilder.append("AND policy.resourceHolderId.RHValue = :rhvalue AND policy.resourceHolderId.RHType = :rhtype");
		boolean createdByLimit = false;
		if (request.getParameter("createdBy") != null && ! request.getParameter("createdBy").equals("")) {
			queryBuilder.append(" AND policyMetaData.creator.userValue = :creator");
		}
		String queryString = queryBuilder.toString();
		
		Query<OrgReturnedPolicy> searchQuery = sess.createQuery(queryString,OrgReturnedPolicy.class);
		searchQuery.setParameter("rhvalue", request.getParameter("resource-holder"));
		searchQuery.setParameter("rhtype", request.getParameter("resource-holder-type"));
		if (createdByLimit) {
			searchQuery.setParameter("creator",  request.getParameter("createdBy"));
		}
		
		// And get the list of all the possible matching policies
		List<OrgReturnedPolicy> resultList = null;
		try {
			resultList = (List<OrgReturnedPolicy>) searchQuery.list();
		} catch (Exception e) {
			sess.close();
			return ArpsiUtility.locError(500, "ERR0051");
		}
		
		// now we have a (possibly very long) list of every possible matching policy in the database.
		// Sieve the results for the return value
		//
		// Process parameters
		// User information
		if (request.getParameter("user") != null && request.getParameter("user-type") != null && ! request.getParameter("user").equals("") && ! request.getParameter("user-type").equals("")) {
			requestedUser = request.getParameter("user");
			requestedUserType = request.getParameter("user-type");
		}
		if (request.getParameter("relying-party") != null && request.getParameter("relying-party-type") != null && ! request.getParameter("relying-party").equals("") && ! request.getParameter("relying-party-type").equals("")) {
			requestedRP = request.getParameter("relying-party");
			requestedRPType = request.getParameter("relying-party-type");
		}
		if (request.getParameter("info-item") != null && request.getParameter("info-item-type") != null && ! request.getParameter("info-item").equals("") && ! request.getParameter("info-item-type").equals("")) {
			requestedInfoItem = request.getParameter("info-item");
			requestedInfoItemType = request.getParameter("info-item-type");
		}
		
		for (OrgReturnedPolicy orp : resultList) {
			// Sieve based on user/user-type, relying-party/relying-party-type, and info-item/info-item-type
			// default to false
			boolean userMatch = (requestedUser == null);
			boolean rpMatch = (requestedRP == null);
			boolean iiMatch = (requestedInfoItem == null);
			
			if (! userMatch) {
				// Test for a real match
				for (UserProperty up : orp.getPolicy().getUserPropertyArray()) {
					if (requestedUserType.equalsIgnoreCase(up.getUserPropName()) && requestedUser.matches(up.getUserPropValue()))
							userMatch = true;
				}
			}
			if (! rpMatch) {
				// Test for a real match
				for (RelyingPartyProperty rp : orp.getPolicy().getRelyingPartyPropertyArray()) {
					if (requestedRPType.equalsIgnoreCase(rp.getRpPropName()) && requestedRP.matches(rp.getRpPropValue())) 
						rpMatch = true;
				}
			}
			if (! iiMatch) {
				// Test for a real match
				for (OrgInfoReleaseStatement oirs : orp.getPolicy().getArrayOfInfoReleaseStatement()) {
					// for each info release statement
					if (requestedInfoItemType.equalsIgnoreCase(oirs.getInfoId().getInfoType()) && requestedInfoItem.matches(oirs.getInfoId().getInfoValue())) 
						iiMatch = true;
				}
			}
			
			// If all three are met, write to the retval list
			if (iiMatch && rpMatch && userMatch) {
				// this is one to keep
				retval.addPolicy(orp);
			}
		}
		
		// Now we should have the result we need
		// check for not found
		if (retval.getContained().isEmpty()) {
			// 404 result
			if (sess != null) {
				sess.close();
			}
			return ArpsiUtility.locError(404, "ERR0019");
		} else {
			// 200 result
			try {
				return buildResponse(Status.OK,retval.toJSON());
			} catch (Exception e) {
				return ArpsiUtility.locError(500, "ERR0016");
			} finally {
				if (sess != null) {
					sess.close();
				}
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
		ArpsiConfig config = null;
		try {
			config = ArpsiUtility.init("putPolicy", request, headers, null);
		} catch (Exception e) {
			return ArpsiUtility.locError(500,"ERR0004");
		}

		
		ObjectMapper mapper = new ObjectMapper(); 
		OrgInfoReleasePolicy inputPolicy = null;
		
		try {
			inputPolicy = mapper.readValue(entity,  OrgInfoReleasePolicy.class);
		} catch (JsonParseException j) {
			return ArpsiUtility.locError(400,"ERR0005");
		} catch (JsonMappingException j) {
			return ArpsiUtility.locError(400, "ERR0006");
		} catch (Exception e) {
			return ArpsiUtility.locError(500,"ERR0007");
		}
		
		// Now we have the input policy
		
		// Verify that the RH matches the RH of the target policy
		
		Session sess = ArpsiUtility.getHibernateSession();
		if (sess == null) {
			return ArpsiUtility.locError(500, "ERR0018");
		}
		
		// Start a transaction
		Transaction tx = sess.beginTransaction();
		
		Query<OrgReturnedPolicy> uQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.state = 0",OrgReturnedPolicy.class);
		
		uQuery.setParameter("baseid", policy_id);
		List<OrgReturnedPolicy> originals = (List<OrgReturnedPolicy>) uQuery.list();
		if (originals == null || originals.isEmpty()) {
			tx.rollback();
			sess.close();
			return ArpsiUtility.locError(404, "ERR0019");
		}
		if (originals.size() > 1) {
			tx.rollback();
			sess.close();
			return ArpsiUtility.locError(409, "ERR0020");
		}
		
		OrgReturnedPolicy original = originals.get(0);
		
		if (inputPolicy.getResourceHolderId() != null && inputPolicy.getResourceHolderId().getRHValue() != null && ! inputPolicy.getResourceHolderId().getRHValue().equals(original.getPolicy().getResourceHolderId().getRHValue())) {
			tx.rollback();
			sess.close();
			return ArpsiUtility.locError(400, "ERR0021","resourceHolder");
		}
		
		ObjectMapper copier = new ObjectMapper();
		OrgReturnedPolicy newPolicy = new OrgReturnedPolicy();
		
		try {
			String serialized = original.toJSON();
			newPolicy = (OrgReturnedPolicy) copier.readValue(serialized.getBytes(), OrgReturnedPolicy.class);
		} catch (Exception e) {
			tx.rollback();
			sess.close();
			return ArpsiUtility.locError(500, "ERR0022");
		}
		
		// Update createTime in the policy
		newPolicy.getPolicyMetaData().setCreateTime(System.currentTimeMillis());

		// Update creator
		// TODO:  For now, creator is constant so this is moot.  Later, need to pull creator from authN context
		
		// Increment version number
		newPolicy.getPolicyMetaData().getPolicyId().setVersion(String.valueOf(Integer.parseInt(newPolicy.getPolicyMetaData().getPolicyId().getVersion()) + 1));
		
		// Set current state to active
		newPolicy.getPolicyMetaData().setState(PolicyState.active);
		
		// Clear the superseded by value
		newPolicy.getPolicyMetaData().setSupersededBy(null);
		
		// Insert the new policy itself
		newPolicy.setPolicy(inputPolicy);
		
		// Mark the old policy as inactive
		original.getPolicyMetaData().setState(PolicyState.inactive);
		
		// And set the successor policy value
		SupersedingPolicyId spi = new SupersedingPolicyId();
		spi.setBaseId(newPolicy.getPolicyMetaData().getPolicyId().getBaseId());
		spi.setSupersedingVersion(newPolicy.getPolicyMetaData().getPolicyId().getVersion());
		original.getPolicyMetaData().setSupersededBy(spi);
		
		// Copy precedence
		newPolicy.setPriority(original.getPriority());
		
		// And save the new
		sess.save(newPolicy);
		tx.commit();
		
		sess.close();
		
		try {
			return buildResponse(Status.OK,newPolicy.toJSON());
		} catch (JsonProcessingException e) {
			return ArpsiUtility.locError(500,"ERR0016");
		}
	}
	
	@DELETE
	@Path("/{policy_id}")
	@Produces({"application/json"})
	public Response deleteById(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("policy_id") String policy_id) {
	
		// Delete a policy (which in our semantics means deactive its currently-active version)
		@SuppressWarnings("unused")
		ArpsiConfig config = null;
		try {
			config = ArpsiUtility.init("deletePolicy", request, headers, null);
		} catch (Exception e) {
			return ArpsiUtility.locError(500,"ERR0004");
		}

		// Authorization verified.
		if (policy_id == null || policy_id.equals("")) {
			return ArpsiUtility.locError(400, "ERR0017");
		}

		Session sess = ArpsiUtility.getHibernateSession();
		if (sess == null) {
			return ArpsiUtility.locError(500,"ERR0018");
		}
		Transaction tx = sess.beginTransaction();
		
		Query<OrgReturnedPolicy> policyQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :baseid and policyMetaData.state = 0",OrgReturnedPolicy.class);
		policyQuery.setParameter("baseid", policy_id);
		List<OrgReturnedPolicy> resultList = (List<OrgReturnedPolicy>) policyQuery.list();
		if (resultList == null || resultList.isEmpty()) {
			tx.rollback();
			sess.close();
			return ArpsiUtility.locError(404, "ERR0019");
		} else if (resultList.size() > 1) {
			tx.rollback();
			sess.close();
			return ArpsiUtility.locError(409, "ERR0020");
		} else {
			// delete it
			if (request.getParameter("expungeOnDelete") != null && request.getParameter("expungeOnDelete").equals("true")) {
				sess.delete(resultList.get(0));
				tx.commit();
				sess.close();
				return buildResponse(Status.NO_CONTENT,"");
			} else {
				resultList.get(0).getPolicyMetaData().setState(PolicyState.inactive);
				tx.commit();
				sess.close();
				return buildResponse(Status.NO_CONTENT,"");
			}
		}
	}
}