package edu.internet2.consent.caradmin;

import java.util.Comparator;

import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;

public class RHMetaInformationComparator implements Comparator<ReturnedRHMetaInformation> {
	public int compare(ReturnedRHMetaInformation a, ReturnedRHMetaInformation b) {
		return a.getDisplayname().getLocales().get(0).getValue().compareTo(b.getDisplayname().getLocales().get(0).getValue());
	}
	public boolean equals(ReturnedRHMetaInformation a, ReturnedRHMetaInformation b) {
		return a.getDisplayname().getLocales().get(0).getValue().equalsIgnoreCase(b.getDisplayname().getLocales().get(0).getValue());
	}
}
