package org.jflame.toolkit.test;

import java.io.IOException;

import org.jflame.toolkit.codec.TranscodeHelper;
import org.junit.Test;

public class TranscodeTest {

    @Test
    public void testIntByte() throws IOException {

       byte[] bs=TranscodeHelper.intTo4Bytes(20);
       int i=TranscodeHelper.bytesToInt(bs);
       System.out.println(i);
    }

 

}
