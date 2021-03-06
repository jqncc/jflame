package org.jflame.commons.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import org.jflame.commons.exception.ConvertException;

/**
 * 日期时间操作工具类.
 * 
 * @see org.apache.commons.lang3.time.DateFormatUtils
 * @see org.apache.commons.lang3.time.DateUtils
 * @author zyc
 */
public final class DateHelper {

    /**
     * 日期格式yyyy-MM-dd
     */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    /**
     * 日期格式yyyyMMdd
     */
    public static final String yyyyMMdd = "yyyyMMdd";
    /**
     * 日期格式yyyy/MM/dd
     */
    public static final String YYYYMMDD_OBLIQUE = "yyyy/MM/dd";
    /**
     * 中文日期格式yyyy年MM月dd日
     */
    public static final String CN_YYYY_MM_DD = "yyyy年MM月dd日";
    /**
     * 中文时间格式HH时mm分ss秒
     */
    public static final String CN_HH_mm_ss = "HH时mm分ss秒";
    /**
     * 时间格式HH:mm:ss
     */
    public static final String HH_mm_ss = "HH:mm:ss";
    /**
     * 时间格式HH:mm
     */
    public static final String HH_mm = "HH:mm";
    /**
     * 时间格式yyyy-MM-dd HH:mm:ss
     */
    public static final String YYYY_MM_DD_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    /**
     * 时间格式yyyy-MM-dd'T'HH:mm:ss
     */
    public static final String YYYY_MM_DD_T_HH_mm_ss = "yyyy-MM-dd'T'HH:mm:ss";
    /**
     * 时间格式yyyyMMddHHmmss
     */
    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
    /**
     * 时间格式yyyyMMddHHmmssSSS
     */
    public static final String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";
    /**
     * 常用日期格式数组
     */
    public static final String[] SHORT_PATTEN = { YYYY_MM_DD,yyyyMMdd,CN_YYYY_MM_DD,YYYYMMDD_OBLIQUE };

