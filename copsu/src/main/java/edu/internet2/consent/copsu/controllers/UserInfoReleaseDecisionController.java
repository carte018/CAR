package edu.internet2.consent.copsu.controllers;

import java.util.ArrayList;
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
import org.hibernate.Session;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.copsu.cfg.CopsuConfig;
import edu.internet2.consent.copsu.model.AllOtherValuesConst;
import edu.internet2.consent.copsu.model.DecisionOnAllOtherValues;
import edu.internet2.consent.copsu.model.DecisionOnValues;
import edu.internet2.consent.copsu.model.DecisionRequestObject;
import edu.internet2.consent.copsu.model.DecisionResponseObject;
import edu.internet2.consent.copsu.model.DecisionsForInfoStatement;
import edu.internet2.consent.copsu.model.DirectiveOnValues;
import edu.internet2.consent.copsu.model.InfoId;
import edu.internet2.consent.copsu.model.InfoIdPlusValues;
import edu.internet2.consent.copsu.model.InfoReleaseStatement;
import edu.internet2.consent.copsu.model.LogCriticality;
import edu.internet2.consent.copsu.model.ReleaseDirective;
import edu.internet2.consent.copsu.model.ReturnedPolicy;
import edu.internet2.consent.copsu.model.ValueObject;
import edu.internet2.consent.copsu.util.CopsuUtility;
import edu.internet2.consent.copsu.util.NewEntityUtilities;
import edu.internet2.consent.exceptions.CopsuInitializationException;

@Path("/user-info-release-decision")
public class UserInfoReleaseDecisionController {
	@SuppressWarnings("unused")
	private static String caller = ""; // calling user/system
	@SuppressWarnings("unused")
	private static Log LOG = LogFactory.getLog(UserInfoReleaseDecisionController.class);
	
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
	
	
	// No authentication support at the moment, although we call a stub authorization check in
	// each endpoint.
	//
	
