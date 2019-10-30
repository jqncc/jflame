package org.jflame.toolkit.cache.redis.serizlizer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jflame.toolkit.cache.redis.RedisAccessException;

import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.MultiKeyBinaryCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

/**
 * jedisCluster的命令适配.
 * <p>
 * 杯具的jedis没有统一接口
 * 
 * @author yucan.zhang
 */
public class BinaryJedisClusterCommandsAdapter implements BinaryJedisCommands, MultiKeyBinaryCommands {

    private JedisCluster cluster;

    public BinaryJedisClusterCommandsAdapter(JedisCluster clusterClient) {
        cluster = clusterClient;
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return cluster.set(key, value);
    }

    @Override
    public String set(byte[] key, byte[] value, byte[] nxxx) {
        throw new RedisAccessException("JedisCluster did not implemented this method");
    }

    @Override
    public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time) {
        return cluster.set(key, value, nxxx, expx, time);
    }

    @Override
    public byte[] get(byte[] key) {
        return cluster.get(key);
    }

    @Override
    public Boolean exists(byte[] key) {
        return cluster.exists(key);
    }

    @Override
    public Long persist(byte[] key) {
        return cluster.persist(key);
    }

    @Override
    public String type(byte[] key) {
        return cluster.type(key);
    }

    @Override
    public Long expire(byte[] key, int seconds) {
        return cluster.expire(key, seconds);
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        return cluster.pexpire(key, milliseconds);
    }

    @Override
    public Long pexpire(byte[] key, long milliseconds) {
        return cluster.pexpire(key, milliseconds);
    }

    @Override
    public Long expireAt(byte[] key, long unixTime) {
        return cluster.expireAt(key, unixTime);
    }

    @Override
    public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
        return cluster.pexpireAt(key, millisecondsTimestamp);
    }

    @Override
    public Long ttl(byte[] key) {
        return cluster.ttl(key);
    }

    @Override
    public Boolean setbit(byte[] key, long offset, boolean value) {
        return cluster.setbit(key, offset, value);
    }

    @Override
    public Boolean setbit(byte[] key, long offset, byte[] value) {
        return cluster.setbit(key, offset, value);
    }

    @Override
    public Boolean getbit(byte[] key, long offset) {
        return cluster.getbit(key, offset);
    }

    @Override
    public Long setrange(byte[] key, long offset, byte[] value) {
        return cluster.setrange(key, offset, value);
    }

    @Override
    public byte[] getrange(byte[] key, long startOffset, long endOffset) {
        return cluster.getrange(key, startOffset, endOffset);
    }

    @Override
    public byte[] getSet(byte[] key, byte[] value) {
        return cluster.getSet(key, value);
    }

    @Override
    public Long setnx(byte[] key, byte[] value) {
        return cluster.setnx(key, value);
    }

    @Override
    public String setex(byte[] key, int seconds, byte[] value) {
        return cluster.setex(key, seconds, value);
    }

    @Override
    public Long decrBy(byte[] key, long integer) {
        return cluster.decrBy(key, integer);
    }

    @Override
    public Long decr(byte[] key) {
        return cluster.decr(key);
    }

    @Override
    public Long incrBy(byte[] key, long integer) {
        return cluster.incrBy(key, integer);
    }

    @Override
    public Double incrByFloat(byte[] key, double value) {
        return cluster.incrByFloat(key, value);
    }

    @Override
    public Long incr(byte[] key) {
        return cluster.incr(key);
    }

    @Override
    public Long append(byte[] key, byte[] value) {
        return cluster.append(key, value);
    }

    @Override
    public byte[] substr(byte[] key, int start, int end) {
        return cluster.substr(key, start, end);
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        return cluster.hset(key, field, value);
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return cluster.hget(key, field);
    }

    @Override
    public Long hsetnx(byte[] key, byte[] field, byte[] value) {
        return cluster.hsetnx(key, field, value);
    }

    @Override
    public String hmset(byte[] key, Map<byte[],byte[]> hash) {
        return cluster.hmset(key, hash);
    }

    @Override
    public List<byte[]> hmget(byte[] key, byte[]... fields) {
        return cluster.hmget(key, fields);
    }

    @Override
    public Long hincrBy(byte[] key, byte[] field, long value) {
        return cluster.hincrBy(key, field, value);
    }

    @Override
    public Double hincrByFloat(byte[] key, byte[] field, double value) {
        return cluster.hincrByFloat(key, field, value);
    }

    @Override
    public Boolean hexists(byte[] key, byte[] field) {
        return cluster.hexists(key, field);
    }

    @Override
    public Long hdel(byte[] key, byte[]... field) {
        return cluster.hdel(key, field);
    }

    @Override
    public Long hlen(byte[] key) {
        return cluster.hlen(key);
    }

    @Override
    public Long hstrlen(byte[] key, byte[] field) {
        return cluster.hstrlen(key, field);
    }

    @Override
    public Set<byte[]> hkeys(byte[] key) {
        return cluster.hkeys(key);
    }

    @Override
    public Collection<byte[]> hvals(byte[] key) {
        return cluster.hvals(key);
    }

    @Override
    public Map<byte[],byte[]> hgetAll(byte[] key) {
        return cluster.hgetAll(key);
    }

    @Override
    public Long rpush(byte[] key, byte[]... args) {
        return cluster.rpush(key, args);
    }

    @Override
    public Long lpush(byte[] key, byte[]... args) {
        return cluster.lpush(key, args);
    }

    @Override
    public Long llen(byte[] key) {
        return cluster.llen(key);
    }

    @Override
    public List<byte[]> lrange(byte[] key, long start, long end) {
        return cluster.lrange(key, start, end);
    }

    @Override
    public String ltrim(byte[] key, long start, long end) {
        return cluster.ltrim(key, start, end);
    }

    @Override
    public byte[] lindex(byte[] key, long index) {
        return cluster.lindex(key, index);
    }

    @Override
    public String lset(byte[] key, long index, byte[] value) {
        return cluster.lset(key, index, value);
    }

    @Override
    public Long lrem(byte[] key, long count, byte[] value) {
        return cluster.lrem(key, count, value);
    }

    @Override
    public byte[] lpop(byte[] key) {
        return cluster.lpop(key);
    }

    @Override
    public byte[] rpop(byte[] key) {
        return cluster.rpop(key);
    }

    @Override
    public Long sadd(byte[] key, byte[]... member) {
        return cluster.sadd(key, member);
    }

    @Override
    public Set<byte[]> smembers(byte[] key) {
        return cluster.smembers(key);
    }

    @Override
    public Long srem(byte[] key, byte[]... member) {
        return cluster.srem(key, member);
    }

    @Override
    public byte[] spop(byte[] key) {
        return cluster.spop(key);
    }

    @Override
    public Set<byte[]> spop(byte[] key, long count) {
        return cluster.spop(key, count);
    }

    @Override
    public Long scard(byte[] key) {
        return cluster.scard(key);
    }

    @Override
    public Boolean sismember(byte[] key, byte[] member) {
        return cluster.sismember(key, member);
    }

    @Override
    public byte[] srandmember(byte[] key) {
        return cluster.srandmember(key);
    }

    @Override
    public List<byte[]> srandmember(byte[] key, int count) {
        return cluster.srandmember(key, count);
    }

    @Override
    public Long strlen(byte[] key) {
        return cluster.strlen(key);
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member) {
        return cluster.zadd(key, score, member);
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
        return cluster.zadd(key, score, member, params);
    }

    @Override
    public Long zadd(byte[] key, Map<byte[],Double> scoreMembers) {
        return cluster.zadd(key, scoreMembers);
    }

    @Override
    public Long zadd(byte[] key, Map<byte[],Double> scoreMembers, ZAddParams params) {
        return cluster.zadd(key, scoreMembers, params);
    }

    @Override
    public Set<byte[]> zrange(byte[] key, long start, long end) {
        return cluster.zrange(key, start, end);
    }

    @Override
    public Long zrem(byte[] key, byte[]... member) {
        return cluster.zrem(key, member);
    }

    @Override
    public Double zincrby(byte[] key, double score, byte[] member) {
        return cluster.zincrby(key, score, member);
    }

    @Override
    public Double zincrby(byte[] key, double score, byte[] member, ZIncrByParams params) {
        return cluster.zincrby(key, score, member, params);
    }

    @Override
    public Long zrank(byte[] key, byte[] member) {
        return cluster.zrank(key, member);
    }

    @Override
    public Long zrevrank(byte[] key, byte[] member) {
        return cluster.zrevrank(key, member);
    }

    @Override
    public Set<byte[]> zrevrange(byte[] key, long start, long end) {
        return cluster.zrevrange(key, start, end);
    }

    @Override
    public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
        return cluster.zrangeWithScores(key, start, end);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end) {
        return cluster.zrevrangeWithScores(key, start, end);
    }

    @Override
    public Long zcard(byte[] key) {
        return cluster.zcard(key);
    }

    @Override
    public Double zscore(byte[] key, byte[] member) {
        return cluster.zscore(key, member);
    }

    @Override
    public List<byte[]> sort(byte[] key) {
        return cluster.sort(key);
    }

    @Override
    public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
        return cluster.sort(key, sortingParameters);
    }

    @Override
    public Long zcount(byte[] key, double min, double max) {
        return cluster.zcount(key, min, max);
    }

    @Override
    public Long zcount(byte[] key, byte[] min, byte[] max) {
        return cluster.zcount(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        return cluster.zrangeByScore(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
        return cluster.zrangeByScore(key, min, max);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
        return cluster.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
        return cluster.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
        return cluster.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return cluster.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
        return cluster.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return cluster.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
        return cluster.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return cluster.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return cluster.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
        return cluster.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
        return cluster.zrevrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return cluster.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
        return cluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return cluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByRank(byte[] key, long start, long end) {
        return cluster.zremrangeByRank(key, start, end);
    }

    @Override
    public Long zremrangeByScore(byte[] key, double start, double end) {
        return cluster.zremrangeByScore(key, start, end);
    }

    @Override
    public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
        return cluster.zremrangeByScore(key, start, end);
    }

    @Override
    public Long zlexcount(byte[] key, byte[] min, byte[] max) {
        return cluster.zlexcount(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
        return cluster.zrangeByLex(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return cluster.zrangeByLex(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
        return cluster.zrevrangeByLex(key, max, min);
    }

    @Override
    public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return cluster.zrevrangeByLex(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
        return cluster.zremrangeByLex(key, min, max);
    }

    @Deprecated
    @Override
    public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot, byte[] value) {
        return cluster.linsert(key, where, pivot, value);
    }

    @Override
    public Long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
        return cluster.linsert(key, where, pivot, value);
    }

    @Override
    public Long lpushx(byte[] key, byte[]... arg) {
        return cluster.lpushx(key, arg);
    }

    @Override
    public Long rpushx(byte[] key, byte[]... arg) {
        return cluster.rpushx(key, arg);
    }

    @Override
    public List<byte[]> blpop(byte[] arg) {
        throw new RedisAccessException("JedisCluster did not implemented this method");
    }

    @Override
    public List<byte[]> brpop(byte[] arg) {
        throw new RedisAccessException("JedisCluster did not implemented this method");
    }

    @Override
    public Long del(byte[] key) {
        return cluster.del(key);
    }

    @Override
    public byte[] echo(byte[] arg) {
        return cluster.echo(arg);
    }

    @Override
    public Long move(byte[] key, int dbIndex) {
        throw new RedisAccessException("JedisCluster did not implemented this method");
    }

    @Override
    public Long bitcount(byte[] key) {
        return cluster.bitcount(key);
    }

    @Override
    public Long bitcount(byte[] key, long start, long end) {
        return cluster.bitcount(key, start, end);
    }

    @Override
    public Long pfadd(byte[] key, byte[]... elements) {
        return cluster.pfadd(key, elements);
    }

    @Override
    public long pfcount(byte[] key) {
        return cluster.pfcount(key);
    }

    @Override
    public ScanResult<Entry<byte[],byte[]>> hscan(byte[] key, byte[] cursor) {
        return cluster.hscan(key, cursor);
    }

    @Override
    public ScanResult<Entry<byte[],byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
        return cluster.hscan(key, cursor, params);
    }

    @Override
    public ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
        return cluster.sscan(key, cursor);
    }

    @Override
    public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
        return cluster.sscan(key, cursor, params);
    }

    @Override
    public ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
        return cluster.zscan(key, cursor);
    }

    @Override
    public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
        return cluster.zscan(key, cursor, params);
    }

    @Override
    public byte[] dump(byte[] key) {
        return cluster.dump(key);
    }

    @Override
    public String restore(byte[] key, int ttl, byte[] serializedValue) {
        return cluster.restore(key, ttl, serializedValue);
    }

    @Override
    public String restoreReplace(byte[] key, int ttl, byte[] serializedValue) {
        throw new RedisAccessException("JedisCluster did not implemented this method");
    }

    @Override
    public Long pttl(byte[] key) {
        return cluster.pttl(key);
    }

    @Override
    public Long touch(byte[] key) {
        return cluster.touch(key);
    }

    @Override
    public String psetex(byte[] key, long milliseconds, byte[] value) {
        return cluster.psetex(key, milliseconds, value);
    }

    @Override
    public Long hset(byte[] key, Map<byte[],byte[]> hash) {
        return cluster.hset(key, hash);
    }

    @Override
    public Long unlink(byte[] key) {
        return cluster.unlink(key);
    }

    @Override
    public Long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
        return cluster.geoadd(key, longitude, latitude, member);
    }

    @Override
    public Long geoadd(byte[] key, Map<byte[],GeoCoordinate> memberCoordinateMap) {
        return cluster.geoadd(key, memberCoordinateMap);
    }

    @Override
    public Double geodist(byte[] key, byte[] member1, byte[] member2) {
        return cluster.geodist(key, member1, member2);
    }

    @Override
    public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
        return cluster.geodist(key, member1, member2, unit);
    }

    @Override
    public List<byte[]> geohash(byte[] key, byte[]... members) {
        return cluster.geohash(key, members);
    }

    @Override
    public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
        return cluster.geopos(key, members);
    }

    @Override
    public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius,
            GeoUnit unit) {
        return cluster.georadius(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return cluster.georadius(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return cluster.georadiusByMember(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return cluster.georadiusByMember(key, member, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius,
            GeoUnit unit) {
        return cluster.georadiusReadonly(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius,
            GeoUnit unit, GeoRadiusParam param) {
        return cluster.georadiusReadonly(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return cluster.georadiusByMemberReadonly(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return cluster.georadiusByMemberReadonly(key, member, radius, unit, param);
    }

    @Override
    public List<Long> bitfield(byte[] key, byte[]... arguments) {
        return cluster.bitfield(key, arguments);
    }

    @Override
    public Long del(byte[]... keys) {
        return cluster.del(keys);
    }

    @Override
    public Long unlink(byte[]... keys) {
        return cluster.unlink(keys);
    }

    @Override
    public Long exists(byte[]... keys) {
        return cluster.exists(keys);
    }

    @Override
    public List<byte[]> blpop(int timeout, byte[]... keys) {
        return cluster.blpop(timeout, keys);
    }

    @Override
    public List<byte[]> brpop(int timeout, byte[]... keys) {
        return cluster.brpop(timeout, keys);
    }

    @Override
    public List<byte[]> blpop(byte[]... args) {
        throw new RedisAccessException("JedisCluster did not implemented this method");
    }

    @Override
    public List<byte[]> brpop(byte[]... args) {
        throw new RedisAccessException("JedisCluster did not implemented this method");
    }

    @Override
    public Set<byte[]> keys(byte[] pattern) {
        return cluster.keys(pattern);
    }

    @Override
    public List<byte[]> mget(byte[]... keys) {
        return cluster.mget(keys);
    }

    @Override
    public String mset(byte[]... keysvalues) {
        return cluster.mset(keysvalues);
    }

    @Override
    public Long msetnx(byte[]... keysvalues) {
        return cluster.msetnx(keysvalues);
    }

    @Override
    public String rename(byte[] oldkey, byte[] newkey) {
        return cluster.rename(oldkey, newkey);
    }

    @Override
    public Long renamenx(byte[] oldkey, byte[] newkey) {
        return cluster.renamenx(oldkey, newkey);
    }

    @Override
    public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
        return cluster.rpoplpush(srckey, dstkey);
    }

    @Override
    public Set<byte[]> sdiff(byte[]... keys) {
        return cluster.sdiff(keys);
    }

    @Override
    public Long sdiffstore(byte[] dstkey, byte[]... keys) {
        return cluster.sdiffstore(dstkey, keys);
    }

    @Override
    public Set<byte[]> sinter(byte[]... keys) {
        return cluster.sinter(keys);
    }

    @Override
    public Long sinterstore(byte[] dstkey, byte[]... keys) {
        return cluster.sinterstore(dstkey, keys);
    }

    @Override
    public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
        return cluster.smove(srckey, dstkey, member);
    }

    @Override
    public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
        return cluster.sort(key, sortingParameters, dstkey);
    }

    @Override
    public Long sort(byte[] key, byte[] dstkey) {
        return cluster.sort(key, dstkey);
    }

    @Override
    public Set<byte[]> sunion(byte[]... keys) {
        return cluster.sunion(keys);
    }

    @Override
    public Long sunionstore(byte[] dstkey, byte[]... keys) {
        return cluster.sunionstore(dstkey, keys);
    }

    @Override
    public String watch(byte[]... keys) {
        throw new RedisAccessException("JedisCluster did not implemented this method");
    }

    @Override
    public String unwatch() {
        throw new RedisAccessException("JedisCluster did not implemented this method");
    }

    @Override
    public Long zinterstore(byte[] dstkey, byte[]... sets) {
        return cluster.zinterstore(dstkey, sets);
    }

    @Override
    public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return cluster.zinterstore(dstkey, params, sets);
    }

    @Override
    public Long zunionstore(byte[] dstkey, byte[]... sets) {
        return cluster.zunionstore(dstkey, sets);
    }

    @Override
    public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return cluster.zunionstore(dstkey, params, sets);
    }

    @Override
    public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
        return cluster.brpoplpush(source, destination, timeout);
    }

    @Override
    public Long publish(byte[] channel, byte[] message) {
        return cluster.publish(channel, message);
    }

    @Override
    public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
        cluster.subscribe(jedisPubSub, channels);
    }

    @Override
    public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
        cluster.psubscribe(jedisPubSub, patterns);
    }

    @Override
    public byte[] randomBinaryKey() {
        throw new RedisAccessException("JedisCluster did not implemented this method");
    }

    @Override
    public Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
        return cluster.bitop(op, destKey, srcKeys);
    }

    @Override
    public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
        return cluster.pfmerge(destkey, sourcekeys);
    }

    @Override
    public Long pfcount(byte[]... keys) {
        return cluster.pfcount(keys);
    }

    @Override
    public Long touch(byte[]... keys) {
        return cluster.touch(keys);
    }

}
