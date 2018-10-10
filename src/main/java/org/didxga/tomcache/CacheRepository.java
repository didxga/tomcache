package org.didxga.tomcache;

/**
 * Cache repository is where the HTTP response is stored.
 *
 * <p>This interface defines three methods:
 * Store, to store HTTP response
 * Retrieve, to get HTTP response from repository
 * Has, check the availability of HTTP response in repository
 * </p>
 */
public interface CacheRepository {

    public void store(Key key, Value value);

    public Value retrieve(Key key);

    public boolean has(Key key);
}
