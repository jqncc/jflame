package org.jflame.toolkit.test.entity;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlSubObj2 implements Serializable {

    @XmlElement(name = "L")
    private String a1;
    @XmlElement(name = "S")
    private String a2;

    public XmlSubObj2() {
    }

    public XmlSubObj2(String a1, String a2) {
        super();
        this.a1 = a1;
        this.a2 = a2;
    }

    public String getA1() {
        return a1;
    }

    public void setA1(String a1) {
        this.a1 = a1;
    }

    public String getA2() {
        return a2;
    }

    public void setA2(String a2) {
        this.a2 = a2;
    }

}