    /**
     * 常用时间长格式数组
     */
    public static final String[] LONG_PATTEN = { YYYY_MM_DD_HH_mm_ss,yyyyMMddHHmmss,YYYY_MM_DD_T_HH_mm_ss,
            yyyyMMddHHmmssSSS };
    /**
     * 常用时间格式数组
     */
    public static final String[] TIME_PATTEN = { HH_mm_ss,HH_mm,CN_HH_mm_ss };

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
     * 格式化时间,新时间类型支持LocalDate,LocalTime,LocalDateTime
     * 
     * @param date 时间
     * @param pattern 格式
     * @return
     */
    public static String format(TemporalAccessor date, String pattern) {
        return DateTimeFormatter.ofPattern(pattern)
                .format(date);
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
     * 使用长格式yyyy-MM-dd HH:mm:ss 格式化时间
     * 
     * @param date date
     * @return
     */
    public static String formatLong(Date date) {
        return DateFormatUtils.format(date, YYYY_MM_DD_HH_mm_ss);
    }

    /**
     * 使用短格式yyyy-MM-dd格式化时间
     * 
     * @param date date
     * @return
     */
    public static String formatShort(Date date) {
        return DateFormatUtils.format(date, YYYY_MM_DD);
    }

    /**
     * 解析时间字符串转为Date,尝试多种格式解析,返回首个可解析的时间
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
     * 解析时间字符串转为LocalDateTime,尝试多种格式解析,返回首个可解析的时间
     * 
     * @param dateStr 时间字符串
     * @param patterns 格式数组
     * @return
     * @throws ConvertException 解析失败
     */
    public static LocalDateTime parseLocalDateTime(String dateStr, String... patterns) throws ConvertException {
        for (String pattern : patterns) {
            try {
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }
        throw new ConvertException(dateStr + "转为LocalDateTime失败,patten:" + Arrays.toString(patterns));
    }

    /**
     * 解析时间字符串转为LocalDate,尝试多种格式解析,返回首个可解析的时间
     * 
     * @param dateStr 时间字符串
     * @param patterns 格式数组
     * @return
     * @throws ConvertException 解析失败
     */
    public static LocalDate parseLocalDate(String dateStr, String... patterns) throws ConvertException {
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }
        throw new ConvertException(dateStr + "转为LocalDate失败,patten:" + Arrays.toString(patterns));
    }

    /**
     * 解析时间字符串转为LocalTime,尝试多种格式解析,返回首个可解析的时间
     * 
     * @param dateStr 时间字符串
     * @param patterns 格式数组
     * @return
     * @throws ConvertException 解析失败
     */
    public static LocalTime parseLocalTime(String dateStr, String... patterns) throws ConvertException {
        for (String pattern : patterns) {
            try {
                return LocalTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }
        throw new ConvertException(dateStr + "转为LocalDate失败,patten:" + Arrays.toString(patterns));
    }

    /**
     * 返回当前时间的全格式字符串，格式为:yyyy-MM-dd HH:mm:ss
     * 
     * @return 格式为:yyyy-MM-dd HH:mm:ss的字符串
     */
    public static String fullNow() {
        return formatNow(YYYY_MM_DD_HH_mm_ss);
    }

    /**
     * 返回当前时间的短格式字符串，格式为:yyyy-MM-dd
     * 
     * @return
     */
    public static String shortNow() {
        return formatNow(YYYY_MM_DD);
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
     * @param month 同Calendar.MONTH. 月份是从0开始
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
     * 返回某天的结束时间，即当天23点59分59秒999毫秒
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
        calendar.set(Calendar.MILLISECOND, 999);
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
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        return calendar.getTime();

    }

    /**
     * 获取指定时间的月份第一天00:00:00.<br>
     * eg:2017-11-21 23:12:12 =&gt; 2017-11-01 00:00:00
     * 
     * @param date Date
     * @return
     */
    public static Date getFirstDayOfMonth(Date date) {
        return DateUtils.truncate(date, Calendar.MONTH);
    }

    /**
     * 获取指定时间的月份最后一天23:59:59.<br>
     * eg:2017-11-21 23:12:12 =&gt; 2017-11-20 23:59:59
     * 
     * @param date Date
     * @return
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar endCalendar = DateUtils.toCalendar(date);
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCalendar.set(Calendar.HOUR_OF_DAY, endCalendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        endCalendar.set(Calendar.MINUTE, endCalendar.getActualMaximum(Calendar.MINUTE));
        endCalendar.set(Calendar.SECOND, endCalendar.getActualMaximum(Calendar.SECOND));
        return endCalendar.getTime();
    }

    /**
     * 计算两个日期间隔秒数.startTime小于endTime时返回负数<br>
     * 
     * @param startTime 时间1
     * @param endTime 时间2
     * @return
     */
    public static long intervalSecond(Date startTime, Date endTime) {
        long differTime = endTime.getTime() - startTime.getTime();
        if (differTime > 0) {
            return TimeUnit.MILLISECONDS.toSeconds(differTime);
        } else {
            return 0 - TimeUnit.MILLISECONDS.toSeconds(Math.abs(differTime));
        }
    }

    /**
     * 计算两个日期间隔分钟数.endTime小于startTime时返回负数<br>
     * 
     * @param startTime 时间1
     * @param endTime 时间2
     * @return
     */
    public static long intervalMinutes(Date startTime, Date endTime) {
        long differTime = endTime.getTime() - startTime.getTime();
        if (differTime > 0) {
            return TimeUnit.MILLISECONDS.toMinutes(differTime);
        } else {
            return 0 - TimeUnit.MILLISECONDS.toMinutes(Math.abs(differTime));
        }
    }

    /**
     * 计算两个日期间隔小时数.endTime小于startTime时返回负数<br>
     * 注:整数计算,如55分钟内为0小时,79分钟为1小时
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
     * 计算两个日期间隔天数.date1大于date2时返回负数
     * 
     * @param date1 java.util.Date时间1
     * @param date2 java.util.Date时间2
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
     * 计算两个日期间隔天数
     * 
     * @param start LocalDate
     * @param end LocalDate
     * @return
     */
    public static long intervalDays(LocalDate start, LocalDate end) {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
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

    /**
     * 返回昨天日期
     * 
     * @return
     */
    public static Date yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    /**
     * Instant转Date
     * 
     * @param instant
     * @return
     */
    public static Date fromInstant(Instant instant) {
        return Date.from(instant);
    }

    /**
     * LocalDateTime转Date
     * 
     * @param localDateTime
     * @return
     */
    public static Date fromLocalDateTime(LocalDateTime localDateTime) {
        return fromInstant(localDateTime.atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static Date fromLocalDate(LocalDate localDate) {
        return fromInstant(localDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * Date转LocalDateTime
     * 
     * @param date
     * @return
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * date转localDate
     * 
     * @param date
     * @return
     */
    public static LocalDate toLocalDate(Date date) {
        return toLocalDateTime(date).toLocalDate();
    }

    /**
     * 根据时间表达式得出天数
     * 
     * @param express 举例:1d=1天
     * @return
     */
    public static long dayExpression(String express) {
        return timeExpression(express, TimeUnit.DAYS);
    }

    /**
     * 根据时间表达式得出小时数
     * 
     * @param express 举例:30h=30小时
     * @return
     */
    public static long hourExpression(String express) {
        return timeExpression(express, TimeUnit.HOURS);
    }

    /**
     * 根据时间表达式得出分钟数
     * 
     * @param express 举例:24m=24分钟
     * @return
     */
    public static long minuteExpression(String express) {
        return timeExpression(express, TimeUnit.MINUTES);
    }

    /**
     * 根据时间表达式得出秒数
     * 
     * @param express 举例:24m=24分钟
     * @return
     */
    public static long secondExpression(String express) {
        return timeExpression(express, TimeUnit.SECONDS);
    }

    /**
     * 根据时间表达式得出具体时间值.<br>
     * 注:如时间单位大于表达式所表示的时间返回0
     * 
     * @param expression 时间表达式,举例:1d=1天,30h=30小时,24m=24分钟,5s=5秒
     * @param timeUnit 返回值的时间单位
     * @return
     */
    public static long timeExpression(String expression, TimeUnit timeUnit) {
        String fmt = "^[1-9]\\d*[d|h|m|s]";
        Pattern pattern = Pattern.compile(fmt, Pattern.CASE_INSENSITIVE);
        if (pattern.matcher(expression)
                .matches()) {
            char c = StringHelper.endChar(expression.toLowerCase());
            int time = Integer.parseInt(expression.substring(0, expression.length() - 1));
            long s;
            if (c == 'h') {
                if (timeUnit == TimeUnit.HOURS) {
                    return time;
                } else {
                    s = time * 3600;
                }
            } else if (c == 'd') {
                if (timeUnit == TimeUnit.DAYS) {
                    return time;
                } else {
                    s = time * 3600 * 24;
                }
            } else if (c == 'm') {
                if (timeUnit == TimeUnit.MINUTES) {
                    return time;
                } else {
                    s = time * 60;
                }
            } else {
                if (timeUnit == TimeUnit.SECONDS) {
                    return time;
                } else {
                    s = time;
                }
            }
            if (timeUnit == TimeUnit.HOURS) {
                return TimeUnit.SECONDS.toHours(s);
            } else if (timeUnit == TimeUnit.MINUTES) {
                return TimeUnit.SECONDS.toMinutes(s);
            } else if (timeUnit == TimeUnit.SECONDS) {
                return s;
            } else if (timeUnit == TimeUnit.DAYS) {
                return TimeUnit.SECONDS.toDays(s);
            }
        }
        throw new IllegalArgumentException("无法解析时间表达式" + expression);
    }

    /**
     * 返回unix时间戳
     * 
     * @param date
     * @return
     */
    public static long unixTimestamp(Date date) {
        return date.getTime() / 1000;
    }

    /**
     * 返回当前时间的unix时间戳
     * 
     * @return
     */
    public static long unixTimestamp() {
        return Instant.now()
                .getEpochSecond();
    }

    /**
     * 获取LocalDateTime的毫秒时间戳
     * 
     * @param dateTime
     * @return
     */
    public static long timestamp(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.ofHours(8))
                .toEpochMilli();
    }

    /**
     * 取指定日期所在年份的第一天时间
     * 
     * @param date 日期
     * @return LocalDate
     */
    public static LocalDate getFirstDayOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * 取指定日期所在年份最后一天时间
     * 
     * @param date 日期
     * @return LocalDate
     */
    public static LocalDate getLastDayOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * 返回指定日期年份的最后时间,即当年最后一天的23:59:59.999999999
     * 
     * @param date
     * @return
     */
    public static LocalDateTime getLastTimeOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfYear())
                .atTime(LocalTime.MAX);
    }

    /**
     * 获取今年的最后时间点.,即今年最后一天的23:59:59.999999999
     * 
     * @return
     */
    public static LocalDateTime getLastTimeOfThisYear() {
        return getLastTimeOfYear(LocalDate.now());
    }

    /**
     * 返回指定日期所在周的周一
     * 
     * @param date
     * @return
     */
    public static LocalDate getMonday(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    /**
     * 返回本周一日期
     * 
     * @return
     */
    public static LocalDate monday() {
        return LocalDate.now()
                .with(DayOfWeek.MONDAY);
    }

    /**
     * 回指定日期所在周的周日
     * 
     * @param date
     * @return
     */
    public static LocalDate getSunday(LocalDate date) {
        return date.with(DayOfWeek.SUNDAY);
    }

    /**
     * 返回本周日日期
     * 
     * @return
     */
    public static LocalDate sunday() {
        return LocalDate.now()
                .with(DayOfWeek.SUNDAY);
    }

    /**
     * 明年第一天
     * 
     * @return LocalDate
     */
    public static LocalDate firstDayOfNextYear() {
        return LocalDate.now()
                .with(TemporalAdjusters.firstDayOfNextYear());
    }

    /**
     * 明年第一天0:0:0
     * 
     * @return Date
     */
    public static Date firstDateOfNextYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) + 1;
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        Date currYearFirst = calendar.getTime();
        return currYearFirst;
    }

    /**
     * 获取上月第一天
     * 
     * @return
     */
    public static LocalDate getStartDayOfLastMonth() {
        return LocalDate.now()
                .minusMonths(1)
                .with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取上月最后一天
     * 
     * @return
     */
    public static LocalDate getEndDayOfLastMonth() {
        return LocalDate.now()
                .minusMonths(1)
                .with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 获取去年第一天
     * 
     * @return
     */
    public static LocalDate getStartDayOfLastYear() {
        return LocalDate.now()
                .minusYears(1)
                .with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * 获取去年最后一天
     * 
     * @return
     */
    public static LocalDate getEndDayOfLastYear() {
        return LocalDate.now()
                .minusYears(1)
                .with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * 获取当前季度第一天的日期
     * 
     * @return LocalDate
     */
    public static LocalDate getStartDayOfCurQuarter() {
        return getStartDayOfQuarter(LocalDate.now());
    }

    /**
     * 获取当前季度最后一天的日期
     * 
     * @return
     */
    public static LocalDate getEndDayOfCurQuarter() {
        return getEndDayOfQuarter(LocalDate.now());
    }

    /**
     * 获取上一季度第一天
     * 
     * @param date
     * @return
     */
    public static LocalDate getStartDayOfLastQuarter() {
        return getStartDayOfQuarter(LocalDate.now()
                .minusMonths(3));
    }

    /**
     * 获取上一季度最后一天
     * 
     * @return
     */
    public static LocalDate getEndDayOfLastQuarter() {
        return getEndDayOfQuarter(LocalDate.now()
                .minusMonths(3));
    }

    /**
     * 获取指定日期所在季度的第一天的日期
     * 
     * @param date
     * @return
     */
    public static LocalDate getStartDayOfQuarter(LocalDate date) {
        Month month = date.getMonth();
        Month firstMonthOfQuarter = month.firstMonthOfQuarter();
        return LocalDate.of(date.getYear(), firstMonthOfQuarter, 1);
    }

    /**
     * 获取指定日期所在季度的最后一天的日期
     * 
     * @param date
     * @return
     */
    public static LocalDate getEndDayOfQuarter(LocalDate date) {
        Month month = date.getMonth();
        Month firstMonthOfQuarter = month.firstMonthOfQuarter();
        Month endMonthOfQuarter = Month.of(firstMonthOfQuarter.getValue() + 2);
        return LocalDate.of(date.getYear(), endMonthOfQuarter, endMonthOfQuarter.length(date.isLeapYear()));
    }

    /**
     * 解析字符串为Date或Date的子类
     * 
     * @param text 时间字符串
     * @param pattern 格式
     * @param dateClazz Date或Date的子类
     * @return
     * @throws ConvertException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Date> T parseDate(final String text, final Optional<String> pattern,
            final Class<T> dateClazz) throws ConvertException {
        Date val;

        // 未指定固定格式器，使用所有支持格式处理
        if (dateClazz == Date.class) {
            if (!pattern.isPresent()) {
                val = DateHelper.parseDate(text, ArrayUtils.addAll(LONG_PATTEN, SHORT_PATTEN));
            } else {
                val = DateHelper.parseDate(text, pattern.get());
            }
            return (T) val;
        } else if (dateClazz == Timestamp.class) {
            if (!pattern.isPresent()) {
                val = DateHelper.parseDate(text, ArrayUtils.addAll(LONG_PATTEN, SHORT_PATTEN));
            } else {
                val = DateHelper.parseDate(text, pattern.get());
            }
            return (T) (new Timestamp(val.getTime()));
        } else if (dateClazz == java.sql.Date.class) {
            if (!pattern.isPresent()) {
                val = DateHelper.parseDate(text, SHORT_PATTEN);
            } else {
                val = DateHelper.parseDate(text, pattern.get());
            }
            return (T) (new java.sql.Date(val.getTime()));
        } else if (dateClazz == java.sql.Time.class) {
            if (!pattern.isPresent()) {
                val = DateHelper.parseDate(text, TIME_PATTEN);
            } else {
                val = DateHelper.parseDate(text, pattern.get());
            }
            return (T) (new java.sql.Time(val.getTime()));
        } else {
            throw new IllegalArgumentException("不支持的时间转换类型" + dateClazz);
        }
    }

}
