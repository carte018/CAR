package edu.internet2.consent.caradmin;

import java.util.Comparator;

import edu.internet2.consent.informed.model.InfoItemIdentifier;

public class InfoItemIdentifierComparator implements Comparator<InfoItemIdentifier> {

	@Override
	public int compare(InfoItemIdentifier o1, InfoItemIdentifier o2) {
		return o1.getIiid().compareToIgnoreCase(o2.getIiid());
	}
}
