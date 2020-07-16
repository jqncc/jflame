package org.jflame.context.cache.redis;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.MapHelper;

public interface RedisClient {

    default void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 先从缓存获取数据,如果不存在则调用Supplier获取数据并缓存,缓存的数据未设置过期时间.
     * 
     * @param key 缓存key
     * @param querySupplier Supplier
     * @return
     */
    default public <T> T get(String key, Supplier<T> querySupplier) {
        T t = get(key);
        if (t == null) {
            t = querySupplier.get();
            if (t != null) {
                set(key, t);
            }
        }
        return t;
    }

    /**
     * 先从缓存获取数据,如果不存在则调用Supplier获取数据并缓存,可指定缓存过期时间.<br>
     * 示例:{@code get("userkey",100,()->{return getUserById(1);)}
     * 
     * @param key 缓存key
     * @param timeout 过期时间,单位秒
     * @param querySupplier Supplier
     * @return
     */
    default public <T> T get(String key, long timeout, Supplier<T> querySupplier) {
        T t = get(key);
        if (t == null) {
            t = querySupplier.get();
            if (t != null) {
                set(key, t, timeout, TimeUnit.SECONDS);
            }
        }
        return t;
    }

    /**
     * 将集合缓存到哈希集,key由集合元素转换,value为集合元素.示例:<br>
     * {@code
     *  
     *  hputAll("key",list, cat -> cat.getName());
     * }
     * 
     * @param key 哈希集key
     * @param value 集合
     * @param mapper key转换方法
     */
    default <T,R> void hputAll(final String key, final Collection<T> value, Function<T,String> mapper) {
        final Map<String,T> map = MapHelper.toMap(value, mapper);
        hputAll(key, map);
    }

