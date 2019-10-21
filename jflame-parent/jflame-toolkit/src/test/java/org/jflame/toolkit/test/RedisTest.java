package org.jflame.toolkit.test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import org.jflame.toolkit.cache.RedisClient;
import org.jflame.toolkit.cache.RedisClientFactory;
import org.jflame.toolkit.cache.serialize.FastJsonRedisSerializer;
import org.jflame.toolkit.lock.RedisLock;
import org.jflame.toolkit.test.entity.Pet;
import org.jflame.toolkit.util.DateHelper;

import redis.clients.jedis.JedisPoolConfig;

public class RedisTest {

    /*<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
    <property name="maxTotal" value="${redis.pool.maxActive}" />
    <property name="maxIdle" value="${redis.pool.maxIdle}" />
    <property name="maxWaitMillis" value="${redis.pool.maxWait}" />
    <property name="testOnBorrow" value="true" />
    </bean>
    
    <bean id="jedisConnection" class="org.jflame.toolkit.cache.JedisConnection">
    <constructor-arg name="hostName" value="${redis.host}"></constructor-arg>
    <constructor-arg name="mode" value="single"></constructor-arg>
    <constructor-arg name="database" value="${redis.db}"></constructor-arg>
    <constructor-arg name="poolConfig" ref="jedisPoolConfig"></constructor-arg>
    <constructor-arg name="lazy" value="false" ></constructor-arg>
    <property name="password" value="${redis.password}"></property>
    </bean>
    */
    JedisPoolConfig poolCfg;
    RedisClient client;
    private String host = "127.0.0.1";
    private int db = 2;

    Pet pet = new Pet();

    @Before
    public void setUp() {
        poolCfg = new JedisPoolConfig();
        poolCfg.setMaxTotal(500);
        poolCfg.setMaxIdle(1);

        pet.setAge(20);
        pet.setBirthday(new Date());
        pet.setCreateDate(LocalDateTime.now());
        pet.setMoney(new BigDecimal("300.2"));
        pet.setName("litte black dog");
        pet.setSkin("black");
        // jedis
        /*JedisConnection jedisConn = new JedisConnection(host, db, poolCfg);
        jedisConn.init();*/
        // spring redis
        JedisConnectionFactory jedisConn = new JedisConnectionFactory(poolCfg);
        jedisConn.setDatabase(db);
        jedisConn.setHostName(host);
        jedisConn.setUsePool(true);
        jedisConn.afterPropertiesSet();

        client = RedisClientFactory.createClient(jedisConn);
    }

    @Test
    public void testDel() {
        String hkey = "hashtest00";
        client.hput(hkey, "deleted", "ok");
        client.delete(hkey);
        System.out.println(client.exists(hkey));
    }

    @Test
    public void testset() {
        client.set("bizappno", "payorderno", 10, TimeUnit.MINUTES);
    }

