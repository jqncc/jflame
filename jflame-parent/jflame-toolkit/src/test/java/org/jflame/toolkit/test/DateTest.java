package org.jflame.toolkit.test;

import java.util.Arrays;
import java.util.Date;

import org.jflame.toolkit.util.DateHelper;
import org.junit.Test;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;

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
        GenericFastJsonRedisSerializer serializer = new GenericFastJsonRedisSerializer();
        byte[] x = { 12,1,3,4 };
        byte[] y = serializer.serialize(x);
        System.out.println(Arrays.toString(y));
    }
}
