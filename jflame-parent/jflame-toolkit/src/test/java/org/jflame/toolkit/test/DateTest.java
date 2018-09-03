package org.jflame.toolkit.test;

import java.util.Date;

import org.jflame.toolkit.util.DateHelper;
import org.junit.Test;

public class DateTest {

    @Test
    public void test() {
        Date date = DateHelper.setDate(1970, 1, 1);
        System.out.println(DateHelper.formatShort(date));
    }

}
