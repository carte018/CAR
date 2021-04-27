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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
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
import org.hibernate.query.Query;
import org.hibernate.Session;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.icm.model.InfoId;
import edu.internet2.consent.icm.model.InfoIdPlusValues;
import edu.internet2.consent.icm.model.LogCriticality;
import edu.internet2.consent.icm.model.ResolvedDecision;
import edu.internet2.consent.icm.cfg.IcmConfig;
import edu.internet2.consent.icm.model.AllOtherValuesConst;
import edu.internet2.consent.icm.model.AugmentedPolicyId;
import edu.internet2.consent.icm.model.DecisionOnValues;
import edu.internet2.consent.icm.model.DecisionRequestObject;
import edu.internet2.consent.icm.model.DecisionResponseObject;
import edu.internet2.consent.icm.model.DecisionsForInfoDiscriminator;
import edu.internet2.consent.icm.model.DecisionsForInfoStatement;
import edu.internet2.consent.icm.model.IcmDecisionOnAllOtherValues;
import edu.internet2.consent.icm.model.IcmDecisionOnValues;
import edu.internet2.consent.icm.model.IcmDecisionResponseObject;
import edu.internet2.consent.icm.model.IcmDecisionsForInfoStatement;
import edu.internet2.consent.icm.model.IcmDirectiveOnValues;
import edu.internet2.consent.icm.model.IcmInfoReleaseStatement;
import edu.internet2.consent.icm.model.IcmReleaseDirective;
import edu.internet2.consent.icm.model.IcmReturnedPolicy;
import edu.internet2.consent.icm.model.PendingDecision;
import edu.internet2.consent.icm.model.PolicyId;
import edu.internet2.consent.icm.model.PolicySourceEnum;
import edu.internet2.consent.icm.model.RelyingPartyProperty;
import edu.internet2.consent.icm.model.ResolvedAnyDecision;
import edu.internet2.consent.icm.model.UserProperty;
import edu.internet2.consent.icm.model.UserReleaseDirective;
import edu.internet2.consent.icm.model.ValueObject;
import edu.internet2.consent.icm.util.IcmHttpClientFactory;
import edu.internet2.consent.icm.util.IcmUtility;
import edu.internet2.consent.icm.util.OMSingleton;



// The enchilada is here

@Path("/info-release-decision")
public class IcmInfoReleaseDecisionController {

	@SuppressWarnings("unused")
	private static String caller = "";
	private static Log LOG = LogFactory.getLog(IcmInfoReleaseDecisionController.class);
	
	//Accept a JSON request and construct a JSON decision response based on it, ICM policy, 
	// COPSU policy, and ARPSI policy combined.  The enchilada.
	//
	
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin", "http://editor.swagger.io").header("Access-Control-Allow-Methods", "POST").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
		
