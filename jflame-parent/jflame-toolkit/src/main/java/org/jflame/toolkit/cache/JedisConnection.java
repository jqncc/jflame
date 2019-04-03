package org.jflame.toolkit.cache;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

/**
 * jedis各种模式的统一
 * 
 * @author yucan.zhang
 */
public class JedisConnection implements Closeable, AutoCloseable {

    private final static Logger log = LoggerFactory.getLogger(JedisConnection.class);

    private final static int MAX_ATTEMPTS = 3;

    private JedisPool single;// 单机模式
    private JedisCluster cluster;// 集群模式
    private ShardedJedisPool sharded;// 分片模式
    private JedisSentinelPool sentinel;// 哨兵主从
    private RedisMode currentMode;
    private String[] hosts;
    private String clusterName;
    private int database;
    private String password;
    private int connectionTimeout = 6000;
    private int soTimeout = 5000;

    private JedisPoolConfig poolConfig;
    private AtomicBoolean isInited = new AtomicBoolean(false);

    public enum RedisMode {
        single,
        sentinel,
        cluster,
        sharded;
    }

    /**
     * 构造函数,默认单机模式
     * 
     * @param hostName
     * @param database
     * @param poolConfig 连接池配置
     * @param lazy 是否延迟开启连接池直到第一次获取连接
     */
    public JedisConnection(String hostName, int database, JedisPoolConfig poolConfig, boolean lazy) {
        this(RedisMode.single.name(), hostName, database, poolConfig, lazy);
    }

    /**
     * 构造函数
     * 
     * @param mode RedisMode 运行模式
     * @param hostName Redis 主机连接信息.格式: ip:port,ip1:port1
     * @param database 数据库
     * @param poolConfig 连接池配置
     * @param lazy 是否延迟开启连接池直到第一次获取连接
     */
    public JedisConnection(String mode, String hostName, int database, JedisPoolConfig poolConfig, boolean lazy) {
        currentMode = RedisMode.valueOf(mode);
        hostName = StringUtils.deleteWhitespace(hostName);
        hosts = hostName.split(",");
        this.database = database;
        this.poolConfig = poolConfig;
        if (!lazy) {
            init();
        }
    }

    private void init() {
        if (!isInited.get()) {
            String[] nodeInfo;
            switch (currentMode) {
                case single:
                    nodeInfo = hosts[0].split(":");
                    int port = (nodeInfo.length > 1) ? Integer.parseInt(nodeInfo[1]) : 6379;
                    this.single = new JedisPool(poolConfig, nodeInfo[0], port, connectionTimeout, password, database);
                    break;
                case cluster:
                    Set<HostAndPort> hostSet = new HashSet<>();
                    for (String node : hosts) {
                        nodeInfo = node.split(":");
                        hostSet.add(new HostAndPort(nodeInfo[0],
                                (nodeInfo.length > 1) ? Integer.parseInt(nodeInfo[1]) : 6379));
                    }
                    this.cluster = new JedisCluster(hostSet, connectionTimeout, soTimeout, MAX_ATTEMPTS, password,
                            poolConfig);
                    break;
                case sentinel:
                    Set<String> nodes = new HashSet<>();
                    Collections.addAll(nodes, hosts);
                    this.sentinel = new JedisSentinelPool(clusterName, nodes, poolConfig, connectionTimeout, password,
                            database);
                    break;
                case sharded:
                    List<JedisShardInfo> shards = new ArrayList<>();
                    try {
                        for (String node : hosts)
                            shards.add(new JedisShardInfo(new URI(node)));
                    } catch (URISyntaxException e) {
                        throw new JedisConnectionException(e);
                    }
                    this.sharded = new ShardedJedisPool(poolConfig, shards);
                    break;
            }
            isInited.set(true);
        }
    }

    /**
     * 获取Jedis底层二进制命令接口
     * 
     * @return
     */
    /*public BinaryJedisCommands getCmd() {
        if (currentMode == RedisMode.single) {
            return single.getResource();
        } else if (currentMode == RedisMode.sentinel) {
            return sentinel.getResource();
        } else if (currentMode == RedisMode.cluster) {
            return toBinaryJedisCommands(cluster);
        } else if (currentMode == RedisMode.sharded) {
            return sharded.getResource();
        } else {
            throw new DataAccessException("redis配置未设置正确的工作模式");
        }
    }*/

