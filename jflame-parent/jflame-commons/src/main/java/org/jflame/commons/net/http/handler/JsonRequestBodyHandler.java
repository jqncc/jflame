package org.jflame.commons.net.http.handler;

import java.io.UnsupportedEncodingException;

import org.jflame.commons.exception.ConvertException;
import org.jflame.commons.net.http.RequestProperty;
import org.jflame.commons.util.json.JsonHelper;

public class JsonRequestBodyHandler implements RequestBodyHandler<Object> {

    @Override
    public byte[] handle(Object requestData, RequestProperty requestProperty) {
        if (requestData != null) {
            String json = JsonHelper.toJson(requestData);
            try {
                return json.getBytes(requestProperty.getCharset());
            } catch (UnsupportedEncodingException e) {
                throw new ConvertException(e);
            }
        }
        return null;
    }

}
