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
