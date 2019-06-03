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
package edu.internet2.consent.car;

import edu.internet2.consent.informed.model.ReturnedValueMetaInformation;

public class CachedValueMetaInformation {
	private long cacheTime;
	private ReturnedValueMetaInformation data;
	public long getCacheTime() {
		return cacheTime;
	}
	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}
	public ReturnedValueMetaInformation getData() {
		return data;
	}
	public void setData(ReturnedValueMetaInformation data) {
		this.data = data;
	}
	
	
}
