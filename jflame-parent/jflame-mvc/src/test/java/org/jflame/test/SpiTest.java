package org.jflame.test;

import org.jflame.toolkit.reflect.SpiFactory;
import org.jflame.web.config.ISysConfig;
import org.junit.Test;


public class SpiTest {

    @Test
    public void test() {
        ISysConfig s1=SpiFactory.getBean(ISysConfig.class);
        ISysConfig s2=SpiFactory.getBean(ISysConfig.class);
        System.out.println(s1==s2);//false加载的为新实例
        
        ISysConfig s3=SpiFactory.getSingleBean(ISysConfig.class);
        ISysConfig s4=SpiFactory.getSingleBean(ISysConfig.class);
        System.out.println(s3==s4);//true加载的为新实例
        
        System.out.println(s3.getTextParam("image.server"));
    }

}
