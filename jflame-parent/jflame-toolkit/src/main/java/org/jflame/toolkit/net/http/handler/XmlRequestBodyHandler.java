package org.jflame.toolkit.net.http.handler;

import java.io.UnsupportedEncodingException;

import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.net.http.RequestProperty;
import org.jflame.toolkit.util.XmlBeanHelper;

public class XmlRequestBodyHandler implements RequestBodyHandler<Object> {

    @Override
    public byte[] handle(Object requestData, RequestProperty requestProperty) {
        if (requestData != null) {
            String xml = XmlBeanHelper.toXml(requestData, requestProperty.getCharset());
            try {
                return xml.getBytes(requestProperty.getCharset());
            } catch (UnsupportedEncodingException e) {
                throw new ConvertException(e);
            }
        }
        return null;
    }

}
