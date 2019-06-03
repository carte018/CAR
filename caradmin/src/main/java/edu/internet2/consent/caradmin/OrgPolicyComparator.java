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
