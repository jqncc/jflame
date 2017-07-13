package org.jflame.toolkit.net.http.handler;

import org.jflame.toolkit.codec.TranscodeException;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.net.http.RequestProperty;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.toolkit.util.XmlBeanHelper;

public class XmlRequestBodyHandler implements RequestBodyHandler<Object> {

    @Override
    public byte[] handle(Object requestData, RequestProperty requestProperty) {
        if (requestData != null) {
            String xml = XmlBeanHelper.toXml(requestData, requestProperty.getCharset());
            try {
                return StringHelper.getBytes(xml, requestProperty.getCharset());
            } catch (TranscodeException e) {
                throw new ConvertException(e);
            }
        }
        return null;
    }

}
