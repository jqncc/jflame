package org.jflame.toolkit.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jflame.toolkit.cache.serialize.IGenericRedisSerializer;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;

import redis.clients.util.SafeEncoder;

public interface RedisClient {

    byte[] nxBytes = CharsetHelper.getUtf8Bytes("NX");
    byte[] xxBytes = CharsetHelper.getUtf8Bytes("XX");
    byte[] exBytes = CharsetHelper.getUtf8Bytes("EX");
    byte[] pxBytes = CharsetHelper.getUtf8Bytes("PX");

    String ok = "OK";

    /**
     * 对象序列化为byte[]
     * 
     * @param obj
     * @return
     */
    default byte[] toBytes(Object obj) {
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        return getSerializer().serialize(obj);
    }

    /**
     * 集合序列化为List&lt;byte[]&gt;
     * 
     * @param set
     * @return
     */
    default List<byte[]> toBytes(Collection<?> set) {
        List<byte[]> keyBytes = null;
        if (CollectionHelper.isNotEmpty(set)) {
            keyBytes = new ArrayList<>(set.size());
            for (Object k : set) {
                keyBytes.add(toBytes(k));
            }
        }
        return keyBytes != null ? keyBytes : Collections.emptyList();
    }

    /**
     * 数组序列化为byte[][]
     * 
     * @param array
     * @return
     */
    default <T> byte[][] toBytes(T[] array) {
        byte[][] bytes = new byte[array.length][];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = toBytes(array[i]);
        }
        return bytes;
    }

    /**
     * map对象键和值序列化
     * 
     * @param map
     * @return Map&lt;byte[],byte[]&gt;
     */
    default <T> Map<byte[],byte[]> toBytes(Map<? extends Serializable,? extends Serializable> map) {
        if (map == null) {
            return null;
        }
        Map<byte[],byte[]> byteMap = new HashMap<>(map.size());
        map.forEach((k, v) -> {
            byteMap.put(toBytes(k), toBytes(v));
        });
        return byteMap;
    }

    /**
     * 集合序列化为二维数组 byte[][]
     * 
     * @param set
     * @return byte[][]
     */
    default <T> byte[][] toByteArray(Collection<?> set) {
        if (CollectionHelper.isEmpty(set)) {
            return null;
        } else {
            byte[][] bytes = new byte[set.size()][];
            int i = 0;
            for (Object obj : set) {
                bytes[i] = toBytes(obj);
                i++;
            }
            return bytes;
        }

    }

    /**
     * 将map的键和值序列化为二维数组,按key0,value0,key1,value1...存放
     * 
     * @param pair
     * @return
     */
    default byte[][] toKeysValues(Map<? extends Serializable,? extends Serializable> pair) {
        byte[][] kvBytes = new byte[pair.size() * 2][];
        int i = 0;
        for (Map.Entry<? extends Serializable,? extends Serializable> kv : pair.entrySet()) {
            kvBytes[i++] = toBytes(kv.getKey());
            kvBytes[i++] = toBytes(kv.getValue());
        }
        return kvBytes;
    }

    @SuppressWarnings("unchecked")
    default <T extends Serializable> T fromBytes(byte[] valueBytes) {
        if (valueBytes != null) {
            return (T) getSerializer().deserialize(valueBytes);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    default <T extends Serializable> Set<T> fromBytes(Set<byte[]> bytes) {
        if (bytes == null) {
            return null;
        }
        Set<T> set = new HashSet<>(bytes.size());
        for (byte[] bs : bytes) {
            set.add((T) getSerializer().deserialize(bs));
        }
        return set;
    }

    @SuppressWarnings("unchecked")
    default <T extends Serializable> List<T> fromBytes(Collection<byte[]> valueBytes) {
        if (CollectionHelper.isNotEmpty(valueBytes)) {
            List<T> values = new ArrayList<>();
            for (byte[] bs : valueBytes) {
                values.add((T) getSerializer().deserialize(bs));
            }
            return values;
        }
        return null;
    }

    @SuppressWarnings({ "unchecked","rawtypes" })
    default <T> T deserializeResult(Object result, Class<T> resultClazz) {
        if (result instanceof byte[]) {
            if (getSerializer() == null) {
                return (T) result;
            }
            return fromBytes((byte[]) result);
        }
        if (result instanceof List) {
            List results = new ArrayList();
            for (Object obj : (List) result) {
                results.add(deserializeResult(obj, resultClazz));
            }
            return (T) results;
        }
        return (T) result;
    }

    @SuppressWarnings("unchecked")
    default Object convertScriptResult(Object result, Class<?> javaType) {
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

    /**
     * 返回内部实际使用的redis命令执行者
     * 
     * @return
     */
    Object getNativeClient();

    IGenericRedisSerializer getSerializer();

    /**
     * 查看剩余过期时间,单位秒
     * 
     * @param key
     * @return
     */
    long ttl(Serializable key);

    /**
     * 设置缓存
     * 
     * @param key
     * @param value
     */
    void set(Serializable key, Serializable value);

    /**
     * 设置缓存和缓存时间
     * 
     * @param key
     * @param value
     * @param timeout 缓存时间
     * @param timeUnit 时间单位
     */
    void set(Serializable key, Serializable value, long timeout, TimeUnit timeUnit);

    /**
     * 一次设置多个缓存.MSET是原子的，所以所有给定的keys是一次性set的,客户端不可能看到这种一部分keys被更新而另外的没有改变的情况.返回总是OK，因为MSET不会失败
     * 
     * @param pair 缓存键和值的map
     */
    void multiSet(Map<? extends Serializable,? extends Serializable> pair);

    /**
     * 设置缓存,只有在键不存在时才设置
     * 
     * @param key
     * @param value
     * @return
     */
    boolean setIfAbsent(Serializable key, Serializable value);

    /**
     * 设置缓存和缓存时间,只有在键不存在时才设置
     * 
     * @param key
     * @param value
     * @param timeout
     * @param timeUnit
     * @return
     */
    boolean setIfAbsent(Serializable key, Serializable value, long timeout, TimeUnit timeUnit);

    /**
     * 一次设置多个缓存,只要有一个key已经存在，MSETNX一个操作都不会执行.其他特性与mset一致{@link #multiSet(Map)}
     * 
     * @param pair
     */
    void multiSetIfAbsent(Map<? extends Serializable,? extends Serializable> pair);

    /**
     * 获取缓存值,结果反序列化为clazz指定的类型
     * 
     * @param key
     * @return
     */
    <T extends Serializable> T get(Serializable key);

    /**
     * 设置一个新值,并返回旧值,结果反序列化为clazz指定的类型
     * 
     * @param key
     * @param newValue
     * @return
     */
    <T extends Serializable> T getAndSet(Serializable key, T newValue);

    /**
     * 一次获取多个值
     * 
     * @param keys
     * @return
     */
    <T extends Serializable> List<T> multiGet(Collection<? extends Serializable> keys);

    /**
     * 删除一个缓存
     * 
     * @param key 要删除的缓存键
     * @return
     */
    boolean delete(Serializable key);

    /**
     * 删除多个缓存
     * 
     * @param keys 要删除的缓存键集合
     * @return 返回成功删除的数量
     */
    long delete(Collection<? extends Serializable> keys);

    /**
     * 判断缓存是否存在
     * 
     * @param key
     * @return
     */
    boolean exists(Serializable key);

    /**
     * 设置缓存过期时间
     * 
     * @param key
     * @param seconds 过期时间,单位秒
     * @return
     */
    boolean expire(Serializable key, int seconds);

    boolean expire(Serializable key, long timeout, TimeUnit timeUnit);

    /**
     * 设置缓存在某个时间点过期
     * 
     * @param key
     * @param date 时间点
     * @return
     */
    boolean expireAt(Serializable key, Date date);

    /**
     * 值增加1,具有原子性
     * 
     * @param key
     * @return 增加后的值
     */
    Long incr(Serializable key);

    /**
     * 值增加指定大小,具有原子性
     * 
     * @param key
     * @param incrValue 要增加的值
     * @return 增加后的值
     */
    Long incr(Serializable key, long incrValue);

    /**
     * 值增加指定大小(浮点型),具有原子性
     * 
     * @param key
     * @param incrValue
     * @return
     */
    Double incrByFloat(Serializable key, double incrValue);

    /**
     * 持久化一个缓存
     * 
     * @param key
     * @return
     */
    boolean persist(Serializable key);

    /**
     * 获取哈希集中的指定key条目值
     * 
     * @param key 哈希集key
     * @param fieldKey 要获取的条目key
     * @return
     */
    <T extends Serializable> T hget(Serializable key, Serializable fieldKey);

    /**
     * 获取哈希集中多个条目值
     * 
     * @param key
     * @param fieldKeys
     * @return
     */
    <T extends Serializable> List<T> hmultiGet(Serializable key, Collection<? extends Serializable> fieldKeys);

    /**
     * 删除哈希集中的条目
     * 
     * @param key 哈希集key
     * @param fieldKey 删除项的key
     */
    long hdelete(Serializable key, Object fieldKey);

    /**
     * 新增项到哈希集中
     * 
     * @param key 哈希集key
     * @param fieldKey 新条目的key
     * @param value 新条目的值
     */
    void hput(Serializable key, Serializable fieldKey, Serializable value);

    /**
     * 新增项到哈希集中,同时设置该hash的过期时间
     * 
     * @param key 哈希集key
     * @param fieldKey 新条目的key
     * @param value 新条目的值
     * @param expireInSecond 过期时间,单位秒
     */
    void hput(Serializable key, Serializable fieldKey, Serializable value, int expireInSecond);

    /**
     * 新增项到哈希集中,仅在原集合中不存在相同key的项才新增
     * 
     * @param key 哈希集key
     * @param fieldKey
     * @param value
     */
    boolean hputIfAbsent(Serializable key, Serializable fieldKey, Serializable value);

    /**
     * 将map所有项新增到哈希集,map的key和value作为哈希项中的key和value
     * 
     * @param key 哈希集key
     * @param map
     */
    void hputAll(Serializable key, Map<? extends Serializable,? extends Serializable> map);

    /**
     * 获取哈希集中所有的值
     * 
     * @param key 哈希集key
     * @return
     */
    <T extends Serializable> List<T> hvalues(Serializable key);

    /**
     * 获取哈希集中所有的键
     * 
     * @param key 哈希集key
     * @return
     */
    <T extends Serializable> Set<T> hkeys(Serializable key);

    /**
     * 判断哈希集中是否存在指定key的条目
     * 
     * @param key 哈希集key
     * @param fieldKey 条目key
     * @return
     */
    boolean hexists(Serializable key, Object fieldKey);

    /**
     * 获取哈希集的元素个数.如果key不存在返回0
     * 
     * @param key 哈希集key
     * @return
     */
    long hsize(Serializable key);

    /**
     * 新增元素到set无序集合.
     * 
     * @param values 要新增的值
     * @return 返回成功新增的个数不包括已经存在的值
     */
    long sadd(Serializable key, Serializable... values);

    /**
     * 取一个集合与给定多个集合的差集的元素,返回第一个集合中不存在于其他集合的元素
     * 
     * @param key 第一个集合key,不存在的 key被视为空集
     * @param keys 要比对的集合
     * @return 返回第一个集合key中不存在于其他集合的元素
     */
    <T extends Serializable> Set<T> sdiff(Serializable key, Collection<? extends Serializable> keys);

    /**
     * 取两个集合的差集的元素,返回第一个集合中不存在另一集合的元素
     * 
     * @param key
     * @param otherKey
     * @return 返回第一个集合key中不存在另一集合otherKey的元素
     */
    <T extends Serializable> Set<T> sdiff(Serializable key, Serializable otherKey);

    /**
     * 取两个集合的差集的元素,并存储到指定的集合中,如果目标集合存在则覆盖
     * 
     * @param key
     * @param otherKey
     * @param destKey 存储结果的目标集合key
     */
    void sdiffAndStore(Serializable key, Serializable otherKey, Serializable destKey);

    /**
     * 求两个集合的交集,如果其中一个集合为空结果为空
     * 
     * @param key
     * @param otherKey
     * @return 交集元素
     */
    <T extends Serializable> Set<T> sintersect(Serializable key, Serializable otherKey);

    /**
     * 求多个集合的交集,如果其中一个集合为空结果为空
     * 
     * @param keys
     * @return 交集元素
     */
    <T extends Serializable> Set<T> sintersect(List<? extends Serializable> keys);

    /**
     * 求集合的交集并将结果存储到指定集合中
     * 
     * @param keys
     * @param destKey 存储结果的集合key
     */
    void sintersectAndStore(List<? extends Serializable> keys, Serializable destKey);

    /**
     * 集合并集
     * 
     * @param key
     * @param otherKey
     * @return
     */
    <T extends Serializable> Set<T> sunion(Serializable key, Serializable otherKey);

    /**
     * 求集合并集并将结果存储到指定的集合中
     * 
     * @param key
     * @param otherKey
     * @param destKey
     */
    void sunionAndStore(Serializable key, Serializable otherKey, Serializable destKey);

    /**
     * 返回set集合所有成员
     * 
     * @param key set key
     * @return
     */
    <T extends Serializable> Set<T> smember(Serializable key);

    /**
     * 移动一个值到目标集合中.
     * 
     * @param key 源集合key
     * @param destKey 目标集合key
     * @param value 要移动的值
     * @return 成功移除返回true,不存在于源集合中返回false
     */
    boolean smove(Serializable key, Serializable destKey, Serializable value);

    /**
     * 从集合中删除并返回一个随机元素
     * 
     * @param key
     * @return
     */
    <T extends Serializable> T spop(Serializable key);

    /**
     * 从集合中随机返回指定个数的元素.
     * <p>
     * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count大于等于集合基数，那么返回整个集合; <br>
     * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值;
     * 
     * @param key
     * @param count
     * @param T 返回的集合元素类型
     * @return
     */
    <T extends Serializable> List<T> srandomMembers(Serializable key, int count);

    /**
     * 从集合中删除一个或多个元素
     * 
     * @param key set key
     * @param members 要删除的元素
     * @return 返回成功删除的个数
     */
    long sremove(Serializable key, Object... members);

    /**
     * 获取set集元素个数.如果不存在返回0
     * 
     * @param key
     * @return
     */
    long ssize(Serializable key);

    /**
     * 新增元素到有序集sortedSet.如果已经存在则更新score
     * 
     * @param key 集合key
     * @param value 新增值
     * @param score 排序值
     * @return 返回被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员
     */
    boolean zsadd(Serializable key, Serializable value, double score);

    interface SortedSetTuple<V> extends Comparable<SortedSetTuple<V>> {

        V getValue();

        Double getScore();

        default public int compareTo(Double o) {
            Double d = (getScore() == null ? Double.valueOf(0.0d) : getScore());
            Double a = (o == null ? Double.valueOf(0.0d) : o);
            return d.compareTo(a);
        }
    }

    /**
     * 新增多个元素到有序集
     * 
     * @param key
     * @param memberScores 成员为key分数为value的map
     * @return
     */
    long zsadd(Serializable key, Map<? extends Serializable,Double> memberScores);

    /**
     * 获取有序集的元素个数
     * 
     * @param key
     * @return
     */
    long zssize(Serializable key);

    /**
     * 返回有序集key中， score值在 min和 max之间(包括等于 min或 max)的成员的数量
     * 
     * @param key 有序集key
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    long zscount(Serializable key, double min, double max);

    /**
     * 为有序集key的成员 member的 score值加上增量 incrScore,incrScore可以是负数.<br>
     * 当 key不存在，或 member不是 key的成员时相当于新增
     * 
     * @param key 有序集key
     * @param member 成员
     * @param incrScore score增量
     * @return member成员的新score值
     */
    Double zsincrScore(Serializable key, Serializable member, double incrScore);

    /**
     * 返回有序集 中，指定区间内的成员,其中成员的位置按 score值递增(从小到大)来排序.
     * <p>
     * 下标参数都以0起始,你可以使用负数下标，以-1表示最后一个成员,超出范围的下标并不会引起错误。比如说，<br>
     * 当startIndex大于最大下标，或是 startIndex&gt;endIndex 时，只是是返回一个空列表。<br>
     * 当endIndex大于最大下标时,取值只到最大下标
     * 
     * @param startIndex 开始下标
     * @param endIndex 结束下标
     * @return
     */
    <T extends Serializable> Set<T> zsrange(Serializable key, long startIndex, long endIndex);

    /**
     * 返回有序集中，所有 score值介于 min和 max之间(包括等 min或 max)的成员,有序集成员按 score值递增(从小到大)次序排列。
     * 
     * @param key sortedset key
     * @param min 最小分数
     * @param max 最大分数
     * @return
     */
    <T extends Serializable> Set<T> zsrangeByScore(Serializable key, double min, double max);

    /**
     * 从有序集中删除一个或多个元素
     * 
     * @param key sortedset key
     * @param members 要删除的元素
     * @return 返回成功删除的个数
     */
    long zsremove(Serializable key, Object... members);

    /**
     * 按给定的索引删除有序集中的元素
     * 
     * @param key sortedset key
     * @param start
     * @param end
     */
    long zsremove(Serializable key, long start, long end);

    long zsremoveByScore(Serializable key, double minScore, double maxScore);

    /**
     * 将一个或多个值 插入到列表的表头,如果列表不存在则新建
     * 
     * @param key
     * @param values
     * @return 返回操作后列表的长度
     */
    long lpush(Serializable key, Serializable... values);

    /**
     * 将值插入到列表的表头,如果key对应列表不存在操作被忽略
     * 
     * @param key
     * @param value
     * @return 返回操作后列表的长度
     */
    long lpushIfAbsent(Serializable key, Serializable value);

    /**
     * 将一个或多个值 插入到列表的表尾
     * 
     * @param key
     * @param values
     * @return 返回操作后表的长度
     */
    long rpush(Serializable key, Serializable... values);

    /**
     * 将值插入到列表的表尾,如果列表不存在操作被忽略
     * 
     * @param key
     * @param value
     * @return 返回操作后列表的长度
     */
    long rpushIfAbsent(Serializable key, Serializable value);

    /**
     * 将值value插入到列表当中，位于值 pivot之前.
     * 
     * @param key
     * @param value
     * @param pivot
     * @return 返回操作后列表的长度
     */
    long linsert(Serializable key, Serializable value, Serializable pivot);

    /**
     * 移除并返回列表的头元素
     * 
     * @param key
     * @return
     */
    <T extends Serializable> T lpop(Serializable key);

    /**
     * 移除并返回列表的头元素(阻塞式),当给定列表内没有任何元素可供弹出的时候，连接将被阻塞，直到等待超时或发现可弹出元素为止.
     * 
     * @param key
     * @param timeout 阻塞时间,单位秒
     * @return
     */
    <T extends Serializable> T lBlockPop(Serializable key, int timeout);

    /**
     * 移除并返回列表的尾元素
     * 
     * @param key
     * @return
     */
    <T extends Serializable> T rpop(Serializable key);

    /**
     * 移除并返回列表的尾元素(阻塞式),当给定列表内没有任何元素可供弹出的时候，连接将被阻塞，直到等待超时或发现可弹出元素为止.
     * 
     * @param key
     * @param timeout 阻塞时间,单位秒
     * @return
     */
    <T extends Serializable> T rBlockPop(Serializable key, int timeout);

    /**
     * 返回列表的长度
     * 
     * @param key
     * @return
     */
    long lsize(Serializable key);

    /**
     * 返回列表中指定区间内的元素，区间以偏移量 start和 end指定
     * 
     * @param key
     * @param start
     * @param end
     * @return
     */
    <T extends Serializable> List<T> lrange(Serializable key, long start, long end);

    /**
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除
     * 
     * @param key
     * @param start
     * @param end
     */
    void ltrim(Serializable key, long start, long end);

    /**
     * 返回列表 key 中，下标为 index的元素
     * 
     * @param key
     * @param index
     * @return
     */
    <T extends Serializable> T lindex(Serializable key, long index);

    /**
     * 将列表下标为 index的元素的值设置为 value
     * 
     * @param key
     * @param index
     * @param value
     */
    void lset(Serializable key, long index, Serializable value);

    /**
     * 移除列表中与参数 value相等的所有元素
     * 
     * @param key 列表key
     * @param value 要删除的元素
     * @return
     */
    Long lremove(Serializable key, Serializable value);

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
    Long lremove(Serializable key, long count, Serializable value);

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
    Set<? extends Serializable> keys(String pattern);

    /**
     * 运行lua脚本命令.eval命令
     * 
     * @param luaScript lua脚本
     * @param keys 脚本中的key
     * @param args 脚本中的参数
     * @param resultClazz 返回结果类型
     */
    public <T> T runScript(final String luaScript, List<? extends Serializable> keys, List<? extends Serializable> args,
            Class<T> resultClazz);

    /**
     * 运行lua脚本命令,缓存脚本在redis.evalsha命令
     * 
     * @param luaScript 脚本
     * @param keys 脚本中的key
     * @param args 脚本中的参数
     * @return
     */
    <T> T runSHAScript(String luaScript, List<? extends Serializable> keys, List<? extends Serializable> args,
            Class<T> resultClazz);

}