	// CORS support for Swagger.io
	
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		return buildResponse(Status.OK,"");
	}
	
	// The meat
	
	@POST
	@Path("/")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response postRootDecisionRequest(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		
		// Handle root POST requests.
		// Input should be a well-formed decisionRequest containing (at a minimum):
		//    userId
		//    relyingPartyId
		//    resourceHolderId
		//    arrayOfInfoIdsPlusValues
		//
		@SuppressWarnings("unused")
		CopsuConfig config = null;
		try {
			config = CopsuUtility.init("postDecisionRequest", request, headers, null);
		} catch (CopsuInitializationException e) {
			return CopsuUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		// Parse the input document
		ObjectMapper mapper = new ObjectMapper();
		DecisionRequestObject inputRequest = null;
		
		try {
			inputRequest = mapper.readValue(entity, DecisionRequestObject.class);
		} catch (JsonParseException pe) {
			return CopsuUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException me) {
			return CopsuUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception ioe) {
			return CopsuUtility.locError(500, "ERR0007",LogCriticality.error);
		}
		
		// Now we have the inputRequest parsed -- check for mandatory fields in the JSON
		
		if (inputRequest == null) {
			// Null input is invalid
			return CopsuUtility.locError(400, "ERR0008",LogCriticality.info);
		}
		
		if (inputRequest.getUserId() == null) {
			return CopsuUtility.locError(400, "ERR0010",LogCriticality.info);
		}
		
		if (inputRequest.getRelyingPartyId() == null) {
			return CopsuUtility.locError(400, "ERR0011",LogCriticality.info);
		}
		
		if (inputRequest.getResourceHolderId() == null) {
			return CopsuUtility.locError(400, "ERR0012",LogCriticality.info);
		}
		
		if (inputRequest.getArrayOfInfoIdsPlusValues() == null) {
			return CopsuUtility.locError(400, "ERR0035",LogCriticality.info);
		}
		
		// Required inputs are present.
		
		// This is a request for a decision.  Oddly, this could be the first time we've ever heard of this 
		// user.  In that case, we'll have no policy to work with.  We use the idempotent newRPTemplate
		// creator to ensure that a new RP template exists before we start anything else.
		
		try {
			NewEntityUtilities.createNewRPTemplateForUser(inputRequest.getUserId());
		} catch (Exception e) {
			CopsuUtility.locLog("LOG",LogCriticality.info,e.getMessage());
			// ignore -- failure to create the new RP template is not an error if one happens to exist
			// We handle failsafe later.
		}
		
		// We are now ready to start building up a decision response for this decision request.
		DecisionResponseObject dro = new DecisionResponseObject();
		
		// Create the list needed for infodecisionstatements
		dro.setArrayOfInfoDecisionStatement(new ArrayList<DecisionsForInfoStatement>());
		
		// Force load the JDBC driver if necessary
		Session sess = CopsuUtility.getHibernateSession();

		ReturnedPolicy applicablePolicy = null;
		try {
		// Prepare our Hibernate session

		// Our goal is to construct a response that addresses all of the requested items in the inputRequest
		// object.  We start by looking for the controlling COPSU policy for the specific request.  Since 
		// requests are specific to particular (user,rp,rh) combinations, as are COPSU policies, we can
		// retrieve the one policy that applies.
		// This call creates the policy as required to get a response back.
		
		applicablePolicy = NewEntityUtilities.retrievePolicy(inputRequest.getUserId(),inputRequest.getRelyingPartyId(),inputRequest.getResourceHolderId());
		
		// At this point, we have the applicable policy in hand.  Use it to populate the response.
		// An applicable policy is *always* returned, even if it is entirely synthetic
		
		// For the COPSU, we can ignore userInfo and rpInfo (since they don't come into play for user specific
		// policies.
		
		DecisionOnAllOtherValues allOtherValuesDecision = new DecisionOnAllOtherValues();
		allOtherValuesDecision.setAllOtherValuesConst(AllOtherValuesConst.allOtherValues);
		for (InfoIdPlusValues ipv : inputRequest.getArrayOfInfoIdsPlusValues()) {
			// Iterate over the requested infoids with their value lists
			//
			InfoId iid = ipv.getInfoId();
			// As there are four possible decision responses, each of which needs to be grouped in
			// the response, we build four separate lists to work with here
			ArrayList<String> permitValues = new ArrayList<String>();
			ArrayList<String> denyValues = new ArrayList<String>();
			ArrayList<String> askMeValues = new ArrayList<String>();
			ArrayList<String> useAdviceValues = new ArrayList<String>();
			
			ArrayList<String> valuesOfInterest = new ArrayList<String>();
			if (! ipv.getInfoItemValues().contains("allValues")) {
				// use input list
				valuesOfInterest.addAll(ipv.getInfoItemValues());
			} else {
				// enumerate all the values for which this user has specific directives
				for (InfoReleaseStatement irs : applicablePolicy.getInfoReleasePolicy().getArrayOfInfoReleaseStatement()) {
					if (irs.getInfoId().getInfoType().equals(iid.getInfoType()) && irs.getInfoId().getInfoValue().equals(iid.getInfoValue())) {
						// this is the one - iterate to add the values
						for (DirectiveOnValues explicit : irs.getArrayOfDirectiveOnValues()) {
							for (ValueObject v : explicit.getValueObjectList()) {
								valuesOfInterest.add(v.getValue());
							}
						}
					}
				}
			}
			for (String iiv : valuesOfInterest) {
				// For every requested value of this info item...or every explicitly set if "all" requested
				// Find the applicable response and add the value to the appropriate list
				boolean foundReleaseDirective = false;
				boolean foundInfoId = false;
				if (applicablePolicy != null && applicablePolicy.getInfoReleasePolicy() != null && applicablePolicy.getInfoReleasePolicy().getArrayOfInfoReleaseStatement() != null)
				for (InfoReleaseStatement irs : applicablePolicy.getInfoReleasePolicy().getArrayOfInfoReleaseStatement()) {
					if (irs.getInfoId() != null && irs.getInfoId().getInfoType().equals(iid.getInfoType()) && irs.getInfoId().getInfoValue().equals(iid.getInfoValue())) {
						foundInfoId = true;
						// Set the all other values decision while we're here
						allOtherValuesDecision.setReleaseDecision(irs.getDirectiveAllOtherValues().getReleaseDirective());
						allOtherValuesDecision.setPolicyId(applicablePolicy.getPolicyMetaData().getPolicyId());
						boolean foundValueStatement = false;
						for (DirectiveOnValues dov : irs.getArrayOfDirectiveOnValues()) {
							ReleaseDirective rd = dov.getReleaseDirective();
							for (ValueObject vo : dov.getValueObjectList()) {
								if (vo.getValue().equals(iiv)) {
									// This is the response
									switch(rd) {
									case permit:
										permitValues.add(iiv);
										foundReleaseDirective = true;
										foundValueStatement = true;
										break;
									case deny:
										denyValues.add(iiv);
										foundReleaseDirective = true;
										foundValueStatement = true;
										break;
									case askMe:
										askMeValues.add(iiv);
										foundReleaseDirective = true;
										foundValueStatement = true;
										break;
									case useAdvice:
										useAdviceValues.add(iiv);
										foundReleaseDirective = true;
										foundValueStatement = true;
										break;
									}
								}
							}
						}
						if (! foundValueStatement) {
							// No specific value statement -- use the allOtherValues value
							switch(irs.getDirectiveAllOtherValues().getReleaseDirective()) {
							case permit:
								permitValues.add(iiv);
								foundReleaseDirective = true;
								break;
							case deny:
								denyValues.add(iiv);
								foundReleaseDirective = true;
								break;
							case askMe:
								askMeValues.add(iiv);
								foundReleaseDirective = true;
								break;
							case useAdvice:
								useAdviceValues.add(iiv);
								foundReleaseDirective = true;
								break;
							}
						}
					}
				}
				if (! foundInfoId) {
					// There was no explicit directive in the policy -- use the allOtherInfoId directive
					allOtherValuesDecision.setPolicyId(applicablePolicy.getPolicyMetaData().getPolicyId());
					allOtherValuesDecision.setReleaseDecision(applicablePolicy.getInfoReleasePolicy().getAllOtherInfoReleaseStatement().getDirectiveAllOtherValues().getReleaseDirective());;
					switch(applicablePolicy.getInfoReleasePolicy().getAllOtherInfoReleaseStatement().getDirectiveAllOtherValues().getReleaseDirective()) {
					case permit:
						permitValues.add(iiv);
						foundReleaseDirective = true;
						break;
					case deny:
						denyValues.add(iiv);
						foundReleaseDirective = true;
						break;
					case askMe:
						askMeValues.add(iiv);
						foundReleaseDirective = true;
						break;
					case useAdvice:
						useAdviceValues.add(iiv);
						foundReleaseDirective = true;
						break;
					}
				}
				if (!foundReleaseDirective) {
					// If no directive can be descried for this case, we have an error
					sess.close();
					return CopsuUtility.locError(404, "ERR0036",LogCriticality.error,iiv,iid.getInfoValue());
				}
			}
			// For iid (one of the requested info IDs) we now have the breakout of what gets what response
			// We can now build the iid decisions for info statement and add it to the decision response object
			DecisionsForInfoStatement dfis = new DecisionsForInfoStatement();
			// build the list of decisionOnValues
			dfis.setArrayOfDecisionOnValues(new ArrayList<DecisionOnValues>());
			dfis.setInfoId(iid);
			dfis.setDecisionOnAllOtherValues(allOtherValuesDecision);
			if (!permitValues.isEmpty()) {
				// We have some permits
				DecisionOnValues permitDOV = new DecisionOnValues();
				permitDOV.setReleaseDecision(ReleaseDirective.permit);
				permitDOV.setReturnedValuesList(permitValues);
				permitDOV.setPolicyId(applicablePolicy.getPolicyMetaData().getPolicyId());
				dfis.getArrayOfDecisionOnValues().add(permitDOV);
			} else {
				// no permits
			}
			if (!denyValues.isEmpty()) {
				// We have some denys
				DecisionOnValues denyDOV = new DecisionOnValues();
				denyDOV.setReleaseDecision(ReleaseDirective.deny);
				denyDOV.setReturnedValuesList(denyValues);
				denyDOV.setPolicyId(applicablePolicy.getPolicyMetaData().getPolicyId());
				dfis.getArrayOfDecisionOnValues().add(denyDOV);
			} else {
				// no denies
			}
			if (!askMeValues.isEmpty()) {
				// we have some askMe values
				DecisionOnValues askMeDOV = new DecisionOnValues();
				askMeDOV.setReleaseDecision(ReleaseDirective.askMe);
				askMeDOV.setReturnedValuesList(askMeValues);
				askMeDOV.setPolicyId(applicablePolicy.getPolicyMetaData().getPolicyId());
				dfis.getArrayOfDecisionOnValues().add(askMeDOV);
			} else {
				// no askmes
			}
			if (!useAdviceValues.isEmpty()) {
				// We have some useAdvice values
				DecisionOnValues useAdviceDOV = new DecisionOnValues();
				useAdviceDOV.setReleaseDecision(ReleaseDirective.useAdvice);
				useAdviceDOV.setReturnedValuesList(useAdviceValues);
				useAdviceDOV.setPolicyId(applicablePolicy.getPolicyMetaData().getPolicyId());
				dfis.getArrayOfDecisionOnValues().add(useAdviceDOV);
			} else {
				// no useadvice
			}
			// And add it to the response
			dro.getArrayOfInfoDecisionStatement().add(dfis);
		}
		// Now fill in the metadata for the response from the request and what we know
		// Decision ID is a uuid -- no permanent storage we can rely upon, so we rely on randomness for uniqueness here
		dro.setDecisionId(UUID.randomUUID().toString());
		// userID is the incoming user ID
		dro.setUserId(inputRequest.getUserId());
		// ditto relying party
		dro.setRelyingPartyId(inputRequest.getRelyingPartyId());
		// and resource holder
		dro.setResourceHolderId(inputRequest.getResourceHolderId());
		// WhileImAwayDirective comes from the policy directly
		dro.setWhileImAwayDirective(applicablePolicy.getInfoReleasePolicy().getWhileImAwayDirective());
		// arrayOfInfoDecisionStatement has already been populated
		// Set decision time to now
		dro.setTimeOfDecision(String.valueOf(System.currentTimeMillis()));
		} catch (Exception e) {
			// leak prevention
			sess.close();
			return CopsuUtility.locError(500, "ERR0037",LogCriticality.error,e.getMessage());
		} 
		// And return
		try {
			// Log what we've done
			CopsuUtility.locLog("LOG0013",LogCriticality.info,dro.getDecisionId(),applicablePolicy.getPolicyMetaData().getPolicyId().getBaseId(),applicablePolicy.getPolicyMetaData().getPolicyId().getVersion());
			return buildResponse(Status.OK,dro.toJSON());
		} catch (Exception e) {
			return CopsuUtility.locError(500, "ERR0016", LogCriticality.error);
		} finally {
			sess.close();
		}
	}
}