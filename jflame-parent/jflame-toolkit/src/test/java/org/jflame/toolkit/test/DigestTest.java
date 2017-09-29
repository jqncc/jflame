package org.jflame.toolkit.test;

import org.jflame.toolkit.crypto.DigestHelper;
import org.junit.Test;


public class DigestTest {

    @Test
    public void test() {
        System.out.println(DigestHelper.md5Hex("中国人"));
    }

}
