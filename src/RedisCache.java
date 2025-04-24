import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCache {
    private static final String REDIS_HOST = "";
    private static final int REDIS_PORT = ;
    private static final String REDIS_USERNAME = "";
    private static final String REDIS_PASSWORD = "";

    private static JedisPool jedisPool;

    static {
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