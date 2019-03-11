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
