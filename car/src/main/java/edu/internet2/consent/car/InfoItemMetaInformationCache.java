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

import org.ehcache.Cache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.ExpiryPolicy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


//import org.ehcache.expiry.Expirations;
//import org.ehcache.expiry.Expirations.ExpiryBuilder;

import edu.internet2.consent.informed.model.ReturnedInfoItemMetaInformation;

public class InfoItemMetaInformationCache {

	private static InfoItemMetaInformationCache iiic = null;
	// private HashMap<String,CachedInfoItemMetaInformation> cache = null;
	private Cache<String,CachedInfoItemMetaInformation> cache = null;

	private InfoItemMetaInformationCache() {
		// cache = new HashMap<String,CachedInfoItemMetaInformation>();		
		CarConfig config = CarConfig.getInstance();
		String ttls = config.getProperty("car.iimic.ttl",false);
		int ttl = 2*60*60;  // 2 hours default ttl for cache entries
		if (ttls != null) {
			ttl = Integer.parseInt(ttls);
		}
		MetaInformationCacheManager manager = MetaInformationCacheManager.getInstance();
		
		ExpiryPolicy<Object,Object> ep = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(Long.valueOf(ttl).longValue(),ChronoUnit.SECONDS));
				
		cache = manager.getCacheManager().createCache("iimic", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,CachedInfoItemMetaInformation.class,ResourcePoolsBuilder.heap(10000)).withExpiry(ep));
	}
	
	public static InfoItemMetaInformationCache getInstance() {
		if (iiic == null) {
			iiic = new InfoItemMetaInformationCache();
		} 
		return iiic;
	}
	
	public Cache<String,CachedInfoItemMetaInformation> getCache() {
		return cache;
	}
	
	public boolean hasCachedInfoItemMetaInformation(String iiid, String iivalue) {
		// return cache.containsKey(iiid+":"+iivalue);
		if (! cache.containsKey(iiid+"|"+iivalue))
			CarUtility.locDebugErr("ERR1121",iiid+"|"+iivalue);
	else
			CarUtility.locDebugErr("ERR1122",iiid+"|"+iivalue);
	return cache.containsKey(iiid+"|"+iivalue);
	}
	
	public CachedInfoItemMetaInformation getCachedInfoItemMetaInformation(String iiid, String iivalue) {
		return cache.get(iiid+"|"+iivalue);
	}
	
	public void storeCachedInfoItemMetaInformation(String iiid, String iivalue, ReturnedInfoItemMetaInformation r) {
		CachedInfoItemMetaInformation c = new CachedInfoItemMetaInformation();
		c.setCacheTime(System.currentTimeMillis());
		c.setData(r);
		cache.put(iiid+"|"+iivalue, c);
	}
	

	public void evictCachedInfoItemMetaInformation(String iiid, String iivalue) {
		cache.remove(iiid+"|"+iivalue);
	}
}
