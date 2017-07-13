package org.jflame.toolkit.net.http.handler;

import org.jflame.toolkit.codec.TranscodeException;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.net.http.RequestProperty;
import org.jflame.toolkit.util.JsonHelper;
import org.jflame.toolkit.util.StringHelper;

public class JsonRequestBodyHandler implements RequestBodyHandler<Object> {

    @Override
    public byte[] handle(Object requestData, RequestProperty requestProperty) {
        if (requestData != null) {
            String json = JsonHelper.toJson(requestData);
            try {
                return StringHelper.getBytes(json, requestProperty.getCharset());
            } catch (TranscodeException e) {
                throw new ConvertException(e);
            }
        }
        return null;
    }

}
