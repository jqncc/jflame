package org.jflame.toolkit.cache.redis;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.jflame.toolkit.cache.redis.serizlizer.FastJsonRedisSerializer;
import org.jflame.toolkit.cache.redis.serizlizer.IGenericRedisSerializer;
import org.jflame.toolkit.cache.redis.serizlizer.IRedisSerializer;
import org.jflame.toolkit.util.CharsetHelper;

import redis.clients.jedis.JedisCluster;

public class JedisClusterClientImpl extends AbstractJedisClient {

    public JedisClusterClientImpl(JedisConnection conn) {
        this(conn, new FastJsonRedisSerializer());
    }

    public JedisClusterClientImpl(JedisConnection conn, IGenericRedisSerializer serializer) {
        this.conn = conn;
        this.valueSerializer = serializer;
    }

    @Override
    public Object getNativeClient() {
        return this.conn.getJedisCluster();
    }

    @Override
    public void hput(String key, String fieldKey, Object value, int expireInSecond) {
        execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                Long l = client.hset(keyBytes[0], rawKey(fieldKey), rawValue(value));
                client.expire(keyBytes[0], expireInSecond);
                return l;
            }
        });

    }

    public Set<String> keys(String pattern) {
        throw new RedisAccessException("集群模式不支持keys命令");
    }

    @Override
    public <T> T runScript(String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz) {
        return runScript(luaScript, keys, args, resultClazz, null);
    }

    @Override
    public <T> T runScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz, IRedisSerializer<T> resultSerializer) {
        // Redis要求单个Lua脚本操作的key必须在同一个节点上,如果只有一个keys执行,如果多key抛异常
        if (keys.size() == 1) {
            try (JedisCluster client = getJedis()) {
                Object r = client.eval(CharsetHelper.getUtf8Bytes(luaScript), rawValues(keys), rawValues(args));
                Object cr = convertScriptResult(r, resultClazz);
                return deserializeResult(cr, resultClazz, resultSerializer);
            } catch (Exception e) {
                throw new RedisAccessException(e);
            }
        } else {
            throw new RedisAccessException("集群模式不支持有多个不同key的脚本");
        }
    }

    @Override
    public <T> T runSHAScript(String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz) {
        return runSHAScript(luaScript, keys, args, resultClazz, null);
    }

    @Override
    public <T> T runSHAScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz, IRedisSerializer<T> resultSerializer) {
        if (keys.size() == 1) {
            try (JedisCluster client = getJedis()) {
                byte[] scriptBytes = client.scriptLoad(CharsetHelper.getUtf8Bytes(luaScript), rawValue(keys.get(0)));
                Object r = client.evalsha(scriptBytes, rawValues(keys), rawValues(args));
                Object cr = convertScriptResult(r, resultClazz);
                return deserializeResult(cr, resultClazz, resultSerializer);
            } catch (Exception e) {
                throw new RedisAccessException(e);
            }
        } else {
            throw new RedisAccessException("集群模式不支持有多个不同key的脚本");
        }
    }

    @Override
    public void flushDB() {
        throw new RedisAccessException("集群模式不支持flushDB");
    }

    @Override
    public void publish(String channel, String message) {
        try (JedisCluster client = getJedis()) {
            client.publish(channel, message);
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

    interface CmdHandler<T> {

        T doHandle(JedisCluster client, byte[]... keyBytes);
    }

    private <T> T execute(Serializable key, CmdHandler<T> handler) throws RedisAccessException {
        assertNotNull(key, "cache key not be null");
        try (JedisCluster client = getJedis()) {
            return handler.doHandle(client, rawValue(key));
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

    private JedisCluster getJedis() {
        return this.conn.getJedisCluster();
    }
}
