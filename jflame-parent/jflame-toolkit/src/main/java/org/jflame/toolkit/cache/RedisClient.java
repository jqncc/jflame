package org.jflame.toolkit.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;

public interface RedisClient {

    byte[] nxBytes = CharsetHelper.getUtf8Bytes("NX");
    byte[] xxBytes = CharsetHelper.getUtf8Bytes("XX");
    byte[] exBytes = CharsetHelper.getUtf8Bytes("EX");
    byte[] pxBytes = CharsetHelper.getUtf8Bytes("PX");

    default byte[] toBytes(Object obj) {
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        return getSerializer().serialize(obj);
    }

    /*
    default Collection<byte[]> toBytes(Collection<?> set) {
        Collection<byte[]> keyBytes = null;
        if (CollectionHelper.isNotEmpty(set)) {
            keyBytes = new ArrayList<>(set.size());
            for (Object k : set) {
                keyBytes.add(toBytes(k));
            }
        }
        return keyBytes != null ? keyBytes : Collections.emptyList();
    }*/

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

    default <T> byte[][] toBytes(T[] array) {
        byte[][] bytes = new byte[array.length][];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = toBytes(array[i]);
        }
        return bytes;
    }

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

    @SuppressWarnings("unchecked")
    default <T extends Serializable> T fromBytes(byte[] valueBytes) {
        if (valueBytes != null) {
            return (T) getSerializer().deserialize(valueBytes);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    default <T extends Serializable> Set<T> fromBytes(Set<byte[]> bytes) {
        if (CollectionHelper.isEmpty(bytes)) {
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

    /**
     * 返回内部实际使用的redis命令执行者
     * 
     * @return
     */
    Object getNativeClient();

    void setSerializer(IGenericSerializer serializer);

    IGenericSerializer getSerializer();

    void set(Object key, Object value);

    void set(Object key, Object value, long timeout, TimeUnit timeUnit);

    boolean setIfAbsent(Object key, Object value);

    boolean setIfAbsent(Object key, Object value, long timeout, TimeUnit timeUnit);

    /**
     * 获取缓存值,结果反序列化为clazz指定的类型
     * 
     * @param key
     * @return
     */
    <T extends Serializable> T get(Object key);

    /**
     * 设置一个新值,并返回旧值,结果反序列化为clazz指定的类型
     * 
     * @param key
     * @param newValue
     * @return
     */
    <T extends Serializable> T getAndSet(Object key, T newValue);

    /**
     * 一次获取多个值
     * 
     * @param keys
     * @param clazz 元素类型
     * @return
     */
    <T extends Serializable> List<T> multiGet(Collection<?> keys);

    boolean delete(Object key);

    long delete(Collection<?> keys);

    boolean exists(Object key);

    /**
     * 设置缓存过期时间
     * 
     * @param key
     * @param seconds 过期时间,单位秒
     * @return
     */
    boolean expire(Object key, int seconds);

    /**
     * 设置缓存在某个时间点过期
     * 
     * @param key
     * @param date
     * @return
     */
    boolean expireAt(Object key, Date date);

    /**
     * 值增加1,具有原子性
     * 
     * @param key
     * @return 增加后的值
     */
    Long incr(Object key);

    /**
     * 值增加指定大小,具有原子性
     * 
     * @param key
     * @param incrValue 要增加的值
     * @return 增加后的值
     */
    Long incr(Object key, long incrValue);

    Double incrByFloat(Object key, double incrValue);

    /**
     * 持久化一个缓存
     * 
     * @param key
     * @return
     */
    boolean persist(Object key);

    /**
     * 获取哈希集中的指定key条目值
     * 
     * @param key 哈希集key
     * @param fieldKey 要获取的条目key
     * @param clazz 结果类型
     * @return
     */
    <T extends Serializable> T hget(Object key, Object fieldKey);

    <T extends Serializable> List<T> hmultiGet(Object key, Collection<?> fieldKeys);

    /**
     * 删除哈希集中的条目
     * 
     * @param key 哈希集key
     * @param fieldKey 删除项的key
     */
    void hdelete(Object key, Object fieldKey);

    /**
     * 新增项到哈希集中
     * 
     * @param key 哈希集key
     * @param fieldKey 新条目的key
     * @param value 新条目的值
     */
    void hput(Object key, Object fieldKey, Object value);

    /**
     * 新增项到哈希集中,仅在原集合中不存在相同key的项才新增
     * 
     * @param key 哈希集key
     * @param fieldKey
     * @param value
     */
    boolean hputIfAbsent(Object key, Object fieldKey, Object value);

    /**
     * 将map所有项新增到哈希集
     * 
     * @param key 哈希集key
     * @param map
     */
    void hputAll(Object key, Map<? extends Serializable,? extends Serializable> map);

    /**
     * 获取哈希集中所有的值
     * 
     * @param key 哈希集key
     * @param clazz 值类型
     * @return
     */
    <T extends Serializable> List<T> hvalues(Object key);

    /**
     * 获取哈希集中所有的键
     * 
     * @param key 哈希集key
     * @param filedKeyClazz 键类型
     * @return
     */
    <T extends Serializable> Set<T> hkeys(Object key);

    /**
     * 判断哈希集中是否存在指定key的条目
     * 
     * @param key 哈希集key
     * @param fieldKey 条目key
     * @return
     */
    boolean hexists(Object key, Object fieldKey);

    /**
     * 获取哈希集的元素个数.如果key不存在返回0
     * 
     * @param key 哈希集key
     * @return
     */
    long hsize(Object key);

    /**
     * 新增元素到set无序集合.返回成功新增的个数不包括已经存在的值
     * 
     * @param value
     * @return
     */
    long sadd(Object key, Serializable... value);

    /**
     * 对比多个set集合,获取第一个集合中不存在于其他集合的元素
     * 
     * @param firstSetKey
     * @param keys
     * @return
     */
    <T extends Serializable> Set<T> sdiff(Object firstSetKey, Collection<?> keys);

    /**
     * 对比两个set集合,获取第一个集合中不存在于另一集合的元素
     * 
     * @param firstSetKey
     * @param key
     * @return
     */
    <T extends Serializable> Set<T> sdiff(Object firstSetKey, Object otherKey);

    /**
     * 对比两个set集合,获取第一个集合中不存在于另一集合的元素并存储到指定的集合中,如果目标集合存在则覆盖
     * 
     * @param firstSetKey
     * @param key
     * @param destKey 目标集合key
     */
    void sdiffAndStore(Object firstSetKey, Object key, Object destKey);

    /**
     * 求集合交集,如果其中一个集合为空结果为空
     * 
     * @param keys
     * @return
     */
    <T extends Serializable> Set<T> sintersect(Object key, Object otherKey);

    /**
     * 求集合的交集并将结果存储到指定集合中
     * 
     * @param keys
     * @param destKey
     */
    void sintersectAndStore(Object key, Object otherKey, Object destKey);

    /**
     * 集合并集
     * 
     * @param keys
     * @return
     */
    <T extends Serializable> Set<T> sunion(Object key, Object otherKey);

    /**
     * 求集合并集并将结果存储到指定的集合中
     * 
     * @param keys
     * @param destKey
     */
    void sunionAndStore(Object key, Object otherKey, Object destKey);

    /**
     * 返回set集合所有成员
     * 
     * @param key set key
     * @return
     */
    <T extends Serializable> Set<T> smember(Object key);

    /**
     * 移动一个值到目标集合中.
     * 
     * @param key 源集合key
     * @param destKey 目标集合key
     * @param value 要移动的值
     * @return 成功移除返回true,不存在于源集合中返回false
     */
    boolean smove(Object key, Object destKey, Serializable value);

    /**
     * 从集合中删除并返回一个随机元素
     * 
     * @param key
     * @return
     */
    <T extends Serializable> T spop(Object key);

    /**
     * 从集合中随机返回指定个数的元素.
     * <p>
     * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count大于等于集合基数，那么返回整个集合; <br>
     * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值;
     * 
     * @param key
     * @param count
     * @return
     */
    <T extends Serializable> List<T> randomMembers(Object key, int count);

    /**
     * 从集合中删除一个或多个元素
     * 
     * @param key set key
     * @param members 要删除的元素
     * @return 返回成功删除的个数
     */
    long sremove(Object key, Object... members);

    /**
     * 获取set集元素个数.如果不存在返回0
     * 
     * @param key
     * @return
     */
    long ssize(Object key);

    /**
     * 新增元素到有序集sortedSet.如果已经存在则更新score
     * 
     * @param key 集合key
     * @param value 新增值
     * @param score 排序值
     * @return 返回被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员
     */
    boolean zsadd(Object key, Serializable value, double score);

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
     * @param tuple
     * @return
     */
    long zsadd(Object key, Set<SortedSetTuple<Serializable>> tuple);

    /**
     * 获取有序集的元素个数
     * 
     * @param key
     * @return
     */
    long zssize(Object key);

    /**
     * 返回有序集key中， score值在 min和 max之间(包括等于 min或 max)的成员的数量
     * 
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    long zscount(Object key, double min, double max);

    /**
     * 为有序集key的成员 member的 score值加上增量 incrScore,incrScore可以是负数.<br>
     * 当 key不存在，或 member不是 key的成员时相当于新增
     * 
     * @param key 有序集key
     * @param member 成员
     * @param incrScore score增量
     * @return member成员的新score值
     */
    Double zsincrScore(Object key, Serializable member, double incrScore);

    /**
     * 返回有序集 中，指定区间内的成员,其中成员的位置按 score值递增(从小到大)来排序.
     * <p>
     * 下标参数都以0起始,你可以使用负数下标，以-1表示最后一个成员,超出范围的下标并不会引起错误。比如说，<br>
     * 当startIndex大于最大下标，或是 startIndex>endIndex 时，只是是返回一个空列表。<br>
     * 当endIndex大于最大下标时,取值只到最大下标
     * 
     * @param startIndex 开始下标
     * @param endIndex 结束下标
     * @return
     */
    <T extends Serializable> Set<T> zsrange(Object key, long startIndex, long endIndex);

    /**
     * 返回有序集中，所有 score值介于 min和 max之间(包括等 min或 max)的成员,有序集成员按 score值递增(从小到大)次序排列。
     * 
     * @param key
     * @param min
     * @param max
     * @return
     */
    <T extends Serializable> Set<T> zrangeByScore(Object key, double min, double max);

    /**
     * 从有序集中删除一个或多个元素
     * 
     * @param key set key
     * @param members 要删除的元素
     * @return 返回成功删除的个数
     */
    long zsremove(Object key, Object... members);

    /**
     * @param key
     * @param start
     * @param end
     */
    void removeRange(Object key, long start, long end);

    void removeRangeByScore(Object key, double minScore, double maxScore);

    /**
     * 将一个或多个值 插入到列表的表头,如果列表不存在则新建
     * 
     * @param key
     * @param values
     * @return 返回操作后列表的长度
     */
    long lpush(Object key, Object... values);

    /**
     * 将值插入到列表的表头,如果列表不存在操作被忽略
     * 
     * @param key
     * @param value
     * @return 返回操作后列表的长度
     */
    long lpushIfAbsent(Object key, Object value);

    /**
     * 将一个或多个值 插入到列表的表尾
     * 
     * @param key
     * @param values
     * @return 返回操作后表的长度
     */
    long rpush(Object key, Object... values);

    /**
     * 将值插入到列表的表尾,如果列表不存在操作被忽略
     * 
     * @param key
     * @param value
     * @return 返回操作后列表的长度
     */
    long rpushIfAbsent(Object key, Object value);

    /**
     * 将值value插入到列表当中，位于值 pivot之前.
     * 
     * @param key
     * @param value
     * @param pivot
     * @return 返回操作后列表的长度
     */
    long linsert(Object key, Object value, Object pivot);

    /**
     * 移除并返回列表的头元素
     * 
     * @param key
     * @return
     */
    <T extends Serializable> T lpop(Object key);

    /**
     * 移除并返回列表的头元素(阻塞式),当给定列表内没有任何元素可供弹出的时候，连接将被阻塞，直到等待超时或发现可弹出元素为止.
     * 
     * @param key
     * @param timeout
     * @param timeUnit
     * @return
     */
    <T extends Serializable> T lpop(Object key, long timeout, TimeUnit timeUnit);

    /**
     * 移除并返回列表的尾元素
     * 
     * @param key
     * @return
     */
    <T extends Serializable> T rpop(Object key);

    /**
     * 移除并返回列表的尾元素(阻塞式),当给定列表内没有任何元素可供弹出的时候，连接将被阻塞，直到等待超时或发现可弹出元素为止.
     * 
     * @param key
     * @param timeout
     * @param timeUnit
     * @return
     */
    <T extends Serializable> T rpop(Object key, long timeout, TimeUnit timeUnit);

    /**
     * 返回列表的长度
     * 
     * @param key
     * @return
     */
    long lsize(Object key);

    /**
     * 返回列表中指定区间内的元素，区间以偏移量 start和 end指定
     * 
     * @param key
     * @param start
     * @param end
     * @return
     */
    <T extends Serializable> List<T> lrange(Object key, long start, long end);

    /**
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除
     * 
     * @param key
     * @param start
     * @param end
     */
    void ltrim(Object key, long start, long end);

    /**
     * 返回列表 key 中，下标为 index的元素
     * 
     * @param key
     * @param index
     * @return
     */
    <T extends Serializable> T lindex(Object key, long index);

    /**
     * 将列表下标为 index的元素的值设置为 value
     * 
     * @param key
     * @param index
     * @param value
     */
    void lset(Object key, long index, Object value);

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
     * @param key
     * @param count
     * @param value
     * @return 被移除元素的数量,所以当 key不存在时返回0
     */
    Long lremove(Object key, long count, Object value);

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
    // Set<Object> keys(String pattern);

    /**
     * 运行一个无返回结果的lua脚本命令
     * 
     * @param luaScript 脚本
     * @param keys 脚本中的key
     * @param args 脚本中的参数
     */
    Object runScript(final String luaScript, List<Object> keys, List<Object> args);

}
