package org.jflame.commons.cache.redis;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.jflame.commons.cache.redis.serizlizer.FastJsonRedisSerializer;
import org.jflame.commons.cache.redis.serizlizer.IGenericRedisSerializer;
import org.jflame.commons.cache.redis.serizlizer.IRedisSerializer;
import org.jflame.commons.cache.redis.serizlizer.SpringRedisSerializeAdapter;
import org.jflame.commons.cache.redis.serizlizer.StringRedisSerializer;
import org.jflame.commons.util.CharsetHelper;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.MapHelper;

/**
 * 基于spring-data-redis实现RedisClient
 * 
 * @author yucan.zhang
 */
@SuppressWarnings("unchecked")
public class SpringCacheClientImpl implements RedisClient {

    private RedisTemplate<String,Object> redisTemplate;
    private IGenericRedisSerializer valueSerializer;
    private IRedisSerializer<String> keySerializer = new StringRedisSerializer();

    public SpringCacheClientImpl(RedisConnectionFactory redisConnection) {

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnection);

        valueSerializer = new FastJsonRedisSerializer();
        keySerializer = new StringRedisSerializer();
        SpringRedisSerializeAdapter<String> keySerializerAdpater = new SpringRedisSerializeAdapter<>(keySerializer);
        redisTemplate.setKeySerializer(keySerializerAdpater);
        redisTemplate.setHashKeySerializer(keySerializerAdpater);
        redisTemplate.setDefaultSerializer(new SpringRedisSerializeAdapter<Object>(valueSerializer));

