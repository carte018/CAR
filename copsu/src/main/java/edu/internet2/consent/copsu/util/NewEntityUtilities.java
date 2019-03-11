package edu.internet2.consent.copsu.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import edu.internet2.consent.copsu.cfg.CopsuConfig;
import edu.internet2.consent.copsu.model.AllOtherInfoId;
import edu.internet2.consent.copsu.model.AllOtherInfoReleaseStatement;
import edu.internet2.consent.copsu.model.AllOtherInfoTypeConst;
import edu.internet2.consent.copsu.model.AllOtherInfoValueConst;
import edu.internet2.consent.copsu.model.AllOtherValuesConst;
import edu.internet2.consent.copsu.model.CreatorId;
import edu.internet2.consent.copsu.model.DirectiveAllOtherValues;
import edu.internet2.consent.copsu.model.InfoReleasePolicy;
import edu.internet2.consent.copsu.model.InfoReleaseStatement;
import edu.internet2.consent.copsu.model.NewRPTemplateTypeConst;
import edu.internet2.consent.copsu.model.NewRPTemplateValueConst;
import edu.internet2.consent.copsu.model.PolicyId;
import edu.internet2.consent.copsu.model.PolicyMetadata;
import edu.internet2.consent.copsu.model.PolicyState;
import edu.internet2.consent.copsu.model.RelyingPartyId;
import edu.internet2.consent.copsu.model.ResourceHolderId;
import edu.internet2.consent.copsu.model.ReturnedChangeOrder;
import edu.internet2.consent.copsu.model.ReturnedPolicy;
import edu.internet2.consent.copsu.model.UserId;

public class NewEntityUtilities {
	
	@SuppressWarnings("unused")
	private static Log LOG = LogFactory.getLog(NewEntityUtilities.class);
	
	// Class wrapper for shared static functions for new entities
	// Create a new RP policy for a user based on the user's new RP template
	// We assume the new RP template already exists for the user due to other constraints.
	//
	public static ReturnedPolicy createNewRPPolicyFromNewRPTemplate(UserId user, RelyingPartyId rp, ResourceHolderId rh) {
		// Get a session from Hibernate
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			throw new RuntimeException(CopsuUtility.locError(500, "ERR0018").getEntity().toString()); // throw here, since we're not inline
		}
		
		ReturnedPolicy newrpt = new ReturnedPolicy();
		
		Transaction tx = null;
		
