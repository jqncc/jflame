package org.jflame.commons.net.http.handler;

import org.jflame.commons.exception.ConvertException;
import org.jflame.commons.model.TypeRef;
import org.jflame.commons.net.http.HttpResponse;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.json.JsonHelper;

/**
 * 返回结果json反解析为java对象
 * 
 * @author yucan.zhang
 */
public class JsonResponseHandler<T> implements ResponseBodyHandler<T> {

    private Class<T> entityClass;
    private TypeRef<T> typeReference;

    /**
     * 构造函数
     * 
     * @param entityClass 结果类型,非复杂Map,List等类型
     */
    public JsonResponseHandler(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 构造函数
     * 
     * @param typeReference TypeRef
     */
    public JsonResponseHandler(TypeRef<T> typeReference) {
        this.typeReference = typeReference;
    }

    @Override
    public T handle(HttpResponse response) throws ConvertException {
        String text = response.getResponseAsText();
        if (StringHelper.isNotEmpty(text)) {
            if (entityClass != null) {
                return JsonHelper.parseObject(text, entityClass);
            } else {
                return JsonHelper.parseObject(text, typeReference);
            }
        }

        return null;
    }

}
