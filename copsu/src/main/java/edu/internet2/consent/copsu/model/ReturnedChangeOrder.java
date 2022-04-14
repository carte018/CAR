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
package edu.internet2.consent.copsu.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.copsu.cfg.CopsuConfig;
import edu.internet2.consent.copsu.util.CopsuUtility;
import edu.internet2.consent.copsu.util.FactoryFactory;
import edu.internet2.consent.copsu.util.OMSingleton;

@javax.persistence.Entity
public class ReturnedChangeOrder {

	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long ChangeOrderIdentifier;

	
	// We don't embed the metadata, since it's used less frequently in searches
	@JsonProperty("changeOrderMetaData")
	private ChangeOrderMetaData changeOrderMetaData;
	
	// We embed the order itself, since it is used extensively in searches
	@JsonProperty("changeOrder")
	@Embedded
	private ChangeOrder changeOrder;

	public Long getChangeOrderIdentifier() {
		return ChangeOrderIdentifier;
	}

	public void setChangeOrderIdentifier(Long changeOrderIdentifier) {
		ChangeOrderIdentifier = changeOrderIdentifier;
	}

	public ChangeOrderMetaData getChangeOrderMetaData() {
		return changeOrderMetaData;
	}

	public void setChangeOrderMetaData(ChangeOrderMetaData changeOrderMetaData) {
		this.changeOrderMetaData = changeOrderMetaData;
	}

	public ChangeOrder getChangeOrder() {
		return changeOrder;
	}

