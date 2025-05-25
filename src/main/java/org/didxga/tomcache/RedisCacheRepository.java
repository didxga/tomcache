package org.didxga.tomcache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisCacheRepository implements CacheRepository {

    private Jedis jedis;

    public RedisCacheRepository() {
        // Default connection to localhost:6379
        // In a production scenario, host and port should be configurable.
        this(new Jedis("localhost", 6379));
    }

    // Constructor for allowing custom Jedis instance (e.g., for testing or specific configurations)
    public RedisCacheRepository(Jedis jedis) {
        this.jedis = jedis;
        try {
            // Check connection
            this.jedis.ping(); 
        } catch (JedisConnectionException e) {
            // Handle connection error appropriately
            // For now, we'll print an error and the application might not work as expected
            // A more robust solution would involve retries, fallback mechanisms, or specific error handling
            System.err.println("Failed to connect to Redis: " + e.getMessage());
            // Optionally rethrow or handle as a critical failure
        }
    }

    @Override
    public void store(Key key, Value value) {
        if (key == null || value == null || value.body == null) {
            // Avoid storing null keys or values, or values with null body
            return; 
        }
        try {
            // Using key.getUri() as the Redis key.
            // Serializing the Value object. For simplicity, storing body directly.
            // A more robust implementation would serialize the entire Value object (including headers and expiry).
            // For now, we assume Value.body is a String.
            // If Value.body can be other types, proper serialization (JSON, Java serialization) is needed.
            String redisKey = key.getUri();
            jedis.set(redisKey, value.body); // Storing only the body for now
            if (key.dueDate != null) { // Accessing dueDate field directly
                long expireTimestamp = key.dueDate.getTime() / 1000; // Convert Date to UNIX timestamp in seconds
                jedis.expireAt(redisKey, expireTimestamp);
            }
        } catch (JedisConnectionException e) {
            System.err.println("Redis error during store: " + e.getMessage());
            // Consider error handling strategy: retry, log, throw exception
        }
    }

    @Override
    public Value retrieve(Key key) {
        if (key == null) {
            return null;
        }
        try {
            String redisKey = key.getUri();
            String body = jedis.get(redisKey);
            if (body != null) {
                Value value = new Value();
                value.body = body;
                // Note: Headers and other Value fields are not currently stored/retrieved.
                // This would require serialization of the Value object in the store method.
                return value;
            } else {
                return null;
            }
        } catch (JedisConnectionException e) {
            System.err.println("Redis error during retrieve: " + e.getMessage());
            // Consider error handling strategy
            return null;
        }
    }

    @Override
    public boolean has(Key key) {
        if (key == null) {
            return false;
        }
        try {
            String redisKey = key.getUri();
            return jedis.exists(redisKey);
        } catch (JedisConnectionException e) {
            System.err.println("Redis error during has: " + e.getMessage());
            // Consider error handling strategy
            return false;
        }
    }

    // Optional: Method to close the Jedis connection when the repository is no longer needed.
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
