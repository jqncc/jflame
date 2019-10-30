package org.jflame.toolkit.cache.redis;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jflame.toolkit.cache.redis.serizlizer.FastJsonRedisSerializer;
import org.jflame.toolkit.cache.redis.serizlizer.IGenericRedisSerializer;
import org.jflame.toolkit.cache.redis.serizlizer.IRedisSerializer;
import org.jflame.toolkit.util.CharsetHelper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 基于jedis的RedisClient实现
 * 
 * @author yucan.zhang
 */
public class JedisClientImpl extends AbstractJedisClient {

    public JedisClientImpl(JedisConnection conn) {
        this(conn, new FastJsonRedisSerializer());
    }

    public JedisClientImpl(JedisConnection conn, IGenericRedisSerializer valueSerializer) {
        this.conn = conn;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public void hput(String key, String fieldKey, Object value, int expireInSecond) {
        List<String> keyList = Arrays.asList(key, fieldKey);
        executePipeline(keyList, new PipelineHandler<Object>() {

            @Override
            public Object doHandle(Pipeline pipeline, byte[]... keyBytes) {
                pipeline.hset(keyBytes[0], keyBytes[1], rawValue(value));
                pipeline.expire(keyBytes[0], expireInSecond);
                pipeline.sync();
                return null;
            }

        });
    }

    @Override
    public <T> T runScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz) {
        return runScript(luaScript, keys, args, resultClazz, null);
    }

    @Override
    public <T> T runScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz, IRedisSerializer<T> resultSerializer) {
        try (Jedis client = getJedis()) {
            Object r = client.eval(rawKey(luaScript), rawKeys(keys), rawValues(args));
            Object cr = convertScriptResult(r, resultClazz);
            return deserializeResult(cr, resultClazz, resultSerializer);
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T runSHAScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz) {
        return runSHAScript(luaScript, keys, args, resultClazz, null);
    }

    @Override
    public <T> T runSHAScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz, IRedisSerializer<T> resultSerializer) {
        try (Jedis client = getJedis()) {
            byte[] scriptBytes = client.scriptLoad(CharsetHelper.getUtf8Bytes(luaScript));
            Object r = client.evalsha(scriptBytes, rawKeys(keys), rawValues(args));
            Object cr = convertScriptResult(r, resultClazz);
            return deserializeResult(cr, resultClazz, resultSerializer);
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

    public Set<String> keys(String pattern) {
        return execute(pattern, new CmdHandler<Set<String>>() {

            @Override
            public Set<String> doHandle(Jedis client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.keys(rawKey(pattern));
                return deserializeKeys(valueBytes);
            }
        });
    }

    @Override
    public void flushDB() {
        try (Jedis client = getJedis()) {
            client.flushDB();
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void publish(String channel, String message) {
        try (Jedis client = getJedis()) {
            client.publish(channel, message);
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

    /**
     * 返回内部连接对象jedis
     */
    @Override
    public Object getNativeClient() {
        return getJedis();
    }

    interface CmdHandler<T> {

        T doHandle(Jedis client, byte[]... keyBytes);
    }

    interface PipelineHandler<T> {

        T doHandle(Pipeline pipeline, byte[]... keyBytes);
    }

    private <T> T execute(String key, CmdHandler<T> handler) throws RedisAccessException {
        final byte[] keyBytes = rawKey(key);
        try (Jedis client = getJedis()) {
            return handler.doHandle(client, keyBytes);
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

    private <T> T executePipeline(Collection<String> keys, PipelineHandler<T> handler) throws RedisAccessException {
        Jedis client = null;
        Pipeline pipeline = null;
        final byte[][] keyBytes = rawKeyArray(keys);
        try {
            client = getJedis();
            pipeline = client.pipelined();
            return handler.doHandle(pipeline, keyBytes);
        } catch (Exception e) {
            throw new RedisAccessException(e);
        } finally {
            if (pipeline != null) {
                try {
                    pipeline.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (client != null) {
                client.close();
            }
        }
    }

    public IGenericRedisSerializer getValueSerializer() {
        return valueSerializer;
    }

    public IRedisSerializer<String> getKeySerializer() {
        return keySerializer;
    }

    private Jedis getJedis() {
        return this.conn.getJedis();
    }

}