    public Jedis getJedis() {
        init();
        if (currentMode == RedisMode.single) {
            return single.getResource();
        } else if (currentMode == RedisMode.sentinel) {
            return sentinel.getResource();
        } else {
            throw new DataAccessException("redis非single/sentinel模式不可获取Jedis");
        }
    }

    public ShardedJedis getShardedJedis() {
        init();
        if (currentMode == RedisMode.sharded) {
            return sharded.getResource();
        } else {
            throw new DataAccessException("redis非sharded模式不可获取ShardedJedis");
        }
    }

    public JedisCluster getJedisCluster() {
        init();
        if (currentMode == RedisMode.cluster) {
            return cluster;
        } else {
            throw new DataAccessException("redis非sharded模式不可获取ShardedJedis");
        }
    }

    public void release(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public void release(ShardedJedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public void release(JedisCluster jedis) {
        if (jedis != null) {
            try {
                jedis.close();
            } catch (IOException e) {
                log.error("Failed to release jedis cluster connection", e);
            }
        }
    }

    /**
     * 释放连接池
     * 
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if (single != null)
            single.close();
        if (sentinel != null)
            sentinel.close();
        if (cluster != null)
            cluster.close();
        if (sharded != null)
            sharded.close();
    }

    /**
     * 为了变态的 jedis 接口设计，搞了五百多行垃圾代码
     * 
     * @param cluster Jedis 集群实例
     * @return
     */
    private BinaryJedisCommands toBinaryJedisCommands(JedisCluster cluster) {
        return new BinaryJedisCommands() {

            @Override
            public String set(byte[] bytes, byte[] bytes1) {
                return cluster.set(bytes, bytes1);
            }

            @Override
            public String set(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return null;
            }

            @Override
            public String set(byte[] bytes, byte[] bytes1, byte[] bytes2, byte[] bytes3, long l) {
                return null;
            }

            @Override
            public byte[] get(byte[] bytes) {
                return cluster.get(bytes);
            }

            @Override
            public Boolean exists(byte[] bytes) {
                return cluster.exists(bytes);
            }

            @Override
            public Long persist(byte[] bytes) {
                return cluster.persist(bytes);
            }

            @Override
            public String type(byte[] bytes) {
                return cluster.type(bytes);
            }

            @Override
            public Long expire(byte[] bytes, int i) {
                return cluster.expire(bytes, i);
            }

            @Override
            public Long pexpire(String s, long l) {
                return cluster.pexpire(s, l);
            }

            @Override
            public Long pexpire(byte[] bytes, long l) {
                return cluster.pexpire(bytes, l);
            }

            @Override
            public Long expireAt(byte[] bytes, long l) {
                return cluster.expireAt(bytes, l);
            }

            @Override
            public Long pexpireAt(byte[] bytes, long l) {
                return cluster.pexpireAt(bytes, l);
            }

            @Override
            public Long ttl(byte[] bytes) {
                return cluster.ttl(bytes);
            }

            @Override
            public Boolean setbit(byte[] bytes, long l, boolean b) {
                return cluster.setbit(bytes, l, b);
            }

            @Override
            public Boolean setbit(byte[] bytes, long l, byte[] bytes1) {
                return cluster.setbit(bytes, l, bytes1);
            }

            @Override
            public Boolean getbit(byte[] bytes, long l) {
                return cluster.getbit(bytes, l);
            }

            @Override
            public Long setrange(byte[] bytes, long l, byte[] bytes1) {
                return cluster.setrange(bytes, l, bytes1);
            }

            @Override
            public byte[] getrange(byte[] bytes, long l, long l1) {
                return cluster.getrange(bytes, l, l1);
            }

            @Override
            public byte[] getSet(byte[] bytes, byte[] bytes1) {
                return cluster.getSet(bytes, bytes1);
            }

            @Override
            public Long setnx(byte[] bytes, byte[] bytes1) {
                return cluster.setnx(bytes, bytes1);
            }

            @Override
            public String setex(byte[] bytes, int i, byte[] bytes1) {
                return cluster.setex(bytes, i, bytes1);
            }

            @Override
            public Long decrBy(byte[] bytes, long l) {
                return cluster.decrBy(bytes, l);
            }

            @Override
            public Long decr(byte[] bytes) {
                return cluster.decr(bytes);
            }

            @Override
            public Long incrBy(byte[] bytes, long l) {
                return cluster.incrBy(bytes, l);
            }

            @Override
            public Double incrByFloat(byte[] bytes, double v) {
                return cluster.incrByFloat(bytes, v);
            }

            @Override
            public Long incr(byte[] bytes) {
                return cluster.incr(bytes);
            }

            @Override
            public Long append(byte[] bytes, byte[] bytes1) {
                return cluster.append(bytes, bytes1);
            }

            @Override
            public byte[] substr(byte[] bytes, int i, int i1) {
                return cluster.substr(bytes, i, i1);
            }

            @Override
            public Long hset(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.hset(bytes, bytes1, bytes2);
            }

            @Override
            public byte[] hget(byte[] bytes, byte[] bytes1) {
                return cluster.hget(bytes, bytes1);
            }

            @Override
            public Long hsetnx(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.hsetnx(bytes, bytes1, bytes2);
            }

            @Override
            public String hmset(byte[] bytes, Map<byte[],byte[]> map) {
                return cluster.hmset(bytes, map);
            }

            @Override
            public List<byte[]> hmget(byte[] bytes, byte[]... bytes1) {
                return cluster.hmget(bytes, bytes1);
            }

            @Override
            public Long hincrBy(byte[] bytes, byte[] bytes1, long l) {
                return cluster.hincrBy(bytes, bytes1, l);
            }

            @Override
            public Double hincrByFloat(byte[] bytes, byte[] bytes1, double v) {
                return cluster.hincrByFloat(bytes, bytes1, v);
            }

            @Override
            public Boolean hexists(byte[] bytes, byte[] bytes1) {
                return cluster.hexists(bytes, bytes1);
            }

            @Override
            public Long hdel(byte[] bytes, byte[]... bytes1) {
                return cluster.hdel(bytes, bytes1);
            }

            @Override
            public Long hlen(byte[] bytes) {
                return cluster.hlen(bytes);
            }

            @Override
            public Set<byte[]> hkeys(byte[] bytes) {
                return cluster.hkeys(bytes);
            }

            @Override
            public Collection<byte[]> hvals(byte[] bytes) {
                return cluster.hvals(bytes);
            }

            @Override
            public Map<byte[],byte[]> hgetAll(byte[] bytes) {
                return cluster.hgetAll(bytes);
            }

            @Override
            public Long rpush(byte[] bytes, byte[]... bytes1) {
                return cluster.rpush(bytes, bytes1);
            }

            @Override
            public Long lpush(byte[] bytes, byte[]... bytes1) {
                return cluster.lpush(bytes, bytes1);
            }

            @Override
            public Long llen(byte[] bytes) {
                return cluster.llen(bytes);
            }

            @Override
            public List<byte[]> lrange(byte[] bytes, long l, long l1) {
                return cluster.lrange(bytes, l, l1);
            }

            @Override
            public String ltrim(byte[] bytes, long l, long l1) {
                return cluster.ltrim(bytes, l, l1);
            }

            @Override
            public byte[] lindex(byte[] bytes, long l) {
                return cluster.lindex(bytes, l);
            }

            @Override
            public String lset(byte[] bytes, long l, byte[] bytes1) {
                return cluster.lset(bytes, l, bytes1);
            }

            @Override
            public Long lrem(byte[] bytes, long l, byte[] bytes1) {
                return cluster.lrem(bytes, l, bytes1);
            }

            @Override
            public byte[] lpop(byte[] bytes) {
                return cluster.lpop(bytes);
            }

            @Override
            public byte[] rpop(byte[] bytes) {
                return cluster.rpop(bytes);
            }

            @Override
            public Long sadd(byte[] bytes, byte[]... bytes1) {
                return cluster.sadd(bytes, bytes1);
            }

            @Override
            public Set<byte[]> smembers(byte[] bytes) {
                return cluster.smembers(bytes);
            }

            @Override
            public Long srem(byte[] bytes, byte[]... bytes1) {
                return cluster.srem(bytes, bytes1);
            }

            @Override
            public byte[] spop(byte[] bytes) {
                return cluster.spop(bytes);
            }

            @Override
            public Set<byte[]> spop(byte[] bytes, long l) {
                return cluster.spop(bytes, l);
            }

            @Override
            public Long scard(byte[] bytes) {
                return cluster.scard(bytes);
            }

            @Override
            public Boolean sismember(byte[] bytes, byte[] bytes1) {
                return cluster.sismember(bytes, bytes1);
            }

            @Override
            public byte[] srandmember(byte[] bytes) {
                return cluster.srandmember(bytes);
            }

            @Override
            public List<byte[]> srandmember(byte[] bytes, int i) {
                return cluster.srandmember(bytes, i);
            }

            @Override
            public Long strlen(byte[] bytes) {
                return cluster.strlen(bytes);
            }

            @Override
            public Long zadd(byte[] bytes, double v, byte[] bytes1) {
                return cluster.zadd(bytes, v, bytes1);
            }

            @Override
            public Long zadd(byte[] bytes, double v, byte[] bytes1, ZAddParams zAddParams) {
                return cluster.zadd(bytes, v, bytes1, zAddParams);
            }

            @Override
            public Long zadd(byte[] bytes, Map<byte[],Double> map) {
                return cluster.zadd(bytes, map);
            }

            @Override
            public Long zadd(byte[] bytes, Map<byte[],Double> map, ZAddParams zAddParams) {
                return cluster.zadd(bytes, map, zAddParams);
            }

            @Override
            public Set<byte[]> zrange(byte[] bytes, long l, long l1) {
                return cluster.zrange(bytes, l, l1);
            }

            @Override
            public Long zrem(byte[] bytes, byte[]... bytes1) {
                return cluster.zrem(bytes, bytes1);
            }

            @Override
            public Double zincrby(byte[] bytes, double v, byte[] bytes1) {
                return cluster.zincrby(bytes, v, bytes1);
            }

            @Override
            public Double zincrby(byte[] bytes, double v, byte[] bytes1, ZIncrByParams zIncrByParams) {
                return cluster.zincrby(bytes, v, bytes1, zIncrByParams);
            }

            @Override
            public Long zrank(byte[] bytes, byte[] bytes1) {
                return cluster.zrank(bytes, bytes1);
            }

            @Override
            public Long zrevrank(byte[] bytes, byte[] bytes1) {
                return cluster.zrevrank(bytes, bytes1);
            }

            @Override
            public Set<byte[]> zrevrange(byte[] bytes, long l, long l1) {
                return cluster.zrevrange(bytes, l, l1);
            }

            @Override
            public Set<Tuple> zrangeWithScores(byte[] bytes, long l, long l1) {
                return cluster.zrangeWithScores(bytes, l, l1);
            }

            @Override
            public Set<Tuple> zrevrangeWithScores(byte[] bytes, long l, long l1) {
                return cluster.zrevrangeWithScores(bytes, l, l1);
            }

            @Override
            public Long zcard(byte[] bytes) {
                return cluster.zcard(bytes);
            }

            @Override
            public Double zscore(byte[] bytes, byte[] bytes1) {
                return cluster.zscore(bytes, bytes1);
            }

            @Override
            public List<byte[]> sort(byte[] bytes) {
                return cluster.sort(bytes);
            }

            @Override
            public List<byte[]> sort(byte[] bytes, SortingParams sortingParams) {
                return cluster.sort(bytes, sortingParams);
            }

            @Override
            public Long zcount(byte[] bytes, double v, double v1) {
                return cluster.zcount(bytes, v, v1);
            }

            @Override
            public Long zcount(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.zcount(bytes, bytes1, bytes2);
            }

            @Override
            public Set<byte[]> zrangeByScore(byte[] bytes, double v, double v1) {
                return cluster.zrangeByScore(bytes, v, v1);
            }

            @Override
            public Set<byte[]> zrangeByScore(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.zrangeByScore(bytes, bytes1, bytes2);
            }

            @Override
            public Set<byte[]> zrevrangeByScore(byte[] bytes, double v, double v1) {
                return cluster.zrevrangeByScore(bytes, v, v1);
            }

            @Override
            public Set<byte[]> zrangeByScore(byte[] bytes, double v, double v1, int i, int i1) {
                return cluster.zrangeByScore(bytes, v, v1, i, i1);
            }

            @Override
            public Set<byte[]> zrevrangeByScore(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.zrevrangeByScore(bytes, bytes1, bytes2);
            }

            @Override
            public Set<byte[]> zrangeByScore(byte[] bytes, byte[] bytes1, byte[] bytes2, int i, int i1) {
                return cluster.zrangeByScore(bytes, bytes1, bytes2, i, i1);
            }

            @Override
            public Set<byte[]> zrevrangeByScore(byte[] bytes, double v, double v1, int i, int i1) {
                return cluster.zrevrangeByScore(bytes, v, v1, i, i1);
            }

            @Override
            public Set<Tuple> zrangeByScoreWithScores(byte[] bytes, double v, double v1) {
                return cluster.zrangeByScoreWithScores(bytes, v, v1);
            }

            @Override
            public Set<Tuple> zrevrangeByScoreWithScores(byte[] bytes, double v, double v1) {
                return cluster.zrevrangeByScoreWithScores(bytes, v, v1);
            }

            @Override
            public Set<Tuple> zrangeByScoreWithScores(byte[] bytes, double v, double v1, int i, int i1) {
                return cluster.zrangeByScoreWithScores(bytes, v, v1, i, i1);
            }

            @Override
            public Set<byte[]> zrevrangeByScore(byte[] bytes, byte[] bytes1, byte[] bytes2, int i, int i1) {
                return cluster.zrevrangeByScore(bytes, bytes1, bytes2, i, i1);
            }

            @Override
            public Set<Tuple> zrangeByScoreWithScores(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.zrangeByScoreWithScores(bytes, bytes1, bytes2);
            }

            @Override
            public Set<Tuple> zrevrangeByScoreWithScores(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.zrevrangeByScoreWithScores(bytes, bytes1, bytes2);
            }

            @Override
            public Set<Tuple> zrangeByScoreWithScores(byte[] bytes, byte[] bytes1, byte[] bytes2, int i, int i1) {
                return cluster.zrangeByScoreWithScores(bytes, bytes1, bytes2, i, i1);
            }

            @Override
            public Set<Tuple> zrevrangeByScoreWithScores(byte[] bytes, double v, double v1, int i, int i1) {
                return cluster.zrevrangeByScoreWithScores(bytes, v, v1, i, i1);
            }

            @Override
            public Set<Tuple> zrevrangeByScoreWithScores(byte[] bytes, byte[] bytes1, byte[] bytes2, int i, int i1) {
                return cluster.zrevrangeByScoreWithScores(bytes, bytes1, bytes2, i, i1);
            }

            @Override
            public Long zremrangeByRank(byte[] bytes, long l, long l1) {
                return cluster.zremrangeByRank(bytes, l, l1);
            }

            @Override
            public Long zremrangeByScore(byte[] bytes, double v, double v1) {
                return cluster.zremrangeByScore(bytes, v, v1);
            }

            @Override
            public Long zremrangeByScore(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.zremrangeByScore(bytes, bytes1, bytes2);
            }

            @Override
            public Long zlexcount(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.zlexcount(bytes, bytes1, bytes2);
            }

            @Override
            public Set<byte[]> zrangeByLex(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.zrangeByLex(bytes, bytes1, bytes2);
            }

            @Override
            public Set<byte[]> zrangeByLex(byte[] bytes, byte[] bytes1, byte[] bytes2, int i, int i1) {
                return cluster.zrangeByLex(bytes, bytes1, bytes2, i, i1);
            }

            @Override
            public Set<byte[]> zrevrangeByLex(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.zrevrangeByLex(bytes, bytes1, bytes2);
            }

            @Override
            public Set<byte[]> zrevrangeByLex(byte[] bytes, byte[] bytes1, byte[] bytes2, int i, int i1) {
                return cluster.zrevrangeByLex(bytes, bytes1, bytes2, i, i1);
            }

            @Override
            public Long zremrangeByLex(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.zremrangeByLex(bytes, bytes1, bytes2);
            }

            @Override
            public Long linsert(byte[] bytes, BinaryClient.LIST_POSITION list_position, byte[] bytes1, byte[] bytes2) {
                return cluster.linsert(bytes, list_position, bytes1, bytes2);
            }

            @Override
            public Long lpushx(byte[] bytes, byte[]... bytes1) {
                return cluster.lpushx(bytes, bytes1);
            }

            @Override
            public Long rpushx(byte[] bytes, byte[]... bytes1) {
                return cluster.rpushx(bytes, bytes1);
            }

            @Override
            public List<byte[]> blpop(byte[] bytes) {
                return cluster.blpop(0, bytes);
            }

            @Override
            public List<byte[]> brpop(byte[] bytes) {
                return cluster.brpop(0, bytes);
            }

            @Override
            public Long del(byte[] bytes) {
                return cluster.del(bytes);
            }

            @Override
            public byte[] echo(byte[] bytes) {
                return cluster.echo(bytes);
            }

            @Override
            public Long move(byte[] bytes, int i) {
                return cluster.move(new String(bytes), i);
            }

            @Override
            public Long bitcount(byte[] bytes) {
                return cluster.bitcount(bytes);
            }

            @Override
            public Long bitcount(byte[] bytes, long l, long l1) {
                return cluster.bitcount(bytes, l, l1);
            }

            @Override
            public Long pfadd(byte[] bytes, byte[]... bytes1) {
                return cluster.pfadd(bytes, bytes1);
            }

            @Override
            public long pfcount(byte[] bytes) {
                return cluster.pfcount(bytes);
            }

            @Override
            public Long geoadd(byte[] bytes, double v, double v1, byte[] bytes1) {
                return cluster.geoadd(bytes, v, v1, bytes1);
            }

            @Override
            public Long geoadd(byte[] bytes, Map<byte[],GeoCoordinate> map) {
                return cluster.geoadd(bytes, map);
            }

            @Override
            public Double geodist(byte[] bytes, byte[] bytes1, byte[] bytes2) {
                return cluster.geodist(bytes, bytes1, bytes2);
            }

            @Override
            public Double geodist(byte[] bytes, byte[] bytes1, byte[] bytes2, GeoUnit geoUnit) {
                return cluster.geodist(bytes, bytes1, bytes2, geoUnit);
            }

            @Override
            public List<byte[]> geohash(byte[] bytes, byte[]... bytes1) {
                return cluster.geohash(bytes, bytes1);
            }

            @Override
            public List<GeoCoordinate> geopos(byte[] bytes, byte[]... bytes1) {
                return cluster.geopos(bytes, bytes1);
            }

            @Override
            public List<GeoRadiusResponse> georadius(byte[] bytes, double v, double v1, double v2, GeoUnit geoUnit) {
                return cluster.georadius(bytes, v, v1, v2, geoUnit);
            }

            @Override
            public List<GeoRadiusResponse> georadius(byte[] bytes, double v, double v1, double v2, GeoUnit geoUnit,
                    GeoRadiusParam geoRadiusParam) {
                return cluster.georadius(bytes, v, v1, v2, geoUnit, geoRadiusParam);
            }

            @Override
            public List<GeoRadiusResponse> georadiusByMember(byte[] bytes, byte[] bytes1, double v, GeoUnit geoUnit) {
                return cluster.georadiusByMember(bytes, bytes1, v, geoUnit);
            }

            @Override
            public List<GeoRadiusResponse> georadiusByMember(byte[] bytes, byte[] bytes1, double v, GeoUnit geoUnit,
                    GeoRadiusParam geoRadiusParam) {
                return cluster.georadiusByMember(bytes, bytes1, v, geoUnit, geoRadiusParam);
            }

            @Override
            public ScanResult<Map.Entry<byte[],byte[]>> hscan(byte[] bytes, byte[] bytes1) {
                return cluster.hscan(bytes, bytes1);
            }

            @Override
            public ScanResult<Map.Entry<byte[],byte[]>> hscan(byte[] bytes, byte[] bytes1, ScanParams scanParams) {
                return cluster.hscan(bytes, bytes1, scanParams);
            }

            @Override
            public ScanResult<byte[]> sscan(byte[] bytes, byte[] bytes1) {
                return cluster.sscan(bytes, bytes1);
            }

            @Override
            public ScanResult<byte[]> sscan(byte[] bytes, byte[] bytes1, ScanParams scanParams) {
                return cluster.sscan(bytes, bytes1, scanParams);
            }

            @Override
            public ScanResult<Tuple> zscan(byte[] bytes, byte[] bytes1) {
                return cluster.zscan(bytes, bytes1);
            }

            @Override
            public ScanResult<Tuple> zscan(byte[] bytes, byte[] bytes1, ScanParams scanParams) {
                return cluster.zscan(bytes, bytes1, scanParams);
            }

            @Override
            public List<byte[]> bitfield(byte[] bytes, byte[]... bytes1) {
                return cluster.bitfield(bytes, bytes1);
            }
        };
    }

    public RedisMode getCurrentMode() {
        return currentMode;
    }

    public String[] getHosts() {
        return hosts;
    }

    public String getClusterName() {
        return clusterName;
    }

    public int getDatabase() {
        return database;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setPassword(String password) {
        if (password != null) {
            this.password = password.trim();
        }
    }

}
