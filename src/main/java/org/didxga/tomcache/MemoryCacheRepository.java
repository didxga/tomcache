package org.didxga.tomcache;

import java.util.HashMap;
import java.util.Map;

public class MemoryCacheRepository implements CacheRepository {

    protected Map<Key, Value> cache = new HashMap<>();

    @Override
    public void store(Key key, Value value) {
        this.cache.put(key, value);
    }

    @Override
    public Value retrieve(Key key) {
        return cache.get(key);
    }

    @Override
    public boolean has(Key key) {
        return cache.containsKey(key);
    }
}
