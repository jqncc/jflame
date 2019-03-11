package org.jflame.toolkit.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jflame.toolkit.exception.DataAccessException;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.DateHelper;

import redis.clients.jedis.Jedis;

public class JedisClientImpl implements RedisClient {

    private IGenericSerializer serializer;
    private String ok = "OK";
    private JedisConnection conn;

    public JedisClientImpl(JedisConnection conn) {
        this.conn = conn;
    }

    private Jedis getJedis() {
        return this.conn.getJedis();
    }

    interface CmdHandler<T> {

        T doHandle(Jedis client, byte[]... keyBytes);
    }

    private <T> T execute(Object key, CmdHandler<T> handler) throws DataAccessException {
        if (key == null) {
            throw new NullPointerException("cache key not be null");
        }
        try (Jedis client = getJedis()) {
            return handler.doHandle(client, toBytes(key));
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(Collection<?> key, CmdHandler<T> handler) throws DataAccessException {
        try (Jedis client = getJedis()) {
            return handler.doHandle(client, toByteArray(key));
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void setSerializer(IGenericSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public IGenericSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void set(Object key, Object value) {
        execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                client.set(keyBytes[0], toBytes(value));
                return null;
            }
        });
    }

    @Override
    public void set(Object key, Object value, long timeout, TimeUnit timeUnit) {
        execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                if (timeUnit == TimeUnit.MILLISECONDS) {
                    client.psetex(toBytes(key), timeUnit.toMillis(timeout), toBytes(value));
                } else {
                    client.setex(toBytes(key), (int) timeUnit.toSeconds(timeout), toBytes(value));
                }
                return null;
            }
        });
    }

    @Override
    public boolean setIfAbsent(Object key, Object value) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                long r = client.setnx(keyBytes[0], toBytes(value));
                return r == 1;
            }

        });
    }

    @Override
    public boolean setIfAbsent(Object key, Object value, long timeout, TimeUnit timeUnit) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                String r;
                if (timeUnit == TimeUnit.MILLISECONDS) {
                    r = client.set(keyBytes[0], toBytes(value), nxBytes, pxBytes, timeUnit.toMillis(timeout));
                } else {
                    r = client.set(keyBytes[0], toBytes(value), nxBytes, exBytes, timeUnit.toSeconds(timeout));
                }
                return ok.equalsIgnoreCase(r);
            }

        });
    }

    @Override
    public <T extends Serializable> T get(Object key) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return fromBytes(client.get(keyBytes[0]));
            }

        });
    }

    @Override
    public <T extends Serializable> T getAndSet(Object key, T newValue) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return fromBytes(client.getSet(keyBytes[0], toBytes(newValue)));
            }

        });
    }

    @Override
    public <T extends Serializable> List<T> multiGet(Collection<?> keys) {
        return execute(keys, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                List<byte[]> valueBytes = client.mget(toByteArray(keys));
                return fromBytes(valueBytes);
            }

        });
    }

    @Override
    public boolean delete(Object key) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return client.del(keyBytes[0]) > 0;
            }

        });
    }

    @Override
    public long delete(Collection<?> keys) {
        return execute(keys, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return client.del(keyBytes);
            }
        });
    }

    @Override
    public boolean exists(Object key) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return client.exists(keyBytes[0]);
            }

        });
    }

    @Override
    public boolean expire(Object key, int seconds) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return client.expire(keyBytes[0], seconds) == 1L;
            }

        });
    }

    @Override
    public boolean expireAt(Object key, Date date) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return client.expireAt(keyBytes[0], DateHelper.unixTimestamp(date)) == 1L;
            }

        });
    }

    @Override
    public Long incr(Object key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return client.incr(keyBytes[0]);
            }

        });
    }

    @Override
    public Long incr(Object key, long incrValue) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return client.incrBy(keyBytes[0], incrValue);
            }

        });
    }

    @Override
    public Double incrByFloat(Object key, double incrValue) {
        return execute(key, new CmdHandler<Double>() {

            @Override
            public Double doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return client.incrByFloat(keyBytes[0], incrValue);
            }

        });
    }

    @Override
    public boolean persist(Object key) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                return client.persist(keyBytes[0]) == 1;
            }

        });
    }

    @Override
    public <T extends Serializable> T hget(Object key, final Object fieldKey) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                byte[] valueBytes = client.hget(keyBytes[0], toBytes(fieldKey));
                return fromBytes(valueBytes);
            }

        });
    }

    @Override
    public <T extends Serializable> List<T> hmultiGet(Object key, Collection<?> fieldKeys) {
        return execute(key, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(Jedis client, byte[]... keyBytes) throws DataAccessException {
                List<byte[]> valueBytes = client.hmget(keyBytes[0], toByteArray(fieldKeys));
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public void hdelete(Object key, Object fieldKey) {
        execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.hdel(keyBytes[0], toBytes(fieldKey));
            }
        });
    }

    @Override
    public void hput(Object key, Object fieldKey, Object value) {
    }

    @Override
    public boolean hputIfAbsent(Object key, Object fieldKey, Object value) {
        return false;
    }

    @Override
    public void hputAll(Object key, Map<? extends Serializable,? extends Serializable> map) {
    }

    @Override
    public <T extends Serializable> List<T> hvalues(Object key) {
        return null;
    }

    @Override
    public <T extends Serializable> Set<T> hkeys(Object key) {
        return null;
    }

    @Override
    public boolean hexists(Object key, Object fieldKey) {
        return false;
    }

    @Override
    public long hsize(Object key) {
        return 0;
    }

    @Override
    public long sadd(Object key, Serializable... value) {
        return 0;
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Object firstSetKey, Collection<?> keys) {
        return null;
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Object firstSetKey, Object otherKey) {
        return null;
    }

    @Override
    public void sdiffAndStore(Object firstSetKey, Object key, Object destKey) {
    }

    @Override
    public <T extends Serializable> Set<T> sintersect(Object key, Object otherKey) {
        return null;
    }

    @Override
    public void sintersectAndStore(Object key, Object otherKey, Object destKey) {
    }

    @Override
    public <T extends Serializable> Set<T> sunion(Object key, Object otherKey) {
        return null;
    }

    @Override
    public void sunionAndStore(Object key, Object otherKey, Object destKey) {
    }

    @Override
    public <T extends Serializable> Set<T> smember(Object key) {
        return null;
    }

    @Override
    public boolean smove(Object key, Object destKey, Serializable value) {
        return false;
    }

    @Override
    public <T extends Serializable> T spop(Object key) {
        return null;
    }

    @Override
    public <T extends Serializable> List<T> randomMembers(Object key, int count) {
        return null;
    }

    @Override
    public long sremove(Object key, Object... members) {
        return 0;
    }

    @Override
    public long ssize(Object key) {
        return 0;
    }

    @Override
    public boolean zsadd(Object key, Serializable value, double score) {
        return false;
    }

    @Override
    public long zsadd(Object key, Set<SortedSetTuple<Serializable>> tuple) {
        return 0;
    }

    @Override
    public long zssize(Object key) {
        return 0;
    }

    @Override
    public long zscount(Object key, double min, double max) {
        return 0;
    }

    @Override
    public Double zsincrScore(Object key, Serializable member, double incrScore) {
        return null;
    }

    @Override
    public <T extends Serializable> Set<T> zsrange(Object key, long startIndex, long endIndex) {
        return null;
    }

    @Override
    public <T extends Serializable> Set<T> zrangeByScore(Object key, double min, double max) {
        return null;
    }

    @Override
    public long zsremove(Object key, Object... members) {
        return 0;
    }

    @Override
    public void removeRange(Object key, long start, long end) {
    }

    @Override
    public void removeRangeByScore(Object key, double minScore, double maxScore) {
    }

    @Override
    public long lpush(Object key, Object... values) {
        return 0;
    }

    @Override
    public long lpushIfAbsent(Object key, Object value) {
        return 0;
    }

    @Override
    public long rpush(Object key, Object... values) {
        return 0;
    }

    @Override
    public long rpushIfAbsent(Object key, Object value) {
        return 0;
    }

    @Override
    public long linsert(Object key, Object value, Object pivot) {
        return 0;
    }

    @Override
    public <T extends Serializable> T lpop(Object key) {
        return null;
    }

    @Override
    public <T extends Serializable> T lpop(Object key, long timeout, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public <T extends Serializable> T rpop(Object key) {
        return null;
    }

    @Override
    public <T extends Serializable> T rpop(Object key, long timeout, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public long lsize(Object key) {
        return 0;
    }

    @Override
    public <T extends Serializable> List<T> lrange(Object key, long start, long end) {
        return null;
    }

    @Override
    public void ltrim(Object key, long start, long end) {
    }

    @Override
    public <T extends Serializable> T lindex(Object key, long index) {
        return null;
    }

    @Override
    public void lset(Object key, long index, Object value) {
    }

    @Override
    public Long lremove(Object key, long count, Object value) {
        return null;
    }

    @Override
    public Object runScript(final String luaScript, List<Object> keys, List<Object> args) {
        try (Jedis client = getJedis()) {
            List<Object> argList = Arrays.asList(args);
            Object r = client.eval(CharsetHelper.getUtf8Bytes(luaScript), toBytes(keys), toBytes(argList));
            return r;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Object getNativeClient() {
        return getJedis();
    }
}
