package org.jflame.toolkit.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.ShardedJedis;

public class JedisSharedClientImpl implements RedisClient {

    private IGenericSerializer serializer;
    private JedisConnection conn;

    public JedisSharedClientImpl(JedisConnection conn) {
        this.conn = conn;
        serializer = new FastJsonSerializer();
    }

    private ShardedJedis getJedis() {
        return this.conn.getShardedJedis();
    }

    @Override
    public Object getNativeClient() {
        return null;
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
    public void set(Object key, Object value, long timeout, TimeUnit timeUnit) {
    }

    @Override
    public boolean setIfAbsent(Object key, Object value) {
        return false;
    }

    @Override
    public boolean setIfAbsent(Object key, Object value, long timeout, TimeUnit timeUnit) {
        return false;
    }

    @Override
    public <T extends Serializable> T get(Object key) {
        return null;
    }

    @Override
    public <T extends Serializable> T getAndSet(Object key, T newValue) {
        return null;
    }

    @Override
    public <T extends Serializable> List<T> multiGet(Collection<?> keys) {
        return null;
    }

    @Override
    public boolean delete(Object key) {
        return false;
    }

    @Override
    public long delete(Collection<?> keys) {
        return 0;
    }

    @Override
    public boolean exists(Object key) {
        return false;
    }

    @Override
    public boolean expire(Object key, int seconds) {
        return false;
    }

    @Override
    public boolean expireAt(Object key, Date date) {
        return false;
    }

    @Override
    public Long incr(Object key) {
        return null;
    }

    @Override
    public Long incr(Object key, long incrValue) {
        return null;
    }

    @Override
    public Double incrByFloat(Object key, double incrValue) {
        return null;
    }

    @Override
    public boolean persist(Object key) {
        return false;
    }

    @Override
    public <T extends Serializable> T hget(Object key, Object fieldKey) {
        return null;
    }

    @Override
    public <T extends Serializable> List<T> hmultiGet(Object key, Collection<?> fieldKeys) {
        return null;
    }

    @Override
    public void hdelete(Object key, Object fieldKey) {
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
    public long zsadd(Object key, Map<Object,Double> memberScores) {
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
    public <T extends Serializable> Set<T> zsrangeByScore(Object key, double min, double max) {
        return null;
    }

    @Override
    public long zsremove(Object key, Object... members) {
        return 0;
    }

    @Override
    public long zsremove(Object key, long start, long end) {
        return 0;
    }

    @Override
    public long zsremoveByScore(Object key, double minScore, double maxScore) {
        return 0;
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
    public <T extends Serializable> T lBlockPop(Object key, int timeout) {
        return null;
    }

    @Override
    public <T extends Serializable> T rpop(Object key) {
        return null;
    }

    @Override
    public <T extends Serializable> T rBlockPop(Object key, int timeout) {
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
    public long ttl(Object key) {
        return 0;
    }

    @Override
    public void set(Object key, Object value) {
    }

    @Override
    public <T extends Serializable> Set<T> sdiff(Object key, Collection<Object> keys) {
        return null;
    }

    @Override
    public <T extends Serializable> Set<T> sintersect(List<Object> keys) {
        return null;
    }

    @Override
    public void sintersectAndStore(List<Object> keys, Object destKey) {
    }

    @Override
    public <T> T runScript(String luaScript, List<Object> keys, List<Object> args, Class<T> resultClazz) {
        return null;
    }

    @Override
    public <T> T runSHAScript(String luaScript, List<Object> keys, List<Object> args, Class<T> resultClazz) {
        return null;
    }

}
