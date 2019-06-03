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
