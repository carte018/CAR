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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.hibernate.query.Query;
import org.hibernate.type.StringType;
import org.hibernate.Session;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.arpsi.cfg.ArpsiConfig;
import edu.internet2.consent.arpsi.model.DecisionOnAllOtherValues;
import edu.internet2.consent.arpsi.model.DecisionOnValues;
import edu.internet2.consent.arpsi.model.DecisionRequestObject;
import edu.internet2.consent.arpsi.model.DecisionResponseObject;
import edu.internet2.consent.arpsi.model.DecisionsForInfoDiscriminator;
import edu.internet2.consent.arpsi.model.DecisionsForInfoStatement;
import edu.internet2.consent.arpsi.model.InfoId;
import edu.internet2.consent.arpsi.model.InfoIdPlusValues;
import edu.internet2.consent.arpsi.model.LogCriticality;
import edu.internet2.consent.arpsi.model.OrgDirectiveOnValues;
import edu.internet2.consent.arpsi.model.OrgInfoReleaseStatement;
import edu.internet2.consent.arpsi.model.OrgReleaseDirective;
import edu.internet2.consent.arpsi.model.OrgReturnedPolicy;
import edu.internet2.consent.arpsi.model.PendingDecision;
import edu.internet2.consent.arpsi.model.RelyingPartyProperty;
import edu.internet2.consent.arpsi.model.ResolvedAnyDecision;
import edu.internet2.consent.arpsi.model.ResolvedDecision;
import edu.internet2.consent.arpsi.model.UserProperty;
import edu.internet2.consent.arpsi.model.ValueObject;
import edu.internet2.consent.arpsi.util.ArpsiUtility;
import edu.internet2.consent.arpsi.util.OMSingleton;

import java.math.BigInteger;

@Path("/org-info-release-decision")
public class OrgInfoReleaseDecisionController {

	@SuppressWarnings("unused")
	private static String caller = ""; // calling user/system
	//@SuppressWarnings("unused")
	//private static Log LOG = LogFactory.getLog(OrgInfoReleaseDecisionController.class);
	
