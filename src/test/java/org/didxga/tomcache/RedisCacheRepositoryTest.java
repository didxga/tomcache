package org.didxga.tomcache;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RedisCacheRepositoryTest {

    @Mock
    private Jedis mockJedis;

    private RedisCacheRepository redisCacheRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        // Simulate successful ping for default constructor path, though we inject the mock
        doNothing().when(mockJedis).ping(); 
        redisCacheRepository = new RedisCacheRepository(mockJedis);
    }

    @Test
    public void testStoreAndRetrieve_Success() {
        Key key = Key.generateKey("test/uri");
        Value value = new Value();
        value.body = "Test Body";

        redisCacheRepository.store(key, value);

        when(mockJedis.get("test/uri")).thenReturn("Test Body");

        Value retrievedValue = redisCacheRepository.retrieve(key);

        assertNotNull(retrievedValue);
        assertEquals("Test Body", retrievedValue.body);
        verify(mockJedis).set("test/uri", "Test Body");
    }

    @Test
    public void testStore_withExpiry() {
        Date expiryDate = new Date(System.currentTimeMillis() + 10000); // 10 seconds from now
        Key key = Key.generateKey("test/uri/expiry", expiryDate);
        Value value = new Value();
        value.body = "Test Body Expiring";

        redisCacheRepository.store(key, value);

        long expectedUnixTimestamp = expiryDate.getTime() / 1000;
        verify(mockJedis).set("test/uri/expiry", "Test Body Expiring");
        verify(mockJedis).expireAt("test/uri/expiry", expectedUnixTimestamp);
    }
    
    @Test
    public void testStore_nullKeyOrValue() {
        Value value = new Value();
        value.body = "Some body";
        redisCacheRepository.store(null, value);
        verifyNoMoreInteractions(mockJedis); // Nothing should happen if key is null

        Key key = Key.generateKey("test/uri_null_value");
        redisCacheRepository.store(key, null);
        verifyNoMoreInteractions(mockJedis); // Nothing should happen if value is null

        Value valueNullBody = new Value(); // body is null
        redisCacheRepository.store(key, valueNullBody);
        verifyNoMoreInteractions(mockJedis); // Nothing should happen if value.body is null
    }


    @Test
    public void testRetrieve_NotFound() {
        Key key = Key.generateKey("test/nonexistent");
        when(mockJedis.get("test/nonexistent")).thenReturn(null);

        Value retrievedValue = redisCacheRepository.retrieve(key);

        assertNull(retrievedValue);
    }
    
    @Test
    public void testRetrieve_nullKey() {
        Value retrievedValue = redisCacheRepository.retrieve(null);
        assertNull(retrievedValue);
        verifyNoMoreInteractions(mockJedis);
    }

    @Test
    public void testHas_True() {
        Key key = Key.generateKey("test/existing");
        when(mockJedis.exists("test/existing")).thenReturn(true);

        assertTrue(redisCacheRepository.has(key));
    }

    @Test
    public void testHas_False() {
        Key key = Key.generateKey("test/nonexistent_has");
        when(mockJedis.exists("test/nonexistent_has")).thenReturn(false);

        assertFalse(redisCacheRepository.has(key));
    }

    @Test
    public void testHas_nullKey() {
        assertFalse(redisCacheRepository.has(null));
        verifyNoMoreInteractions(mockJedis);
    }

    @Test
    public void testStore_JedisConnectionException() {
        Key key = Key.generateKey("test/uri_conn_exception");
        Value value = new Value();
        value.body = "Test Body";

        doThrow(new JedisConnectionException("Connection failed")).when(mockJedis).set(anyString(), anyString());

        // We expect the exception to be caught and logged, not rethrown by default
        redisCacheRepository.store(key, value); 
        // Verify set was called, even if it threw an exception internally that was handled
        verify(mockJedis).set("test/uri_conn_exception", "Test Body");
    }

    @Test
    public void testRetrieve_JedisConnectionException() {
        Key key = Key.generateKey("test/uri_retrieve_exception");
        when(mockJedis.get(anyString())).thenThrow(new JedisConnectionException("Connection failed"));
        
        Value retrievedValue = redisCacheRepository.retrieve(key);
        assertNull(retrievedValue); // Expect null if connection fails during retrieve
        verify(mockJedis).get("test/uri_retrieve_exception");
    }

    @Test
    public void testHas_JedisConnectionException() {
        Key key = Key.generateKey("test/uri_has_exception");
        when(mockJedis.exists(anyString())).thenThrow(new JedisConnectionException("Connection failed"));

        boolean result = redisCacheRepository.has(key);
        assertFalse(result); // Expect false if connection fails during has
        verify(mockJedis).exists("test/uri_has_exception");
    }
    
    @Test
    public void testClose() {
        redisCacheRepository.close();
        verify(mockJedis).close();
    }

    // Test for the constructor that initializes a real Jedis client (optional, might need a running Redis)
    // For now, we focus on testing with the mocked Jedis instance.
    // It's also important to test the constructor's ping.
    @Test
    public void testConstructor_PingSuccess() {
        // This is implicitly covered by setUp if mockJedis.ping() is expected & verified.
        // For an explicit test with a new instance:
        Jedis localMockJedis = mock(Jedis.class);
        doNothing().when(localMockJedis).ping();
        RedisCacheRepository newRepo = new RedisCacheRepository(localMockJedis);
        verify(localMockJedis).ping(); // Verify ping was called during construction
        newRepo.close(); // Clean up
    }

    @Test
    public void testConstructor_PingFailure() {
        Jedis localMockJedis = mock(Jedis.class);
        doThrow(new JedisConnectionException("Ping failed")).when(localMockJedis).ping();
        // We expect the constructor to catch this and print an error, not rethrow.
        RedisCacheRepository newRepo = new RedisCacheRepository(localMockJedis);
        verify(localMockJedis).ping();
        newRepo.close();
    }
}
