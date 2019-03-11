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
import org.jflame.toolkit.util.CollectionHelper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.RedisScript;

public class SpringCacheClientImpl implements RedisClient {

    private RedisTemplate<byte[],byte[]> redisTemplate;
    private IGenericSerializer serializer;

    public SpringCacheClientImpl(RedisTemplate<byte[],byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void set(Object key, Object value) {
        redisTemplate.opsForValue()
                .set(toBytes(key), toBytes(value));
    }

    @Override
    public void set(Object key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue()
                .set(toBytes(key), toBytes(value), timeout, timeUnit);
    }

    @Override
    public boolean setIfAbsent(Object key, Object value) {
        return redisTemplate.opsForValue()
                .setIfAbsent(toBytes(key), toBytes(value));
    }

    @Override
    public boolean setIfAbsent(Object key, Object value, long timeout, TimeUnit timeUnit) {
        // spring-data-redis2.1.5以前版本未实现该接口
        /* return redisTemplate.execute(new SessionCallback<Boolean>() {
        
            List<Object> exec = null;
        
            @SuppressWarnings({ "rawtypes","unchecked" })
            @Override
            public Boolean execute(RedisOperations operations) throws DataAccessException {
                byte[] keyBytes = toBytes(key);
                byte[] valueBytes = toBytes(value);
                operations.multi();
                operations.opsForValue()
                        .setIfAbsent(keyBytes, valueBytes);
                operations.expire(key, timeout, timeUnit);
                exec = operations.exec();
                if (exec.size() > 0) {
                    return (Boolean) exec.get(0);
                }
                return false;
            }
        
        });*/
        return redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = toBytes(key);
                byte[] valueBytes = toBytes(value);
                Object obj = connection.execute("set", new byte[][] { keyBytes,valueBytes,nxBytes,pxBytes,
                        CharsetHelper.getUtf8Bytes(String.valueOf(timeUnit.toMillis(timeout))) });
                if (obj != null) {
                    return "OK".equals(obj);
                }
                return false;
            }
        });

    }

    @Override
    public <T extends Serializable> T get(Object key) {
        byte[] valueBytes = redisTemplate.opsForValue()
                .get(toBytes(key));
        return fromBytes(valueBytes);
    }

    @Override
    public <T extends Serializable> T getAndSet(Object key, T newValue) {
        byte[] valueBytes = redisTemplate.opsForValue()
                .getAndSet(toBytes(key), toBytes(newValue));
        return fromBytes(valueBytes);
    }

    @Override
    public <T extends Serializable> List<T> multiGet(Collection<?> keys) {
        Collection<byte[]> keyBytes = toBytes(keys);
        List<byte[]> valueBytes = redisTemplate.opsForValue()
                .multiGet(keyBytes);
        return fromBytes(valueBytes);
    }

    @Override
    public boolean delete(Object key) {
        return redisTemplate.delete(toBytes(key));
    }

    @Override
    public long delete(Collection<?> keys) {
        return redisTemplate.delete(toBytes(keys));
    }

    @Override
    public boolean exists(Object key) {
        return redisTemplate.hasKey(toBytes(key));
    }

    @Override
    public boolean expire(Object key, int seconds) {
        return redisTemplate.expire(toBytes(key), seconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean expireAt(Object key, Date date) {
        return redisTemplate.expireAt(toBytes(key), date);
    }

    @Override
    public Long incr(Object key) {
        return redisTemplate.opsForValue()
                .increment(toBytes(key), 1);
    }

    @Override
    public Long incr(Object key, long incrValue) {
        return redisTemplate.opsForValue()
                .increment(toBytes(key), incrValue);
    }

    @Override
    public Double incrByFloat(Object key, double incrValue) {
        return redisTemplate.opsForValue()
                .increment(toBytes(key), incrValue);
    }

    @Override
    public boolean persist(Object key) {
        return redisTemplate.persist(toBytes(key));
    }

    @Override
    public <T extends Serializable> T hget(Object key, Object fieldKey) {
        byte[] valueBytes = getHashOpt(key).get(toBytes(fieldKey));
        return fromBytes(valueBytes);
    }

    private BoundHashOperations<byte[],byte[],byte[]> getHashOpt(Object key) {
        return redisTemplate.boundHashOps(toBytes(key));
    }

    @Override
    public <T extends Serializable> List<T> hmultiGet(Object key, Collection<?> fieldKeys) {
        List<byte[]> valueBytes = getHashOpt(key).multiGet(toBytes(fieldKeys));
        return fromBytes(valueBytes);
    }

    @Override
    public void hdelete(Object key, Object fieldKey) {
        getHashOpt(key).delete(toBytes(fieldKey));
    }

    @Override
    public void hput(Object key, Object fieldKey, Object value) {
        getHashOpt(key).put(toBytes(fieldKey), toBytes(value));
    }

    @Override
    public boolean hputIfAbsent(Object key, Object fieldKey, Object value) {
        return getHashOpt(key).putIfAbsent(toBytes(fieldKey), toBytes(value));
    }

    @Override
    public void hputAll(Object key, Map<? extends Serializable,? extends Serializable> map) {
        BoundHashOperations<byte[],Serializable,Serializable> hashOpt = redisTemplate.boundHashOps(toBytes(key));
        hashOpt.putAll(map);
    }

    @Override
    public <T extends Serializable> List<T> hvalues(Object key) {
        List<byte[]> valueBytes = getHashOpt(key).values();
        return fromBytes(valueBytes);
    }

    @Override
    public <T extends Serializable> Set<T> hkeys(Object key) {
        Set<byte[]> keyBytes = getHashOpt(key).keys();
        if (CollectionHelper.isNotEmpty(keyBytes)) {
            Set<T> values = new HashSet<>();
            for (byte[] bs : keyBytes) {
                values.add(fromBytes(bs));
            }
            return values;
        }
        return null;
    }

    @Override
    public boolean hexists(Object key, Object fieldKey) {
        return getHashOpt(key).hasKey(toBytes(fieldKey));
    }

    @Override
    public long hsize(Object key) {
        return getHashOpt(key).size();
    }

    @Override
    public long sadd(Object key, Serializable... values) {
        byte[][] valueBytes = toBytes(values);
        return redisTemplate.boundSetOps(toBytes(key))
                .add(valueBytes);
    }

    private BoundSetOperations<byte[],byte[]> getSetOpt(Object key) {
        return redisTemplate.boundSetOps(toBytes(key));
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Object firstSetKey, Collection<?> keys) {
        Set<byte[]> valueBytes = getSetOpt(firstSetKey).diff(toBytes(keys));
        return fromBytes(valueBytes);
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Object firstSetKey, Object otherKey) {
        Set<byte[]> valueBytes = getSetOpt(firstSetKey).diff(toBytes(otherKey));
        return fromBytes(valueBytes);
    }

    @Override
    public void sdiffAndStore(Object firstSetKey, Object key, Object destKey) {
        getSetOpt(firstSetKey).diffAndStore(toBytes(key), toBytes(destKey));
    }

    @Override
    public <T extends Serializable> Set<T> sintersect(Object key, Object otherKey) {
        Set<byte[]> valueBytes = getSetOpt(key).intersect(toBytes(otherKey));
        return fromBytes(valueBytes);
    }

    @Override
    public void sintersectAndStore(Object key, Object otherKey, Object destKey) {
        getSetOpt(key).intersectAndStore(toBytes(otherKey), toBytes(destKey));
    }

    @Override
    public <T extends Serializable> Set<T> sunion(Object key, Object otherKey) {
        Set<byte[]> valueBytes = getSetOpt(key).union(toBytes(otherKey));
        return fromBytes(valueBytes);
    }

    /**
     * 求集合并集并将结果存储到指定的集合中
     * 
     * @param keys
     * @param destKey
     */
    public void sunionAndStore(Object key, Object otherKey, Object destKey) {
        getSetOpt(key).unionAndStore(toBytes(otherKey), toBytes(destKey));
    }

    @Override
    public <T extends Serializable> Set<T> smember(Object key) {
        Set<byte[]> valueBytes = getSetOpt(key).members();
        return fromBytes(valueBytes);
    }

    @Override
    public boolean smove(Object key, Object destKey, Serializable value) {
        return getSetOpt(key).move(toBytes(destKey), toBytes(value));
    }

    @Override
    public <T extends Serializable> T spop(Object key) {
        byte[] valueBytes = getSetOpt(key).pop();
        return fromBytes(valueBytes);
    }

    @Override
    public <T extends Serializable> List<T> randomMembers(Object key, int count) {
        List<byte[]> valueBytes = getSetOpt(key).randomMembers(count);
        return fromBytes(valueBytes);
    }

    @Override
    public long sremove(Object key, Object... members) {
        return getSetOpt(key).remove(members);
    }

    @Override
    public long ssize(Object key) {
        return getSetOpt(key).size();
    }

    private BoundZSetOperations<byte[],byte[]> getZsetOpt(Object key) {
        return redisTemplate.boundZSetOps(toBytes(key));
    }

    @Override
    public boolean zsadd(Object key, Serializable value, double score) {
        return getZsetOpt(key).add(toBytes(value), score);
    }

    @Override
    public long zsadd(Object key, Set<SortedSetTuple<Serializable>> tuples) {
        Set<TypedTuple<byte[]>> springTuples = new HashSet<>();
        for (SortedSetTuple<Serializable> t : tuples) {
            springTuples.add(new DefaultTypedTuple<byte[]>(toBytes(t.getValue()), t.getScore()));
        }
        return getZsetOpt(key).add(springTuples);
    }

    @Override
    public long zssize(Object key) {
        return getZsetOpt(key).size();
    }

    @Override
    public long zscount(Object key, double min, double max) {
        return getZsetOpt(key).count(min, max);
    }

    @Override
    public Double zsincrScore(Object key, Serializable member, double incrScore) {
        return getZsetOpt(key).incrementScore(toBytes(member), incrScore);
    }

    @Override
    public <T extends Serializable> Set<T> zsrange(Object key, long startIndex, long endIndex) {
        Set<byte[]> valueBytes = getZsetOpt(key).range(startIndex, endIndex);
        return fromBytes(valueBytes);
    }

    @Override
    public <T extends Serializable> Set<T> zrangeByScore(Object key, double min, double max) {
        Set<byte[]> valueBytes = getZsetOpt(key).rangeByScore(min, max);
        return fromBytes(valueBytes);
    }

    @Override
    public long zsremove(Object key, Object... members) {
        return getZsetOpt(key).remove(members);
    }

    @Override
    public void removeRange(Object key, long start, long end) {
        getZsetOpt(key).removeRange(start, end);
    }

    @Override
    public void removeRangeByScore(Object key, double minScore, double maxScore) {
        getZsetOpt(key).removeRangeByScore(minScore, maxScore);
    }

    private BoundListOperations<byte[],byte[]> getListOpt(Object key) {
        return redisTemplate.boundListOps(toBytes(key));
    }

    @Override
    public long lpush(Object key, Object... values) {
        if (values.length == 1) {
            return getListOpt(key).leftPush(toBytes(values[0]));
        } else {
            byte[][] valueBytes = toBytes(values);
            return getListOpt(key).leftPushAll(valueBytes);
        }
    }

    @Override
    public long lpushIfAbsent(Object key, Object value) {
        return getListOpt(key).leftPushIfPresent(toBytes(value));
    }

    @Override
    public long rpush(Object key, Object... values) {
        if (values.length == 1) {
            return getListOpt(key).rightPush(toBytes(values[0]));
        } else {
            byte[][] valueBytes = toBytes(values);
            return getListOpt(key).rightPushAll(valueBytes);
        }
    }

    @Override
    public long rpushIfAbsent(Object key, Object value) {
        return getListOpt(key).rightPushIfPresent(toBytes(value));
    }

    @Override
    public long linsert(Object key, Object value, Object pivot) {
        return getListOpt(key).leftPush(toBytes(pivot), toBytes(value));
    }

    @Override
    public <T extends Serializable> T lpop(Object key) {
        byte[] valueBytes = getListOpt(key).leftPop();
        return fromBytes(valueBytes);
    }

    @Override
    public <T extends Serializable> T lpop(Object key, long timeout, TimeUnit timeUnit) {
        byte[] valueBytes = getListOpt(key).leftPop(timeout, timeUnit);
        return fromBytes(valueBytes);
    }

    @Override
    public <T extends Serializable> T rpop(Object key) {
        byte[] valueBytes = getListOpt(key).rightPop();
        return fromBytes(valueBytes);
    }

    @Override
    public <T extends Serializable> T rpop(Object key, long timeout, TimeUnit timeUnit) {
        byte[] valueBytes = getListOpt(key).rightPop(timeout, timeUnit);
        return fromBytes(valueBytes);
    }

    @Override
    public long lsize(Object key) {
        return getListOpt(key).size();
    }

    @Override
    public <T extends Serializable> List<T> lrange(Object key, long start, long end) {
        List<byte[]> valueBytes = getListOpt(key).range(start, end);
        return fromBytes(valueBytes);
    }

    @Override
    public void ltrim(Object key, long start, long end) {
        getListOpt(key).trim(start, end);
    }

    @Override
    public <T extends Serializable> T lindex(Object key, long index) {
        byte[] valueBytes = getListOpt(key).index(index);
        return fromBytes(valueBytes);
    }

    @Override
    public void lset(Object key, long index, Object value) {
        getListOpt(key).set(index, toBytes(value));
    }

    @Override
    public Long lremove(Object key, long count, Object value) {
        return getListOpt(key).remove(count, value);
    }

    @Override
    public void setSerializer(IGenericSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public IGenericSerializer getSerializer() {
        return serializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object runScript(final String luaScript, List<Object> keys, List<Object> args) {
        Object[] argArray = null;
        if (CollectionHelper.isNotEmpty(args)) {
            argArray = new Object[args.size()];
            for (int i = 0; i < argArray.length; i++) {
                argArray[i] = toBytes(args.get(i));
            }
        }
        return redisTemplate.execute(RedisScript.of(luaScript), toBytes(keys), argArray);
    }

    @Override
    public Object getNativeClient() {
        return redisTemplate;
    }

    /*    @Override
    public Set<Object> keys(String pattern) {
        Set<byte[]> keySet = redisTemplate.keys(toBytes(pattern));
        return fromBytes(keySet, Object.class);
    }*/

}
