package org.jflame.toolkit.net.http.handler;

import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.net.http.HttpResponse;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.toolkit.util.XmlBeanHelper;

/**
 * 返回结果xml反解析为java对象,使用jaxb
 * 
 * @author yucan.zhang
 * @param <T> 结果对象类型
 */
public class XmlResponseHandler<T> implements ResponseBodyHandler<T> {

    private Class<T> entityClass;

    public XmlResponseHandler(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T handle(HttpResponse response) throws ConvertException {
        String text = response.getResponseAsText();
        if (StringHelper.isNotEmpty(text)) {
            return XmlBeanHelper.toBean(text, entityClass);
        }
        return null;
    }
}
