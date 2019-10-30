package org.jflame.toolkit.cache;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.toolkit.exception.DataAccessException;
import org.jflame.toolkit.util.StringHelper;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.ShardedJedis;

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
    // private ShardedJedisPool sharded;// 分片模式
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
        cluster
        // sharded;
    }

    /**
     * 构造函数,默认单机模式
     * 
     * @param hostName Redis主机连接信息.格式: ip:port,ip1:port1
     * @param database 数据库
     * @param poolConfig 连接池配置
     */
    public JedisConnection(String hostName, int database, JedisPoolConfig poolConfig) {
        this(RedisMode.single.name(), hostName, database, null, poolConfig);
    }

    /**
     * 构造函数,默认单机模式
     * 
     * @param hostName Redis主机连接信息.格式: ip:port,ip1:port1
     * @param database 数据库
     * @param password 连接密码
     * @param poolConfig 连接池配置
     */
    public JedisConnection(String hostName, int database, String password, JedisPoolConfig poolConfig) {
        this(RedisMode.single.name(), hostName, database, password, poolConfig);
    }

    /**
     * 构造函数
     * 
     * @param mode RedisMode运行模式
     * @param hostName Redis主机连接信息.格式: ip:port,ip1:port1
     * @param database 数据库
     * @param poolConfig 连接池配置
     */
    public JedisConnection(String mode, String hostName, int database, String password, JedisPoolConfig poolConfig) {
        currentMode = RedisMode.valueOf(mode);
        hostName = StringUtils.deleteWhitespace(hostName);
        hosts = hostName.split(",");
        this.database = database;
        setPassword(password);
        this.poolConfig = poolConfig;
    }

    @PostConstruct
    public void init() {
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
                /*case sharded:
                    List<JedisShardInfo> shards = new ArrayList<>();
                    try {
                        for (String node : hosts)
                            shards.add(new JedisShardInfo(new URI(node)));
                    } catch (URISyntaxException e) {
                        throw new JedisConnectionException(e);
                    }
                    this.sharded = new ShardedJedisPool(poolConfig, shards);
                    break;*/
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

    /* public ShardedJedis getShardedJedis() {
        init();
        if (currentMode == RedisMode.sharded) {
            return sharded.getResource();
        } else {
            throw new DataAccessException("redis非sharded模式不可获取ShardedJedis");
        }
    }
    */
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
        /*if (sharded != null)
            sharded.close();*/
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
        if (StringHelper.isNotEmpty(password)) {
            this.password = password.trim();
        }
    }

}
