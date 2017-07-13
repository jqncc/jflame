package org.jflame.toolkit.net.http.handler;

import org.jflame.toolkit.codec.TranscodeException;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.net.http.RequestProperty;
import org.jflame.toolkit.util.StringHelper;

/**
 * 字符串类型body参数处理
 * 
 * @author yucan.zhang
 */
public class TextRequestBodyHandler implements RequestBodyHandler<String> {

    @Override
    public byte[] handle(String requestData, RequestProperty requestProperty) throws ConvertException {
        if (StringHelper.isEmpty(requestData)) {
            return null;
        }
        try {
            return StringHelper.getBytes(requestData, requestProperty.getCharset());
        } catch (TranscodeException e) {
            throw new ConvertException(e);
        }
    }

}
