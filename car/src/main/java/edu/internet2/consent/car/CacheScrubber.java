package edu.internet2.consent.car;

import java.util.Iterator;
import java.util.TimerTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.ehcache.Cache.Entry;

import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;

public class CacheScrubber extends TimerTask {

	static long runcount = 0;
	static long lastrpsize = 0;
	static long lastiisize = 0;
	static long lastvalsize = 0;
	static long lastrunduration = 0;
	static long lastrunstart = 0;
	static long maxduration = 0;
	static String status = "Idle";
	
	public static long getRuncount() {
		return runcount;
	}
	
	public static long getLastrpsize() {
		return lastrpsize;
	}
	
	public static long getLastiisize() {
		return lastiisize;
	}
	
	public static long getLastvalsize() {
		return lastvalsize;
	}
	
	public static long getLastrunduration() {
		return lastrunduration;
	}
	
	public static long getLastrunstart() {
		 return lastrunstart;
	}
	
	public static long getMaxduration() {
		return maxduration;
	}
	
	public static String getStatus() {
		return status;
	}
	
	@Override
	public void run() {
		// 
		// We need to walk the caches, refreshing the values as we go.
		//
		// We operate somewhat like the clock hands:
		//     car.cache.scrubber.mintime = #seconds entry must be in cache before it will be scrubbed
		//     car.cache.scrubber.cycletime = #seconds in scrubbing cycle (scrub the cache every N seconds)
		// mintime sets the minimum time between updates of a single value from the cache
		// cycletime sets the overall frequency of runs
		// Typically, mintime may be longer than cycletime for fast cycling (eg., mintime = 600 sec, cycletime = 60 sec to scrub
		// 10-minute-old entries every minute) or vice versa for lazy cache scrubbing (eg. mintime = 60sec, cycletime = 600 sec, 
		// to guarantee scrub of everything every 10 minutes).  Aggressive scrubbing may work better for very large caches, but may
		// be less predictable.  Lazy scrubbing may be better for small caches if predictability is critical.
		//
		// In either case, we're scheduled only if we're not currently running.
		//
		
		// We have three caches to scrub in sequence.  If this becomes problematic, we may need three CacheScrubber threads to
		// scrub each cache individually.
		//
		
		status = "Starting";
		lastrunstart = System.currentTimeMillis();
		
		// Get our parameter (mintime -- cycletime matters for the scheduler not for us
		CarConfig config = CarConfig.getInstance();
		String mintimes = config.getProperty("car.cache.scrubber.mintime",false);
		int mintime = 600;  // default to 600 seconds lifetime in cache
		if (mintimes != null) {
			mintime = Integer.parseInt(mintimes);
		}
		
		status = "Scrubbing RPs";
		
		// And start scrubbing with the RP metainformation
		CarUtility.locError("ERR1123",LogCriticality.debug);
		RPMetaInformationCache rpmic = RPMetaInformationCache.getInstance();
		Iterator<Entry<String,CachedRPMetaInformation>> iter = rpmic.getCache().iterator();
		long c = 0;
		long n = 0;
		HttpClient httpClient = HttpClientBuilder.create().build();
		while (iter.hasNext()) {
			c += 1;
			Entry<String,CachedRPMetaInformation> e = iter.next();
			if (e.getValue().getCacheTime() < System.currentTimeMillis() - (mintime * 1000)) {
				// reload the cache
				// now we potentially have two kinds of cache entries -- those with rptype and those without
				// Handle both
				if (e.getKey().matches("^.*\\|.*\\|.*$")) {
					String[] p = e.getKey().split("\\|",3);
					String rhid = p[0];
					String rptype = p[1];
					String rpid = p[2];
					rpmic.evictCachedRPMetaInformation(rhid, rptype,rpid);
					ReturnedRPMetaInformation foo = CarUtility.getRPMetaInformation(rhid, rptype, rpid,config);
					if (foo == null) {
						n += 1;
						CarUtility.locDebugErr("ERR1128",rhid,rptype+"|"+rpid);
					} else {
						CarUtility.locDebugErr("ERR1129",rhid,rptype+"|"+rpid,foo.getDisplayname().getLocales().get(0).getValue());
					}
				} else {
					String[] p = e.getKey().split("\\|",2);
					String rhid = p[0];
					String rpid = p[1];
					rpmic.evictCachedRPMetaInformation(rhid, rpid);
				// 	rpmic.storeCachedRPMetaInformation(rhid, rpid, CarUtility.getRPMetaInformation(rhid, rpid, config)); 
					ReturnedRPMetaInformation foo = CarUtility.getRPMetaInformation(rhid,rpid,config);
					if (foo == null) {
						n += 1;
						CarUtility.locDebugErr("ERR1128",rhid,rpid);
					} else {
						CarUtility.locDebugErr("ERR1129",rhid,rpid,foo.getDisplayname().getLocales().get(0).getValue());
					}
				}
			}
		}
		lastrpsize = c - n;
		
		status = "Scrubbing IIs";
		
		// And then scrub the II metainformation in the same fashion
		CarUtility.locError("ERR1124",LogCriticality.debug);
		InfoItemMetaInformationCache iimic = InfoItemMetaInformationCache.getInstance();
		Iterator<Entry<String,CachedInfoItemMetaInformation>> iiter = iimic.getCache().iterator();
		c = 0;
		n = 0;
		while (iiter.hasNext()) {
			c += 1;
			Entry<String,CachedInfoItemMetaInformation> e = iiter.next();
			if (e.getValue().getCacheTime() < System.currentTimeMillis() - (mintime * 1000)) {
				// Reload
				String[] p = e.getKey().split("\\|",2);
				String rhid = p[0];
				String iiid = p[1];
				iimic.evictCachedInfoItemMetaInformation(rhid,iiid);
				//iimic.storeCachedInfoItemMetaInformation(rhid, iiid,CarUtility.getInfoItemMetaInformation(rhid, iiid, config));
				if (CarUtility.getInfoItemMetaInformation(rhid, iiid, config, httpClient) == null) {
					n += 1;
					CarUtility.locDebugErr("ERR1128",rhid,iiid);
				}
			}
		}
		lastiisize = c - n;
		status = "Scrubbing values";
		
		// And finally the value metainformation
		CarUtility.locError("ERR1125",LogCriticality.debug);
		ValueMetaInformationCache vmic = ValueMetaInformationCache.getInstance();
		Iterator<Entry<String,CachedValueMetaInformation>> viter = vmic.getCache().iterator();
		c = 0;
		n = 0;
		while (viter.hasNext()) {
			c += 1;
			Entry<String,CachedValueMetaInformation> e = viter.next();
			if (e.getValue().getCacheTime() < System.currentTimeMillis() - (mintime * 1000)) {
				// Reload
				String[] p = e.getKey().split("\\|",2);
				String iiid = p[0];
				String value = p[1];
				vmic.evictCachedValueMetaInformation(iiid, value);
				//vmic.storeCachedValueMetaInformation(iiid, value, CarUtility.getValueMetaInformation(iiid, value, config));
				if (CarUtility.getValueMetaInformation(iiid,value,config, httpClient) == null) {
					n += 1;
					CarUtility.locDebugErr("ERR1128",iiid,value);
				}
			}
		}
		lastvalsize = c - n;
		CarUtility.locError("ERR1126",LogCriticality.debug);
		status = "Idle";
		runcount += 1;
		lastrunduration = System.currentTimeMillis() - lastrunstart;
		if (lastrunduration > maxduration) maxduration = lastrunduration;
		
		HttpClientUtils.closeQuietly(httpClient);
	}

}
