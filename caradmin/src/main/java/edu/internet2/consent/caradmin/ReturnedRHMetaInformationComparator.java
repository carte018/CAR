package edu.internet2.consent.caradmin;

import java.util.Comparator;

import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;

public class ReturnedRHMetaInformationComparator implements Comparator<ReturnedRHMetaInformation> {
	@Override
	public int compare(ReturnedRHMetaInformation o1, ReturnedRHMetaInformation o2) {
		return o1.getDisplayname().getLocales().get(0).getValue().compareToIgnoreCase(o2.getDisplayname().getLocales().get(0).getValue());
	}
	public boolean equals(ReturnedRHMetaInformation o1, ReturnedRHMetaInformation o2) {
		return o1.getDisplayname().getLocales().get(0).getValue().equalsIgnoreCase(o2.getDisplayname().getLocales().get(0).getValue());
	}
}