		try{
		// Start with new metadata
		PolicyMetadata md = new PolicyMetadata();
		//Create time is now
		md.setCreateTime(System.currentTimeMillis());
		// Creator is SYSTEM/COPSU
		CreatorId creator = new CreatorId();
		creator.setCreatingUserType("SYSTEM");
		creator.setCreatingUserValue("COPSU");
		md.setCreator(creator);
		// new RP Template is active to start with
		md.setState(PolicyState.active);
		// new RP Template version is "1", policy id is unique
		PolicyId pid = new PolicyId();
		pid.setVersion("1");
		// Get a unique UUID
		String unusedUUID = null;
		while (unusedUUID == null) {
			Query<ReturnedPolicy> collisionCheckQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId",ReturnedPolicy.class);
			unusedUUID = UUID.randomUUID().toString();
			collisionCheckQuery.setParameter("baseId", unusedUUID);
			List<ReturnedPolicy> cl = collisionCheckQuery.list();
			if (cl != null && ! cl.isEmpty()) {
				unusedUUID = null;   // that one is taken
			}
		}
		pid.setBaseId(unusedUUID);
		md.setPolicyId(pid);
		
		// Construct the new InfoRelease policy from the new RP template for the user
		//
		Query<ReturnedPolicy> getNewRPTemplate = sess.createQuery("from ReturnedPolicy where policyMetaData.state=0 and infoReleasePolicy.userId.userValue=:userValue and infoReleasePolicy.relyingPartyId.RPvalue=:rpValue",ReturnedPolicy.class);
		getNewRPTemplate.setParameter("userValue", user.getUserValue());
		getNewRPTemplate.setParameter("rpValue", NewRPTemplateValueConst.newRPTemplateValue.toString());
		List<ReturnedPolicy> pl = getNewRPTemplate.list();
		// if the policy doesn't exist, it's a major error
		if (pl == null || pl.isEmpty()) {
			throw new RuntimeException(CopsuUtility.locError(500, "ERR0037","RP template missing for " + user.getUserValue()).getEntity().toString());
		}
		newrpt.setPolicyMetaData(md);
		//
		// It turns out that simply embedding the same infReleasePolicy results in a conflict in the 
		// Hibernate flush -- the arrays in the info release policy are shared in the persistence layer
		// Hence we have to copy the data, rather than linking in place 
		
		// Copy the inforelease policy
		InfoReleasePolicy irp = new InfoReleasePolicy();
		irp.setUserId(pl.get(0).getInfoReleasePolicy().getUserId());
		irp.setDescription("Default policy for " + rp.getRPvalue());
		irp.setRelyingPartyId(rp);
		irp.setResourceHolderId(rh);
		irp.setWhileImAwayDirective(pl.get(0).getInfoReleasePolicy().getWhileImAwayDirective());
		ArrayList<InfoReleaseStatement> ari = new ArrayList<InfoReleaseStatement>();
		for (InfoReleaseStatement is : pl.get(0).getInfoReleasePolicy().getArrayOfInfoReleaseStatement()) {
			ari.add(is);
		}
		irp.setAllOtherInfoReleaseStatement(pl.get(0).getInfoReleasePolicy().getAllOtherInfoReleaseStatement());
		
		newrpt.setInfoReleasePolicy(irp);
		
		// And persist the new policy
		tx = sess.beginTransaction();
		sess.save(newrpt);
		tx.commit();
		return newrpt;
		
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			return null;
		} finally {
			if (sess != null) {
				try {
					sess.close();
				} catch (Exception i) {
					//ignore
				}
			}
		}
	}
	
	// Utility method for creating a new RP template for a given user
	// Used only when there is no new RP template for a user and one is required by the API
	//
	public static ReturnedPolicy createNewRPTemplateForUser(UserId user) {
		// We presume that we are only called if no RP Template exists for the user, 
		// so we can simply create the template without checking
		// A NewRPTemplate is simply a ReturnedPolicy that contains the key constants for new RP templates
		// It is now created based on a new user config conveyed in a well-known Change Order, created at 
		// inception of the instance.
		
		CopsuConfig config = null;
		try {
			config = CopsuConfig.getInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed retrieving copsu configuration");
		}
		
		// Get a session from Hibernate
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			throw new RuntimeException(CopsuUtility.locError(500, "ERR0018").getEntity().toString());
		}

		ReturnedPolicy newrpt = new ReturnedPolicy();
		
		Transaction tx = null;
		
		// Make this idempotent (to facilitate calling without prior testing where appropriate)
		//
		Query<ReturnedPolicy> idempotentTest = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue = :userValue and infoReleasePolicy.relyingPartyId.RPvalue = :rpValue",ReturnedPolicy.class);
		idempotentTest.setParameter("userValue", user.getUserValue());
		idempotentTest.setParameter("rpValue", "newRPTemplateValue");
		List<ReturnedPolicy> idemResult = idempotentTest.list();
		if (idemResult != null && ! idemResult.isEmpty()) {
			// already one there, so we can simply return it
			sess.close();
			return idemResult.get(0);
		}
		Query<ReturnedChangeOrder> rcoQuery = sess.createQuery("from ReturnedChangeOrder where changeOrderMetaData.changeOrderId = :changeId",ReturnedChangeOrder.class);
		rcoQuery.setParameter("changeId","NewUserChangeOrder");
		List<ReturnedChangeOrder> lrco = rcoQuery.list();
		if (lrco.isEmpty()) {
			throw new RuntimeException(CopsuUtility.locError(500, "ERR0037","Failed to retrieve nwe user configuration").getEntity().toString());
		}
		
		ReturnedChangeOrder newUserChangeOrder = lrco.get(0);
		
		// Now we have the change order to build from...
		try{
		// Start with new metadata
		PolicyMetadata md = new PolicyMetadata();
		//Create time is now
		md.setCreateTime(System.currentTimeMillis());
		// Creator is SYSTEM/COPSU
		CreatorId creator = new CreatorId();
		creator.setCreatingUserType("SYSTEM");
		creator.setCreatingUserValue("COPSU");
		md.setCreator(creator);
		// new RP Template is active to start with
		md.setState(PolicyState.active);
		// new RP Template version is "1", policy id is unique
		PolicyId pid = new PolicyId();
		pid.setVersion("1");
		md.setChangeOrder("https://"+config.getProperty("copsu.server.name", true)+"/consent/v1/copsu/user-info-release-change-orders/NewUserChangeOrder");
		// Get a unique UUID
		String unusedUUID = null;
		while (unusedUUID == null) {
			Query<ReturnedPolicy> collisionCheckQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.policyId.baseId = :baseId",ReturnedPolicy.class);
			unusedUUID = UUID.randomUUID().toString();
			collisionCheckQuery.setParameter("baseId", unusedUUID);
			List<ReturnedPolicy> cl = collisionCheckQuery.list();
			if (cl != null && ! cl.isEmpty()) {
				unusedUUID = null;   // that one is taken
			}
		}
		pid.setBaseId(unusedUUID);
		md.setPolicyId(pid);
		
		// Construct the inforelease policy
		InfoReleasePolicy irp = new InfoReleasePolicy();
		
		// Set up the new RP template value from the new User template (static and hard coded)
		//
		// display name
		irp.setDescription("New RP Template");
		// User Id is the incoming user id
		irp.setUserId(user);
		// Relying Party Id is the special newRPTemplate relying party id constant
		RelyingPartyId rpi = new RelyingPartyId();
		rpi.setRPtype(NewRPTemplateTypeConst.newRPTemplateType.toString());
		rpi.setRPvalue(NewRPTemplateValueConst.newRPTemplateValue.toString());
		irp.setRelyingPartyId(rpi);
		// Special Case:  Resource Holder Id is newRPTemplate / newRPTemplate
		// for newRPTemplate entries
		ResourceHolderId rhi = new ResourceHolderId();
		rhi.setRHType("newRPTemplate");
		rhi.setRHValue("newRPTemplate");
		irp.setResourceHolderId(rhi);
		// WhileImAway directive comes from the new user change order
		
		irp.setWhileImAwayDirective(newUserChangeOrder.getChangeOrder().getWhileImAwayDirective());
		
		//
		// Typically there will be no specific info release statements in the change order, but in the event
		// there are, the newRPTemplate must match
		//
		if (newUserChangeOrder.getChangeOrder().getArrayOfInfoReleaseStatement() != null && !newUserChangeOrder.getChangeOrder().getArrayOfInfoReleaseStatement().isEmpty()) {
			irp.getArrayOfInfoReleaseStatement().addAll(newUserChangeOrder.getChangeOrder().getArrayOfInfoReleaseStatement());
		}
		// AllOtherInfoReleaseStatement is set to match the one in the new user change order
		
		AllOtherInfoReleaseStatement ais = new AllOtherInfoReleaseStatement();
		AllOtherInfoId aid = new AllOtherInfoId();
		aid.setAllOtherInfoType(AllOtherInfoTypeConst.allOtherInfoType);
		aid.setAllOtherInfoValue(AllOtherInfoValueConst.allOtherInfoValue);
		ais.setAllOtherInfoId(aid);
		DirectiveAllOtherValues dav = new DirectiveAllOtherValues();
		dav.setAllOtherValues(AllOtherValuesConst.allOtherValues);
		dav.setReleaseDirective(newUserChangeOrder.getChangeOrder().getAllOtherInfoReleaseStatement().getDirectiveAllOtherValues().getReleaseDirective());
		ais.setDirectiveAllOtherValues(dav);
		irp.setAllOtherInfoReleaseStatement(ais);
		//Merge them into a new policy object
		newrpt.setPolicyMetaData(md);
		newrpt.setInfoReleasePolicy(irp);
		
		// Now, persist the new policy object
		tx = sess.beginTransaction();
		sess.save(newrpt);
		tx.commit();
		return newrpt;
	} catch (Exception e) {
		CopsuUtility.infoLog("LOG0014",user.getUserValue(),e.getMessage());
			if (tx != null) {
			tx.rollback();
		}
		return null;
	} finally {
		try {
			if (sess != null) {
				sess.close();
			}
		} catch (Exception i) {
			//ignore
		}
	}
	}
	
	public static ReturnedPolicy retrievePolicy(UserId userId, RelyingPartyId rpId, ResourceHolderId rhId) {
		// Given a userId, an rpId, and an rhId, return the policy that should be controlling for it.
		// If a policy is found in the COPSU persistence store, return it.
		// If a policy is not found in the COPSU persistence store, perform an idempotent create of the 
		// user's newRP Template and then create a policy for the RP from the new RP template and returne it.
		// 
		// Establish a session.
		
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			throw new RuntimeException(CopsuUtility.locError(500, "ERR0018").getEntity().toString());
		}
		
		// First, try to find the matching policy
		Query<ReturnedPolicy> findControlling = sess.createQuery("from ReturnedPolicy where infoReleasePolicy.userId.userValue = :userValue and infoReleasePolicy.relyingPartyId.RPvalue = :rpValue and infoReleasePolicy.resourceHolderId.RHValue = :rhValue and policyMetaData.state = 0",ReturnedPolicy.class);
		findControlling.setParameter("userValue", userId.getUserValue());
		findControlling.setParameter("rpValue",  rpId.getRPvalue());
		findControlling.setParameter("rhValue", rhId.getRHValue());
		List<ReturnedPolicy> clist = findControlling.list();
		if (clist != null && ! clist.isEmpty()) {
			// we got one -- return it
			sess.close();
			return clist.get(0);
		}
		// Otherwise, we did not find the policy we need. 
		// Create it from whole cloth.
		
		// First, clean up the existing session
		if (sess != null) 
			sess.close();

		// Idempotently create the user's new RP template
		createNewRPTemplateForUser(userId);
		
		// Then build a policy for the situation and return it
		
		return createNewRPPolicyFromNewRPTemplate(userId, rpId, rhId);
	}
}
