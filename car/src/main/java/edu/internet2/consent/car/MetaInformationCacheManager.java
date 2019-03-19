package edu.internet2.consent.car;

import java.util.Timer;
import java.util.TimerTask;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;

public class MetaInformationCacheManager {

	private static MetaInformationCacheManager micm = null;
	private static Object mutex = new Object();
	private static TimerTask timed = null;
	private static Timer timer = null;
	
	private CacheManager cacheManager = null;
	
	private MetaInformationCacheManager() {
		cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
	}
	
	public TimerTask getTimed() {
		return timed;
	}
	
	public Timer getTimer() {
		return timer;
	}
	
	public static MetaInformationCacheManager getInstance() {
		if (micm == null) {
		  synchronized(mutex) {
		   if (micm == null) {
			micm = new MetaInformationCacheManager();
			// This is the opportune time/place to start the cache scrubber, now that we have a cache manager
			// The scrubber will instantiate the caches the first time it scrubs if they don't already exist
			timed = new CacheScrubber();
			Timer timer = new Timer(true);
			CarConfig config = CarConfig.getInstance();
			String cycletimes = config.getProperty("car.cache.scrubber.cycletime", false);
			long cycletime = 60;
			if (cycletimes != null) {
				cycletime = Long.parseLong(cycletimes);
			}
			CarUtility.locError("ERR1127",LogCriticality.info,String.valueOf(cycletime));
			timer.scheduleAtFixedRate(timed,cycletime*1000,cycletime*1000);
		   }
		  }
		} 
		return micm;
	}
	
	public CacheManager getCacheManager() {
		return cacheManager;
	}
}
