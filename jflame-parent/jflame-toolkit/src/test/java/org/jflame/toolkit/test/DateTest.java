package org.jflame.toolkit.test;

import java.util.Date;

import org.jflame.toolkit.test.entity.Pet;
import org.jflame.toolkit.util.DateHelper;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class DateTest {

    @Test
    public void test() {
        Date date = DateHelper.setDate(1970, 1, 1);
        System.out.println(DateHelper.formatShort(date));
    }

    public static void main(String[] args) {
        Pet p = new Pet();
        p.setAge(19740991);
        p.setMoney(5.4d);
        p.setName("i测试中");
        p.setSkin("red");
        String x = JSON.toJSONString(p, SerializerFeature.BrowserCompatible);
        System.out.println(x);
    }
}
