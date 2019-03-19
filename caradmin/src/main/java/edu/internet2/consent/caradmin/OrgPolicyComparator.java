package edu.internet2.consent.caradmin;

import java.util.Comparator;

import edu.internet2.consent.arpsi.model.ListOfReturnedPrecedenceObject;
import edu.internet2.consent.arpsi.model.OrgReturnedPolicy;

public class OrgPolicyComparator implements Comparator<OrgReturnedPolicy> {

	public int compare(OrgReturnedPolicy o1, OrgReturnedPolicy o2) {
		// Acquire the numeric rank values for each policy from the policy-precedence endpoint and return comparison
		String base1 = o1.getPolicyMetaData().getPolicyId().getBaseId();
		String base2 = o2.getPolicyMetaData().getPolicyId().getBaseId();
		
		ListOfReturnedPrecedenceObject l1 = CarAdminUtils.getOrgPrecedence(o1.getPolicy().getResourceHolderId().getRHType(), o1.getPolicy().getResourceHolderId().getRHValue(), base1);
		ListOfReturnedPrecedenceObject l2 = CarAdminUtils.getOrgPrecedence(o2.getPolicy().getResourceHolderId().getRHType(), o2.getPolicy().getResourceHolderId().getRHValue(), base2);
		if (l1 == null || l1.getContained() == null || l1.getContained().isEmpty()) {
			CarAdminUtils.locError("ERR0055",LogCriticality.error,base1);
		}
		if (l2 == null || l2.getContained() == null || l2.getContained().isEmpty()) {
			CarAdminUtils.locError("ERR0055",LogCriticality.error,base2);
		}
		Long p1 = Long.valueOf(l1.getContained().get(0).getNumericRank());
		Long p2 = Long.valueOf(l2.getContained().get(0).getNumericRank());
		
		return p1.compareTo(p2);
	}
}
