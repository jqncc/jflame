package org.jflame.toolkit.test;

import java.util.Date;

import org.junit.Test;

import org.jflame.commons.util.DateHelper;

public class DateTest {

    @Test
    public void test() {
        Date date = DateHelper.setDate(1970, 1, 1);
        System.out.println(DateHelper.formatShort(date));
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
    }
}
