package edu.internet2.consent.car;

import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;

public class CachedRPMetaInformation {
	private long cacheTime;
	private ReturnedRPMetaInformation data;
	public long getCacheTime() {
		return cacheTime;
	}
	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}
	public ReturnedRPMetaInformation getData() {
		return data;
	}
	public void setData(ReturnedRPMetaInformation data) {
		this.data = data;
	}
}