    /**
     * 先从缓存获取集合数据,如果不存在则调用Supplier获取数据并缓存,集合缓存为哈希集,各元素key由参数hkeyMapper转换
     * 
     * @param key 缓存key
     * @param hkeyMapper 集合元素转为map时key的转换方法Function
     * @param querySupplier 实际数据查询的方法Supplier
     * @return List
     */
    default public <T extends Serializable> List<T> hvalues(String key, Function<T,String> hkeyMapper,
            Supplier<List<T>> querySupplier) {
        List<T> t = hvalues(key);
        if (CollectionHelper.isEmpty(t)) {
            t = querySupplier.get();
            if (CollectionHelper.isNotEmpty(t)) {
                try {
                    hputAll(key, t, hkeyMapper);
                } catch (RedisAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return t;
    }

    /**
     * 运行更新数据同时清除对应缓存
     * 
     * @param key 关联数据的缓存key
     * @param executeSupplier 更新数据方法
     * @return
     */
    default public Boolean executeAndClearCache(String key, Supplier<Boolean> executeSupplier) {
        Boolean r = executeSupplier.get();
        if (r) {
            try {
                delete(key);
            } catch (RedisAccessException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    /**
     * 查看剩余过期时间,单位秒
     * 
     * @param key
     * @return
     */
    long ttl(final String key);

    /**
     * 设置缓存
     * 
     * @param key
     * @param value
     */
    void set(final String key, final Object value);

    /**
     * 设置缓存和缓存时间
     * 
     * @param key
     * @param value
     * @param timeout 缓存时间
     * @param timeUnit 时间单位
     */
    void set(final String key, final Object value, long timeout, TimeUnit timeUnit);

    /**
     * 一次设置多个缓存.MSET是原子的，所以所有给定的keys是一次性set的,客户端不可能看到这种一部分keys被更新而另外的没有改变的情况.返回总是OK，因为MSET不会失败
     * 
     * @param pair 缓存键和值的map
     */
    <V> void multiSet(final Map<String,V> pair);

    /**
     * 设置缓存,只有在键不存在时才设置
     * 
     * @param key
     * @param value
     * @return 设置成功返回true
     */
    boolean setIfAbsent(final String key, final Object value);

    /**
     * 设置缓存和缓存时间,只有在键不存在时才设置
     * 
     * @param key
     * @param value
     * @param timeout
     * @param timeUnit
     * @return 设置成功返回true
     */
    boolean setIfAbsent(final String key, final Object value, long timeout, TimeUnit timeUnit);

    /**
     * 一次设置多个缓存,只要有一个key已经存在，MSETNX一个操作都不会执行.其他特性与mset一致{@link #multiSet(Map)}
     * 
     * @param pair
     */
    <V> void multiSetIfAbsent(Map<String,V> pair);

    /**
     * 获取缓存值,结果反序列化为clazz指定的类型
     * 
     * @param key
     * @return
     */
    <T> T get(final String key);

    /**
     * 设置一个新值,并返回旧值,结果反序列化为clazz指定的类型
     * 
     * @param key
     * @param newValue
     * @return
     */
    <T> T getAndSet(final String key, T newValue);

    /**
     * 一次获取多个值
     * 
     * @param keys
     * @return
     */
    <T> List<T> multiGet(Collection<String> keys);

    /**
     * 删除一个缓存
     * 
     * @param key 要删除的缓存键
     * @return
     */
    boolean delete(final String key);

    /**
     * 删除多个缓存
     * 
     * @param keys 要删除的缓存键集合
     * @return 返回成功删除的数量
     */
    long delete(final Set<String> keys);

    /**
     * 判断缓存是否存在
     * 
     * @param key
     * @return
     */
    boolean exists(final String key);

    /**
     * 设置缓存过期时间
     * 
     * @param key
     * @param seconds 过期时间,单位秒
     * @return
     */
    boolean expire(final String key, final int seconds);

    boolean expire(final String key, final long timeout, final TimeUnit timeUnit);

    /**
     * 设置缓存在某个时间点过期
     * 
     * @param key
     * @param date 时间点
     * @return
     */
    boolean expireAt(final String key, final Date date);

    /**
     * 值增加1,具有原子性
     * 
     * @param key
     * @return 增加后的值
     */
    Long incr(final String key);

    /**
     * 值增加指定大小,具有原子性
     * 
     * @param key
     * @param incrValue 要增加的值
     * @return 增加后的值
     */
    Long incr(final String key, final long incrValue);

    /**
     * 值增加指定大小(浮点型),具有原子性
     * 
     * @param key
     * @param incrValue
     * @return
     */
    Double incrByFloat(final String key, final double incrValue);

    /**
     * 持久化一个缓存
     * 
     * @param key
     * @return
     */
    boolean persist(final String key);

    /**
     * 获取哈希集中的指定key条目值
     * 
     * @param key 哈希集key
     * @param fieldKey 要获取的条目key
     * @return
     */
    <T> T hget(final String key, final String fieldKey);

    /**
     * 获取哈希集中多个条目值
     * 
     * @param key
     * @param fieldKeys
     * @return
     */
    <T> List<T> hmultiGet(final String key, Collection<String> fieldKeys);

    /**
     * 删除哈希集中的条目
     * 
     * @param key 哈希集key
     * @param fieldKey 删除项的key
     */
    long hdelete(final String key, final String fieldKey);

    /**
     * 新增项到哈希集中
     * 
     * @param key 哈希集key
     * @param fieldKey 新条目的key
     * @param value 新条目的值
     */
    void hput(final String key, final String fieldKey, final Object value);

    /**
     * 新增项到哈希集中,同时设置该哈希的过期时间
     * 
     * @param key 哈希集key
     * @param fieldKey 新条目的key
     * @param value 新条目的值
     * @param expireInSecond 过期时间,单位秒
     */
    void hput(final String key, final String fieldKey, final Object value, int expireInSecond);

    /**
     * 新增项到哈希集中,仅在原集合中不存在相同key的项才新增
     * 
     * @param key 哈希集key
     * @param fieldKey 成员key
     * @param value 成员
     * @return 如果不存在成员且成功新增了返回true
     */
    boolean hputIfAbsent(final String key, final String fieldKey, Object value);

    /**
     * 将map所有项新增到哈希集,map的key和value作为哈希项中的key和value
     * 
     * @param key 哈希集key
     * @param map
     */
    void hputAll(final String key, final Map<String,?> map);

    /* default <T,R> void hputAll(final String key, final Collection<T> list, Function<T,String> mapper,
            int expireInSecond) {
        final Map<String,T> map = MapHelper.toMap(list, mapper);
        hputAll(key, map, expireInSecond);
    }*/

    /**
     * 获取哈希集中所有的值
     * 
     * @param key 哈希集key
     * @return
     */
    <T> List<T> hvalues(final String key);

    /**
     * 获取哈希集中所有的键
     * 
     * @param key 哈希集key
     * @return
     */
    Set<String> hkeys(final String key);

    /**
     * 判断哈希集中是否存在指定key的条目
     * 
     * @param key 哈希集key
     * @param fieldKey 条目key
     * @return
     */
    boolean hexists(final String key, final String fieldKey);

    /**
     * 获取哈希集的元素个数.如果key不存在返回0
     * 
     * @param key 哈希集key
     * @return
     */
    long hsize(final String key);

    /**
     * 新增元素到set无序集合.
     * 
     * @param values 要新增的值
     * @return 返回成功新增的个数不包括已经存在的值
     */
    long sadd(final String key, Object... values);

    /**
     * 取一个集合与给定多个集合的差集的元素,返回第一个集合中不存在于其他集合的元素
     * 
     * @param key 第一个集合key,不存在的 key被视为空集
     * @param keys 要比对的集合
     * @return 返回第一个集合key中不存在于其他集合的元素
     */
    <T> Set<T> sdiff(String key, Set<String> keys);

    /**
     * 取两个集合的差集的元素,返回第一个集合中不存在另一集合的元素
     * 
     * @param key
     * @param otherKey
     * @return 返回第一个集合key中不存在另一集合otherKey的元素
     */
    <T> Set<T> sdiff(String key, String otherKey);

    /**
     * 取两个集合的差集的元素,并存储到指定的集合中,如果目标集合存在则覆盖
     * 
     * @param key
     * @param otherKey
     * @param destKey 存储结果的目标集合key
     */
    void sdiffAndStore(String key, String otherKey, String destKey);

    /**
     * 求两个集合的交集,如果其中一个集合为空结果为空
     * 
     * @param key
     * @param otherKey
     * @return 交集元素
     */
    <T> Set<T> sintersect(final String key, final String otherKey);

    /**
     * 求多个集合的交集,如果其中一个集合为空结果为空
     * 
     * @param keys
     * @return 交集元素
     */
    <T> Set<T> sintersect(Set<String> keys);

    /**
     * 求集合的交集并将结果存储到指定集合中
     * 
     * @param keys
     * @param destKey 存储结果的集合key
     */
    void sintersectAndStore(final Set<String> keys, final String destKey);

    /**
     * 集合并集
     * 
     * @param key
     * @param otherKey
     * @return
     */
    <T> Set<T> sunion(final String key, final String otherKey);

    /**
     * 求集合并集并将结果存储到指定的集合中
     * 
     * @param key
     * @param otherKey
     * @param destKey
     */
    void sunionAndStore(final String key, final String otherKey, final String destKey);

    /**
     * 返回set集合所有成员
     * 
     * @param key set key
     * @return
     */
    <T> Set<T> smember(String key);

    /**
     * 移动一个值到目标集合中.
     * 
     * @param key 源集合key
     * @param destKey 目标集合key
     * @param value 要移动的值
     * @return 成功移除返回true,不存在于源集合中返回false
     */
    boolean smove(String key, String destKey, Object value);

    /**
     * 从集合中删除并返回一个随机元素
     * 
     * @param key
     * @return
     */
    <T> T spop(String key);

    /**
     * 从集合中随机返回指定个数的元素.
     * <p>
     * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count大于等于集合基数，那么返回整个集合; <br>
     * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值;
     * 
     * @param key
     * @param count 要返回的无数个数
     * @return
     */
    <T> List<T> srandomMembers(String key, int count);

    /**
     * 从集合中删除一个或多个元素
     * 
     * @param key set key
     * @param members 要删除的元素
     * @return 返回成功删除的个数
     */
    long sremove(String key, Object... members);

    /**
     * 获取set集元素个数.如果不存在返回0
     * 
     * @param key
     * @return
     */
    long ssize(String key);

    /**
     * 新增元素到有序集sortedSet.如果已经存在则更新score
     * 
     * @param key 集合key
     * @param mermber 新增值
     * @param score 排序值
     * @return 返回被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员
     */
    boolean zsadd(String key, Object mermber, double score);

    /**
     * 新增多个元素到有序集
     * 
     * @param key zset key
     * @param memberScores 成员为key,分数为value的map
     * @return
     */
    long zsadd(String key, Map<? extends Serializable,Double> memberScores);

    /**
     * 获取有序集的元素个数
     * 
     * @param key
     * @return
     */
    long zssize(String key);

    /**
     * 返回有序集key中， score值在 min和 max之间(包括等于 min或 max)的成员的数量
     * 
     * @param key 有序集key
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    long zscount(String key, double min, double max);

    /**
     * 为有序集成员 member的 score值加上增量 incrScore,incrScore可以是负数.<br>
     * 当 key不存在，或 member不是 key的成员时相当于新增
     * 
     * @param key 有序集key
     * @param member 成员
     * @param incrScore score增量
     * @return member成员的新score值
     */
    Double zsincrBy(String key, Object member, double incrScore);

    /**
     * 返回有序集成员的分值score
     * 
     * @param key 有序集key
     * @param member 成员
     * @return score,如果不存在返回null
     */
    Double zscore(String key, Object member);

    /**
     * 返回有序集中，指定区间内的成员,其中成员的位置按 score值递增(从小到大)来排序.
     * <p>
     * 下标参数都以0起始,你可以使用负数下标，以-1表示最后一个成员,超出范围的下标并不会引起错误。比如说，<br>
     * 当startIndex大于最大下标，或是 startIndex&gt;endIndex 时，只是是返回一个空列表。<br>
     * 当endIndex大于最大下标时,取值只到最大下标
     * 
     * @param key 有序集key
     * @param startIndex 开始下标
     * @param endIndex 结束下标
     * @return
     */
    <T> Set<T> zsrange(String key, long startIndex, long endIndex);

    /**
     * 返回有序集中，指定区间内的成员和成员score,其中成员的位置按 score值递增(从小到大)来排序.
     * 
     * @param key 有序集key
     * @param startIndex 开始下标
     * @param endIndex 结束下标
     * @return
     */
    <T> Map<T,Double> zsrangeWithScores(String key, long startIndex, long endIndex);

    /**
     * 返回有序集中，所有 score值介于 min和 max之间(包括等 min或 max)的成员,有序集成员按 score值递增(从小到大)次序排列。
     * 
     * @param key sortedset key
     * @param min 最小分数
     * @param max 最大分数
     * @return
     */
    <T> Set<T> zsrangeByScore(String key, double min, double max);

    /**
     * 从有序集中删除一个或多个元素
     * 
     * @param key sortedset key
     * @param members 要删除的元素
     * @return 返回成功删除的个数
     */
    long zsremove(String key, Object... members);

    /**
     * 按给定的索引删除有序集中的元素
     * 
     * @param key sortedset key
     * @param start
     * @param end
     */
    long zsremove(String key, long start, long end);

    long zsremoveByScore(String key, double minScore, double maxScore);

    /**
     * 将一个或多个值 插入到列表的表头,如果列表不存在则新建
     * 
     * @param key
     * @param values
     * @return 返回操作后列表的长度
     */
    long lpush(String key, Object... values);

    /**
     * 将值插入到列表的表头,如果key对应列表不存在操作被忽略
     * 
     * @param key
     * @param value
     * @return 返回操作后列表的长度
     */
    long lpushIfAbsent(String key, Object value);

    /**
     * 将一个或多个值 插入到列表的表尾
     * 
     * @param key
     * @param values
     * @return 返回操作后表的长度
     */
    long rpush(String key, Object... values);

    /**
     * 将值插入到列表的表尾,如果列表不存在操作被忽略
     * 
     * @param key
     * @param value
     * @return 返回操作后列表的长度
     */
    long rpushIfAbsent(String key, Object value);

    /**
     * 将值value插入到列表当中，位于值 pivot之前.
     * 
     * @param key
     * @param value
     * @param pivot
     * @return 返回操作后列表的长度
     */
    long linsert(String key, Object value, Object pivot);

    /**
     * 移除并返回列表的头元素
     * 
     * @param key
     * @return
     */
    <T> T lpop(String key);

    /**
     * 移除并返回列表的头元素(阻塞式),当给定列表内没有任何元素可供弹出的时候，连接将被阻塞，直到等待超时或发现可弹出元素为止.
     * 
     * @param key
     * @param timeout 阻塞时间,单位秒
     * @return
     */
    <T> T lBlockPop(String key, int timeout);

    /**
     * 移除并返回列表的尾元素
     * 
     * @param key
     * @return
     */
    <T> T rpop(String key);

    /**
     * 移除并返回列表的尾元素(阻塞式),当给定列表内没有任何元素可供弹出的时候，连接将被阻塞，直到等待超时或发现可弹出元素为止.
     * 
     * @param key
     * @param timeout 阻塞时间,单位秒
     * @return
     */
    <T> T rBlockPop(String key, int timeout);

    /**
     * 返回列表的长度
     * 
     * @param key
     * @return
     */
    long lsize(String key);

    /**
     * 返回列表中指定区间内的元素，区间以偏移量 start和 end指定
     * 
     * @param key
     * @param start
     * @param end
     * @return
     */
    <T> List<T> lrange(String key, long start, long end);

    /**
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除
     * 
     * @param key
     * @param start
     * @param end
     */
    void ltrim(String key, long start, long end);

    /**
     * 返回列表 key 中，下标为 index的元素
     * 
     * @param key
     * @param index
     * @return
     */
    <T> T lindex(String key, long index);

    /**
     * 将列表下标为 index的元素的值设置为 value
     * 
     * @param key
     * @param index
     * @param value
     */
    void lset(String key, long index, Object value);

    /**
     * 移除列表中与参数 value相等的所有元素
     * 
     * @param key 列表key
     * @param value 要删除的元素
     * @return
     */
    Long lremove(String key, Object value);

    /**
     * 根据参数 count的值，移除列表中与参数 value相等的元素.
     * <p>
     * count 的值可以是以下几种：
     * <ul>
     * <li>count &gt; 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。</li>
     * <li>count &lt; 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。</li>
     * <li>count = 0 : 移除表中所有与 value 相等的值。</li>
     * </ul>
     * 
     * @param key 列表key
     * @param count 要删除的元素个数
     * @param value 要删除的元素
     * @return 被移除元素的数量,所以当 key不存在时返回0
     */
    Long lremove(String key, long count, Object value);

    /**
     * 查找所有符合给定模式 pattern的 key.
     * <p>
     * pattern示例:<br>
     * KEYS * 匹配数据库中所有 key;<br>
     * ?匹配单个字符,如KEYS h?llo 匹配 hello, hallo和 hxllo等;<br>
     * *匹配0可多个字符,如KEYS h*llo 匹配 hllo和 heeeeello等;<br>
     * []匹配符号内的任意一个字符,如KEYS h[ae]llo 匹配hello和 hallo，但不匹配 hillo;<br>
     * 特殊符号用 \ 隔开
     * 
     * @param pattern
     * @return
     */
    Set<String> keys(String pattern);

    /**
     * 运行lua脚本命令,eval命令.结果返回使用默认的JSON转换器,返回脚本返回的数据应该是可识别的json格式,或者使用
     * {@link #runScript(String, List, List, Class, IRedisSerializer)}
     * 
     * @param luaScript lua脚本
     * @param keys 脚本中的key
     * @param args 脚本中的参数
     * @param resultClazz 返回结果类型
     */
    public <T> T runScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz);

    /**
     * 运行lua脚本命令(eval).结果可使用自定义序列化类转换
     * 
     * @param luaScript lua脚本
     * @param keys 脚本中的key
     * @param args 脚本中的参数
     * @param resultClazz 返回结果类型
     * @param resultSerializer 结果反序列化类
     * @return
     */
    /*public <T> T runScript(final String luaScript, List<String> keys, List<? extends Serializable> args,
            Class<T> resultClazz, RedisSerializer<T> resultSerializer);
    */
    // void flushDB();

    void publish(String channel, String message);

    public byte[] rawKey(String key);

    public byte[] rawValue(Object value);
}
