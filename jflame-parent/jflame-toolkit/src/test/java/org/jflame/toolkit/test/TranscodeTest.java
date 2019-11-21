package org.jflame.toolkit.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.Test;

import org.jflame.commons.codec.TranscodeHelper;

public class TranscodeTest {

    @Test
    public void testIntByte() throws IOException {

        byte[] bs = TranscodeHelper.intTo4Bytes(20);
        int i = TranscodeHelper.bytesToInt(bs);
        System.out.println(i);
    }

    @Test
    public void testBase64() throws IOException {
        byte[] bs = "49102419r418fdfg".getBytes(StandardCharsets.UTF_8);
        String jstr = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bs);
        String astr = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(bs);
        System.out.println(jstr);
        System.out.println(astr);
    }

}
