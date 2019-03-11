package edu.internet2.consent.caradmin;

import java.util.Comparator;

public class InjectedRHMetaInformationComparator implements Comparator<InjectedRHMetainformation> {

	@Override
	public int compare(InjectedRHMetainformation o1, InjectedRHMetainformation o2) {
		// TODO Auto-generated method stub
		return o1.getDisplayname().compareToIgnoreCase(o2.getDisplayname());
	}
	public boolean equals(InjectedRHMetainformation o1, InjectedRHMetainformation o2) {
		return o1.getDisplayname().equalsIgnoreCase(o2.getDisplayname());
	}

}
