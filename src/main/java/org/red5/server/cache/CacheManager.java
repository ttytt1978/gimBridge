package org.red5.server.cache;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.red5.conf.ExtConfiguration;
import org.red5.server.api.scheduling.IScheduledJob;
import org.red5.server.api.scheduling.ISchedulingService;
import org.red5.server.scheduling.QuartzSchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache Manager
 * @author penglrien
 *
 */
public class CacheManager {
		
	private static final Logger log = LoggerFactory.getLogger(CacheManager.class);
	
	private ConcurrentHashMap<String, ObjectCache> items = new ConcurrentHashMap<String, ObjectCache>();

	private static final class SingletonHolder {
		private static final CacheManager INSTANCE = new CacheManager();
	}

	private CacheManager() {
		
		constructDefault();
		QuartzSchedulingService.getInstance().addScheduledJob(ExtConfiguration.CACHE_INTERVAL * 1000, new CacheCollectorJob());
	}

	public static CacheManager getInstance() {

		return SingletonHolder.INSTANCE;
	}

	public ObjectCache removeCache(String key) {
	
		log.debug("cache manager remove all cache!");
		items.get(key).removeAll();
		return items.remove(key); 
	}
	
	public ObjectCache getCache(String key) {
		
		ObjectCache cache = items.get(key);
		return cache; 
	}
	
	private void constructDefault() {
		if (getCache("org.red5.io.amf.Output.stringCache") == null) 
			items.put("org.red5.io.amf.Output.stringCache", new ObjectCache());
		if (getCache("org.red5.io.amf.Output.getterCache") == null)
			items.put("org.red5.io.amf.Output.getterCache", new ObjectCache());
		if (getCache("org.red5.io.amf.Output.fieldCache") == null)
			items.put("org.red5.io.amf.Output.fieldCache", new ObjectCache());
		if (getCache("org.red5.io.amf.Output.serializeCache") == null)
			items.put("org.red5.io.amf.Output.serializeCache", new ObjectCache());
		if (getCache("org.red5.server.stream.seek.fileCache") == null)
			items.put("org.red5.server.stream.seek.fileCache", new ObjectCache());
		if (getCache("org.red5.server.stream.hls.fileCache") == null)
			items.put("org.red5.server.stream.hls.fileCache", new ObjectCache());
	}
	
	private class CacheCollectorJob implements IScheduledJob {

		@Override
		public void execute(ISchedulingService service)
				throws CloneNotSupportedException {
			// check all cache item
			Iterator<String> it = items.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				ObjectCache item = items.get(key);
				item.collect();
			}
		}
	}
}
