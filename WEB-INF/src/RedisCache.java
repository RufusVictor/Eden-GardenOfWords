import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RedisCache {

    private static final String REDIS_HOST;
    private static final int REDIS_PORT;
    private static final String REDIS_USERNAME;
    private static final String REDIS_PASSWORD;

    private static JedisPool jedisPool;

    static {
        // Load properties
        Properties props = new Properties();
        try (InputStream input = RedisCache.class.getClassLoader().getResourceAsStream("redis.properties")) {
            if (input == null) {
                throw new RuntimeException("❌ Configuration file 'redis.properties' not found in classpath.");
            }
            props.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("❌ Failed to load 'redis.properties'", ex);
        }

        // Validate and load REDIS_HOST
        REDIS_HOST = props.getProperty("redis.host");
        if (REDIS_HOST == null || REDIS_HOST.trim().isEmpty()) {
            throw new RuntimeException("❌ Property 'redis.host' is required but missing or empty in redis.properties");
        }

        // Validate and load REDIS_PORT
        String portStr = props.getProperty("redis.port");
        if (portStr == null || portStr.trim().isEmpty()) {
            throw new RuntimeException("❌ Property 'redis.port' is required but missing or empty in redis.properties");
        }
        try {
            REDIS_PORT = Integer.parseInt(portStr.trim());
            if (REDIS_PORT <= 0 || REDIS_PORT > 65535) {
                throw new RuntimeException("❌ Property 'redis.port' must be between 1 and 65535");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("❌ Property 'redis.port' must be a valid integer: " + portStr);
        }

        // Validate and load REDIS_USERNAME
        REDIS_USERNAME = props.getProperty("redis.username");
        if (REDIS_USERNAME == null || REDIS_USERNAME.trim().isEmpty()) {
            throw new RuntimeException("❌ Property 'redis.username' is required but missing or empty in redis.properties");
        }

        // Validate and load REDIS_PASSWORD
        REDIS_PASSWORD = props.getProperty("redis.password");
        if (REDIS_PASSWORD == null) {
            throw new RuntimeException("❌ Property 'redis.password' is required but missing in redis.properties");
        }
        
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT, 2000, REDIS_USERNAME, REDIS_PASSWORD);
    }

    public static String getFromCache(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void storeInCache(String key, String value, int expirationInSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, expirationInSeconds, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}