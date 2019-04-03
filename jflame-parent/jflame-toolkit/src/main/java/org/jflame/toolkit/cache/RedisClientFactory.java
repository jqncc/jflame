package org.jflame.toolkit.cache;

import org.jflame.toolkit.cache.JedisConnection.RedisMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public final class RedisClientFactory {

    public static RedisClient createClient(Object x) {
        if (x instanceof JedisConnection) {
            JedisConnection conn = (JedisConnection) x;
            if (conn.getCurrentMode() == RedisMode.cluster) {
                return new JedisClusterClientImpl(conn);
            } else if (conn.getCurrentMode() == RedisMode.sharded) {
                return new JedisSharedClientImpl(conn);
            } else {
                return new JedisClientImpl(conn);
            }
        } else if (x instanceof RedisConnectionFactory) {
            RedisTemplate<byte[],byte[]> redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory((RedisConnectionFactory) x);
            redisTemplate.afterPropertiesSet();
            return new SpringCacheClientImpl(redisTemplate);
        }
        throw new IllegalArgumentException("无法生成RedisClient实例,参数只支持JedisConnection,RedisConnectionFactory类型");
    }
}
