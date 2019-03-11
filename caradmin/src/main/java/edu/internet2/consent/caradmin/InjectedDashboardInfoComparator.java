package edu.internet2.consent.caradmin;

import java.util.Comparator;

public class InjectedDashboardInfoComparator implements Comparator<InjectedDashboardInfo> {

	@Override
	public int compare(InjectedDashboardInfo o1, InjectedDashboardInfo o2) {
		// TODO Auto-generated method stub
		return o1.getDisplayname().getLocales().get(0).getValue().compareToIgnoreCase(o2.getDisplayname().getLocales().get(0).getValue());
	}
	public boolean equals(InjectedDashboardInfo o1, InjectedDashboardInfo o2) {
		return o1.getDisplayname().getLocales().get(0).getValue().equalsIgnoreCase(o2.getDisplayname().getLocales().get(0).getValue());
	}

}
