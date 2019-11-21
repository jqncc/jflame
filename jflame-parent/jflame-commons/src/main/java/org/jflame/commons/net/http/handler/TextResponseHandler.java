package org.jflame.commons.net.http.handler;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;

import org.jflame.commons.exception.ConvertException;
import org.jflame.commons.net.http.HttpResponse;
import org.jflame.commons.util.CharsetHelper;

public class TextResponseHandler implements ResponseBodyHandler<String> {

    @Override
    public String handle(HttpResponse response) throws ConvertException {
        byte[] data = response.getResponseBody();
        if (data != null) {
            if (response.getCharset() != null) {
                try {
                    return new String(data, response.getCharset());
                } catch (UnsupportedEncodingException e) {
                    throw new ConvertException(e);
                }

            } else {
                return CharsetHelper.getUtf8String(data);
            }
        }
        return StringUtils.EMPTY;
    }

}
