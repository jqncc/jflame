package org.jflame.toolkit.net.http.handler;

import org.apache.poi.ss.formula.functions.T;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.net.http.HttpResponse;
import org.jflame.toolkit.util.JsonHelper;
import org.jflame.toolkit.util.StringHelper;

import com.alibaba.fastjson.TypeReference;

/**
 * 返回结果json反解析为java对象
 * 
 * @author yucan.zhang
 */
@SuppressWarnings("hiding")
public class JsonResponseHandler<T> implements ResponseBodyHandler<T> {

    private Class<T> entityClass;
    private TypeReference<T> typeReference;

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
     * @param typeReference 复杂类型
     */
    public JsonResponseHandler(TypeReference<T> typeReference) {
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