	public void setChangeOrder(ChangeOrder changeOrder) {
		this.changeOrder = changeOrder;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ReturnedChangeOrder rco = (ReturnedChangeOrder)o;
		return(rco.getChangeOrderMetaData().equals(this.getChangeOrderMetaData()) && rco.getChangeOrder().equals(this.getChangeOrder()));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(changeOrderMetaData,changeOrder);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ReturnedChangeOrder {\n");
		sb.append("changeOrderMetaData: ").append(changeOrderMetaData.toString()).append("\n");
		sb.append("changeOrder: ").append(changeOrder.toString()).append("\n");
		sb.append("}");
		return sb.toString();
	}
	
	public String toJSON() throws JsonProcessingException{
		// Return JSON representation of self
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		String retval = mapper.writeValueAsString(this);
		return retval;
	}
	
	public void apply() throws Exception {
		// Here, we apply the attached change order to the database.
		//
		// This is hairy.
		// We are never called for type 3 (createNewPolicyFromNewUserconfig) type requests, 
		// only for changePoliciesAboutSpecificRP or fixErrorInPolicy requests.
		//
		// We apply roughly as follows
		// 
		//  Inside a Hibernate transaction
		//	 try:
		//    if a list of policy IDs is present, retrieve those specific policies
		//    otherwise
		//    For each userId specified (or all userIds, if the allUsers value is present in the list)
		//       For each relying party specified (or all RPs if the allRPs value is present in the list)
		//           Retrieve the associated policy or policies
		//    For each retrieved policy
		//           Update its parameters according to the specifiers in the change order
		//		     Modify its changeorder value to match the change order number in this change order
		//           Modify its creator to SYSTEM/COPSU to indicate that a change order was applied.  
		//   catch:
		//		rollback the transaction
		//      close the session
		//	    throw a runtime exception
		//    close the transaction
		//    close the session
		//    return (void)
		
		CopsuConfig config = CopsuConfig.getInstance();
		
		// Get a Hibernate session/transaction started
		SessionFactory sf = FactoryFactory.getSessionFactory();
		Session sess = sf.openSession();
		Transaction tx = sess.beginTransaction();
		
		try {
		// Note that it will be inefficient (and absurd, really) to pass a list of explicit userids or 
		// relying parties *and* either the allUsers or the allRPs value in either of the arrays.
		List<ReturnedPolicy> resultList = new ArrayList<ReturnedPolicy>();
		Query<ReturnedPolicy> policyQuery = null;
		

		
		
		if (this.getChangeOrder().getPolicyIdArray() != null && ! this.getChangeOrder().getPolicyIdArray().isEmpty() && ! this.getChangeOrder().getPolicyIdArray().get(0).getBaseId().equalsIgnoreCase("allPolicies")) { 
			// operate in explicit policy id mode
			// Since we want to avoid any possibility of a take-over through insidious fiddling of policy ids in our database,
			// we build up a result set of our own using the result sets from potentially multiple queries.
			// Inefficient, but the use case for listing multiple policy IDs for change orders is presumed edgy until 
			// demonstrated otherwise in the wild.
			List<ReturnedPolicy> interimlist = null;
			for (ListablePolicyId lpi : this.getChangeOrder().getPolicyIdArray()) {
				policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.state = 0 and policyMetaData.policyId.baseId = :baseid",ReturnedPolicy.class);
				policyQuery.setParameter("baseid", lpi.getBaseId());
				interimlist = policyQuery.list();
				resultList.addAll(interimlist);
			}
		} else {
		
		for (ListableUserId lui : this.getChangeOrder().getUserIdArray()) {
			// For every specified user id
			for (ListableRelyingPartyId lrpi : this.getChangeOrder().getRelyingPartyIdArray()) {
				// selective retrieval
				if (lui.getUserValue().equals("allUsers")) {
					// get all the user values
					if (lrpi.getRPvalue().equals("allRPs")) {
						// get all RPs as well
						policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.state = 0 and infoReleasePolicy.resourceHolderId.RHValue=:rhvalue",ReturnedPolicy.class);
						policyQuery.setParameter("rhvalue",this.getChangeOrder().getResourceHolderId().getRHValue());
					} else {
						// Get only the specific rp
						policyQuery  = sess.createQuery("from ReturnedPolicy where policyMetaData.state = 0 and infoReleasePolicy.resourceHolderId.RHValue=:rhvalue and infoReleasePolicy.relyingPartyId.RPvalue=:rpvalue",ReturnedPolicy.class);
						policyQuery.setParameter("rhvalue", this.getChangeOrder().getResourceHolderId().getRHValue());
						policyQuery.setParameter("rpvalue", lrpi.getRPvalue());
					}
				} else if (lrpi.getRPvalue().equals("allRPs")) {
					// get all RPs but only the one user
					policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.state = 0 and infoReleasePolicy.resourceHolderId.RHValue=:rhvalue and infoReleasePolicy.userId.userValue=:uservalue",ReturnedPolicy.class);
					policyQuery.setParameter("rhvalue",  this.getChangeOrder().getResourceHolderId().getRHValue());
					policyQuery.setParameter("uservalue",  lui.getUserValue());
				} else {
					// specific 1:1
					policyQuery = sess.createQuery("from ReturnedPolicy where policyMetaData.state = 0 and infoReleasePolicy.resourceHolderId.RHValue=:rhvalue and infoReleasePolicy.userId.userValue=:uservalue and infoReleasePolicy.relyingPartyId.RPvalue=:rpvalue",ReturnedPolicy.class);
					policyQuery.setParameter("rhvalue",  this.getChangeOrder().getResourceHolderId().getRHValue());
					policyQuery.setParameter("uservalue", lui.getUserValue());
					policyQuery.setParameter("rpvalue", lrpi.getRPvalue());
				}
				//
				// Run the query
				resultList.addAll(policyQuery.list());
			}
		} // for
		} // else
		// At this point, resultList is set as needed.
		// Iterate over the results and make the appropriate changes
		//
		for (ReturnedPolicy rp : resultList) {
			// for every policy in the result list...
			// Make the changes specified in the changeOrder
			ReturnedPolicy newrp = new ReturnedPolicy();
			//ObjectMapper copier = new ObjectMapper();
			ObjectMapper copier = OMSingleton.getInstance().getOm();
			String serialized = rp.toJSON();
			newrp = (ReturnedPolicy) copier.readValue(serialized.getBytes(),ReturnedPolicy.class);
			
			// Set up newrp metadata and such
			newrp.getPolicyMetaData().setCreateTime(System.currentTimeMillis());
			newrp.getPolicyMetaData().getCreator().setCreatingUserType("SYSTEM");
			newrp.getPolicyMetaData().getCreator().setCreatingUserValue("COPSU");
			// bump version number
			newrp.getPolicyMetaData().getPolicyId().setVersion(String.valueOf(Integer.parseInt(newrp.getPolicyMetaData().getPolicyId().getVersion())+1));
			// this one becomes active
			newrp.getPolicyMetaData().setState(PolicyState.active);
			newrp.getPolicyMetaData().setSupersededBy(null);
			
			
			
			if (this.getChangeOrder().getWhileImAwayDirective() != null) 
				newrp.getInfoReleasePolicy().setWhileImAwayDirective(this.getChangeOrder().getWhileImAwayDirective());
			if (this.getChangeOrder().getArrayOfInfoReleaseStatement() != null) 
				newrp.getInfoReleasePolicy().setArrayOfInfoReleaseStatement(new ArrayList<InfoReleaseStatement>(this.getChangeOrder().getArrayOfInfoReleaseStatement()));
			if (this.getChangeOrder().getAllOtherInfoReleaseStatement() != null) 
				newrp.getInfoReleasePolicy().setAllOtherInfoReleaseStatement(this.getChangeOrder().getAllOtherInfoReleaseStatement());
			
			// And adjust the old policy
			rp.getPolicyMetaData().setState(PolicyState.inactive);
			SupersedingPolicyId spi = new SupersedingPolicyId();
			spi.setSupersedingId(newrp.getPolicyMetaData().getPolicyId().getBaseId());
			spi.setSupersedingVersion(newrp.getPolicyMetaData().getPolicyId().getVersion());
			rp.getPolicyMetaData().setSupersededBy(spi);
			newrp.getPolicyMetaData().setChangeOrder("https://"+config.getProperty("icm.server.name", true)+":"+config.getProperty("icm.server.port", true)+"/consent/v1/icm/user-info-release-change-orders/"+this.getChangeOrderMetaData().getChangeOrderId());
			
			// And save the new policy (the old policy will autosave if the transaction completes
			sess.save(newrp);
		}
			
		} catch (Exception e) {
			tx.rollback();
			sess.close();
			CopsuUtility.locDebug("LOG0015","Exception while updating existing policy: " + e.getMessage());
			throw new RuntimeException(e);
		}
		tx.commit();
		sess.close();
		return;
	}
}
