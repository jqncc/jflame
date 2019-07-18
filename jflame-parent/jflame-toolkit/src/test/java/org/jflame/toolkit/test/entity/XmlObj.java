package org.jflame.toolkit.test.entity;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(name = "datasets")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(value = { XmlSubObj.class,XmlSubObj2.class })
public class XmlObj<T> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @XmlElement
    private int status;
    // @XmlElements(value = { @XmlElement(name = "dataset", type = XmlSubObj.class),@XmlElement(type = XmlSubObj2.class)
    // })
    @XmlElement(name = "dataset")
    private List<T> data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

}
