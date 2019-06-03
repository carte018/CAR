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

import java.time.temporal.ChronoUnit;
import org.ehcache.Cache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import java.time.Duration;
import org.ehcache.expiry.ExpiryPolicy;

import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;

public class RPMetaInformationCache {
	private static RPMetaInformationCache rpic = null;
	// private HashMap<String,CachedRPMetaInformation> cache = null;
	private Cache<String,CachedRPMetaInformation> cache = null;

	private RPMetaInformationCache() {
		// cache = new HashMap<String,CachedRPMetaInformation>();
		CarConfig config = CarConfig.getInstance();
		String ttls = config.getProperty("car.rpmic.ttl",false);
		int ttl = 2*60*60;  // 2 hours default ttl for cache entries
		if (ttls != null) {
			ttl = Integer.parseInt(ttls);
		}
		MetaInformationCacheManager manager = MetaInformationCacheManager.getInstance();
		
		ExpiryPolicy<Object,Object> ep = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(Long.valueOf(ttl).longValue(),ChronoUnit.SECONDS));

		
		cache = manager.getCacheManager().createCache("rpmic", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,CachedRPMetaInformation.class,ResourcePoolsBuilder.heap(10000)).withExpiry(ep));
	}
	
	public static RPMetaInformationCache getInstance() {
		if (rpic == null) {
			rpic = new RPMetaInformationCache();
		} 
		return rpic;
	}
	
	public Cache<String,CachedRPMetaInformation> getCache() {
		return cache;
	}
	
	public boolean hasCachedRPMetaInformation(String rhid, String rptype, String rpid) {
		if (! cache.containsKey(rhid+"|"+rptype+"|"+rpid))
			CarUtility.locDebugErr("ERR1121",rhid+"|"+rptype+"|"+rpid);
		else
			CarUtility.locDebugErr("ERR1122",rhid+"|"+rptype+"|"+rpid);
		return cache.containsKey(rhid+"|"+rptype+"|"+rpid);
	}
	
	public boolean hasCachedRPMetaInformation(String rhid, String rpid) {
		// return cache.containsKey(rhid+":"+rpid);
		if (! cache.containsKey(rhid+"|"+rpid))
			CarUtility.locDebugErr("ERR1121",rhid+"|"+rpid);
		else
			CarUtility.locDebugErr("ERR1122",rhid+"|"+rpid);
	return cache.containsKey(rhid+"|"+rpid);
	}
	
	public CachedRPMetaInformation getCachedRPMetaInformation(String rhid, String rptype, String rpid) {
		return cache.get(rhid+"|"+rptype+"|"+rpid);
	}
	
	public CachedRPMetaInformation getCachedRPMetaInformation(String rhid, String rpid) {
		return cache.get(rhid+"|"+rpid);
	}
	
	public void storeCachedRPMetaInformation(String rhid, String rptype, String rpid, ReturnedRPMetaInformation r) {
		CachedRPMetaInformation c = new CachedRPMetaInformation();
		c.setCacheTime(System.currentTimeMillis());
		c.setData(r);
		cache.put(rhid+"|"+rptype+"|"+rpid, c);
	}
	public void storeCachedRPMetaInformation(String rhid, String rpid, ReturnedRPMetaInformation r) {
		CachedRPMetaInformation c = new CachedRPMetaInformation();
		c.setCacheTime(System.currentTimeMillis());
		c.setData(r);
		cache.put(rhid+"|"+rpid, c);
	}
	
	public void evictCachedRPMetaInformation(String rhid, String rptype, String rpid) {
		cache.remove(rhid + "|" + rptype + "|" + rpid);
	}
	public void evictCachedRPMetaInformation(String rhid, String rpid) {
		cache.remove(rhid+"|"+rpid);
	}
}
