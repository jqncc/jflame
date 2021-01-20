package org.jflame.toolkit.test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import org.jflame.commons.util.DateHelper;

public class DateTest {

    @Test
    public void test() {
        // Date date = DateHelper.setDate(1970, 1, 1);
        // System.out.println(DateHelper.formatShort(date));
        LocalDate now = LocalDate.of(2020, 12, 1);
        System.out.println(now.minusDays(29));
        System.out.println(now.plusDays(0));

        LocalDate startDate = LocalDate.of(2020, 12, 1);
        LocalDate endDate = LocalDate.of(2020, 12, 10);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DateHelper.yyyyMMdd);
        int intervalDay = (int) startDate.until(endDate, ChronoUnit.DAYS) + 1;// 需包含endDate
        System.out.println(intervalDay);
        ArrayList<Integer> countPointers = new ArrayList<>(intervalDay);
        for (int i = 0; i < intervalDay; i++) {
            countPointers.add(Integer.parseInt(startDate.plusDays(i)
                    .format(dtf)));
        }

        System.out.println(countPointers);

    }

    @Test
    public void testWeek() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.with(DayOfWeek.MONDAY);
        LocalDate endDate = now.with(DayOfWeek.SUNDAY);
        System.out.println("周一:" + startDate);
        System.out.println("周日:" + endDate);

        startDate = now.with(TemporalAdjusters.firstDayOfMonth());
        endDate = now.with(TemporalAdjusters.lastDayOfMonth());

        System.out.println("月初:" + startDate);
        System.out.println("月末:" + endDate);

        System.out.println(startDate.until(endDate, ChronoUnit.DAYS));
    }

    public static void main(String[] args) {
        /* Pet p = new Pet();
        p.setAge(19740991);
        p.setMoney(5.4d);
        p.setName("i测试中");
        p.setSkin("red");
        String x = JSON.toJSONString(p, SerializerFeature.BrowserCompatible);
        System.out.println(x);*/
        /* GenericFastJsonRedisSerializer serializer = new GenericFastJsonRedisSerializer();
        byte[] x = { 12,1,3,4 };
        byte[] y = serializer.serialize(x);
        System.out.println(Arrays.toString(y));*/
        Date enDate = DateHelper.parseDate("2019-05-26 10:22:22", DateHelper.YYYY_MM_DD_HH_mm_ss);
        long dt = DateHelper.intervalMinutes(enDate, new Date());
        System.out.println(dt);

        System.out.println(DateHelper.getLastTimeOfThisYear());

        System.out.println(DateHelper.fromLocalDate(LocalDate.now()));

        System.out.println(String.format("%d月", 12));
        System.out.println(String.format("%d月", 2));

        StringBuffer buffer = new StringBuffer();
        buffer.append(20190526);
        buffer.insert(4, '/')
                .insert(7, '/');
        System.out.println(buffer);
    }
}
