package org.jflame.context.dubbo;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.jaxrs.FastJsonProvider;

/**
 * 替换dubbox resteasy的json提供器为fastjson
 * 
 * @author yucan.zhang
 */
@Provider
@Consumes({ "application/*+json","text/json" })
@Produces({ "application/*+json","text/json" })
public class FastJsonJaxrsProvider extends FastJsonProvider {

    public FastJsonJaxrsProvider() {
        super();
        getFastJsonConfig().setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteBigDecimalAsPlain, SerializerFeature.BrowserCompatible);
    }
    /**
     * public void writeTo(Object obj, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
     * MultivaluedMap<String,Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
     * { super.writeTo(obj, type, genericType, annotations, mediaType, httpHeaders, entityStream); // dubbox client使用
     * httpclient发送请求时设置了Content-Length导致"Content-Length header already present"异常 //
     * httpHeaders.remove("Content-Length"); }
     */
}
