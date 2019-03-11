package edu.internet2.consent.car;

import edu.internet2.consent.informed.model.ReturnedInfoItemMetaInformation;

public class CachedInfoItemMetaInformation {

	private long cacheTime;
	private ReturnedInfoItemMetaInformation data;
	public long getCacheTime() {
		return cacheTime;
	}
	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}
	public ReturnedInfoItemMetaInformation getData() {
		return data;
	}
	public void setData(ReturnedInfoItemMetaInformation data) {
		this.data = data;
	}
}
