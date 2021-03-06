package org.jflame.commons.net.http.handler;

import java.io.UnsupportedEncodingException;

import org.jflame.commons.exception.ConvertException;
import org.jflame.commons.net.http.RequestProperty;
import org.jflame.commons.util.XmlBeanHelper;

public class XmlRequestBodyHandler implements RequestBodyHandler<Object> {

    @Override
    public byte[] handle(Object requestData, RequestProperty requestProperty) {
        if (requestData != null) {
            String xml = XmlBeanHelper.toXml(requestData, requestProperty.getCharset(), true);
            try {
                return xml.getBytes(requestProperty.getCharset());
            } catch (UnsupportedEncodingException e) {
                throw new ConvertException(e);
            }
        }
        return null;
    }

}
