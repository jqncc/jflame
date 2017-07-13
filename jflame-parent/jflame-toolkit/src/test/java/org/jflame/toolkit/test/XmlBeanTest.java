package org.jflame.toolkit.test;

import org.jflame.toolkit.test.entity.Pig;
import org.jflame.toolkit.util.XmlBeanHelper;

public class XmlBeanTest {

    public static void main(String[] args) {
        Pig pig=new Pig(10,"黑猪",200,"黑色");
        System.out.println(XmlBeanHelper.toXml(pig));
        String xml="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><pigs><age>10</age><name>红猪</name>"
                + "<skin>红色</skin><weight>220</weight></pigs>";
        Pig pigred=XmlBeanHelper.toBean(xml,Pig.class);
        System.out.println(pigred.getName());
    }
}
