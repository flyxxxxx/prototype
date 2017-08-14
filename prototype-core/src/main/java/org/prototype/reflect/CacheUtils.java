package org.prototype.reflect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存工具
 * @author lj
 *
 */
public class CacheUtils {

	private static final Map<Object, Cache> caches = new ConcurrentHashMap<>();

	public static Cache getCache(Object key) {
		Cache cache = caches.get(key);
		if (cache == null) {
			synchronized (key) {
				cache = new Cache();
				caches.put(key, cache);
			}
		}
		return cache;
	}

	public static class Cache {
		private final Map<Object, Object> cache = new ConcurrentHashMap<>();

		private Cache() {
		}

		public void put(Object key, Object value) {
			cache.put(key, value);
		}

		@SuppressWarnings("unchecked")
		public <T> T get(Object key, Class<T> targetType) {
			return (T) cache.get(key);
		}
	}

	public static void clear() {
		caches.clear();
	}
}
