package org.jflame.toolkit.net.http.handler;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.net.http.HttpResponse;
import org.jflame.toolkit.util.StringHelper;

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
                return StringHelper.getUtf8String(data);
            }
        }
        return StringUtils.EMPTY;
    }

}
