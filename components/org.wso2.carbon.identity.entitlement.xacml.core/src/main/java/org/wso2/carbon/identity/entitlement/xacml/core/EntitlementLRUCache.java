
package org.wso2.carbon.identity.entitlement.xacml.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a simple LRU cache, based on <code>LinkedHashMap</code>. If the cache is full and another
 * entry is added, the least recently used entry is dropped.
 */
public class EntitlementLRUCache<String, Set> extends LinkedHashMap<String, Set> {

    private static final long serialVersionUID = -1308554805704597171L;
    private final static int INITIAL_CACHE_CAPACITY = 16;
    private final static float LOAD_FACTOR = 75f;
    private int cacheSize;

    public EntitlementLRUCache(int cacheSize) {
        super(INITIAL_CACHE_CAPACITY, LOAD_FACTOR, true);
        this.cacheSize = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        // oldest entry of the cache would be removed when max cache size become
        return size() == this.cacheSize;
    }

}
