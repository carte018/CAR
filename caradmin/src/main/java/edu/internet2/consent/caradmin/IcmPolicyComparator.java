package edu.internet2.consent.caradmin;

import java.util.Comparator;

import edu.internet2.consent.icm.model.ListOfReturnedPrecedenceObject;
import edu.internet2.consent.icm.model.IcmReturnedPolicy;

public class IcmPolicyComparator implements Comparator<IcmReturnedPolicy> {

	public int compare(IcmReturnedPolicy o1, IcmReturnedPolicy o2) {
		// Acquire the numeric rank values for each policy from the policy-precedence endpoint and return comparison
		String base1 = o1.getPolicyMetaData().getPolicyId().getBaseId();
		String base2 = o2.getPolicyMetaData().getPolicyId().getBaseId();
		
		ListOfReturnedPrecedenceObject l1 = CarAdminUtils.getIcmPrecedence(o1.getPolicy().getResourceHolderId().getRHType(), o1.getPolicy().getResourceHolderId().getRHValue(), base1);
		ListOfReturnedPrecedenceObject l2 = CarAdminUtils.getIcmPrecedence(o2.getPolicy().getResourceHolderId().getRHType(), o2.getPolicy().getResourceHolderId().getRHValue(), base2);
		
		Long p1 = Long.valueOf(l1.getContained().get(0).getNumericRank());
		Long p2 = Long.valueOf(l2.getContained().get(0).getNumericRank());
		
		return p1.compareTo(p2);
	}
}
