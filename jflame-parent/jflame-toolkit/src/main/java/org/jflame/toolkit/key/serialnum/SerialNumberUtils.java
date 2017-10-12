package org.jflame.toolkit.key.serialnum;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.jflame.toolkit.util.DateHelper;

/**
 * 常用编号生成工具
 * 
 * @author yucan.zhang
 */
public final class SerialNumberUtils {

    private static ConcurrentMap<String,TimeUnitIncreaseNum> minuteSeqMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<String,TimeUnitIncreaseNum> daySeqMap = new ConcurrentHashMap<>();

    /**
     * 编号生成,时间戳+随机数字
     * 
     * @param randomCount 随机数字个数
     * @return
     */
    public static String millisAndRandomNo(int randomCount) {
        return System.currentTimeMillis() + RandomStringUtils.randomNumeric(randomCount);
    }

    /**
     * 编号生成,时间戳(yyMMddHHmmssSSS)+随机数字
     * 
     * @param randomCount 随机数字个数
     * @return
     */
    public static String timeAndRandomNo(int randomCount) {
        return DateHelper.formatNow("yyMMddHHmmssSSS") + RandomStringUtils.randomNumeric(randomCount);
    }

    /**
     * 编号生成,规则: 业务标识+时间(yyMMddHHmm)+随机数(1位)+1分钟内流水号+随机数(2位)
     * 
     * @param prefix 业务标识,区分业务场景,相同标识使用同一流水号生成实例,分布式场景建议同时加入机器id
     * @return
     */
    public static String minuteAndSequence(String prefix) {
        TimeUnitIncreaseNum tin = getMinuteSequencer(prefix);
        return prefix + DateHelper.formatNow("yyMMddHHmm") + RandomStringUtils.randomNumeric(1) + tin.nextNum()
                + RandomStringUtils.randomNumeric(2);
    }

    /**
     * 编号生成,规则: 业务标识+时间(yyMMdd)+随机数(1位)+1天内流水号+随机数(2位)
     * 
     * @param prefix 业务标识,区分业务场景,相同标识使用同一流水号生成实例,分布式场景建议同时加入机器id
     * @return
     */
    public static String dayAndSequence(String prefix) {
        TimeUnitIncreaseNum tin = getDaySequencer(prefix);
        return prefix + DateHelper.formatNow("yyMMdd") + RandomStringUtils.randomNumeric(1) + tin.nextNum()
                + RandomStringUtils.randomNumeric(2);
    }

    private static TimeUnitIncreaseNum getMinuteSequencer(String prefix) {
        TimeUnitIncreaseNum tin;
        if (minuteSeqMap.containsKey(prefix)) {
            tin = minuteSeqMap.get(prefix);
        } else {
            tin = new TimeUnitIncreaseNum(TimeUnit.MINUTES);
            minuteSeqMap.put(prefix, tin);
        }
        return tin;
    }

    private static TimeUnitIncreaseNum getDaySequencer(String prefix) {
        TimeUnitIncreaseNum tin;
        if (daySeqMap.containsKey(prefix)) {
            tin = daySeqMap.get(prefix);
        } else {
            tin = new TimeUnitIncreaseNum(TimeUnit.DAYS);
            daySeqMap.put(prefix, tin);
        }
        return tin;
    }

    // public static void main(String[] args) {
    // for (int i = 0; i < 100; i++) {
    // System.out.println(dayAndSequence("1"));
    // try {
    // Thread.sleep(500);
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // }
    // }
}