    /**
     * jedis实现RedisClient测试
     */
    @Test
    public void testRedis() {

        // set,get,ttl,expire command
        client.set(pet.getName(), pet);
        long ttl = client.ttl(pet.getName());
        Pet cachePet = client.get(pet.getName());
        System.out.println("pet ttl:" + ttl);
        System.out.println(cachePet);

        cachePet.setName("redpig");
        cachePet.setAge(9);
        client.set(cachePet.getName(), cachePet, 60, TimeUnit.SECONDS);
        long ttl2 = client.ttl(cachePet.getName());
        System.out.println("cachePet ttl:" + ttl2);
        client.expire(cachePet.getName(), 20);
        ttl2 = client.ttl(cachePet.getName());
        System.out.println("cachePet ttl 2:" + ttl2);

        cachePet.setAge(1);
        cachePet.setMoney(new BigDecimal("663"));
        cachePet.setSkin("blue");

        Pet cacheOldPet = client.getAndSet(cachePet.getName(), cachePet);
        System.out.println("cacheOldPet:" + cacheOldPet);

        // exists ,delete
        if (client.exists(pet.getName())) {
            System.out.println("exists:" + pet.getName());
            client.delete(pet.getName());
        }

        // hashset
        Map<Long,Pet> testMap = new HashMap<>();
        testMap.put(999L, new Pet("999kingcat"));
        testMap.put(888L, new Pet("888kingpig"));
        String hsetkey = "htest";
        client.hput(hsetkey, pet.getName(), pet);
        client.hput(hsetkey, cachePet.getName(), cachePet);
        List<Pet> pets = client.hvalues(hsetkey);
        System.out.println("pets hset:" + pets);
        client.hget(hsetkey, pet.getName());

        if (client.hexists(hsetkey, cachePet.getName())) {
            client.hdelete(hsetkey, cachePet.getName());
        }
        Pet nullPet = client.hget(hsetkey, cachePet.getName());
        if (nullPet == null) {
            System.out.println("null pet");
        }

        client.hputAll(hsetkey, testMap);
        boolean hputIfAbsent = client.hputIfAbsent(hsetkey, 888L, new Pet("8889kingpig"));
        System.out.println("hputIfAbsent:" + hputIfAbsent);
        Set<String> setkeys = client.hkeys(hsetkey);
        System.out.println("hset keys:" + setkeys + " size:" + client.hsize(hsetkey));
        List<Pet> petset = client.hmultiGet(hsetkey, Arrays.asList(999L, 888L));
        System.out.println("petset:" + petset);

        // list
        String listKey = "listkey";
        client.lpush(listKey, cacheOldPet);
        client.lpush(listKey, cachePet);
        client.lpush(listKey, testMap.get(999L));
        long pushif99 = client.lpushIfAbsent(listKey, testMap.get(999L));
        System.out.println("pushif9:" + pushif99);
        long pushif88 = client.lpushIfAbsent(listKey, testMap.get(888L));
        System.out.println("pushif88:" + pushif88);

        client.lset(listKey, 1, new Pet("setlist"));
        Pet indexPet = client.lindex(listKey, 1);
        System.out.println("indexPet:" + indexPet);
        Pet popPet = client.lpop(listKey);
        System.out.println("popPet:" + popPet);

        client.linsert(listKey, new Pet("insertpet"), testMap.get(888L));
        client.lremove(listKey, popPet);
        client.rpush(listKey, new Pet("rpushpet"));

        System.out.println("list size:" + client.lsize(listKey));

        // set
        String setkey = "noorderset",otherSetKey = "otherSetKey";
        Pet nPet = new Pet("noOrderSet");
        Pet oPet = new Pet("biggirl", 24, "white", DateHelper.setDate(1994, 4, 1), new BigDecimal("200"));
        client.sadd(setkey, pet);
        client.sadd(setkey, nPet);
        long i = client.sadd(setkey, new Pet("noOrderSet"));
        System.out.println("new ns:" + i);// 0

        client.sadd(otherSetKey, oPet);
        client.sadd(otherSetKey, nPet);
        Set<Pet> diffSet = client.sdiff(setkey, otherSetKey);
        System.out.println("diffSet:" + diffSet);
        Set<Pet> intersectSet = client.sintersect(setkey, otherSetKey);
        System.out.println("intersectSet:" + intersectSet);
        client.sunionAndStore(setkey, otherSetKey, "unionkey");
        // sorted set
        String sortedsetKey = "sortedsetKey";
        client.zsadd(sortedsetKey, nPet, 300d);
        client.zsadd(sortedsetKey, nPet, 400d);
        client.zsadd(sortedsetKey, oPet, 55);
        System.out.println("zs size:" + client.zssize(sortedsetKey));
        System.out.println("zs score 299-400 size:" + client.zscount(sortedsetKey, 300, 400));

    }

    @Test
    public void testIncr() {
        Long x = client.incr("testincr");
        System.out.println(x);
    }

    @Test
    public void testLock() {
        RedisLock lock = new RedisLock(client, "test-redis-lock", 50);
        int i = 0;
        while (i < 3) {
            try {
                if (lock.lock(500)) {
                    System.out.println("locked");
                } else {
                    System.out.println("lock error");
                }
            } finally {
                lock.unlock();
            }
            i++;
            try {
                Thread.sleep(RandomUtils.nextInt(100, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testLua() {
        StringBuilder sb = new StringBuilder();
        sb.append("local maxscores=redis.call('ZRANGE',KEYS[1],-1,-1,'WITHSCORES') ");
        sb.append("local maxscore=1 ");
        sb.append("if maxscores~=nil then ");
        sb.append("  return '\"'..tostring(maxscores[1])..'\"' end ");
        // sb.append(" maxscore=tonumber(maxscores[1])+1 end ");// tonumber(maxscores[1])+1tostring(maxscores)
        // sb.append("redis.call('zadd',KEYS[1],maxscore,ARGV[1]) ");
        sb.append("return '\"9\"' ");

        // Long m = client.runScript(sb.toString(), Arrays.asList("zsetdemo"), Arrays.asList("zsetmember2"),
        // Long.class);
        String m = client.runScript(sb.toString(), Arrays.asList("zsetdemo"), Arrays.asList("zsetmember2"),
                String.class);
        System.out.println(m);
    }

    @Test
    public void testSerial() {

        FastJsonRedisSerializer serializer = new FastJsonRedisSerializer();
        byte[] bytes = "y".getBytes(StandardCharsets.UTF_8);
        Object c = serializer.deserialize(bytes);
    }
}
