package org.jflame.toolkit.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jflame.toolkit.exception.ConvertException;

/**
 * 日期时间操作工具类.
 * 
 * @see org.apache.commons.lang3.time.DateFormatUtils
 * @see org.apache.commons.lang3.time.DateUtils
 * @author zyc
 */
public final class DateHelper {

    /**
     * 时间格式yyyy-MM-dd
     */
    public static final String DATE_YYYY_MM_DD = "yyyy-MM-dd";
    /**
     * 时间格式yyyy-MM-dd HH:mm:ss
     */
    public static final String DATE_YYYY_MM_DD_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    /**
     * 中文日期格式yyyy年MM月dd日
     */
    public static final String CN_DATE_YYYY_MM_DD = "yyyy年MM月dd日";
    /**
     * 中文时间格式HH时mm分ss秒
     */
    public static final String CN_TIME_HH_mm_ss = "HH时mm分ss秒";
    /**
     * 时间格式yyyyMMddHHmmss
     */
    public static final String DATE_yyyyMMddHHmmss = "yyyyMMddHHmmss";
    /**
     * 时间格式yyyyMMddHHmmssSSS
     */
    public static final String DATE_yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";

    /**
     * 格式化时间
     * 
     * @param date 时间
     * @param pattern 格式
     * @return
     */
    public static String format(Date date, String pattern) {
        return DateFormatUtils.format(date, pattern);
    }

    /**
     * 格式化当前时间
     * 
     * @param pattern 格式
     * @return 当前时间的格式化字符串
     */
    public static String formatNow(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }

    /**
     * 解析时间字符串转为时间对象
     * 
     * @param dateStr 时间字符串
     * @param patterns 格式
     * @return
     * @throws ConvertException 解析失败
     */
    public static Date parseDate(String dateStr, String... patterns) throws ConvertException {
        try {
            return DateUtils.parseDate(dateStr, patterns);
        } catch (ParseException e) {
            throw new ConvertException(dateStr + "转为时间失败", e);
        }
    }

    /**
     * 返回当前时间的全格式字符串，格式为:yyyy-MM-dd HH:mm:ss
     * 
     * @return
     */
    public static String fullNow() {
        return formatNow(DATE_YYYY_MM_DD_HH_mm_ss);
    }

    /**
     * 返回当前时间的短格式字符串，格式为:yyyy-MM-dd
     * 
     * @return
     */
    public static String shortNow() {
        return formatNow(DATE_YYYY_MM_DD);
    }

    /**
     * 返回java.sql.Timestamp类型的当前时间
     * 
     * @return java.sql.Timestamp
     */
    public static java.sql.Timestamp nowTimestamp() {
        Date dt = new Date();
        return new java.sql.Timestamp(dt.getTime());
    }

    /**
     * 返回java.sql.Date类型的当前时间
     * 
     * @return java.sql.Date
     */
    public static java.sql.Date nowSqlDate() {
        Date dt = new Date();
        return new java.sql.Date(dt.getTime());
    }

    /**
     * 将timestamp转换成date
     * 
     * @param tt timestamp
     * @return
     */
    public static Date timestampToDate(Timestamp tt) {
        return new Date(tt.getTime());
    }

    /**
     * 指定年 月 日返回日期date
     * 
     * @param year 年,同Calendar.YEAR
     * @param month 月,同Calendar.MONTH
     * @param dayOfMonth 日,同Calendar.DAY_OF_MONTH
     * @return
     */
    public static Date setDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        return calendar.getTime();
    }

    /**
     * 指定年 月 日 小时 分 秒 返回日期date
     * 
     * @param year 同Calendar.YEAR
     * @param month 同Calendar.MONTH
     * @param dayOfMonth 同Calendar.DAY_OF_MONTH
     * @param hour 同Calendar.HOUR
     * @param minute 同Calendar.MINUTE
     * @param second 同Calendar.SECOND
     * @return
     */
    public static Date setDate(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, hour, minute, second);
        return calendar.getTime();
    }

    /**
     * 返回某天的结束时间，即当天23点59分59秒
     * 
     * @param date 时间
     * @return
     */
    public static Date getEndTimeOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 返回某天的开始时间,即当天0点0分0秒
     * 
     * @param date 时间
     * @return
     */
    public static Date getStartTimeOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 计算两个日期间隔小时数.startTime小于endTime时返回负数
     * 
     * @param startTime 时间1
     * @param endTime 时间2
     * @return
     */
    public static long intervalHours(Date startTime, Date endTime) {
        long differTime = endTime.getTime() - startTime.getTime();
        if (differTime > 0) {
            return TimeUnit.MILLISECONDS.toHours(differTime);
        } else {
            return 0 - TimeUnit.MILLISECONDS.toHours(Math.abs(differTime));
        }
    }

    /**
     * 计算两个日期间隔天数.date1小于date2时返回负数
     * 
     * @param date1 时间1
     * @param date2 时间2
     * @return
     */
    public static long intervalDays(Date date1, Date date2) {
        long differTime = date1.getTime() - date2.getTime();
        if (differTime > 0) {
            return TimeUnit.MILLISECONDS.toDays(differTime);
        } else {
            return 0 - TimeUnit.MILLISECONDS.toDays(Math.abs(differTime));
        }
    }

    /**
     * 计算两个日期间隔年数,月份不足不算一年
     * 
     * @param date1 时间1
     * @param date2 时间2
     * @return
     */
    public static int intervalYears(Date date1, Date date2) {
        Calendar calBig = Calendar.getInstance();
        Calendar calSmall = Calendar.getInstance();
        boolean isBig = date1.after(date2);
        if (isBig) {
            calBig.setTime(date1);
            calSmall.setTime(date2);
        } else {
            calBig.setTime(date2);
            calSmall.setTime(date1);
        }
        int y = calBig.get(Calendar.YEAR) - calSmall.get(Calendar.YEAR);
        if (y == 0) {
            return 0;
        } else {
            int m = calBig.get(Calendar.MONTH) - calSmall.get(Calendar.MONTH);
            if (m < 0) {
                y = y - 1;// 月份未年数满减1
            } else if (m == 0) {
                int d = calBig.get(Calendar.DAY_OF_MONTH) - calSmall.get(Calendar.DAY_OF_MONTH);
                if (d < 0) {
                    y = y - 1;// 月份相等，天数未满减1
                }
            }
        }
        return isBig ? y : 0 - y;
    }

    /**
     * 判断两个时间是否是同一天
     * 
     * @param date1 时间1
     * @param date2 时间2
     * @return
     */
    public static boolean isSameDay(Date date1, Date date2) {
        return DateUtils.isSameDay(date1, date2);
    }
    
}