        redisTemplate.afterPropertiesSet();
    }

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue()
                    .set(key, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue()
                    .set(key, value, timeout, timeUnit);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <V> void multiSet(Map<String,V> pair) {
        if (MapHelper.isEmpty(pair)) {
            return;
        }
        try {
            redisTemplate.opsForValue()
                    .multiSet(pair);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean setIfAbsent(String key, Object value) {
        try {
            return redisTemplate.opsForValue()
                    .setIfAbsent(key, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit timeUnit) {
        // spring-data-redis2.1.5以前版本未实现该接口,使用底层接口拼接命令
        try {
            return redisTemplate.execute(new RedisCallback<Boolean>() {

                @Override
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    byte[] keyBytes = rawKey(key);
                    byte[] valueBytes = rawValue(value);
                    byte[][] args = null;
                    if (timeUnit == TimeUnit.MILLISECONDS) {
                        args = new byte[][] { keyBytes,valueBytes,nxBytes,pxBytes,
                                CharsetHelper.getUtf8Bytes(String.valueOf(timeUnit.toMillis(timeout))) };
                    } else {
                        args = new byte[][] { keyBytes,valueBytes,nxBytes,exBytes,
                                CharsetHelper.getUtf8Bytes(String.valueOf(timeUnit.toSeconds(timeout))) };
                    }
                    Object obj = connection.execute("set", args);
                    if (obj != null) {
                        String r = CharsetHelper.getUtf8String((byte[]) obj);
                        return ok.equals(r);
                    }
                    return false;
                }
            });
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <V> void multiSetIfAbsent(Map<String,V> pair) {
        if (MapHelper.isEmpty(pair)) {
            return;
        }
        try {
            redisTemplate.opsForValue()
                    .multiSetIfAbsent(pair);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue()
                    .get(key);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T getAndSet(String key, T newValue) {
        try {
            return (T) redisTemplate.opsForValue()
                    .getAndSet(key, newValue);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> List<T> multiGet(Collection<String> keys) {
        try {
            List<Object> valueBytes = redisTemplate.opsForValue()
                    .multiGet(keys);
            return (List<T>) valueBytes;
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            redisTemplate.delete(key);
            return true;// 2.0以上版本才支持返回值
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long delete(Set<String> keys) {
        // return redisTemplate.delete(toBytes(keys));2.0以上版本才支持返回数量
        try {
            return redisTemplate.execute(new RedisCallback<Long>() {

                @Override
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                    byte[][] keyBytes = rawKeyArray(keys);
                    return connection.del(keyBytes);
                }
            });
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean expire(String key, int seconds) {
        try {
            return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, timeout, timeUnit);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean expireAt(String key, Date date) {
        try {
            return redisTemplate.expireAt(key, date);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Long incr(String key) {
        try {
            return redisTemplate.opsForValue()
                    .increment(key, 1);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Long incr(String key, long incrValue) {
        try {
            return redisTemplate.opsForValue()
                    .increment(key, incrValue);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Double incrByFloat(String key, double incrValue) {
        try {
            return redisTemplate.opsForValue()
                    .increment(key, incrValue);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean persist(String key) {
        try {
            return redisTemplate.persist(key);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T hget(String key, String fieldKey) {
        try {
            return (T) getHashOpt(key).get(fieldKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    private BoundHashOperations<String,String,Object> getHashOpt(String key) {
        return redisTemplate.boundHashOps(key);
    }

    @Override
    public <T> List<T> hmultiGet(String key, Collection<String> fieldKeys) {
        try {
            return (List<T>) getHashOpt(key).multiGet(fieldKeys);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long hdelete(String key, String fieldKey) {
        try {
            return getHashOpt(key).delete(fieldKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void hput(String key, String fieldKey, Object value) {
        try {
            getHashOpt(key).put(fieldKey, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void hput(String key, String fieldKey, Object value, int expireInSecond) {

        redisTemplate.executePipelined(new RedisCallback<Object>() {

            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = rawKey(key);
                byte[] fieldKeyBytes = rawKey(fieldKey);
                byte[] valueBytes = rawValue(value);
                connection.hSet(keyBytes, fieldKeyBytes, valueBytes);
                connection.expire(keyBytes, expireInSecond);
                return null;
            }
        });
    }

    @Override
    public boolean hputIfAbsent(String key, String fieldKey, Object value) {
        try {
            return getHashOpt(key).putIfAbsent(fieldKey, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void hputAll(String key, Map<String,? extends Serializable> map) {
        try {
            getHashOpt(key).putAll(map);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> List<T> hvalues(String key) {
        try {
            return (List<T>) getHashOpt(key).values();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Set<String> hkeys(String key) {
        try {
            return getHashOpt(key).keys();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean hexists(String key, String fieldKey) {
        try {
            return getHashOpt(key).hasKey(fieldKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long hsize(String key) {
        try {
            return getHashOpt(key).size();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long sadd(String key, Object... values) {
        try {
            return getSetOpt(key).add(values);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> Set<T> sdiff(String key, Set<String> keys) {
        try {
            return (Set<T>) getSetOpt(key).diff(keys);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> Set<T> sdiff(String key, String otherKey) {
        try {
            return (Set<T>) getSetOpt(key).diff(otherKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void sdiffAndStore(String firstSetKey, String key, String destKey) {
        try {
            getSetOpt(firstSetKey).diffAndStore(key, destKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> Set<T> sintersect(String key, String otherKey) {
        try {
            return (Set<T>) getSetOpt(key).intersect(otherKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> Set<T> sintersect(Set<String> keys) {
        if (keys == null || keys.size() < 2) {
            throw new IllegalArgumentException("parameter 'keys' size >=2");
        }
        String mainKey = keys.iterator()
                .next();
        keys.remove(mainKey);
        try {
            return (Set<T>) getSetOpt(mainKey).intersect(keys);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void sintersectAndStore(Set<String> keys, String destKey) {
        if (keys == null || keys.size() < 2) {
            throw new IllegalArgumentException("parameter 'keys' size >=2");
        }
        String mainKey = keys.iterator()
                .next();
        keys.remove(mainKey);
        try {
            getSetOpt(mainKey).intersectAndStore(keys, destKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> Set<T> sunion(String key, String otherKey) {
        try {
            return (Set<T>) getSetOpt(key).union(otherKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void sunionAndStore(String key, String otherKey, String destKey) {
        try {
            getSetOpt(key).unionAndStore(otherKey, destKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> Set<T> smember(String key) {
        try {
            return (Set<T>) getSetOpt(key).members();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean smove(String key, String destKey, Object value) {
        try {
            return getSetOpt(key).move(destKey, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T spop(String key) {
        try {
            return (T) getSetOpt(key).pop();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> List<T> srandomMembers(String key, int count) {
        try {
            return (List<T>) getSetOpt(key).randomMembers(count);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long sremove(String key, Object... members) {
        try {
            return getSetOpt(key).remove(members);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long ssize(String key) {
        try {
            return getSetOpt(key).size();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    private BoundZSetOperations<String,Object> getZsetOpt(String key) {
        return redisTemplate.boundZSetOps(key);
    }

    @Override
    public boolean zsadd(String key, Object mermber, double score) {
        try {
            return getZsetOpt(key).add(mermber, score);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zsadd(String key, Map<? extends Serializable,Double> memberScores) {
        Set<TypedTuple<Object>> springTuples = new HashSet<>();
        for (Map.Entry<? extends Serializable,Double> kv : memberScores.entrySet()) {
            if (kv.getKey() == null) {
                throw new IllegalArgumentException("不允许有null key");
            }
            springTuples.add(new DefaultTypedTuple<Object>(kv.getKey(), kv.getValue()));
        }
        try {
            return getZsetOpt(key).add(springTuples);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zssize(String key) {
        try {
            return getZsetOpt(key).size();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zscount(String key, double min, double max) {
        try {
            return getZsetOpt(key).count(min, max);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Double zsincrBy(String key, Object member, double incrScore) {
        try {
            return getZsetOpt(key).incrementScore(member, incrScore);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Double zscore(String key, Object member) {
        try {
            return getZsetOpt(key).score(member);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> Set<T> zsrange(String key, long startIndex, long endIndex) {
        try {
            return (Set<T>) getZsetOpt(key).range(startIndex, endIndex);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> Map<T,Double> zsrangeWithScores(String key, long startIndex, long endIndex) {
        Set<TypedTuple<Object>> tuples = getZsetOpt(key).rangeWithScores(startIndex, endIndex);
        Map<T,Double> memberScoreMap = null;
        if (CollectionHelper.isNotEmpty(tuples)) {
            memberScoreMap = new HashMap<>();
            for (TypedTuple<Object> tuple : tuples) {
                memberScoreMap.put((T) tuple.getValue(), tuple.getScore());
            }
        }
        return memberScoreMap;
    }

    @Override
    public <T> Set<T> zsrangeByScore(String key, double min, double max) {
        try {
            return (Set<T>) getZsetOpt(key).rangeByScore(min, max);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zsremove(String key, Object... members) {
        try {
            return getZsetOpt(key).remove(members);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zsremove(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet()
                    .removeRange(key, start, end);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zsremoveByScore(String key, double minScore, double maxScore) {
        try {
            return redisTemplate.opsForZSet()
                    .removeRangeByScore(key, minScore, maxScore);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long lpush(String key, Object... values) {
        try {
            if (values.length == 1) {
                return getListOpt(key).leftPush(values[0]);
            } else {
                return getListOpt(key).leftPushAll(values);
            }
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long lpushIfAbsent(String key, Object value) {
        try {
            return getListOpt(key).leftPushIfPresent(value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long rpush(String key, Object... values) {
        try {
            if (values.length == 1) {
                return getListOpt(key).rightPush(values[0]);
            } else {
                return getListOpt(key).rightPushAll(values);
            }
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long rpushIfAbsent(String key, Object value) {
        try {
            return getListOpt(key).rightPushIfPresent(value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long linsert(String key, Object value, Object pivot) {
        try {
            return getListOpt(key).leftPush(pivot, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T lpop(String key) {
        try {
            return (T) getListOpt(key).leftPop();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T lBlockPop(String key, int timeout) {
        try {
            return (T) getListOpt(key).leftPop(timeout, TimeUnit.SECONDS);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T rpop(String key) {
        try {
            return (T) getListOpt(key).rightPop();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T rBlockPop(String key, int timeout) {
        try {
            return (T) getListOpt(key).rightPop(timeout, TimeUnit.SECONDS);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long lsize(String key) {
        try {
            return getListOpt(key).size();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> List<T> lrange(String key, long start, long end) {
        try {
            return (List<T>) getListOpt(key).range(start, end);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void ltrim(String key, long start, long end) {
        try {
            getListOpt(key).trim(start, end);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T lindex(String key, long index) {
        try {
            return (T) getListOpt(key).index(index);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void lset(String key, long index, Object value) {
        try {
            getListOpt(key).set(index, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Long lremove(String key, Object value) {
        return lremove(key, 0, value);
    }

    @Override
    public Long lremove(String key, long count, Object value) {
        try {
            return getListOpt(key).remove(count, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T runScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz) {
        Object[] argArray = null;
        if (args != null) {
            argArray = new Object[args.size()];
        }
        for (int i = 0; i < argArray.length; i++) {
            argArray[i] = args.get(i);
        }
        DefaultRedisScript<T> script = new DefaultRedisScript<>(luaScript, resultClazz);
        try {
            return redisTemplate.execute(script, keys, argArray);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T runScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz, IRedisSerializer<T> resultSerializer) {
        Object[] argArray = null;
        if (args != null) {
            argArray = new Object[args.size()];
        }
        for (int i = 0; i < argArray.length; i++) {
            argArray[i] = args.get(i);
        }
        SpringRedisSerializeAdapter<T> serializeAdpater = new SpringRedisSerializeAdapter<>(resultSerializer);

        DefaultRedisScript<T> script = new DefaultRedisScript<>(luaScript, resultClazz);
        try {
            return redisTemplate.execute(script, redisTemplate.getValueSerializer(), serializeAdpater, keys, argArray);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T runSHAScript(String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz) {
        return runScript(luaScript, keys, args, resultClazz);// redisTemplate 内部默认先使用evalsha命令
    }

    public <T> T runSHAScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz, IRedisSerializer<T> resultSerializer) {
        return runScript(luaScript, keys, args, resultClazz, resultSerializer);
    }

    @Override
    public Object getNativeClient() {
        return redisTemplate;
    }

    @Override
    public long ttl(String key) {
        try {
            return redisTemplate.getExpire(key);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void flushDB() {
        redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                return null;
            }
        });
    }

    @Override
    public void publish(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }

    private BoundSetOperations<String,Object> getSetOpt(String key) {
        return redisTemplate.boundSetOps(key);
    }

    private BoundListOperations<String,Object> getListOpt(String key) {
        return redisTemplate.boundListOps(key);
    }

    @Override
    public IGenericRedisSerializer getValueSerializer() {
        return valueSerializer;
    }

    @Override
    public IRedisSerializer<String> getKeySerializer() {
        return keySerializer;
    }

}
