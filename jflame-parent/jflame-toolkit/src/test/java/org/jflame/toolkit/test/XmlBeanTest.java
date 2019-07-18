package org.jflame.toolkit.test;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.test.entity.XmlObj;
import org.jflame.toolkit.test.entity.XmlSubObj;
import org.jflame.toolkit.test.entity.XmlSubObj2;
import org.jflame.toolkit.util.CollectionHelper;

import org.junit.Test;

public class XmlBeanTest {

    @Test
    public void test() {
        XmlObj<XmlSubObj> obj = new XmlObj<>();
        obj.setStatus(1);
        obj.setData(CollectionHelper.newList(new XmlSubObj("test", "test555")));
        String objXml = toXml(obj, "utf-8", true);
        System.out.println(objXml);

        obj = toBean(objXml, XmlObj.class);

        XmlObj<XmlSubObj2> obj2 = new XmlObj<>();
        obj2.setStatus(1);
        obj2.setData(CollectionHelper.newList(new XmlSubObj2("2test2", "tdfdfdfest555")));
        System.out.println(toXml(obj2, "utf-8", true));
    }

    @Test
    public void testToBean() {
        String xml = "<datasets><status>1</status><dataset><A>test</A><B>test555</B></dataset></datasets>";
        XmlObj<XmlSubObj> obj = toBean(xml, XmlObj.class);
        System.out.println(obj.getData()
                .get(0)
                .getA2());

        /*  String xml2 = "<datasets><status>3</status><dataset><L>2test2</L><S>tdfdfdfest555</S></dataset></datasets>";
        XmlObj<XmlSubObj2> obj2 = toBean(xml2, XmlObj.class);
        System.out.println(obj2.getData()
                .get(0)
                .getA1());*/
    }

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
}
