package org.jflame.test;

import org.jflame.web.ISysConfig;
import org.jflame.web.SpiFactory;
import org.junit.Test;


public class SpiTest {

    @Test
    public void test() {
        ISysConfig s1=SpiFactory.loadService(ISysConfig.class);
        ISysConfig s2=SpiFactory.loadService(ISysConfig.class);
        System.out.println(s1==s2);//false加载的为新实例
        
        ISysConfig s3=SpiFactory.loadSingleService(ISysConfig.class);
        ISysConfig s4=SpiFactory.loadSingleService(ISysConfig.class);
        System.out.println(s3==s4);//true加载的为新实例
    }

}