	// CORS support for Swagger.io
	
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		return buildResponse(Status.OK,"");
	}
	
	// The meat

	// Single POST endpoint.  We ask questions and get back responses from the interface this way.
	// Technically, we're creating request resources, which return result resources.
	
	// Matching here is highly complex.
	
	@POST
	@Path("/")
	@Consumes({"application/json;charset=UTF-8"})
	@Produces({"application/json;charset=UTF-8"})
	public Response postRootDecisionRequest(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		
		// Parse the input into a request object
		IcmConfig config = null;
		try {
			config = IcmUtility.init("postRequest", request, headers, null);
		} catch (Exception e) {
			return IcmUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		String copsuHost = config.getProperty("copsu.server.name", true);
		String copsuPort = config.getProperty("copsu.server.port", true);
		
		// Authorized.  Now we parse the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		DecisionRequestObject inputRequest = null;
		
		try {
			inputRequest = mapper.readValue(entity,  DecisionRequestObject.class);
		} catch (JsonParseException e) {
			return IcmUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return IcmUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0007",LogCriticality.error);
		}
		
		// Validate the input request
		
		if (inputRequest == null) {
			return IcmUtility.locError(400, "ERR0008",LogCriticality.info);
		}
		if (inputRequest.getUserId() == null) {
			return IcmUtility.locError(400, "ERR0010",LogCriticality.info);
		}
		if (inputRequest.getRelyingPartyId() == null) {
			return IcmUtility.locError(400, "ERR0011",LogCriticality.info);
		}
		if (inputRequest.getResourceHolderId() == null) {
			return IcmUtility.locError(400, "ERR0012",LogCriticality.info);
		}
		if (inputRequest.getArrayOfInfoIdsPlusValues() == null) {
			return IcmUtility.locError(400, "ERR0035",LogCriticality.info);
		}
		
		// While an array of properties is not required for user or relying party, lacking them will make 
		// many institutional policies fail to match which might otherwise match.  Caveat caller.
		
		// First, we collect the responses provided by the COPSU and the ARPSI, as we do in the 
		// front-end policy controller endpoints of the ICM policy API.  Here, though, we call the 
		// decision request endpoints of the COPSU and ARPSI with the decision request we have to 
		// get back a decision response JSON object that we can manipulate.
		//
		// We take advantage of the presence of both COPSU and ARPSI models in our context
		//
		
		// DEBUGGING
		
		IcmUtility.locLog("ERR1137", LogCriticality.error,"Incoming ICM decision request: " + entity);
		IcmUtility.locLog("ERR1137", LogCriticality.error,"Incoming ICM decision request content-type header was: " + headers.getHeaderString("Content-Type"));
		// Hibernate
		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// And now, the unpleasantness.
		
		// Essentially, this interface mimics the behavior of the equivalent interface in the ARPSI, but
		// using a dynamically-defined back-end store (the results of passing the decision request to the 
		// COPSU and the ARPSI) and computes each decision it computes based on those values rather than 
		// values in the policy store.  It does, however, base its decision on which decision response to 
		// use to render a decision on policies in its own policy store, so it's essentially a meta-case
		// for the ARPSI decision endpoint. 
		//
		// Start by reetrieving the responses from the COPSU and the ARPSI.

		//
		
		// Retrieve a decision from the COPSU by passing our input to the COPSU.
		// The response may be a COPSU decision response, in which case we use that, or it may be 
		// an error object, in which case the mapping of the response to a decision response will fail 
		// with a mapping exception, and we can proceed based on the error.
		//
		// In the event that an error is returned by the COPSU, we error out the decision at the ICM -- 
		// there is no way for the ICM to continue if it cannot retrieve a decision from both sources.
		
		StringBuilder sb = new StringBuilder();

		sb.append("/consent/v1/copsu/user-info-release-decision");
		
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
		
		String authzHeader = IcmUtility.buildAuthorizationHeader(config);

		edu.internet2.consent.copsu.model.DecisionResponseObject copsuDecision = null;
		edu.internet2.consent.copsu.model.DecisionRequestObject copsuRequest = new edu.internet2.consent.copsu.model.DecisionRequestObject();
		String copsuEntity = null;
		try {
			// map this to a COPSU request
			//ObjectMapper convert = new ObjectMapper();
			ObjectMapper convert = OMSingleton.getInstance().getOm();
			DecisionRequestObject idro = convert.readValue(entity, DecisionRequestObject.class);

			edu.internet2.consent.copsu.model.UserId ui = new edu.internet2.consent.copsu.model.UserId();
			ui.setUserType(idro.getUserId().getUserType());
			ui.setUserValue(idro.getUserId().getUserValue());
			copsuRequest.setUserId(ui);
			
			edu.internet2.consent.copsu.model.RelyingPartyId ri = new edu.internet2.consent.copsu.model.RelyingPartyId();
			ri.setRPtype(idro.getRelyingPartyId().getRPtype());
			ri.setRPvalue(idro.getRelyingPartyId().getRPvalue());
			copsuRequest.setRelyingPartyId(ri);
			
			edu.internet2.consent.copsu.model.ResourceHolderId rh = new edu.internet2.consent.copsu.model.ResourceHolderId();
			rh.setRHType(idro.getResourceHolderId().getRHType());
			rh.setRHValue(idro.getResourceHolderId().getRHValue());
			copsuRequest.setResourceHolderId(rh);
			
			ArrayList<edu.internet2.consent.copsu.model.InfoIdPlusValues> iipv = new ArrayList<edu.internet2.consent.copsu.model.InfoIdPlusValues>();
			
			for (InfoIdPlusValues iip : idro.getArrayOfInfoIdsPlusValues()) {
				edu.internet2.consent.copsu.model.InfoIdPlusValues ip = new edu.internet2.consent.copsu.model.InfoIdPlusValues();
				edu.internet2.consent.copsu.model.InfoId ii = new edu.internet2.consent.copsu.model.InfoId();
				ii.setInfoType(iip.getInfoId().getInfoType());
				ii.setInfoValue(iip.getInfoId().getInfoValue());
				ip.setInfoId(ii);
				ip.setInfoItemValues(new ArrayList<String>(iip.getInfoItemValues()));
				iipv.add(ip);
			}
			copsuRequest.setArrayOfInfoIdsPlusValues(iipv);
			copsuEntity = convert.writeValueAsString(copsuRequest);
			
			// debug
			IcmUtility.locError(200,"ERR0056", LogCriticality.error,"CopsuRequest = " + copsuEntity);
		} catch (Exception e) {
			// Fail if we cannot create the copsu request
			return IcmUtility.locError(500, "ERR0054",LogCriticality.error);
		}
		try {
			response = IcmUtility.sendRequest(httpClient, "POST", copsuHost, copsuPort, sb.toString(), copsuEntity, authzHeader);
			String rbody = IcmUtility.extractBody(response);
			int status = IcmUtility.extractStatusCode(response);
			
			if (status >= 300)
				return IcmUtility.locError(status,"ERR0055",LogCriticality.error);
			
			// debug
			IcmUtility.locError(200, "ERR0056", LogCriticality.error,"CopsuResponse = " + rbody);
			//ObjectMapper om = new ObjectMapper();
			ObjectMapper om = OMSingleton.getInstance().getOm();
			copsuDecision = om.readValue(rbody,edu.internet2.consent.copsu.model.DecisionResponseObject.class);
			IcmUtility.locLog("LOG0016",LogCriticality.info,copsuDecision.getDecisionId());

		} catch (Exception e) {
			// If we except along the way, fail
			return IcmUtility.locError(500, "ERR0056",LogCriticality.error,e.getMessage());
		} finally {
			EntityUtils.consumeQuietly(response.getEntity());
			HttpClientUtils.closeQuietly(response);
			//HttpClientUtils.closeQuietly(httpClient);
		}
		// And repeat the process getting the ARPSI response
		StringBuilder sb2 = new StringBuilder();
		String arpsiHost = config.getProperty("arpsi.server.name", true);
		String arpsiPort = config.getProperty("arpsi.server.port", true);
		
		sb2.append("/consent/v1/arpsi/org-info-release-decision");
		//HttpClient httpClient2 = HttpClientBuilder.create().build();
		HttpClient httpClient2 = null;
		try {
			httpClient2 = IcmHttpClientFactory.getHttpsClient();
		} catch (Exception e) {
			// Log and create a raw client instead
			IcmUtility.locLog("ERR1136", LogCriticality.error,"Falling back to default HttpClient d/t failed client initialization");
			httpClient = HttpClientBuilder.create().build();
		}

		HttpResponse response2 = null;
		String authzHeader2 = IcmUtility.buildAuthorizationHeader(config);
		
		DecisionResponseObject arpsiDecision = null;
		
		try {
			response2 = IcmUtility.sendRequest(httpClient2, "POST", arpsiHost, arpsiPort, sb2.toString(), entity, authzHeader2);
			String rbody2 = IcmUtility.extractBody(response2);
			int status2 = IcmUtility.extractStatusCode(response2);
			
			if (status2 >= 300) 
				return IcmUtility.locError(status2, "ERR0059",LogCriticality.error);
			
			//ObjectMapper om2 = new ObjectMapper();
			ObjectMapper om2 = OMSingleton.getInstance().getOm();
			arpsiDecision = om2.readValue(rbody2, DecisionResponseObject.class);
			IcmUtility.locLog("LOG0017",LogCriticality.info,arpsiDecision.getDecisionId());
		} catch (Exception e) {
			// If we except along the way, fail
			return IcmUtility.locError(500, "ERR0060",LogCriticality.error);
		} finally {
			EntityUtils.consumeQuietly(response2.getEntity());
			HttpClientUtils.closeQuietly(response2);
			//HttpClientUtils.closeQuietly(httpClient2);
		}
		
		// At this point, we should have a copsu decision in copsuDecision and an arpsi decision in arpsiDecision
		
		// Now we must perform an ARPSI-like sieve to determine which of the ICM policies to apply
		// If we fail to find a matching ICM policy for one of the requested results, we use
		// the value of the config.decisionOfLastResort setting in the configuration file.
		// If we have a decision that requires using the arpsi or copsu decision and we lack an explicit
		// decision from the copsu or the arpsi, we rely on the all other values setting or the all other 
		// attributes setting in the response.  If we lack that, as well, we return an error.
		
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
		
		IcmDecisionResponseObject dro = new IcmDecisionResponseObject();
				
		// Tourist info for the decision response
		dro.setDecisionId(UUID.randomUUID().toString());  // randomized UUID
		dro.setTimeOfDecision(String.valueOf(System.currentTimeMillis())); // now
		dro.setUserId(inputRequest.getUserId()); // source -> target
		dro.setRelyingPartyId(inputRequest.getRelyingPartyId()); // source -> target
		dro.setResourceHolderId(inputRequest.getResourceHolderId()); // source -> target
		
		
		// Prepare to perform JDBC operations
		
		// Build an HQL query to retrieve policies from the IcmReturnedPolicy store that match our 
		// input criteria.  Be liberal in this pass, since we don't want to miss a match based on a
		// regex.
		
		StringBuilder sbq = new StringBuilder();
		
		sbq.append("select distinct irp from IcmReturnedPolicy irp, UserProperty up, RelyingPartyProperty rp  where irp.policyMetaData.state = 0 and irp.policy.resourceHolderId.RHValue = :rhvalue and irp.policy.resourceHolderId.RHType = :rhtype");  // this will get every active policy

		if (inputRequest.getArrayOfUserProperty().isEmpty()) {
			sbq.append(" and (up in elements(irp.policy.userPropertyArray) and (up.userPropValue = :username or up.userPropValue like '.*' or up.userPropValue like '^.*' or up.userPropValue like '.*$' or up.userPropValue like '^.*$'))");

		} else {
			sbq.append(" and (up in elements(irp.policy.userPropertyArray) and (up.userPropValue = :username or up.userPropValue like '.*' or up.userPropValue like '^.*' or up.userPropValue like '.*$' or up.userPropValue like '^.*$' or (up.userPropName in (:upnamelist) and (up.userPropValue in (:upvaluelist) or up.userPropValue like '%*%' or up.userPropValue like '%+%' or up.userPropValue like '%[%'))))");
		}

		if (inputRequest.getArrayOfRelyingPartyProperty().isEmpty()) {
			sbq.append(" and (rp in elements(irp.policy.relyingPartyPropertyArray) and (rp.rpPropValue = :relyingpartyname or rp.rpPropValue like '.*' or rp.rpPropValue like '^.*' or rp.rpPropValue like '.*$' or rp.rpPropValue like '^.*$'))");

		} else {
			sbq.append(" and (rp in elements(irp.policy.relyingPartyPropertyArray) and (rp.rpPropValue = :relyingpartyname or rp.rpPropValue like '.*' or rp.rpPropValue like '^.*' or rp.rpPropValue like '.*$' or rp.rpPropValue like '^.*$' or (rp.rpPropName in (:rpnamelist) and (rp.rpPropValue in (:rpvaluelist) or rp.rpPropValue like '%*%' or rp.rpPropValue like '%+%' or rp.rpPropValue like '%[%'))))");

		}
		
		sbq.append(" order by irp.priority");  // order by increasing priority value
		
		// Load the query string with values
		
		//IcmUtility.locLog("LOG9006", LogCriticality.debug, sbq.toString());
		
		Query<IcmReturnedPolicy> matchQuery = sess.createQuery(sbq.toString(),IcmReturnedPolicy.class);

		//IcmUtility.locLog("LOG9007",  LogCriticality.debug, sbq.toString());
		
		matchQuery.setParameter("rhvalue", inputRequest.getResourceHolderId().getRHValue());
		matchQuery.setParameter("rhtype",  inputRequest.getResourceHolderId().getRHType());
		matchQuery.setParameter("username", inputRequest.getUserId().getUserValue());
		matchQuery.setParameter("relyingpartyname", inputRequest.getRelyingPartyId().getRPvalue());
		
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
			matchQuery.setParameterList("upnamelist",  upnames);
			matchQuery.setParameterList("upvaluelist",  upvalues);
		}
		
		if (! inputRequest.getArrayOfRelyingPartyProperty().isEmpty()) {
			matchQuery.setParameterList("rpnamelist", rpnames);
			matchQuery.setParameterList("rpvaluelist", rpvalues);
		}
		
		// We need to be careful about initialization of the result set in this case since
		// we are going to combine two result sets and Hibernate tends to be lazy.
		//
		List<IcmReturnedPolicy> fList = (List<IcmReturnedPolicy>)matchQuery.list();
		List<IcmReturnedPolicy> retList = new ArrayList<IcmReturnedPolicy>();
		retList.addAll(fList);
		//List<IcmReturnedPolicy> retList = (List<IcmReturnedPolicy>)matchQuery.list();
		
		// Look for policy with baseId "LastResort" and add to the end of the list for reference
		// This is best-effort -- if we fail retrieving a LastResort policy, we continue without
		try {
			Query<IcmReturnedPolicy> lastResortQuery = sess.createQuery("from IcmReturnedPolicy where policyMetaData.policyId.baseId = :lastresort and policyMetaData.state = 0",IcmReturnedPolicy.class);
			lastResortQuery.setParameter("lastresort", "LastResort");
			// Likewise about initialization here...
			List<IcmReturnedPolicy> lrList = lastResortQuery.list();
			retList.addAll(lrList);
			//retList.addAll(lastResortQuery.list());
		} catch (Exception lre) {
			// Ignore and log
			IcmUtility.locLog("ERR1137", LogCriticality.error, "LastResortQuery threw: " + lre.getMessage());
		}
		
		String inUser = inputRequest.getUserId().getUserValue();
		String inUserType = inputRequest.getUserId().getUserType();
		String inRP = inputRequest.getRelyingPartyId().getRPvalue();
		String inRPType = inputRequest.getRelyingPartyId().getRPtype();
		
		ArrayList<PendingDecision> rv = new ArrayList<PendingDecision>();
		
		for (IcmReturnedPolicy checkPolicy : retList) {
			
			if (anyOtherInfoDirective == null && checkPolicy.getPolicy().getAllOtherOrgInfoReleaseStatement() != null) {
				anyOtherInfoDirective = new ResolvedAnyDecision();
				anyOtherInfoDirective.setDirective(checkPolicy.getPolicy().getAllOtherOrgInfoReleaseStatement().getIcmDirectiveAllOtherValues().getIcmReleaseDirective());
				anyOtherInfoDirective.setPolicyId(checkPolicy.getPolicyMetaData().getPolicyId());
			}
			if (pendingList.isEmpty() && anyOtherInfoDirective != null) {
				break;
			}
			boolean userMatch = false;
			boolean rpMatch = false;
			for (UserProperty up : checkPolicy.getPolicy().getUserPropertyArray()) {
				if (inUserType.matches(up.getUserPropName()) && inUser.matches(up.getUserPropValue())) {
					userMatch = true;
					continue;
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
				for (RelyingPartyProperty rp : checkPolicy.getPolicy().getRelyingPartyPropertyArray()) {
					if (inRPType.matches(rp.getRpPropName()) && inRP.matches(rp.getRpPropValue())) {
						rpMatch = true;
						continue;
					} else {
						for (RelyingPartyProperty irp : inputRequest.getArrayOfRelyingPartyProperty()) {
							if (irp.getRpPropName().matches(rp.getRpPropName()) && irp.getRpPropValue().matches(rp.getRpPropValue())) {
								rpMatch = true;
								continue;
							}
						}
					}
					if (rpMatch)
						continue;
				}
			}
			if (!userMatch || !rpMatch) {
				continue;
			}
			
			for (PendingDecision pd : pendingList) {
				for (IcmInfoReleaseStatement iir : checkPolicy.getPolicy().getArrayOfInfoReleaseStatement()) {
					if (pd.getInfoId().getInfoType().matches(iir.getInfoId().getInfoType()) && pd.getInfoId().getInfoValue().matches(iir.getInfoId().getInfoValue())) {
						if (iir.getIcmDirectiveAllOtherValues() != null && ! allOtherDecisions.containsKey(pd.getInfoId())) {
							ResolvedAnyDecision rad = new ResolvedAnyDecision();
							rad.setDirective(iir.getIcmDirectiveAllOtherValues().getIcmReleaseDirective());
							rad.setPolicyId(checkPolicy.getPolicyMetaData().getPolicyId());
							allOtherDecisions.put(pd.getInfoId(), rad);
						}
						directiveLoop:
							for (IcmDirectiveOnValues idov : iir.getArrayOfIcmDirectiveOnValues()) {
								for (ValueObject v : idov.getValueObjectList()) {
									if (pd.getValue().matches(v.getValue())) {
										ResolvedDecision rd = new ResolvedDecision();
										rd.setInfoId(pd.getInfoId());
										rd.setPolicyId(checkPolicy.getPolicyMetaData().getPolicyId());
										rd.setDirective(idov.getIcmReleaseDirective());
										rd.setValue(pd.getValue());
										resolvedDecisions.add(rd);
										rv.add(pd);
										break directiveLoop;
									}
								}
							}
					}
				}
			}
			pendingList.removeAll(rv);
			rv.clear();
		}
		
		for (PendingDecision pd : pendingList) {
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
		pendingList.removeAll(rv);
		rv.clear();
		
		for (PendingDecision pd : pendingList) {
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
		
		for (PendingDecision pd: pendingList) {
			ResolvedDecision rd = new ResolvedDecision();
			rd.setDirective(IcmReleaseDirective.valueOf(config.getProperty("icm.decisionOfLastResort", true)));
			rd.setInfoId(pd.getInfoId());
			rd.setValue(pd.getValue());
			PolicyId dpi = new PolicyId();
			dpi.setBaseId("default");
			dpi.setVersion("1");
			rd.setPolicyId(dpi);
			resolvedDecisions.add(rd);
			rv.add(pd);
		}
		
		pendingList.removeAll(rv);
		rv.clear();
		
		// Here's where we really diverge again from the ARPSI paradigm.
		// Build out the response object based on the resolved decisions from the ICM and the decision 
		// results from the two back-end systems.  We take care to track the policy numbers properly in 
		// both directions (front and back).
		
		// dro is the putative icm DecisionResponseObject that needs to be populated with "real" decisions
		dro.setArrayOfInfoDecisionStatement(new ArrayList<IcmDecisionsForInfoStatement>());
		
		HashMap<DecisionsForInfoDiscriminator, IcmDecisionOnValues> dovBuilt = new HashMap<DecisionsForInfoDiscriminator, IcmDecisionOnValues>();
		HashMap<DecisionsForInfoDiscriminator, String> doavBuilt = new HashMap<DecisionsForInfoDiscriminator, String>();
		for (ResolvedDecision rd : resolvedDecisions) {
			// DEBUG only -- rgc
			IcmUtility.locLog("LOG9003",LogCriticality.debug, rd.getInfoId().getInfoValue(),rd.getValue(),rd.getDirective().toString());
	
			DecisionsForInfoDiscriminator did = new DecisionsForInfoDiscriminator();
			did.setInfoId(rd.getInfoId());
			did.setIcm_policyId(rd.getPolicyId());
			// find the appropriate entry in the appropriate source policy
			String finalDirective = null;
			PolicyId finalPid = new PolicyId();
			String finalOtherDirective = null;
			
			
			if (rd.getDirective().equals(IcmReleaseDirective.valueOf("ARPSI"))) {
				// find it in the ARPSI returned policy
				IcmUtility.locLog("LOG9000",LogCriticality.debug,rd.getInfoId().getInfoValue() + "," + rd.getValue());
				did.setPolicySource("ARPSI");
				adfisLoop:
				for (DecisionsForInfoStatement adfis : arpsiDecision.getArrayOfInfoDecisionStatement()) {
					if (adfis.getInfoId().getInfoType().equals(rd.getInfoId().getInfoType()) && adfis.getInfoId().getInfoValue().equals(rd.getInfoId().getInfoValue())) {
						// match on info id -- find the value return
						for (DecisionOnValues adov : adfis.getArrayOfDecisionOnValues()) {
							// For international strings, we have to compare twice
							try {
								if (adov.getValuesList().contains(rd.getValue()) || adov.getValuesList().contains(new String(rd.getValue().getBytes("ISO-8859-1"),"UTF-8")) || adov.getValuesList().contains(new String(rd.getValue().getBytes("UTF-16"),"UTF-8")) || adov.getValuesList().contains(new String(rd.getValue().getBytes("UTF-8"),"UTF-16")) || adov.getValuesList().contains(new String(rd.getValue().getBytes("ISO-8859-1"),"UTF-16")) || adov.getValuesList().contains(new String(rd.getValue().getBytes("UTF-16"),"ISO-8859-1")) || adov.getValuesList().contains(new String(rd.getValue().getBytes("UTF-8"),"ISO-8859-1")) || adov.getValuesList().contains(new String(rd.getValue().getBytes("ISO-8859-1"),"US-ASCII"))) {
									// 	we have a winner
									finalDirective = adov.getReleaseDecision().toString();
									finalPid = adov.getPolicyId();
									break adfisLoop;
								} else {
									IcmUtility.locLog("LOG9002",LogCriticality.debug,"ADOV(0)->"+adov.getValuesList().get(0), "RD->"+rd.getValue());
								}
							} catch (Exception e) {
								//ignore and fall through to the fallback if we cannot evaluate
							}
						}
						if (finalDirective == null) {
							// No exact match - use the all other values instead
							// Log the strings involved
							finalDirective = adfis.getDecisionOnAllOtherValues().getReleaseDecision().toString();
							finalPid = adfis.getDecisionOnAllOtherValues().getPolicyId();
						}
						// Regardless, set finalOtherDirective
						if (adfis.getDecisionOnAllOtherValues() != null && adfis.getDecisionOnAllOtherValues().getReleaseDecision() != null)
							finalOtherDirective = adfis.getDecisionOnAllOtherValues().getReleaseDecision().toString();
					}
				}
				if (finalDirective == null) {
					// This is an error -- the ARPSI did not return a result we can use
					return IcmUtility.locError(500, "ERR0061",LogCriticality.error,rd.getInfoId().getInfoValue(),rd.getValue());
				}
				did.setDirective(finalDirective);
				IcmUtility.locLog("LOG9001",LogCriticality.debug,rd.getInfoId().getInfoValue() + "," + rd.getValue(),finalDirective);
			} else {
				// Return the value from the COPSU
				did.setPolicySource("COPSU");
				cdfisLoop:
				for (edu.internet2.consent.copsu.model.DecisionsForInfoStatement cdfis : copsuDecision.getArrayOfInfoDecisionStatement()) {
					if (cdfis.getInfoId().getInfoType().equals(rd.getInfoId().getInfoType()) && cdfis.getInfoId().getInfoValue().equals(rd.getInfoId().getInfoValue())) {
						// match on info id -- find the value return
						for (edu.internet2.consent.copsu.model.DecisionOnValues cdov : cdfis.getArrayOfDecisionOnValues()) {
							if (cdov.getReturnedValuesList().contains(rd.getValue())) {
								// we have a winner
								// but if the winner is "useAdvice", we have to do something different...
								if (! cdov.getReleaseDecision().toString().equalsIgnoreCase("useAdvice")) {
									finalDirective = cdov.getReleaseDecision().toString();
								} else { // useAdvice case
									// here, we have to repeat the ARPSI filter (expensively, unfortunately) to 
									// get the real finalDirective, which matches what would have been generated by the 
									// ARPSI sieve.
									adfisLoop2:
										for (DecisionsForInfoStatement adfis : arpsiDecision.getArrayOfInfoDecisionStatement()) {
											if (adfis.getInfoId().getInfoType().equals(rd.getInfoId().getInfoType()) && adfis.getInfoId().getInfoValue().equals(rd.getInfoId().getInfoValue())) {
												// match on info id -- find the value return
												for (DecisionOnValues adov : adfis.getArrayOfDecisionOnValues()) {
													if (adov.getValuesList().contains(rd.getValue())) {
														// we have a winner
														finalDirective = adov.getReleaseDecision().toString();
														break adfisLoop2;
													}
												}
												if (finalDirective == null) {
													// No exact match - use the all other values instead
													finalDirective = adfis.getDecisionOnAllOtherValues().getReleaseDecision().toString();
												}
											}
										}
								}
								// DEBUG only -- rgc
								IcmUtility.locLog("LOG9005", LogCriticality.debug, cdfis.getInfoId().getInfoValue(),rd.getValue(),cdov.getReleaseDecision().toString());
								finalPid.setBaseId(cdov.getPolicyId().getBaseId());
								finalPid.setVersion(cdov.getPolicyId().getVersion());
								break cdfisLoop;
							}
						}
						if (finalDirective == null) {
							// No exact match - use the all other values instead
							finalDirective = cdfis.getDecisionOnAllOtherValues().getReleaseDecision().toString();
							finalPid.setBaseId(cdfis.getDecisionOnAllOtherValues().getPolicyId().getBaseId());
							finalPid.setVersion(cdfis.getDecisionOnAllOtherValues().getPolicyId().getVersion());
						}
						// Regardless, set finalOtherDirective
						if (cdfis.getDecisionOnAllOtherValues() != null && cdfis.getDecisionOnAllOtherValues().getReleaseDecision() != null)
							finalOtherDirective = cdfis.getDecisionOnAllOtherValues().getReleaseDecision().toString();
					}
				}
				if (finalDirective == null) {
					// This is an error -- the COPSU did not return a result we can use
					return IcmUtility.locError(500, "ERR0062",LogCriticality.error,rd.getInfoId().getInfoValue(),rd.getValue());
				}
				did.setDirective(finalDirective);
			}
			did.setPolicyId(finalPid);
			
			if (!dovBuilt.containsKey(did)) {
				IcmDecisionOnValues dov = new IcmDecisionOnValues();
				AugmentedPolicyId api = new AugmentedPolicyId();
				api.setICM_policyBaseId(rd.getPolicyId().getBaseId());
				api.setICM_policyVersion(rd.getPolicyId().getVersion());
				api.setPolicySource(PolicySourceEnum.valueOf(rd.getDirective().toString()));
				api.setPolicyBaseId(did.getPolicyId().getBaseId());
				api.setPolicyVersion(did.getPolicyId().getVersion());
				dov.setAugmentedPolicyId(api);
				dov.setReleaseDecision(UserReleaseDirective.valueOf(finalDirective));
				dov.setReturnedValuesList(new ArrayList<String>());
				dovBuilt.put(did,dov);
			} 
			// Regardless, use it
			dovBuilt.get(did).getReturnedValuesList().add(rd.getValue());
			// DEBUG only -- rgc
			IcmUtility.locLog("LOG9004", LogCriticality.debug, rd.getValue(),finalDirective);
			if (!doavBuilt.containsKey(did) && finalOtherDirective != null) {
				doavBuilt.put(did, finalOtherDirective);
			}
		}
		HashMap<InfoId,IcmDecisionsForInfoStatement> dfisBuilt = new HashMap<InfoId,IcmDecisionsForInfoStatement>();
		for (DecisionsForInfoDiscriminator did : dovBuilt.keySet()) {
			InfoId ii = did.getInfoId();
			if (!dfisBuilt.containsKey(ii)) {
				IcmDecisionsForInfoStatement dfis = new IcmDecisionsForInfoStatement();
				dfis.setInfoId(ii);
				dfis.setArrayOfDecisionOnValues(new ArrayList<IcmDecisionOnValues>());
				if (allOtherDecisions.containsKey(ii)) {
					dfis.setDecisionOnAllOtherValues(new IcmDecisionOnAllOtherValues());
					dfis.getDecisionOnAllOtherValues().setAugmentedPolicyId(dovBuilt.get(did).getAugmentedPolicyId());
					dfis.getDecisionOnAllOtherValues().setAllOtherValuesConst(AllOtherValuesConst.allOtherValues);
					if (doavBuilt.containsKey(did)) {
						dfis.getDecisionOnAllOtherValues().setReleaseDecision(UserReleaseDirective.valueOf(doavBuilt.get(did)));
					}
				} else {
					// no default
					// Actually, we need a default value, and the default value if none is specified will be "askMe"
					AugmentedPolicyId defapi = new AugmentedPolicyId();
					defapi.setICM_policyBaseId("default");
					defapi.setICM_policyVersion("1");
					defapi.setPolicyBaseId("default");
					defapi.setPolicySource(PolicySourceEnum.COPSU);
					defapi.setPolicyVersion("1");
					dfis.setDecisionOnAllOtherValues(new IcmDecisionOnAllOtherValues());
					dfis.getDecisionOnAllOtherValues().setAugmentedPolicyId(defapi);
					dfis.getDecisionOnAllOtherValues().setAllOtherValuesConst(AllOtherValuesConst.allOtherValues);
					dfis.getDecisionOnAllOtherValues().setReleaseDecision(UserReleaseDirective.valueOf("askMe"));
				}
				dfisBuilt.put(ii, dfis);
			}
			dfisBuilt.get(ii).getArrayOfDecisionOnValues().add(dovBuilt.get(did));
		}
		for (InfoId ii : dfisBuilt.keySet()) {
			dro.getArrayOfInfoDecisionStatement().add(dfisBuilt.get(ii));
		}
		try {
			return buildResponse(Status.OK,dro.toJSON());
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0016",LogCriticality.error);
		} finally {
			if (sess != null) {
				sess.close();
			}
		}
	}
}
