package org.jflame.toolkit.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jflame.toolkit.exception.DataAccessException;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.DateHelper;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.JedisCluster;

public class JedisClusterClientImpl implements RedisClient {

    private IGenericSerializer serializer;
    private JedisConnection conn;

    public JedisClusterClientImpl(JedisConnection conn) {
        this.conn = conn;
        serializer = new FastJsonSerializer();
    }

    private JedisCluster getJedis() {
        return this.conn.getJedisCluster();
    }

    @Override
    public Object getNativeClient() {
        return getJedis();
    }

    @Override
    public void setSerializer(IGenericSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public IGenericSerializer getSerializer() {
        return serializer;
    }

    interface CmdHandler<T> {

        T doHandle(JedisCluster client, byte[]... keyBytes);
    }

    private <T> T execute(Object key, CmdHandler<T> handler) throws DataAccessException {
        if (key == null) {
            throw new NullPointerException("cache key not be null");
        }
        try (JedisCluster client = getJedis()) {
            return handler.doHandle(client, toBytes(key));
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(Collection<?> key, CmdHandler<T> handler) throws DataAccessException {
        try (JedisCluster client = getJedis()) {
            return handler.doHandle(client, toByteArray(key));
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void set(Object key, Object value) {
        execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                String reply = client.set(keyBytes[0], toBytes(value));
                return ok.equals(reply);
            }
        });
    }

    @Override
    public void set(Object key, Object value, long timeout, TimeUnit timeUnit) {
        execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                String reply;
                if (timeUnit == TimeUnit.MILLISECONDS) {
                    reply = client.set(keyBytes[0], toBytes(value), xxBytes, pxBytes, timeUnit.toMillis(timeout));
                } else {
                    reply = client.setex(keyBytes[0], (int) timeUnit.toSeconds(timeout), toBytes(value));
                }
                return ok.equals(reply);
            }
        });
    }

    @Override
    public boolean setIfAbsent(Object key, Object value) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                long r = client.setnx(keyBytes[0], toBytes(value));
                return r == 1;
            }
        });
    }

    @Override
    public boolean setIfAbsent(Object key, Object value, long timeout, TimeUnit timeUnit) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
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
            public T doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return fromBytes(client.get(keyBytes[0]));
            }
        });
    }

    @Override
    public <T extends Serializable> T getAndSet(Object key, T newValue) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return fromBytes(client.getSet(keyBytes[0], toBytes(newValue)));
            }
        });
    }

    @Override
    public <T extends Serializable> List<T> multiGet(Collection<?> keys) {
        return execute(keys, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                List<byte[]> valueBytes = client.mget(toByteArray(keys));
                return fromBytes(valueBytes);
            }

        });
    }

    @Override
    public boolean delete(Object key) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return client.del(keyBytes[0]) > 0;
            }

        });
    }

    @Override
    public long delete(Collection<?> keys) {
        return execute(keys, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return client.del(keyBytes);
            }
        });
    }

    @Override
    public boolean exists(Object key) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return client.exists(keyBytes[0]);
            }

        });
    }

    @Override
    public boolean expire(Object key, int seconds) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return client.expire(keyBytes[0], seconds) == 1L;
            }
        });
    }

    @Override
    public boolean expireAt(Object key, Date date) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return client.expireAt(keyBytes[0], DateHelper.unixTimestamp(date)) == 1L;
            }
        });
    }

    @Override
    public Long incr(Object key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return client.incr(keyBytes[0]);
            }

        });
    }

    @Override
    public Long incr(Object key, long incrValue) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return client.incrBy(keyBytes[0], incrValue);
            }

        });
    }

    @Override
    public Double incrByFloat(Object key, double incrValue) {
        return execute(key, new CmdHandler<Double>() {

            @Override
            public Double doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return client.incrByFloat(keyBytes[0], incrValue);
            }
        });
    }

    @Override
    public boolean persist(Object key) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                return client.persist(keyBytes[0]) == 1;
            }
        });
    }

    @Override
    public <T extends Serializable> T hget(Object key, Object fieldKey) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                byte[] valueBytes = client.hget(keyBytes[0], toBytes(fieldKey));
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public <T extends Serializable> List<T> hmultiGet(Object key, Collection<?> fieldKeys) {
        return execute(key, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(JedisCluster client, byte[]... keyBytes) throws DataAccessException {
                List<byte[]> valueBytes = client.hmget(keyBytes[0], toByteArray(fieldKeys));
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public long hdelete(Object key, Object fieldKey) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.hdel(keyBytes[0], toBytes(fieldKey));
            }
        });
    }

    @Override
    public void hput(Object key, Object fieldKey, Object value) {
        execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.hset(keyBytes[0], toBytes(fieldKey), toBytes(value));
            }
        });
    }

    @Override
    public boolean hputIfAbsent(Object key, Object fieldKey, Object value) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) {
                Long r = client.hsetnx(keyBytes[0], toBytes(fieldKey), toBytes(value));
                return r == 1;
            }
        });
    }

    @Override
    public void hputAll(Object key, Map<? extends Serializable,? extends Serializable> map) {
        execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) {
                client.hmset(keyBytes[0], toBytes(map));
                return null;
            }
        });
    }

    @Override
    public <T extends Serializable> List<T> hvalues(Object key) {
        return execute(key, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                Collection<byte[]> valueBytes = client.hvals(keyBytes[0]);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> hkeys(Object key) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.hkeys(keyBytes[0]);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public boolean hexists(Object key, Object fieldKey) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.hexists(keyBytes[0], toBytes(fieldKey));
            }
        });
    }

    @Override
    public long hsize(Object key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.hlen(keyBytes[0]);
            }
        });
    }

    @Override
    public long sadd(Object key, Serializable... values) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.sadd(keyBytes[0], toBytes(values));
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Object key, Object otherKey) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.sdiff(keyBytes[0], toBytes(otherKey));
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Object key, Collection<Object> keys) {
        List<Object> keyList = new ArrayList<>(keys);
        keyList.add(0, key);
        return execute(keyList, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.sdiff(keyBytes);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public void sdiffAndStore(Object key, Object otherKey, Object destKey) {
        execute(key, new CmdHandler<Object>() {

            @Override
            public Object doHandle(JedisCluster client, byte[]... keyBytes) {
                client.sdiffstore(toBytes(destKey), keyBytes[0], toBytes(otherKey));
                return null;
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> sintersect(Object key, Object otherKey) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.sinter(keyBytes[0], toBytes(otherKey));
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> sintersect(List<Object> keys) {
        if (keys != null && keys.size() >= 2) {
            return execute(keys, new CmdHandler<Set<T>>() {

                @Override
                public Set<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                    Set<byte[]> valueBytes = client.sinter(keyBytes);
                    return fromBytes(valueBytes);
                }
            });
        }
        return null;
    }

    @Override
    public void sintersectAndStore(List<Object> keys, Object destKey) {
        if (keys != null && keys.size() >= 2) {
            execute(keys, new CmdHandler<Object>() {

                @Override
                public Object doHandle(JedisCluster client, byte[]... keyBytes) {
                    client.sinterstore(toBytes(destKey), keyBytes);
                    return null;
                }
            });
        }
    }

    @Override
    public <T extends Serializable> Set<T> sunion(Object key, Object otherKey) {
        return execute(Arrays.asList(key, otherKey), new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.sunion(keyBytes);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public void sunionAndStore(Object key, Object otherKey, Object destKey) {
        execute(Arrays.asList(key, otherKey), new CmdHandler<Object>() {

            @Override
            public Object doHandle(JedisCluster client, byte[]... keyBytes) {
                client.sunionstore(toBytes(destKey), keyBytes);
                return null;
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> smember(Object key) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.smembers(keyBytes[0]);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public boolean smove(Object key, Object destKey, Serializable value) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) {
                long r = client.smove(keyBytes[0], toBytes(destKey), toBytes(value));
                return r == 1;
            }
        });
    }

    @Override
    public <T extends Serializable> T spop(Object key) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(JedisCluster client, byte[]... keyBytes) {
                byte[] valueByte = client.spop(keyBytes[0]);
                return fromBytes(valueByte);
            }
        });
    }

    @Override
    public <T extends Serializable> List<T> srandomMembers(Object key, int count) {
        return execute(key, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                List<byte[]> valueByte = client.srandmember(keyBytes[0], count);
                return fromBytes(valueByte);
            }
        });
    }

    @Override
    public long sremove(Object key, Object... members) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.srem(keyBytes[0], toBytes(members));
            }
        });
    }

    @Override
    public long ssize(Object key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.scard(keyBytes[0]);
            }
        });
    }

    @Override
    public boolean zsadd(Object key, Serializable value, double score) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(JedisCluster client, byte[]... keyBytes) {
                long r = client.zadd(keyBytes[0], score, toBytes(value));
                return r == 1;
            }
        });
    }

    @Override
    public long zsadd(Object key, Map<Object,Double> memberScores) {
        Map<byte[],Double> sMap = new HashMap<>();
        for (Map.Entry<Object,Double> kv : memberScores.entrySet()) {
            sMap.put(toBytes(kv.getKey()), kv.getValue());
        }
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.zadd(keyBytes[0], sMap);
            }
        });
    }

    @Override
    public long zssize(Object key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.zcard(keyBytes[0]);
            }
        });
    }

    @Override
    public long zscount(Object key, double min, double max) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.zcount(keyBytes[0], min, max);
            }
        });
    }

    @Override
    public Double zsincrScore(Object key, Serializable member, double incrScore) {
        return execute(key, new CmdHandler<Double>() {

            @Override
            public Double doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.zincrby(keyBytes[0], incrScore, toBytes(member));
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> zsrange(Object key, long startIndex, long endIndex) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.zrange(keyBytes[0], startIndex, endIndex);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> zsrangeByScore(Object key, double min, double max) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.zrangeByScore(keyBytes[0], min, max);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public long zsremove(Object key, Object... members) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.zrem(keyBytes[0], toBytes(members));
            }
        });
    }

    @Override
    public long zsremove(Object key, long start, long end) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.zremrangeByRank(keyBytes[0], start, end);
            }
        });
    }

    @Override
    public long zsremoveByScore(Object key, double minScore, double maxScore) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.zremrangeByScore(keyBytes[0], minScore, maxScore);
            }
        });
    }

    @Override
    public long lpush(Object key, Object... values) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.lpush(keyBytes[0], toBytes(values));
            }
        });
    }

    @Override
    public long lpushIfAbsent(Object key, Object value) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.lpushx(keyBytes[0], toBytes(value));
            }
        });
    }

    @Override
    public long rpush(Object key, Object... values) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.rpush(keyBytes[0], toBytes(values));
            }
        });
    }

    @Override
    public long rpushIfAbsent(Object key, Object value) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.rpushx(keyBytes[0], toBytes(value));
            }
        });
    }

    @Override
    public long linsert(Object key, Object value, Object pivot) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.linsert(keyBytes[0], LIST_POSITION.BEFORE, toBytes(pivot), toBytes(value));
            }
        });
    }

    @Override
    public <T extends Serializable> T lpop(Object key) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(JedisCluster client, byte[]... keyBytes) {
                byte[] valueByte = client.lpop(keyBytes[0]);
                return fromBytes(valueByte);
            }
        });
    }

    @Override
    public <T extends Serializable> T lBlockPop(Object key, int timeout) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(JedisCluster client, byte[]... keyBytes) {
                List<byte[]> valueBytes = client.blpop(timeout, keyBytes[0]);
                if (CollectionHelper.isNotEmpty(valueBytes)) {
                    return fromBytes(valueBytes.get(0));
                }
                return null;
            }
        });
    }

    @Override
    public <T extends Serializable> T rpop(Object key) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(JedisCluster client, byte[]... keyBytes) {
                byte[] valueByte = client.rpop(keyBytes[0]);
                return fromBytes(valueByte);
            }
        });
    }

    @Override
    public <T extends Serializable> T rBlockPop(Object key, int timeout) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(JedisCluster client, byte[]... keyBytes) {
                List<byte[]> valueBytes = client.brpop(timeout, keyBytes[0]);
                if (CollectionHelper.isNotEmpty(valueBytes)) {
                    return fromBytes(valueBytes.get(0));
                }
                return null;
            }
        });
    }

    @Override
    public long lsize(Object key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.llen(keyBytes[0]);
            }
        });
    }

    @Override
    public <T extends Serializable> List<T> lrange(Object key, long start, long end) {
        return execute(key, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(JedisCluster client, byte[]... keyBytes) {
                List<byte[]> valueBytes = client.lrange(keyBytes[0], start, end);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public void ltrim(Object key, long start, long end) {
        execute(key, new CmdHandler<Object>() {

            @Override
            public Object doHandle(JedisCluster client, byte[]... keyBytes) {
                client.ltrim(keyBytes[0], start, end);
                return null;
            }
        });
    }

    @Override
    public <T extends Serializable> T lindex(Object key, long index) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(JedisCluster client, byte[]... keyBytes) {
                byte[] valueByte = client.lindex(keyBytes[0], index);
                return fromBytes(valueByte);
            }
        });
    }

    @Override
    public void lset(Object key, long index, Object value) {
        execute(key, new CmdHandler<Object>() {

            @Override
            public Object doHandle(JedisCluster client, byte[]... keyBytes) {
                client.lset(keyBytes[0], index, toBytes(value));
                return null;
            }
        });
    }

    @Override
    public Long lremove(Object key, long count, Object value) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(JedisCluster client, byte[]... keyBytes) {
                return client.lrem(keyBytes[0], count, toBytes(value));
            }
        });
    }

    @Override
    public long ttl(Object key) {
        try (JedisCluster client = getJedis()) {
            return client.ttl(toBytes(key));
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public <T> T runScript(String luaScript, List<Object> keys, List<Object> args, Class<T> resultClazz) {
        // Redis要求单个Lua脚本操作的key必须在同一个节点上,如果只有一个keys执行,如果多key抛异常
        if (keys.size() == 1) {
            try (JedisCluster client = getJedis()) {
                Object r = client.eval(CharsetHelper.getUtf8Bytes(luaScript), toBytes(keys), toBytes(args));
                Object cr = convertScriptResult(r, resultClazz);
                return deserializeResult(cr, resultClazz);
            } catch (Exception e) {
                throw new DataAccessException(e);
            }
        } else {
            throw new DataAccessException("暂不支持redis集群有多个不同key的LUA脚本");
        }
    }

    @Override
    public <T> T runSHAScript(String luaScript, List<Object> keys, List<Object> args, Class<T> resultClazz) {
        if (keys.size() == 1) {
            try (JedisCluster client = getJedis()) {
                byte[] scriptBytes = client.scriptLoad(CharsetHelper.getUtf8Bytes(luaScript), toBytes(keys.get(0)));
                Object r = client.evalsha(scriptBytes, toBytes(keys), toBytes(args));
                Object cr = convertScriptResult(r, resultClazz);
                return deserializeResult(cr, resultClazz);
            } catch (Exception e) {
                throw new DataAccessException(e);
            }
        } else {
            throw new DataAccessException("暂不支持redis集群有多个不同key的LUA脚本");
        }
    }

}
