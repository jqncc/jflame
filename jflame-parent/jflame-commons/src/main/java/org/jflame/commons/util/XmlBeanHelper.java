package org.jflame.commons.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jflame.commons.exception.SerializeException;

/**
 * xml和JavaBean互转工具类,使用jaxb2标准实现
 * 
 * @author yucan.zhang
 */
public final class XmlBeanHelper {

    /**
     * JavaBean转换成xml 默认编码UTF-8
     * 
     * @param bean javabean
     * @return xml格式字符串
     */
    public static <T> String toXml(T bean) {
        return toXml(bean, StandardCharsets.UTF_8.name());
    }

    /**
     * JavaBean转换成xml
     * 
     * @param bean javabean
     * @param encoding 字符编码
     * @return xml格式字符串
     * @throws SerializeException 转换异常
     */
    public static <T> String toXml(T bean, String encoding) {
        return toXml(bean, encoding, false);
    }

    public static <T> String toXml(JAXBElement<T> beanEle, String encoding, boolean isIgnoreHeader) {
        String result = null;
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(beanEle.getValue()
                    .getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if (encoding != null) {
                marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            }
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, isIgnoreHeader);
            StringWriter writer = new StringWriter();
            marshaller.marshal(beanEle, writer);
            result = writer.toString();
        } catch (JAXBException e) {
            throw new SerializeException(e);
        }

        return result;
    }

    /**
     * JavaBean转为xml文件
     * 
     * @param bean 待转换对象
     * @param xmlPath 要保存的xml文件路径
     */
    public static <T> void toXmlFile(T bean, Path xmlPath) {
        toXmlFile(bean, StandardCharsets.UTF_8.name(), false, xmlPath);
    }

    public static <T> void toXmlFile(T bean, String encoding, boolean isIgnoreHeader, Path xmlPath) {
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(bean.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if (encoding != null) {
                marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            }
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, isIgnoreHeader);
            marshaller.marshal(bean, xmlPath.toFile());
        } catch (JAXBException e) {
            throw new SerializeException(e);
        }

    }

    /**
     * JavaBean转换成xml
     * 
     * @param bean javabean
     * @param encoding 字符编码
     * @param isIgnoreHeader 是否忽略xml头 ,即&lt;?xml encoding?&gt;部分
     * @return xml字符串
     * @throws SerializeException 转换异常
     */
    public static <T> String toXml(T bean, String encoding, boolean isIgnoreHeader) {
        String result = null;
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(bean.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if (encoding != null) {
                marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            }
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, isIgnoreHeader);
            StringWriter writer = new StringWriter();
            marshaller.marshal(bean, writer);
            result = writer.toString();
        } catch (JAXBException e) {
            throw new SerializeException(e);
        }

        return result;
    }

    /**
     * xml字符串转换成JavaBean
     * 
     * @param xml xml格式字符串
     * @param beanClass bean类型
     * @return T
     * @throws SerializeException 转换异常
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> T toBean(String xml, Class<T>... beanClass) {
        T t = null;
        try {
            JAXBContext context = JAXBContext.newInstance(beanClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            t = (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new SerializeException(e);
        }

        return t;
    }

    @SuppressWarnings("unchecked")
    public static <T> T xmlFileToBean(Path xmlPath, Class<T>... beanClass) {
        T t = null;
        try {
            JAXBContext context = JAXBContext.newInstance(beanClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            t = (T) unmarshaller.unmarshal(xmlPath.toFile());
        } catch (JAXBException e) {
            throw new SerializeException(e);
        }

        return t;
    }
}
