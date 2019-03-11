package edu.internet2.consent.car;

import java.time.temporal.ChronoUnit;
import org.ehcache.Cache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import java.time.Duration;
import org.ehcache.expiry.ExpiryPolicy;

import edu.internet2.consent.informed.model.ReturnedValueMetaInformation;

public class ValueMetaInformationCache {

	private static ValueMetaInformationCache vmic = null;
	//private HashMap<String,CachedValueMetaInformation> cache = null;
	private Cache<String,CachedValueMetaInformation> cache = null;
	
	private ValueMetaInformationCache() {
		//cache = new HashMap<String,CachedValueMetaInformation>();
		CarConfig config = CarConfig.getInstance();
		String ttls = config.getProperty("car.vmic.ttl",false);
		int ttl = 2*60*60;  // 2 hours default ttl for cache entries
		if (ttls != null) {
			ttl = Integer.parseInt(ttls);
		}
		MetaInformationCacheManager manager = MetaInformationCacheManager.getInstance();
	
		ExpiryPolicy<Object,Object> ep = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(Long.valueOf(ttl).longValue(),ChronoUnit.SECONDS));

		
		cache = manager.getCacheManager().createCache("vmic", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,CachedValueMetaInformation.class,ResourcePoolsBuilder.heap(10000)).withExpiry(ep));
	}
	
	public static ValueMetaInformationCache getInstance() {
		if (vmic == null) {
			vmic = new ValueMetaInformationCache();
		} 
		return vmic;
	}
	
	public Cache<String,CachedValueMetaInformation> getCache() {
		return cache;
	}
	
	public boolean hasCachedValueMetaInformation(String iiid, String iivalue) {
		//return cache.containsKey(iiid+":"+iivalue);
		if (! cache.containsKey(iiid+"|"+iivalue))
				CarUtility.locDebugErr("ERR1121",iiid+"|"+iivalue);
		else
				CarUtility.locDebugErr("ERR1122",iiid+"|"+iivalue);
		return cache.containsKey(iiid+"|"+iivalue);
	}
	
	public CachedValueMetaInformation getCachedValueMetaInformation(String iiid, String iivalue) {
		return cache.get(iiid+"|"+iivalue);
	}
	
	public void storeCachedValueMetaInformation(String iiid, String iivalue, ReturnedValueMetaInformation r) {
		CachedValueMetaInformation c = new CachedValueMetaInformation();
		c.setCacheTime(System.currentTimeMillis());
		c.setData(r);
		cache.put(iiid+"|"+iivalue, c);
	}
	
	public void evictCachedValueMetaInformation(String iiid, String iivalue) {
		cache.remove(iiid+"|"+iivalue);
	}
}
