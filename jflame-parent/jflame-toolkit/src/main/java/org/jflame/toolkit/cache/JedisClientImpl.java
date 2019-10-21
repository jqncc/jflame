package org.jflame.toolkit.cache;

import java.io.IOException;
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

import org.jflame.toolkit.cache.serialize.FastJsonRedisSerializer;
import org.jflame.toolkit.cache.serialize.IGenericRedisSerializer;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.DateHelper;
import org.jflame.toolkit.util.MapHelper;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

/**
 * 基于jedis的RedisClient实现
 * 
 * @author yucan.zhang
 */
public class JedisClientImpl implements RedisClient {

    private IGenericRedisSerializer serializer;

    private JedisConnection conn;

    public JedisClientImpl(JedisConnection conn) {
        this(conn, new FastJsonRedisSerializer());
    }

    public JedisClientImpl(JedisConnection conn, IGenericRedisSerializer serializer) {
        this.conn = conn;
        this.serializer = serializer;
    }

    private Jedis getJedis() {
        return this.conn.getJedis();
    }

    @Override
    public IGenericRedisSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void set(Serializable key, Serializable value) {
        execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                String reply = client.set(keyBytes[0], toBytes(value));
                return ok.equals(reply);
            }
        });
    }

    @Override
    public void set(Serializable key, Serializable value, long timeout, TimeUnit timeUnit) {
        execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
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
    public void multiSet(Map<? extends Serializable,? extends Serializable> pair) {
        if (MapHelper.isEmpty(pair)) {
            return;
        }
        byte[][] kvBytes = toKeysValues(pair);
        execute(kvBytes, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                client.mset(keyBytes);
                return true;
            }
        });
    }

    @Override
    public boolean setIfAbsent(Serializable key, Serializable value) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                long r = client.setnx(keyBytes[0], toBytes(value));
                return r == 1;
            }

        });
    }

    @Override
    public boolean setIfAbsent(Serializable key, Serializable value, long timeout, TimeUnit timeUnit) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
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
    public void multiSetIfAbsent(Map<? extends Serializable,? extends Serializable> pair) {
        if (MapHelper.isEmpty(pair)) {
            return;
        }
        byte[][] kvBytes = toKeysValues(pair);

        execute(kvBytes, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                client.msetnx(keyBytes);
                return true;
            }
        });
    }

    @Override
    public <T extends Serializable> T get(Serializable key) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                return fromBytes(client.get(keyBytes[0]));
            }
        });
    }

    @Override
    public <T extends Serializable> T getAndSet(Serializable key, T newValue) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                return fromBytes(client.getSet(keyBytes[0], toBytes(newValue)));
            }
        });
    }

    @Override
    public <T extends Serializable> List<T> multiGet(Collection<? extends Serializable> keys) {
        return execute(keys, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                List<byte[]> valueBytes = client.mget(toByteArray(keys));
                return fromBytes(valueBytes);
            }

        });
    }

    @Override
    public boolean delete(Serializable key) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                return client.del(keyBytes[0]) > 0;
            }

        });
    }

    @Override
    public long delete(Collection<? extends Serializable> keys) {
        return execute(keys, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                return client.del(keyBytes);
            }
        });
    }

    @Override
    public boolean exists(Serializable key) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                return client.exists(keyBytes[0]);
            }
        });
    }

    @Override
    public boolean expire(Serializable key, int seconds) {
        return expire(key, seconds, TimeUnit.SECONDS);
    }

    public boolean expire(Serializable key, long timeout, TimeUnit timeUnit) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                if (TimeUnit.SECONDS == timeUnit) {
                    return client.expire(keyBytes[0], (int) timeout) == 1L;
                } else {
                    return client.expire(keyBytes[0], (int) timeUnit.toSeconds(timeout)) == 1L;
                }
            }
        });
    }

    @Override
    public boolean expireAt(Serializable key, Date date) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                return client.expireAt(keyBytes[0], DateHelper.unixTimestamp(date)) == 1L;
            }

        });
    }

    @Override
    public Long incr(Serializable key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                return client.incr(keyBytes[0]);
            }

        });
    }

    @Override
    public Long incr(Serializable key, long incrValue) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                return client.incrBy(keyBytes[0], incrValue);
            }

        });
    }

    @Override
    public Double incrByFloat(Serializable key, double incrValue) {
        return execute(key, new CmdHandler<Double>() {

            @Override
            public Double doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                return client.incrByFloat(keyBytes[0], incrValue);
            }
        });
    }

    @Override
    public boolean persist(Serializable key) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                return client.persist(keyBytes[0]) == 1;
            }
        });
    }

    @Override
    public <T extends Serializable> T hget(Serializable key, final Serializable fieldKey) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                byte[] valueBytes = client.hget(keyBytes[0], toBytes(fieldKey));
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public <T extends Serializable> List<T> hmultiGet(Serializable key, Collection<? extends Serializable> fieldKeys) {
        return execute(key, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(Jedis client, byte[]... keyBytes) throws RedisAccessException {
                List<byte[]> valueBytes = client.hmget(keyBytes[0], toByteArray(fieldKeys));
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public long hdelete(Serializable key, Object fieldKey) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.hdel(keyBytes[0], toBytes(fieldKey));
            }
        });
    }

    @Override
    public void hput(final Serializable key, final Serializable fieldKey, final Serializable value) {
        execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.hset(keyBytes[0], toBytes(fieldKey), toBytes(value));
            }
        });

    }

    @Override
    public void hput(Serializable key, Serializable fieldKey, Serializable value, int expireInSecond) {
        executePipeline(key, new PipelineHandler<Object>() {

            @Override
            public Object doHandle(Pipeline pipeline, byte[]... keyBytes) {
                pipeline.hset(keyBytes[0], toBytes(fieldKey), toBytes(value));
                pipeline.expire(keyBytes[0], expireInSecond);
                pipeline.sync();
                return null;
            }

        });
    }

    @Override
    public boolean hputIfAbsent(final Serializable key, final Serializable fieldKey, final Serializable value) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) {
                Long r = client.hsetnx(keyBytes[0], toBytes(fieldKey), toBytes(value));
                return r == 1;
            }
        });
    }

    @Override
    public void hputAll(Serializable key, final Map<? extends Serializable,? extends Serializable> map) {
        execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) {
                client.hmset(keyBytes[0], toBytes(map));
                return null;
            }
        });
    }

    @Override
    public <T extends Serializable> List<T> hvalues(Serializable key) {
        return execute(key, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(Jedis client, byte[]... keyBytes) {
                List<byte[]> valueBytes = client.hvals(keyBytes[0]);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> hkeys(Serializable key) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(Jedis client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.hkeys(keyBytes[0]);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public boolean hexists(Serializable key, Object fieldKey) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) {
                return client.hexists(keyBytes[0], toBytes(fieldKey));
            }
        });
    }

    @Override
    public long hsize(Serializable key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.hlen(keyBytes[0]);
            }
        });
    }

    @Override
    public long sadd(Serializable key, Serializable... values) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.sadd(keyBytes[0], toBytes(values));
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Serializable key, Collection<? extends Serializable> keys) {
        List<Object> keyList = new ArrayList<>(keys);
        keyList.add(0, key);
        return execute(keyList, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(Jedis client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.sdiff(keyBytes);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Serializable key, Serializable otherKey) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(Jedis client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.sdiff(keyBytes[0], toBytes(otherKey));
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public void sdiffAndStore(Serializable key, Serializable otherKey, Serializable destKey) {
        execute(key, new CmdHandler<Object>() {

            @Override
            public Object doHandle(Jedis client, byte[]... keyBytes) {
                client.sdiffstore(toBytes(destKey), keyBytes[0], toBytes(otherKey));
                return null;
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> sintersect(Serializable key, Serializable otherKey) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(Jedis client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.sinter(keyBytes[0], toBytes(otherKey));
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> sintersect(List<? extends Serializable> keys) {
        if (keys != null && keys.size() >= 2) {
            return execute(keys, new CmdHandler<Set<T>>() {

                @Override
                public Set<T> doHandle(Jedis client, byte[]... keyBytes) {
                    Set<byte[]> valueBytes = client.sinter(keyBytes);
                    return fromBytes(valueBytes);
                }
            });
        }
        return null;
    }

    @Override
    public void sintersectAndStore(List<? extends Serializable> keys, Serializable destKey) {
        if (keys != null && keys.size() >= 2) {
            execute(keys, new CmdHandler<Object>() {

                @Override
                public Object doHandle(Jedis client, byte[]... keyBytes) {
                    client.sinterstore(toBytes(destKey), keyBytes);
                    return null;
                }
            });
        }
    }

    @Override
    public <T extends Serializable> Set<T> sunion(Serializable key, Serializable otherKey) {
        return execute(Arrays.asList(key, otherKey), new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(Jedis client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.sunion(keyBytes);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public void sunionAndStore(Serializable key, Serializable otherKey, Serializable destKey) {
        execute(Arrays.asList(key, otherKey), new CmdHandler<Object>() {

            @Override
            public Object doHandle(Jedis client, byte[]... keyBytes) {
                client.sunionstore(toBytes(destKey), keyBytes);
                return null;
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> smember(Serializable key) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(Jedis client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.smembers(keyBytes[0]);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public boolean smove(Serializable key, Serializable destKey, Serializable value) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) {
                long r = client.smove(keyBytes[0], toBytes(destKey), toBytes(value));
                return r == 1;
            }
        });
    }

    @Override
    public <T extends Serializable> T spop(Serializable key) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) {
                byte[] valueByte = client.spop(keyBytes[0]);
                return fromBytes(valueByte);
            }
        });
    }

    @Override
    public <T extends Serializable> List<T> srandomMembers(Serializable key, int count) {
        return execute(key, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(Jedis client, byte[]... keyBytes) {
                List<byte[]> valueByte = client.srandmember(keyBytes[0], count);
                return fromBytes(valueByte);
            }
        });
    }

    @Override
    public long sremove(Serializable key, Object... members) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.srem(keyBytes[0], toBytes(members));
            }
        });
    }

    @Override
    public long ssize(Serializable key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.scard(keyBytes[0]);
            }
        });
    }

    @Override
    public boolean zsadd(Serializable key, Serializable value, double score) {
        return execute(key, new CmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(Jedis client, byte[]... keyBytes) {
                long r = client.zadd(keyBytes[0], score, toBytes(value));
                return r == 1;
            }
        });
    }

    @Override
    public long zsadd(Serializable key, Map<? extends Serializable,Double> memberScores) {
        Map<byte[],Double> sMap = new HashMap<>();
        for (Map.Entry<? extends Serializable,Double> kv : memberScores.entrySet()) {
            if (kv.getKey() == null) {
                throw new IllegalArgumentException("不允许有null key");
            }
            sMap.put(toBytes(kv.getKey()), kv.getValue());
        }
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.zadd(keyBytes[0], sMap);
            }
        });
    }

    @Override
    public long zssize(Serializable key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.zcard(keyBytes[0]);
            }
        });
    }

    @Override
    public long zscount(Serializable key, double min, double max) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.zcount(keyBytes[0], min, max);
            }
        });
    }

    @Override
    public Double zsincrBy(Serializable key, Serializable member, double incrScore) {
        return execute(key, new CmdHandler<Double>() {

            @Override
            public Double doHandle(Jedis client, byte[]... keyBytes) {
                return client.zincrby(keyBytes[0], incrScore, toBytes(member));
            }
        });
    }

    @Override
    public Double zscore(Serializable key, Serializable member) {
        return execute(key, new CmdHandler<Double>() {

            @Override
            public Double doHandle(Jedis client, byte[]... keyBytes) {
                return client.zscore(keyBytes[0], toBytes(member));
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> zsrange(Serializable key, long startIndex, long endIndex) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(Jedis client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.zrange(keyBytes[0], startIndex, endIndex);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public Map<? extends Serializable,Double> zsrangeWithScores(Serializable key, long startIndex, long endIndex) {
        return execute(key, new CmdHandler<Map<? extends Serializable,Double>>() {

            @Override
            public Map<? extends Serializable,Double> doHandle(Jedis client, byte[]... keyBytes) {
                Set<Tuple> tuples = client.zrangeWithScores(keyBytes[0], startIndex, endIndex);
                Map<Serializable,Double> memberScoreMap = null;
                if (CollectionHelper.isNotEmpty(tuples)) {
                    memberScoreMap = new HashMap<>();
                    for (Tuple tuple : tuples) {
                        memberScoreMap.put(fromBytes(tuple.getBinaryElement()), tuple.getScore());
                    }
                }
                return memberScoreMap;
            }
        });
    }

    @Override
    public <T extends Serializable> Set<T> zsrangeByScore(Serializable key, double min, double max) {
        return execute(key, new CmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(Jedis client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.zrangeByScore(keyBytes[0], min, max);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public long zsremove(Serializable key, Object... members) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.zrem(keyBytes[0], toBytes(members));
            }
        });
    }

    @Override
    public long zsremove(Serializable key, long start, long end) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.zremrangeByRank(keyBytes[0], start, end);
            }
        });
    }

    @Override
    public long zsremoveByScore(Serializable key, double minScore, double maxScore) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.zremrangeByScore(keyBytes[0], minScore, maxScore);
            }
        });
    }

    @Override
    public long lpush(Serializable key, Serializable... values) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.lpush(keyBytes[0], toBytes(values));
            }
        });
    }

    @Override
    public long lpushIfAbsent(Serializable key, Serializable value) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.lpushx(keyBytes[0], toBytes(value));
            }
        });
    }

    @Override
    public long rpush(Serializable key, Serializable... values) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.rpush(keyBytes[0], toBytes(values));
            }
        });
    }

    @Override
    public long rpushIfAbsent(Serializable key, Serializable value) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.rpushx(keyBytes[0], toBytes(value));
            }
        });
    }

    @Override
    public long linsert(Serializable key, Serializable value, Serializable pivot) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.linsert(keyBytes[0], LIST_POSITION.BEFORE, toBytes(pivot), toBytes(value));
            }
        });
    }

    @Override
    public <T extends Serializable> T lpop(Serializable key) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) {
                byte[] valueByte = client.lpop(keyBytes[0]);
                return fromBytes(valueByte);
            }
        });
    }

    @Override
    public <T extends Serializable> T lBlockPop(Serializable key, int timeout) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) {
                List<byte[]> valueBytes = client.blpop(timeout, keyBytes[0]);
                if (CollectionHelper.isNotEmpty(valueBytes)) {
                    return fromBytes(valueBytes.get(0));
                }
                return null;
            }
        });
    }

    @Override
    public <T extends Serializable> T rpop(Serializable key) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) {
                byte[] valueByte = client.rpop(keyBytes[0]);
                return fromBytes(valueByte);
            }
        });
    }

    @Override
    public <T extends Serializable> T rBlockPop(Serializable key, int timeout) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) {
                List<byte[]> valueBytes = client.brpop(timeout, keyBytes[0]);
                if (CollectionHelper.isNotEmpty(valueBytes)) {
                    return fromBytes(valueBytes.get(0));
                }
                return null;
            }
        });
    }

    @Override
    public long lsize(Serializable key) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.llen(keyBytes[0]);
            }
        });
    }

    @Override
    public <T extends Serializable> List<T> lrange(Serializable key, long start, long end) {
        return execute(key, new CmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(Jedis client, byte[]... keyBytes) {
                List<byte[]> valueBytes = client.lrange(keyBytes[0], start, end);
                return fromBytes(valueBytes);
            }
        });
    }

    @Override
    public void ltrim(Serializable key, long start, long end) {
        execute(key, new CmdHandler<Object>() {

            @Override
            public Object doHandle(Jedis client, byte[]... keyBytes) {
                client.ltrim(keyBytes[0], start, end);
                return null;
            }
        });
    }

    @Override
    public <T extends Serializable> T lindex(Serializable key, long index) {
        return execute(key, new CmdHandler<T>() {

            @Override
            public T doHandle(Jedis client, byte[]... keyBytes) {
                byte[] valueByte = client.lindex(keyBytes[0], index);
                return fromBytes(valueByte);
            }
        });
    }

    @Override
    public void lset(Serializable key, long index, Serializable value) {
        execute(key, new CmdHandler<Object>() {

            @Override
            public Object doHandle(Jedis client, byte[]... keyBytes) {
                client.lset(keyBytes[0], index, toBytes(value));
                return null;
            }
        });
    }

    @Override
    public Long lremove(Serializable key, Serializable value) {
        return lremove(key, 0, value);
    }

    @Override
    public Long lremove(Serializable key, long count, Serializable value) {
        return execute(key, new CmdHandler<Long>() {

            @Override
            public Long doHandle(Jedis client, byte[]... keyBytes) {
                return client.lrem(keyBytes[0], count, toBytes(value));
            }
        });
    }

    @Override
    public <T> T runScript(final String luaScript, List<? extends Serializable> keys, List<? extends Serializable> args,
            Class<T> resultClazz) {
        try (Jedis client = getJedis()) {
            Object r = client.eval(CharsetHelper.getUtf8Bytes(luaScript), toBytes(keys), toBytes(args));
            Object cr = convertScriptResult(r, resultClazz);
            return deserializeResult(cr, resultClazz);
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T runSHAScript(final String luaScript, List<? extends Serializable> keys,
            List<? extends Serializable> args, Class<T> resultClazz) {
        try (Jedis client = getJedis()) {
            byte[] scriptBytes = client.scriptLoad(CharsetHelper.getUtf8Bytes(luaScript));
            Object r = client.evalsha(scriptBytes, toBytes(keys), toBytes(args));
            Object cr = convertScriptResult(r, resultClazz);
            return deserializeResult(cr, resultClazz);
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

    @Override
    public long ttl(Serializable key) {
        try (Jedis client = getJedis()) {
            return client.ttl(toBytes(key));
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

    public Set<? extends Serializable> keys(String pattern) {
        return execute(pattern, new CmdHandler<Set<? extends Serializable>>() {

            @Override
            public Set<? extends Serializable> doHandle(Jedis client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.keys(toBytes(pattern));
                return fromBytes(valueBytes);
            }
        });
    }

    interface CmdHandler<T> {

        T doHandle(Jedis client, byte[]... keyBytes);
    }

    interface PipelineHandler<T> {

        T doHandle(Pipeline pipeline, byte[]... keyBytes);
    }

    private <T> T execute(Serializable key, CmdHandler<T> handler) throws RedisAccessException {
        if (key == null) {
            throw new NullPointerException("cache key not be null");
        }
        try (Jedis client = getJedis()) {
            return handler.doHandle(client, toBytes(key));
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

    private <T> T executePipeline(Serializable key, PipelineHandler<T> handler) throws RedisAccessException {
        if (key == null) {
            throw new NullPointerException("cache key not be null");
        }
        Jedis client = null;
        Pipeline pipeline = null;
        try {
            client = getJedis();
            pipeline = client.pipelined();
            return handler.doHandle(pipeline, toBytes(key));
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

    private <T> T execute(Collection<?> key, CmdHandler<T> handler) throws RedisAccessException {
        try (Jedis client = getJedis()) {
            return handler.doHandle(client, toByteArray(key));
        } catch (Exception e) {
            throw new RedisAccessException(e);
        }
    }

}
