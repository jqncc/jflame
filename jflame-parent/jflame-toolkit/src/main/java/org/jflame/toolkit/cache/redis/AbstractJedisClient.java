package org.jflame.toolkit.cache.redis;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jflame.toolkit.cache.redis.serizlizer.BinaryJedisClusterCommandsAdapter;
import org.jflame.toolkit.cache.redis.serizlizer.IGenericRedisSerializer;
import org.jflame.toolkit.cache.redis.serizlizer.IRedisSerializer;
import org.jflame.toolkit.cache.redis.serizlizer.StringRedisSerializer;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.DateHelper;
import org.jflame.toolkit.util.MapHelper;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.MultiKeyBinaryCommands;
import redis.clients.jedis.Tuple;
import redis.clients.util.SafeEncoder;

public abstract class AbstractJedisClient implements RedisClient {

    protected IGenericRedisSerializer valueSerializer;
    protected StringRedisSerializer keySerializer = new StringRedisSerializer();

    protected JedisConnection conn;

    @Override
    public IGenericRedisSerializer getValueSerializer() {
        return valueSerializer;
    }

    @Override
    public IRedisSerializer<String> getKeySerializer() {
        return keySerializer;
    }

    @Override
    public void set(final String key, final Object value) {
        execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                String reply = client.set(key, rawValue(value));
                return ok.equals(reply);
            }
        });
    }

    @Override
    public void set(final String key, final Object value, long timeout, TimeUnit timeUnit) {
        execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                if (timeUnit == TimeUnit.MILLISECONDS) {
                    client.psetex(rawValue(key), timeUnit.toMillis(timeout), rawValue(value));
                } else {
                    client.setex(rawValue(key), (int) timeUnit.toSeconds(timeout), rawValue(value));
                }
                return null;
            }
        });
    }

    @Override
    public boolean setIfAbsent(String key, Object value) {
        return execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                long r = client.setnx(key, rawValue(value));
                return r == 1;
            }

        });
    }

    @Override
    public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit timeUnit) {
        return execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                String r;
                if (timeUnit == TimeUnit.MILLISECONDS) {
                    r = client.set(key, rawValue(value), nxBytes, pxBytes, timeUnit.toMillis(timeout));
                } else {
                    r = client.set(key, rawValue(value), nxBytes, exBytes, timeUnit.toSeconds(timeout));
                }
                return ok.equalsIgnoreCase(r);
            }

        });
    }

    @Override
    public <V> void multiSet(Map<String,V> pair) {
        if (MapHelper.isEmpty(pair)) {
            return;
        }
        final byte[][] kvBytes = rawKeyValueMap(pair);
        execute(Collections.emptySet(), new JedisMultiKeyCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(MultiKeyBinaryCommands client, byte[]... keys) throws RedisAccessException {
                client.mset(kvBytes);
                return true;
            }
        });
    }

    @Override
    public <V> void multiSetIfAbsent(Map<String,V> pair) {
        if (MapHelper.isEmpty(pair)) {
            return;
        }
        final byte[][] kvBytes = rawKeyValueMap(pair);
        execute(Collections.emptySet(), new JedisMultiKeyCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(MultiKeyBinaryCommands client, byte[]... keys) throws RedisAccessException {
                client.msetnx(kvBytes);
                return true;
            }
        });
    }

    @Override
    public <T> T get(final String key) {
        return execute(key, new JedisCmdHandler<T>() {

            @Override
            public T doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                return deserializeValue(client.get(key));
            }
        });
    }

    @Override
    public <T> List<T> multiGet(Collection<String> keyset) {
        if (CollectionHelper.isEmpty(keyset)) {
            return Collections.emptyList();
        }
        return execute(keyset, new JedisMultiKeyCmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(MultiKeyBinaryCommands client, byte[]... keys) throws RedisAccessException {
                List<byte[]> valueBytes = client.mget(keys);
                return deserializeValues(valueBytes);
            }

        });
    }

    @Override
    public <T> T getAndSet(final String key, T newValue) {
        return execute(key, new JedisCmdHandler<T>() {

            @Override
            public T doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                return deserializeValue(client.getSet(key, rawValue(newValue)));
            }
        });
    }

    @Override
    public boolean delete(final String key) {
        return execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                return client.del(key) > 0;
            }

        });
    }

    @Override
    public long delete(Set<String> keyset) {
        return execute(keyset, new JedisMultiKeyCmdHandler<Long>() {

            @Override
            public Long doHandle(MultiKeyBinaryCommands client, byte[]... keys) throws RedisAccessException {
                return client.del(keys);
            }
        });
    }

    @Override
    public boolean exists(final String key) {
        return execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                return client.exists(key);
            }
        });
    }

    @Override
    public boolean expire(final String key, final int seconds) {
        return expire(key, seconds, TimeUnit.SECONDS);
    }

    public boolean expire(final String key, long timeout, TimeUnit timeUnit) {
        return execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                if (TimeUnit.SECONDS == timeUnit) {
                    return client.expire(key, (int) timeout) == 1L;
                } else {
                    return client.expire(key, (int) timeUnit.toSeconds(timeout)) == 1L;
                }
            }
        });
    }

    @Override
    public boolean expireAt(final String key, Date date) {
        return execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                return client.expireAt(key, DateHelper.unixTimestamp(date)) == 1L;
            }

        });
    }

    @Override
    public Long incr(final String key) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                return client.incr(key);
            }

        });
    }

    @Override
    public Long incr(final String key, final long incrValue) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                return client.incrBy(key, incrValue);
            }

        });
    }

    @Override
    public Double incrByFloat(final String key, final double incrValue) {
        return execute(key, new JedisCmdHandler<Double>() {

            @Override
            public Double doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                return client.incrByFloat(key, incrValue);
            }
        });
    }

    @Override
    public <T> T hget(final String key, final String fieldKey) {
        return execute(key, new JedisCmdHandler<T>() {

            @Override
            public T doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                byte[] valueBytes = client.hget(key, rawKey(fieldKey));
                return deserializeValue(valueBytes);
            }
        });
    }

    @Override
    public <T> List<T> hmultiGet(final String key, Collection<String> fieldKeys) {
        return execute(key, new JedisCmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                List<byte[]> valueBytes = client.hmget(key, rawKeyArray(fieldKeys));
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public void hput(final String key, final String fieldKey, final Object value) {
        execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.hset(key, rawKey(fieldKey), rawValue(value));
            }
        });

    }

    @Override
    public boolean hputIfAbsent(final String key, final String fieldKey, final Object value) {
        return execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) {
                Long r = client.hsetnx(key, rawKey(fieldKey), rawValue(value));
                return r == 1;
            }
        });
    }

    @Override
    public void hputAll(final String key, final Map<String,? extends Serializable> map) {
        execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) {
                client.hmset(key, rawMap(map));
                return null;
            }
        });
    }

    @Override
    public <T> List<T> hvalues(final String key) {
        return execute(key, new JedisCmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(BinaryJedisCommands client, byte[] key) {
                Collection<byte[]> valueBytes = client.hvals(key);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public Set<String> hkeys(final String key) {
        return execute(key, new JedisCmdHandler<Set<String>>() {

            @Override
            public Set<String> doHandle(BinaryJedisCommands client, byte[] keybyte) {
                Set<byte[]> keyBytes = client.hkeys(keybyte);
                return deserializeKeys(keyBytes);
            }
        });
    }

    @Override
    public long hdelete(final String key, final String fieldKey) {
        assertNotNull(fieldKey, "fieldKey not be null");
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] keybyte) {
                return client.hdel(keybyte, rawKey(fieldKey));
            }
        });
    }

    @Override
    public boolean hexists(final String key, final String fieldKey) {
        assertNotNull(fieldKey, "fieldKey not be null");
        return execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] keybyte) {
                return client.hexists(keybyte, rawKey(fieldKey));
            }
        });
    }

    @Override
    public long hsize(String key) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] keybyte) {
                return client.hlen(keybyte);
            }
        });
    }

    @Override
    public long sadd(final String key, final Object... values) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] keybyte) {
                return client.sadd(keybyte, rawValues(values));
            }
        });
    }

    @Override
    public <T> Set<T> smember(String key) {
        return execute(key, new JedisCmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(BinaryJedisCommands client, byte[] key) {
                Set<byte[]> valueBytes = client.smembers(key);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public <T> Set<T> sdiff(String key, String otherKey) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(key);
        set.add(otherKey);
        return execute(set, new JedisMultiKeyCmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(MultiKeyBinaryCommands client, byte[]... keys) {
                Set<byte[]> valueBytes = client.sdiff(keys[0], keys[1]);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public <T> Set<T> sdiff(String key, Set<String> otherKeys) {
        LinkedHashSet<String> keyList = new LinkedHashSet<>();
        keyList.add(key);
        keyList.addAll(otherKeys);
        return execute(keyList, new JedisMultiKeyCmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(MultiKeyBinaryCommands client, byte[]... keys) {
                Set<byte[]> valueBytes = client.sdiff(keys);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public void sdiffAndStore(String key, String otherKey, String destKey) {
        LinkedHashSet<String> keyList = new LinkedHashSet<>();
        keyList.add(key);
        keyList.add(otherKey);
        keyList.add(destKey);
        execute(keyList, new JedisMultiKeyCmdHandler<Object>() {

            @Override
            public Object doHandle(MultiKeyBinaryCommands client, byte[]... keys) {
                client.sdiffstore(keys[2], keys[0], keys[1]);
                return null;
            }
        });
    }

    @Override
    public <T> Set<T> sintersect(final String key, final String otherKey) {
        LinkedHashSet<String> keyList = new LinkedHashSet<>();
        keyList.add(key);
        keyList.add(otherKey);
        return execute(keyList, new JedisMultiKeyCmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(MultiKeyBinaryCommands client, byte[]... keys) {
                Set<byte[]> valueBytes = client.sinter(keys[0], keys[1]);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public <T> Set<T> sintersect(final Set<String> keys) {
        if (keys == null || keys.size() < 2) {
            throw new IllegalArgumentException("parameter 'keys' size >=2");
        }
        return execute(keys, new JedisMultiKeyCmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(MultiKeyBinaryCommands client, byte[]... keys) {
                Set<byte[]> valueBytes = client.sinter(keys);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public void sintersectAndStore(final Set<String> keys, final String destKey) {
        if (keys == null || keys.size() < 2) {
            throw new IllegalArgumentException("parameter 'keys' size >=2");
        }
        execute(keys, new JedisMultiKeyCmdHandler<Object>() {

            @Override
            public Object doHandle(MultiKeyBinaryCommands client, byte[]... keyBytes) {
                client.sinterstore(rawKey(destKey), keyBytes);
                return null;
            }
        });
    }

    @Override
    public <T> Set<T> sunion(final String key, final String otherKey) {
        LinkedHashSet<String> keyset = new LinkedHashSet<>();
        keyset.add(key);
        keyset.add(otherKey);
        return execute(keyset, new JedisMultiKeyCmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(MultiKeyBinaryCommands client, byte[]... keyBytes) {
                Set<byte[]> valueBytes = client.sunion(keyBytes);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public void sunionAndStore(String key, String otherKey, String destKey) {
        LinkedHashSet<String> keyset = new LinkedHashSet<>();
        keyset.add(key);
        keyset.add(otherKey);
        keyset.add(destKey);
        execute(keyset, new JedisMultiKeyCmdHandler<Object>() {

            @Override
            public Object doHandle(MultiKeyBinaryCommands client, byte[]... keys) {
                client.sunionstore(keys[2], keys[0], keys[1]);
                return null;
            }
        });
    }

    @Override
    public boolean smove(final String key, final String destKey, final Object value) {
        LinkedHashSet<String> keyset = new LinkedHashSet<>();
        keyset.add(key);
        keyset.add(destKey);
        return execute(keyset, new JedisMultiKeyCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(MultiKeyBinaryCommands client, byte[]... keys) {
                long r = client.smove(keys[0], keys[1], rawValue(value));
                return r == 1;
            }
        });
    }

    @Override
    public long ssize(String key) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.scard(key);
            }
        });
    }

    @Override
    public <T> T spop(final String key) {
        return execute(key, new JedisCmdHandler<T>() {

            @Override
            public T doHandle(BinaryJedisCommands client, byte[] key) {
                byte[] valueBytes = client.spop(key);
                return deserializeValue(valueBytes);
            }
        });
    }

    @Override
    public <T> List<T> srandomMembers(final String key, int count) {
        return execute(key, new JedisCmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(BinaryJedisCommands client, byte[] key) {
                List<byte[]> valueBytes = client.srandmember(key, count);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public long sremove(final String key, Object... members) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.srem(key, rawValues(members));
            }
        });
    }

    @Override
    public boolean zsadd(String key, Object mermber, double score) {
        return execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) {
                long r = client.zadd(key, score, rawValue(mermber));
                return r == 1;
            }
        });
    }

    @Override
    public long zsadd(String key, Map<? extends Serializable,Double> memberScores) {
        Map<byte[],Double> sMap = new HashMap<>();
        for (Map.Entry<? extends Serializable,Double> kv : memberScores.entrySet()) {
            if (kv.getKey() == null) {
                throw new IllegalArgumentException("不允许有null key");
            }
            sMap.put(rawValue(kv.getKey()), kv.getValue());
        }
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.zadd(key, sMap);
            }
        });
    }

    @Override
    public Double zsincrBy(String key, Object member, double incrScore) {
        return execute(key, new JedisCmdHandler<Double>() {

            @Override
            public Double doHandle(BinaryJedisCommands client, byte[] key) {
                return client.zincrby(key, incrScore, rawValue(member));
            }
        });
    }

    @Override
    public Double zscore(String key, Object member) {
        return execute(key, new JedisCmdHandler<Double>() {

            @Override
            public Double doHandle(BinaryJedisCommands client, byte[] key) {
                return client.zscore(key, rawValue(member));
            }
        });
    }

    @Override
    public <T> Set<T> zsrange(String key, long startIndex, long endIndex) {
        return execute(key, new JedisCmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(BinaryJedisCommands client, byte[] key) {
                Set<byte[]> valueBytes = client.zrange(key, startIndex, endIndex);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public <T> Map<T,Double> zsrangeWithScores(String key, long startIndex, long endIndex) {
        return execute(key, new JedisCmdHandler<Map<T,Double>>() {

            @Override
            public Map<T,Double> doHandle(BinaryJedisCommands client, byte[] key) {
                Set<Tuple> tuples = client.zrangeWithScores(key, startIndex, endIndex);
                Map<T,Double> memberScoreMap = null;
                if (CollectionHelper.isNotEmpty(tuples)) {
                    memberScoreMap = new HashMap<>();
                    for (Tuple tuple : tuples) {
                        memberScoreMap.put(deserializeValue(tuple.getBinaryElement()), tuple.getScore());
                    }
                }
                return memberScoreMap;
            }
        });
    }

    @Override
    public <T> Set<T> zsrangeByScore(String key, double min, double max) {
        return execute(key, new JedisCmdHandler<Set<T>>() {

            @Override
            public Set<T> doHandle(BinaryJedisCommands client, byte[] key) {
                Set<byte[]> valueBytes = client.zrangeByScore(key, min, max);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public long zsremove(String key, Object... members) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.zrem(key, rawValues(members));
            }
        });
    }

    @Override
    public long zsremove(final String key, final long start, final long end) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.zremrangeByRank(key, start, end);
            }
        });
    }

    @Override
    public long zsremoveByScore(final String key, final double minScore, final double maxScore) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.zremrangeByScore(key, minScore, maxScore);
            }
        });
    }

    @Override
    public long zssize(String key) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.zcard(key);
            }
        });
    }

    @Override
    public long zscount(String key, double min, double max) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.zcount(key, min, max);
            }
        });
    }

    @Override
    public long lpush(String key, Object... values) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.lpush(key, rawValues(values));
            }
        });
    }

    @Override
    public long lpushIfAbsent(String key, Object value) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.lpushx(key, rawValue(value));
            }
        });
    }

    @Override
    public long rpush(String key, Object... values) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.rpush(key, rawValues(values));
            }
        });
    }

    @Override
    public long rpushIfAbsent(String key, Object value) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.rpushx(key, rawValue(value));
            }
        });
    }

    @Override
    public long linsert(String key, Object value, Object pivot) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.linsert(key, ListPosition.BEFORE, rawValue(pivot), rawValue(value));
            }
        });
    }

    @Override
    public <T> T lpop(String key) {
        return execute(key, new JedisCmdHandler<T>() {

            @Override
            public T doHandle(BinaryJedisCommands client, byte[] key) {
                byte[] valueByte = client.lpop(key);
                return deserializeValue(valueByte);
            }
        });
    }

    @Override
    public <T> T lBlockPop(String key, int timeout) {
        Set<String> keys = CollectionHelper.newSet(key);
        return execute(keys, new JedisMultiKeyCmdHandler<T>() {

            @Override
            public T doHandle(MultiKeyBinaryCommands client, byte[]... keyBytes) {
                List<byte[]> valueBytes = client.blpop(timeout, keyBytes[0]);
                if (CollectionHelper.isNotEmpty(valueBytes)) {
                    return deserializeValue(valueBytes.get(0));
                }
                return null;
            }
        });
    }

    @Override
    public <T> T rpop(String key) {
        return execute(key, new JedisCmdHandler<T>() {

            @Override
            public T doHandle(BinaryJedisCommands client, byte[] key) {
                byte[] valueByte = client.rpop(key);
                return deserializeValue(valueByte);
            }
        });
    }

    @Override
    public <T> T rBlockPop(String key, int timeout) {
        Set<String> keyset = CollectionHelper.newSet(key);
        return execute(keyset, new JedisMultiKeyCmdHandler<T>() {

            @Override
            public T doHandle(MultiKeyBinaryCommands client, byte[]... keys) {
                List<byte[]> valueBytes = client.brpop(timeout, keys[0]);
                if (CollectionHelper.isNotEmpty(valueBytes)) {
                    return deserializeValue(valueBytes.get(0));
                }
                return null;
            }
        });
    }

    @Override
    public <T> List<T> lrange(String key, long start, long end) {
        return execute(key, new JedisCmdHandler<List<T>>() {

            @Override
            public List<T> doHandle(BinaryJedisCommands client, byte[] key) {
                List<byte[]> valueBytes = client.lrange(key, start, end);
                return deserializeValues(valueBytes);
            }
        });
    }

    @Override
    public <T> T lindex(String key, long index) {
        return execute(key, new JedisCmdHandler<T>() {

            @Override
            public T doHandle(BinaryJedisCommands client, byte[] key) {
                byte[] valueByte = client.lindex(key, index);
                return deserializeValue(valueByte);
            }
        });
    }

    @Override
    public void lset(String key, long index, Object value) {
        execute(key, new JedisCmdHandler<Object>() {

            @Override
            public Object doHandle(BinaryJedisCommands client, byte[] key) {
                client.lset(key, index, rawValue(value));
                return null;
            }
        });
    }

    @Override
    public Long lremove(String key, Object value) {
        return lremove(key, 0, value);
    }

    @Override
    public Long lremove(String key, long count, Object value) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.lrem(key, count, rawValue(value));
            }
        });
    }

    @Override
    public long lsize(String key) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] key) {
                return client.llen(key);
            }
        });
    }

    @Override
    public void ltrim(String key, long start, long end) {
        execute(key, new JedisCmdHandler<Object>() {

            @Override
            public Object doHandle(BinaryJedisCommands client, byte[] key) {
                client.ltrim(key, start, end);
                return null;
            }
        });
    }

    @Override
    public long ttl(final String key) {
        return execute(key, new JedisCmdHandler<Long>() {

            @Override
            public Long doHandle(BinaryJedisCommands client, byte[] keyBytes) {
                return client.ttl(keyBytes);
            }

        });
    }

    @Override
    public boolean persist(final String key) {
        return execute(key, new JedisCmdHandler<Boolean>() {

            @Override
            public Boolean doHandle(BinaryJedisCommands client, byte[] key) throws RedisAccessException {
                return client.persist(key) == 1;
            }
        });
    }

    @SuppressWarnings({ "unchecked","rawtypes" })
    protected <T> T deserializeResult(Object result, Class<T> resultClazz, IRedisSerializer<T> resultSerializer) {
        if (result instanceof byte[]) {
            if (resultSerializer != null) {
                return (T) resultSerializer.deserialize((byte[]) result);
            } else {
                return deserializeValue((byte[]) result);
            }
        }
        if (result instanceof List) {
            List results = new ArrayList();
            for (Object obj : (List) result) {
                results.add(deserializeResult(obj, resultClazz, resultSerializer));
            }
            return (T) results;
        }
        return (T) result;
    }

    @SuppressWarnings("unchecked")
    protected Object convertScriptResult(Object result, Class<?> javaType) {
        if (result instanceof String) {
            // evalsha converts byte[] to String. Convert back for consistency
            return SafeEncoder.encode((String) result);
        }
        if (javaType == null) {
            return CharsetHelper.getUtf8String((byte[]) result);
        }
        if (javaType.isAssignableFrom(Boolean.class)) {
            // Lua false comes back as a null bulk reply
            if (result == null) {
                return Boolean.FALSE;
            }
            return ((Long) result == 1);
        }
        if (javaType.isAssignableFrom(List.class)) {
            List<Object> resultList = (List<Object>) result;
            List<Object> convertedResults = new ArrayList<Object>();
            for (Object res : resultList) {
                if (res instanceof String) {
                    // evalsha converts byte[] to String. Convert back for
                    // consistency
                    convertedResults.add(SafeEncoder.encode((String) res));
                } else {
                    convertedResults.add(res);
                }
            }
            return convertedResults;
        }
        return result;
    }

    protected interface JedisCmdHandler<T> {

        T doHandle(BinaryJedisCommands cmd, byte[] key);
    }

    protected <T> T execute(String key, JedisCmdHandler<T> handler) throws RedisAccessException {
        assertNotNull(key, "key not be null");
        Object nativeClient = null;
        try {
            nativeClient = getNativeClient();
            BinaryJedisCommands command = null;
            if (nativeClient instanceof JedisCluster) {
                command = new BinaryJedisClusterCommandsAdapter((JedisCluster) nativeClient);
            } else {
                command = (BinaryJedisCommands) nativeClient;
            }
            return handler.doHandle(command, rawKey(key));
        } catch (Exception e) {
            throw new RedisAccessException(e);
        } finally {
            if (nativeClient != null) {
                try {
                    ((Closeable) nativeClient).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected interface JedisMultiKeyCmdHandler<T> {

        T doHandle(MultiKeyBinaryCommands cmd, byte[]... key);
    }

    protected <T> T execute(Collection<String> keys, JedisMultiKeyCmdHandler<T> handler) throws RedisAccessException {
        Object nativeClient = null;
        try {
            nativeClient = getNativeClient();
            MultiKeyBinaryCommands command = null;
            if (nativeClient instanceof JedisCluster) {
                command = new BinaryJedisClusterCommandsAdapter((JedisCluster) nativeClient);
            } else {
                command = (MultiKeyBinaryCommands) nativeClient;
            }
            return handler.doHandle(command, rawKeyArray(keys));
        } catch (Exception e) {
            throw new RedisAccessException(e);
        } finally {
            if (nativeClient != null) {
                try {
                    ((Closeable) nativeClient).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
