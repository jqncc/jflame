package org.jflame.toolkit.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.MapHelper;
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
import org.springframework.data.redis.serializer.RedisSerializer;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;

@SuppressWarnings({ "rawtypes","unchecked" })
public class SpringCacheClientImpl implements RedisClient {

    private RedisTemplate<Serializable,Serializable> redisTemplate;
    private IGenericSerializer serializer;

    public SpringCacheClientImpl(RedisConnectionFactory redisConnection) {
        this.redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnection);
        GenericFastJsonRedisSerializer redisJsonSerializer = new GenericFastJsonRedisSerializer();
        redisTemplate.setDefaultSerializer(redisJsonSerializer);
        redisTemplate.afterPropertiesSet();
        serializer = new FastJsonSerializer();
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
    public void set(Serializable key, Serializable value) {
        try {
            redisTemplate.opsForValue()
                    .set(key, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void set(Serializable key, Serializable value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue()
                    .set(key, value, timeout, timeUnit);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void multiSet(Map<? extends Serializable,? extends Serializable> pair) {
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
    public boolean setIfAbsent(Serializable key, Serializable value) {
        try {
            return redisTemplate.opsForValue()
                    .setIfAbsent(key, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean setIfAbsent(Serializable key, Serializable value, long timeout, TimeUnit timeUnit) {
        // spring-data-redis2.1.5以前版本未实现该接口,使用底层接口拼接命令
        try {
            return redisTemplate.execute(new RedisCallback<Boolean>() {

                @Override
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    RedisSerializer kserializer = redisTemplate.getKeySerializer();
                    byte[] keyBytes = kserializer.serialize(key);
                    byte[] valueBytes = kserializer.serialize(value);
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
    public void multiSetIfAbsent(Map<? extends Serializable,? extends Serializable> pair) {
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
    public <T extends Serializable> T get(Serializable key) {
        try {
            return (T) redisTemplate.opsForValue()
                    .get(key);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> T getAndSet(Serializable key, T newValue) {
        try {
            return (T) redisTemplate.opsForValue()
                    .getAndSet(key, newValue);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> List<T> multiGet(Collection<? extends Serializable> keys) {
        try {
            List<Serializable> valueBytes = redisTemplate.opsForValue()
                    .multiGet((Collection<Serializable>) keys);
            return (List<T>) valueBytes;
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean delete(Serializable key) {
        // return redisTemplate.delete(key);2.0以上版本才支持返回值
        // redisTemplate.delete(key);
        try {
            return redisTemplate.execute(new RedisCallback<Boolean>() {

                @Override
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    RedisSerializer rserializer = redisTemplate.getKeySerializer();
                    return connection.del(rserializer.serialize(key)) > 0;
                }
            });
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long delete(Collection<? extends Serializable> keys) {
        // return redisTemplate.delete(toBytes(keys));2.0以上版本才支持返回数量
        try {
            return redisTemplate.execute(new RedisCallback<Long>() {

                @Override
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                    RedisSerializer rserializer = redisTemplate.getKeySerializer();
                    byte[][] keyBytes = new byte[keys.size()][];
                    int i = 0;
                    for (Serializable key : keys) {
                        keyBytes[i++] = rserializer.serialize(key);
                    }
                    return connection.del(keyBytes);
                }
            });
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean exists(Serializable key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean expire(Serializable key, int seconds) {
        try {
            return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean expire(Serializable key, long timeout, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, timeout, timeUnit);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean expireAt(Serializable key, Date date) {
        try {
            return redisTemplate.expireAt(key, date);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Long incr(Serializable key) {
        try {
            return redisTemplate.opsForValue()
                    .increment(key, 1);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Long incr(Serializable key, long incrValue) {
        try {
            return redisTemplate.opsForValue()
                    .increment(key, incrValue);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Double incrByFloat(Serializable key, double incrValue) {
        try {
            return redisTemplate.opsForValue()
                    .increment(key, incrValue);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean persist(Serializable key) {
        try {
            return redisTemplate.persist(key);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> T hget(Serializable key, Serializable fieldKey) {
        try {
            return (T) getHashOpt(key).get(fieldKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    private BoundHashOperations<Serializable,Serializable,Serializable> getHashOpt(Serializable key) {
        return redisTemplate.boundHashOps(key);
    }

    @Override
    public <T extends Serializable> List<T> hmultiGet(Serializable key, Collection<? extends Serializable> fieldKeys) {
        try {
            return (List<T>) getHashOpt(key).multiGet((Collection<Serializable>) fieldKeys);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long hdelete(Serializable key, Object fieldKey) {
        try {
            return getHashOpt(key).delete(fieldKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void hput(Serializable key, Serializable fieldKey, Serializable value) {
        try {
            getHashOpt(key).put(fieldKey, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean hputIfAbsent(Serializable key, Serializable fieldKey, Serializable value) {
        try {
            return getHashOpt(key).putIfAbsent(fieldKey, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void hputAll(Serializable key, Map<? extends Serializable,? extends Serializable> map) {
        try {
            getHashOpt(key).putAll(map);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> List<T> hvalues(Serializable key) {
        try {
            return (List<T>) getHashOpt(key).values();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> Set<T> hkeys(Serializable key) {
        try {
            return (Set<T>) getHashOpt(key).keys();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean hexists(Serializable key, Object fieldKey) {
        try {
            return getHashOpt(key).hasKey(fieldKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long hsize(Serializable key) {
        try {
            return getHashOpt(key).size();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    private BoundSetOperations<Serializable,Serializable> getSetOpt(Serializable key) {
        return redisTemplate.boundSetOps(key);
    }

    @Override
    public long sadd(Serializable key, Serializable... values) {
        try {
            return getSetOpt(key).add(values);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Serializable key, Collection<? extends Serializable> keys) {
        try {
            return (Set<T>) getSetOpt(key).diff((Collection<Serializable>) keys);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Serializable key, Serializable otherKey) {
        try {
            return (Set<T>) getSetOpt(key).diff(otherKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void sdiffAndStore(Serializable firstSetKey, Serializable key, Serializable destKey) {
        try {
            getSetOpt(firstSetKey).diffAndStore(key, destKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> Set<T> sintersect(Serializable key, Serializable otherKey) {
        try {
            return (Set<T>) getSetOpt(key).intersect(otherKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> Set<T> sintersect(List<? extends Serializable> keys) {
        if (keys != null && keys.size() >= 2) {
            Serializable first = keys.remove(0);
            try {
                return (Set<T>) getSetOpt(first).intersect((Collection<Serializable>) keys);
            } catch (DataAccessException e) {
                throw new RedisAccessException(e);
            }
        }
        return null;
    }

    @Override
    public void sintersectAndStore(List<? extends Serializable> keys, Serializable destKey) {
        if (keys != null && keys.size() >= 2) {
            Serializable first = keys.remove(0);
            try {
                getSetOpt(first).intersectAndStore((Collection<Serializable>) keys, destKey);
            } catch (DataAccessException e) {
                throw new RedisAccessException(e);
            }
        }
    }

    @Override
    public <T extends Serializable> Set<T> sunion(Serializable key, Serializable otherKey) {
        try {
            return (Set<T>) getSetOpt(key).union(otherKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void sunionAndStore(Serializable key, Serializable otherKey, Serializable destKey) {
        try {
            getSetOpt(key).unionAndStore(otherKey, destKey);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> Set<T> smember(Serializable key) {
        try {
            return (Set<T>) getSetOpt(key).members();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public boolean smove(Serializable key, Serializable destKey, Serializable value) {
        try {
            return getSetOpt(key).move(destKey, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> T spop(Serializable key) {
        try {
            return (T) getSetOpt(key).pop();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> List<T> srandomMembers(Serializable key, int count) {
        try {
            return (List<T>) getSetOpt(key).randomMembers(count);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long sremove(Serializable key, Object... members) {
        try {
            return getSetOpt(key).remove(members);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long ssize(Serializable key) {
        try {
            return getSetOpt(key).size();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    private BoundZSetOperations<Serializable,Serializable> getZsetOpt(Serializable key) {
        return redisTemplate.boundZSetOps(key);
    }

    @Override
    public boolean zsadd(Serializable key, Serializable value, double score) {
        try {
            return getZsetOpt(key).add(value, score);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zsadd(Serializable key, Map<? extends Serializable,Double> memberScores) {
        Set<TypedTuple<Serializable>> springTuples = new HashSet<>();
        for (Map.Entry<? extends Serializable,Double> kv : memberScores.entrySet()) {
            if (kv.getKey() == null) {
                throw new IllegalArgumentException("不允许有null key");
            }
            springTuples.add(new DefaultTypedTuple<Serializable>(kv.getKey(), kv.getValue()));
        }
        try {
            return getZsetOpt(key).add(springTuples);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zssize(Serializable key) {
        try {
            return getZsetOpt(key).size();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zscount(Serializable key, double min, double max) {
        try {
            return getZsetOpt(key).count(min, max);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Double zsincrScore(Serializable key, Serializable member, double incrScore) {
        try {
            return getZsetOpt(key).incrementScore(member, incrScore);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> Set<T> zsrange(Serializable key, long startIndex, long endIndex) {
        try {
            return (Set<T>) getZsetOpt(key).range(startIndex, endIndex);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> Set<T> zsrangeByScore(Serializable key, double min, double max) {
        try {
            return (Set<T>) getZsetOpt(key).rangeByScore(min, max);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zsremove(Serializable key, Object... members) {
        try {
            return getZsetOpt(key).remove(members);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zsremove(Serializable key, long start, long end) {
        try {
            return redisTemplate.opsForZSet()
                    .removeRange(key, start, end);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long zsremoveByScore(Serializable key, double minScore, double maxScore) {
        try {
            return redisTemplate.opsForZSet()
                    .removeRangeByScore(key, minScore, maxScore);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    private BoundListOperations<Serializable,Serializable> getListOpt(Serializable key) {
        return redisTemplate.boundListOps(key);
    }

    @Override
    public long lpush(Serializable key, Serializable... values) {
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
    public long lpushIfAbsent(Serializable key, Serializable value) {
        try {
            return getListOpt(key).leftPushIfPresent(value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long rpush(Serializable key, Serializable... values) {
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
    public long rpushIfAbsent(Serializable key, Serializable value) {
        try {
            return getListOpt(key).rightPushIfPresent(value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long linsert(Serializable key, Serializable value, Serializable pivot) {
        try {
            return getListOpt(key).leftPush(pivot, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> T lpop(Serializable key) {
        try {
            return (T) getListOpt(key).leftPop();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> T lBlockPop(Serializable key, int timeout) {
        try {
            return (T) getListOpt(key).leftPop(timeout, TimeUnit.SECONDS);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> T rpop(Serializable key) {
        try {
            return (T) getListOpt(key).rightPop();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> T rBlockPop(Serializable key, int timeout) {
        try {
            return (T) getListOpt(key).rightPop(timeout, TimeUnit.SECONDS);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public long lsize(Serializable key) {
        try {
            return getListOpt(key).size();
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> List<T> lrange(Serializable key, long start, long end) {
        try {
            return (List<T>) getListOpt(key).range(start, end);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void ltrim(Serializable key, long start, long end) {
        try {
            getListOpt(key).trim(start, end);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> T lindex(Serializable key, long index) {
        try {
            return (T) getListOpt(key).index(index);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public void lset(Serializable key, long index, Serializable value) {
        try {
            getListOpt(key).set(index, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public Long lremove(Serializable key, Serializable value) {
        return lremove(key, 0, value);
    }

    @Override
    public Long lremove(Serializable key, long count, Serializable value) {
        try {
            return getListOpt(key).remove(count, value);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T runScript(final String luaScript, List<? extends Serializable> keys, List<? extends Serializable> args,
            Class<T> resultClazz) {
        Object[] argArray = null;
        if (args != null) {
            argArray = new Object[args.size()];
        }
        for (int i = 0; i < argArray.length; i++) {
            argArray[i] = args.get(i);
        }
        try {
            DefaultRedisScript<T> script = new DefaultRedisScript<>(luaScript, resultClazz);
            return redisTemplate.execute(script, (List<Serializable>) keys, argArray);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    @Override
    public <T> T runSHAScript(String luaScript, List<? extends Serializable> keys, List<? extends Serializable> args,
            Class<T> resultClazz) {
        return runScript(luaScript, keys, args, resultClazz);// redisTemplate 内部默认先使用evalsha命令
    }

    @Override
    public Object getNativeClient() {
        return redisTemplate;
    }

    @Override
    public long ttl(Serializable key) {
        try {
            return redisTemplate.getExpire(exBytes);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }

    public Set<? extends Serializable> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (DataAccessException e) {
            throw new RedisAccessException(e);
        }
    }
}
