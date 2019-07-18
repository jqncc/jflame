package org.jflame.toolkit.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jflame.toolkit.exception.ConvertException;

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
     * @throws ConvertException 转换异常
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
            throw new ConvertException(e);
        }

        return result;
    }

    /**
     * JavaBean转换成xml
     * 
     * @param bean javabean
     * @param encoding 字符编码
     * @param isIgnoreHeader 是否忽略xml头 ,即&lt;?xml encoding?&gt;部分
     * @return xml字符串
     * @throws ConvertException 转换异常
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
            throw new ConvertException(e);
        }

        return result;
    }

    /**
     * xml转换成JavaBean
     * 
     * @param xml xml格式字符串
     * @param beanClass bean类型
     * @return T
     * @throws ConvertException 转换异常
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
            throw new ConvertException(e);
        }

        return t;
    }
}