	// This WS comprises a single (POST) endpoint that accepts a JSON represented decisionRequestObject
	// and returns a decisionResponseObject.
	//
	// Stock utility methods first
	// Utility method for internal use only for generating responses in proper format.
	// We tack on the headers required for CORS with Swagger.io here automatically
	// We assume that the caller is setting both status code and entity, so we don't differentiate
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin", "http://editor.swagger.io").header("Access-Control-Allow-Methods", "POST").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
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
			Query q =  sess.createSQLQuery("select 1 from dual");
			c =  ((BigInteger) q.uniqueResult()).longValue();
			if (c == 1) {
				healthy = true;
			}
		} catch (Exception e) {
			// ignore
			ArpsiUtility.locDebug("LOG0021","Exception thrown during healthcheck: " + e.getMessage());
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
	
	
	// CORS support for Swagger.io
	
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		return buildResponse(Status.OK,"");
	}
	
	// The meat
	
	// There is only one endpoint in this service -- the POST endpoint for retrieving decisions from the ARPSI.
	// It is much more complex than the matching endpoint in the COPSU, however.
	
	@POST
	@Path("/")
	@Consumes({"application/json; charset=UTF-8"})
	@Produces({"application/json; charset=UTF-8"})
	public Response postRootDecisionRequest(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		
		// First, parse out the input -- must be a well-formed DecisionRequestObject or we cannot proceed
		
		@SuppressWarnings("unused")
		ArpsiConfig config = null;
		try {
			config = ArpsiUtility.init("postPolicy", request, headers, null);
		} catch (Exception e) {
			return ArpsiUtility.locError(500,"ERR0004",LogCriticality.error);
		}
	
		// Authorized.  Now we parse the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		DecisionRequestObject inputRequest = null;
		
		try {
			inputRequest = mapper.readValue(entity,  DecisionRequestObject.class);
		} catch (JsonParseException e) {
			return ArpsiUtility.locError(400,"ERR0005",LogCriticality.info);
		} catch (JsonMappingException me) {
			return ArpsiUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception ioe) {
			return ArpsiUtility.locError(500, "ERR0007",LogCriticality.error);
		}

		// Now the input is in inputRequest
		
		// validate the input for required request data
		
		if (inputRequest == null) {
			return ArpsiUtility.locError(400, "ERR0008",LogCriticality.info);
		}
		if (inputRequest.getUserId() == null) {
			return ArpsiUtility.locError(400,"ERR0010",LogCriticality.info);
		}
		if (inputRequest.getRelyingPartyId() == null) {
			return ArpsiUtility.locError(400, "ERR0011",LogCriticality.info);
		}
		if (inputRequest.getResourceHolderId() == null) {
			return ArpsiUtility.locError(400,"ERR0012",LogCriticality.info);
		}
		if (inputRequest.getArrayOfInfoIdsPlusValues() == null) {
			return ArpsiUtility.locError(400, "ERR0035",LogCriticality.info);
		}
		
		// Arrays of properties are not required, although not passing them may make some policies 
		// that would otherwise match a situation fail to match the situation.  Caveat caller.
		
		// All required information is present in the decision request.
		// Build a list of pending decision requests against which to measure
		
		ArrayList<PendingDecision> pendingList = new ArrayList<PendingDecision>();
		
		// And a list of resolved decisions to collect results into
		ArrayList<ResolvedDecision> resolvedDecisions = new ArrayList<ResolvedDecision>();
		
		// And a HashMap to collect allOtherValueDecisions
		HashMap<InfoId,ResolvedAnyDecision> allOtherDecisions = new HashMap<InfoId,ResolvedAnyDecision>();
		
		// And a holder for an "all other info items" decision if one is found along the way
		ResolvedAnyDecision anyOtherInfoDirective = null;
		
		for (InfoIdPlusValues iipv : inputRequest.getArrayOfInfoIdsPlusValues()) {
			// for each of the iipv's referenced in the request, add a set of pairs to the pending list
			for (String iv : iipv.getInfoItemValues()) {
				PendingDecision pd = new PendingDecision();
				pd.setInfoId(iipv.getInfoId());
				pd.setValue(iv);
				pendingList.add(pd);
			}
		}
		
		// pendingList now contains a list of decisions we need to render
		
		// Prepare a decision response object for later user
		
		DecisionResponseObject dro = new DecisionResponseObject();
				
		// Tourist info for the decision response
		dro.setDecisionId(UUID.randomUUID().toString());  // randomized UUID
		dro.setTimeOfDecision(String.valueOf(System.currentTimeMillis())); // now
		dro.setUserId(inputRequest.getUserId()); // source -> target
		dro.setRelyingPartyId(inputRequest.getRelyingPartyId()); // source -> target
		dro.setResourceHolderId(inputRequest.getResourceHolderId()); // source -> target
		
		
		// Prepare to perform JDBC operations

		Session sess = ArpsiUtility.getHibernateSession();
		if (sess == null) {
			return ArpsiUtility.locError(500,"ERR0018",LogCriticality.error);
		}
		
		// Now things get unpleasant
		
		// Build an HQL request to retrieve only policies that may apply to this relying party and 
		// this resourceHolder. 
		// We must cast a broad net in this case, since we don't have HQL regex matching capabilities...
		//
		
		StringBuilder sb = new StringBuilder();
		
		// Start by retrieving only active policies that pertain to the current resource holder
		
		sb.append("select distinct orp from OrgReturnedPolicy orp, UserProperty up, RelyingPartyProperty rp  where orp.policyMetaData.state = 0 and orp.policy.resourceHolderId.RHValue = :rhvalue and orp.policy.resourceHolderId.RHType = :rhtype");  // this will get every active policy
		
		// Append components to the select to restrict returned policies to those that match the input case.
		// We look for cases where the input user *is* the target of the policy or where the policy applies to a collection of users
		// AND the input relying party *is* the target of the policy or where the policy applies to a collection of relying parties.
		// We cannot perform regex matches in the select, so we have to cast a wide net and return all the policies that *could* 
		// end up matching.
		
		// This restricts to cases where the user identifier is the userPropValue or where the userPropName is in the list of user properties presented to us and the userPropValue is in the list of values presented to us
		// There are two distinct cases -- one in which the request does not specify any userProp values and one in which it does
		if (inputRequest.getArrayOfUserProperty().isEmpty()) {
			// no incoming user properties to match against -- use just the userid attribute and blankets cases
			sb.append(" and (up in elements(orp.policy.userPropertyArray) and (up.userPropValue = :username or up.userPropValue like '.*' or up.userPropValue like '^.*' or up.userPropValue like '.*$' or up.userPropValue like '^.*$'))");
		} else { 
			// At least one incoming user property to deal with
			sb.append(" and (up in elements(orp.policy.userPropertyArray) and (up.userPropValue = :username or up.userPropValue like '.*' or up.userPropValue like '^.*' or up.userPropValue like '.*$' or up.userPropValue like '^.*$' or (up.userPropName in (:upnamelist) and (up.userPropValue in (:upvaluelist) or up.userPropValue like '%*%' or up.userPropValue like '%+%' or up.userPropValue like '%[%'))))");
		}
		// And this does the same for relying party values
		if (inputRequest.getArrayOfRelyingPartyProperty().isEmpty()) {
			// no incoming relying party properties to match against
			sb.append(" and (rp in elements(orp.policy.relyingPartyPropertyArray) and (rp.rpPropValue = :relyingpartyname or rp.rpPropValue like '.*' or rp.rpPropValue like '^.*' or rp.rpPropValue like '.*$' or rp.rpPropValue like '^.*$'))");
		} else {
			// At least one to match against
			sb.append(" and (rp in elements(orp.policy.relyingPartyPropertyArray) and (rp.rpPropValue = :relyingpartyname or rp.rpPropValue like '.*' or rp.rpPropValue like '^.*' or rp.rpPropValue like '.*$' or rp.rpPropValue like '^.*$' or (rp.rpPropName in (:rpnamelist) and (rp.rpPropValue in (:rpvaluelist) or rp.rpPropValue like '%*%' or rp.rpPropValue like '%+%' or rp.rpPropValue like '%[%'))))");
		}
		
		// And specify a sorting order so that we can process down the list sanely
		sb.append(" order by orp.priority");
		
		// And load the query string up with values
		Query<OrgReturnedPolicy> matchQuery = sess.createQuery(sb.toString(),OrgReturnedPolicy.class);

		matchQuery.setParameter("rhvalue",inputRequest.getResourceHolderId().getRHValue());
		matchQuery.setParameter("rhtype", inputRequest.getResourceHolderId().getRHType());
		matchQuery.setParameter("username", inputRequest.getUserId().getUserValue(),StringType.INSTANCE);
		matchQuery.setParameter("relyingpartyname", inputRequest.getRelyingPartyId().getRPvalue(),StringType.INSTANCE);
		
		int upct = 1;
		int rpct = 1;
		if (inputRequest.getArrayOfUserProperty() != null)
			upct = (((int) (inputRequest.getArrayOfUserProperty().size() / 10) + 1) * 10);
		if (inputRequest.getArrayOfRelyingPartyProperty() != null)
			rpct = (((int) (inputRequest.getArrayOfRelyingPartyProperty().size() / 10) + 1) * 10);
		
		List<String> upnames = new ArrayList<String>(upct);
		List<String> upvalues = new ArrayList<String>(upct);
		List<String> rpnames = new ArrayList<String>(rpct);
		List<String> rpvalues = new ArrayList<String>(rpct);
		
		
		for (UserProperty uprop : inputRequest.getArrayOfUserProperty()) {
			upnames.add(uprop.getUserPropName());
			upvalues.add(uprop.getUserPropValue());
		}
		for (RelyingPartyProperty rprop : inputRequest.getArrayOfRelyingPartyProperty()) {
			rpnames.add(rprop.getRpPropName());
			rpvalues.add(rprop.getRpPropValue());
		}
		
		
		if (! inputRequest.getArrayOfUserProperty().isEmpty()) {
			matchQuery.setParameterList("upnamelist", upnames);
			matchQuery.setParameterList("upvaluelist", upvalues);
		}
		if (! inputRequest.getArrayOfRelyingPartyProperty().isEmpty()) {
			matchQuery.setParameterList("rpnamelist", rpnames);
			matchQuery.setParameterList("rpvaluelist", rpvalues);
		}
		
		// We must be careful to force initialization of the retrieved data 
		// before adding to the end of the list, so here we do a bit of double-entry
		// collection processing
		List<OrgReturnedPolicy> fList = (List<OrgReturnedPolicy>)matchQuery.list();
		List<OrgReturnedPolicy> retList = new ArrayList<OrgReturnedPolicy>();
		retList.addAll(fList);
		// List<OrgReturnedPolicy> retList = (List<OrgReturnedPolicy>)matchQuery.list();
		
		// Add the last resort policy to the end of the whole thing for good measure
		// It is always a possible matching policy if it exists
		// This is best-effort -- if we fail to add, we simply continue on without
		try {
			Query<OrgReturnedPolicy> lastResortQuery = sess.createQuery("from OrgReturnedPolicy where policyMetaData.policyId.baseId = :lastresort and policyMetaData.state = 0",OrgReturnedPolicy.class);
			lastResortQuery.setParameter("lastresort", "LastResort");
			// Must be careful again about initialization of query results here
			List<OrgReturnedPolicy> lrList = lastResortQuery.list();
			retList.addAll(lrList);
			//retList.addAll(lastResortQuery.list());
		} catch (Exception lre) {
			// log and ignore
			ArpsiUtility.locDebug("LOG0021", "LastResortQuery retrieval exception: " + lre.getMessage());
		}
		
		// retList now contains our list of potentially matching policies. 
		// The list should be ordered in increasing order of priority (most important first)
		// the list should only include policies that match to the resource holder already
		// 
		// For each possible matching policy, starting from the top, we:
		//
		//- check to see if there are unmatched info item/value pairs remaining
		//   - if not, break out of the for()
		//   - otherwise check to see if it actually matches the userProperty/user, rpProperty/rp values we have
		//   	- if not, continue the for()
		//		- if so, iterate over the unmatched info items:
		//			- if info item/value matches
		//				- add decision to decision set
		//				- remove info item/value from pending list
		//			- if not, continue iterate over unmatched info items
		//
		// At the end of the process, we should have a set of decisions and possibly an unresolved set of 
		// info item requests.
		// If there remain unresolved info item requests, apply the fallback policy to them and put the result
		// in the decision set.
		// If there is no fallback policy, apply the "deny everything" rule and put the result in the 
		// decision set.
		// 
		// Finally, return the decision set.
		//
		// Simple, right? ;-)
		//
		
		// Some simplification layering
		String inUser = inputRequest.getUserId().getUserValue();
		String inUserType = inputRequest.getUserId().getUserType();
		String inRP = inputRequest.getRelyingPartyId().getRPvalue();
		String inRPType = inputRequest.getRelyingPartyId().getRPtype();
		
		
		ArrayList<PendingDecision> rv = new ArrayList<PendingDecision>();
		ArpsiUtility.locDebug("LOG0017",inUser,inRP);
		// For each of the possibly matching policies...
		for (OrgReturnedPolicy checkPolicy : retList) {
			// If the policy has a catch-all and we don't have one yet, record it
			if (anyOtherInfoDirective == null && checkPolicy.getPolicy().getAllOtherOrgInfoReleaseStatement() != null) {
				anyOtherInfoDirective = new ResolvedAnyDecision();
				anyOtherInfoDirective.setDirective(checkPolicy.getPolicy().getAllOtherOrgInfoReleaseStatement().getOrgDirectiveAllOtherValues().getOrgReleaseDirective());
				anyOtherInfoDirective.setPolicyId(checkPolicy.getPolicyMetaData().getPolicyId());
			}
			// bail if there are no pending requests and we have an anyOtherInfoDirective
			if (pendingList.isEmpty() && anyOtherInfoDirective != null) {
				break;  // exit the policy check -- we've met our requirement
			}
			// Otherwise, check to see if it matches the actual case
			// We have to determine matching on two outer layers -- user and RP
			// check user and user properties against policy values
			boolean userMatch = false;  // assume no match to begin with
			boolean rpMatch = false; // assume no match to begin with
			for (UserProperty up : checkPolicy.getPolicy().getUserPropertyArray()) {
				if (inUserType.matches(up.getUserPropName()) && inUser.matches(up.getUserPropValue())) {
					userMatch = true;
					continue;  // leave the up check
				} else {
					for (UserProperty ip : inputRequest.getArrayOfUserProperty()) {
						if (ip.getUserPropName().matches(up.getUserPropName()) && ip.getUserPropValue().matches(up.getUserPropValue())) {
							userMatch = true;
							continue;
						}
					}
					if (userMatch)
						continue;
				}
			}
			if (userMatch) {
				ArpsiUtility.locDebug("LOG0018",checkPolicy.getPolicyMetaData().getPolicyId().getBaseId());
				// We matched on user information -- see if we match on RP now
				for (RelyingPartyProperty rp : checkPolicy.getPolicy().getRelyingPartyPropertyArray()) {
					ArpsiUtility.locDebug("LOG0020",rp.getRpPropName(),rp.getRpPropValue(),"RP identity: " + inRPType + "/" + inRP);
					if (inRPType.matches(rp.getRpPropName()) && inRP.matches(rp.getRpPropValue())) {
						rpMatch = true;
						continue;
					} else {
						for (RelyingPartyProperty irp : inputRequest.getArrayOfRelyingPartyProperty()) {
							ArpsiUtility.locDebug("LOG0020",irp.getRpPropName(),irp.getRpPropValue(),rp.getRpPropName()+","+rp.getRpPropValue());
							if (irp.getRpPropName().matches(rp.getRpPropName()) && irp.getRpPropValue().matches(rp.getRpPropValue())) {
								rpMatch = true;
								continue;
							}
						}
					}
					if (rpMatch)
						continue;
				}
			} else {
				ArpsiUtility.locDebug("LOG0019",checkPolicy.getPolicyMetaData().getPolicyId().getBaseId());
			}
			if (!userMatch || !rpMatch) {
				// we failed to match on either user or RP
				ArpsiUtility.locDebug("ERR0907",LogCriticality.debug);
				continue;
			}
			// Otherwise, we have an applicable policy -- check to see if it fulfills any of the info requests
			// Iterate over the remaining items in the pendingList and check
			
			for (PendingDecision pd : pendingList) {
				// Here, we need to determine whether the pending decision (with an infoId and value string)
				// matches something in the checkPolicy, which may contain an array of arrays of arrays.  
				for (OrgInfoReleaseStatement oir : checkPolicy.getPolicy().getArrayOfInfoReleaseStatement()) {
					if (pd.getInfoId().getInfoType().matches(oir.getInfoId().getInfoType()) && pd.getInfoId().getInfoValue().matches(oir.getInfoId().getInfoValue())) {
						// right info ID
						// Grab the allOtherDecisions value if it's the first and it exists
						if (oir.getOrgDirectiveAllOtherValues() != null && ! allOtherDecisions.containsKey(pd.getInfoId())) {
							ResolvedAnyDecision rad = new ResolvedAnyDecision();
							rad.setDirective(oir.getOrgDirectiveAllOtherValues().getOrgReleaseDirective());
							rad.setPolicyId(checkPolicy.getPolicyMetaData().getPolicyId());
							allOtherDecisions.put(pd.getInfoId(),rad);
						}
						// check the arrayOfOrgDirective elements for a ValueList that contains a value that one of our values matches
						directiveLoop:   // label for break from inside loop
						for (OrgDirectiveOnValues odov : oir.getArrayOfOrgDirectiveOnValues()) {
							for (ValueObject v : odov.getValueObjectList()) {
								if (pd.getValue().matches(v.getValue())) {
									// We Have A Match!
									// Add a resolved decision to the list
									ResolvedDecision rd = new ResolvedDecision();
									rd.setInfoId(pd.getInfoId());
									rd.setPolicyId(checkPolicy.getPolicyMetaData().getPolicyId());
									rd.setDirective(odov.getOrgReleaseDirective());
									rd.setValue(pd.getValue());
									resolvedDecisions.add(rd);
									// And remove pd from the pending list
									// pendingList.remove(pd);
									rv.add(pd);
									// And move to the next dov
									break directiveLoop;  // NOTE:  break out of BOTH enclosing loops here
								} 
							}
						}
					} 
				}
			}
			pendingList.removeAll(rv);
			rv.clear();
		}
		
		// At this point, we have either exhausted the list of pending decisions to acquire
		// and/or exhausted the list of possible matching policies.
		//
		// If there are remaining decisions in the pending decisions list, attempt to fulfill them from 
		// one of the fallbacks that we have
		for (PendingDecision pd : pendingList) {
			// If there are any remaining, iterate through them
			if (allOtherDecisions.containsKey(pd.getInfoId())) {
				ResolvedDecision rd = new ResolvedDecision();
				ResolvedAnyDecision rad = allOtherDecisions.get(pd.getInfoId());
				rd.setDirective(rad.getDirective());
				rd.setInfoId(pd.getInfoId());
				rd.setValue(pd.getValue());
				rd.setPolicyId(rad.getPolicyId());
				resolvedDecisions.add(rd);
				rv.add(pd);
			}
		}
		// Remove what we added
		pendingList.removeAll(rv);
		rv.clear();
		
		for (PendingDecision pd : pendingList) {
			// If any have still not been resolved, resolve them using the any value we have
			if (anyOtherInfoDirective != null) {
				ResolvedDecision rd = new ResolvedDecision();
				rd.setDirective(anyOtherInfoDirective.getDirective());
				rd.setInfoId(pd.getInfoId());
				rd.setValue(pd.getValue());
				rd.setPolicyId(anyOtherInfoDirective.getPolicyId());
				resolvedDecisions.add(rd);
				rv.add(pd);
			}
		}
		pendingList.removeAll(rv);
		rv.clear();
		
		// And if there are still values, set them to deny and mark them as "default"
		for (PendingDecision pd : pendingList) {
			ResolvedDecision rd = new ResolvedDecision();
			rd.setDirective(OrgReleaseDirective.deny);
			rd.setInfoId(pd.getInfoId());
			rd.setValue(pd.getValue());
			edu.internet2.consent.arpsi.model.PolicyId dpi = new edu.internet2.consent.arpsi.model.PolicyId();
			dpi.setBaseId("default");
			dpi.setVersion("1");
			rd.setPolicyId(dpi);
			resolvedDecisions.add(rd);
			rv.add(pd);
		}
		
		pendingList.removeAll(rv);
		rv.clear();
				
		// We have now by definition exhausted the pendingList
		// Build out the response object based on the resolvedDecisions list
		dro.setArrayOfInfoDecisionStatement(new ArrayList<DecisionsForInfoStatement>());
		// Build a set of DecisionOnValues mapped from InfoIds based on resolvedDecisions
		HashMap<DecisionsForInfoDiscriminator,DecisionOnValues> dovBuilt = new HashMap<DecisionsForInfoDiscriminator,DecisionOnValues>();
		for (ResolvedDecision rd : resolvedDecisions) {
			// for all the decisions we've arrived at...
			// Build the discriminator
			DecisionsForInfoDiscriminator did = new DecisionsForInfoDiscriminator();
			did.setInfoId(rd.getInfoId());
			did.setPolicyId(rd.getPolicyId());
			did.setDirective(rd.getDirective().toString()); // rgc -- fix discriminator
			if (! dovBuilt.containsKey(did)) {
				// build a new one
				DecisionOnValues dov = new DecisionOnValues();
				dov.setPolicyId(rd.getPolicyId());
				dov.setReleaseDecision(rd.getDirective());
				dov.setReturnedValuesList(new ArrayList<String>());
				dovBuilt.put(did,dov);
			}
			// And use it regardless
			dovBuilt.get(did).getReturnedValuesList().add(rd.getValue());
		}
		// At this point, dovBuilt should contain a map of all the DecisionOnValues responses we need
		// Tracking for InfoIds we have established DFISs for 
		HashMap<InfoId,DecisionsForInfoStatement> dfisBuilt = new HashMap<InfoId,DecisionsForInfoStatement>();
		for (DecisionsForInfoDiscriminator did : dovBuilt.keySet()) {
			InfoId ii = did.getInfoId();
			// Iterating over the InfoIds we have to respond for...
			// We populate the response from the resolvedDecisions list
			// dro is the decision response object we're working on
			if (!dfisBuilt.containsKey(ii)) {
				// First, create a new dfis
				DecisionsForInfoStatement dfis = new DecisionsForInfoStatement();
				dfis.setInfoId(ii);
				dfis.setArrayOfDecisionOnValues(new ArrayList<DecisionOnValues>());
				// No longer setting a DecisionOnAllOtherValues response default -- we have explicit decisions
				// for every requested value, some of which may come from a policy's decision on all other values,
				// but each of which is returned as a unique decision.  
	
				if (allOtherDecisions.containsKey(ii)) {
					dfis.setDecisionOnAllOtherValues(new DecisionOnAllOtherValues());
					dfis.getDecisionOnAllOtherValues().setPolicyId(allOtherDecisions.get(ii).getPolicyId());
					dfis.getDecisionOnAllOtherValues().setReleaseDecision(allOtherDecisions.get(ii).getDirective());
				} 
				// and set it into the dro and the dfisbuilt list
				dfisBuilt.put(ii, dfis);
			}
			// Regardless, pull in the dfis from dfisBuilt and add the decision to it
			dfisBuilt.get(ii).getArrayOfDecisionOnValues().add(dovBuilt.get(did));	
		}
		// At this point dfisBuilt contains all the DecisionsForInfoStatements we need
		// Add them to the dro
		for (InfoId ii : dfisBuilt.keySet()) {
			dro.getArrayOfInfoDecisionStatement().add(dfisBuilt.get(ii));
		}
		// At this point, dro is ready to return as the response.
		try {
			try {
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false))) {
					ArpsiUtility.locLog("LOG0021","DRO: " + dro.getDecisionId() + " for (" + dro.getResourceHolderId().getRHType() + "," + dro.getResourceHolderId().getRHValue() + "),(" + dro.getRelyingPartyId().getRPtype() + "," + dro.getRelyingPartyId().getRPvalue() + "),(" + dro.getUserId().getUserType() + "," + dro.getUserId().getUserValue() + ")");
					ArpsiUtility.locDebug("LOG0021","Returned decision response: " + dro.toJSON());
				} else {
					ArpsiUtility.locLog("LOG0021","DRO: " + dro.getDecisionId() + " for (" + dro.getResourceHolderId().getRHType() + "," + dro.getResourceHolderId().getRHValue() + "),(" + dro.getRelyingPartyId().getRPtype() + "," + dro.getRelyingPartyId().getRPvalue() + ")");
				}
			} catch (Exception e) {
				// ignore -- logging is best effort for DROs
			}
			
			return buildResponse(Status.OK,dro.toJSON());
		} catch (JsonProcessingException e) {
			return ArpsiUtility.locError(500, "ERR0016",LogCriticality.error);
		} finally {
			if (sess != null) {
				sess.close();
			}
		}
	}
}
