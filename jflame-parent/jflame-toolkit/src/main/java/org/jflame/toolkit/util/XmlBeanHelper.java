package org.jflame.toolkit.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
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
     * @param obj
     * @param writer
     * @return
     */
    public static String convertToXml(Object obj) {
        return toXml(obj, CharsetHelper.UTF_8);
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
        String result = null;
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(bean.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);

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
    @SuppressWarnings("unchecked")
    public static <T> T toBean(String xml, Class<T> beanClass) {
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
