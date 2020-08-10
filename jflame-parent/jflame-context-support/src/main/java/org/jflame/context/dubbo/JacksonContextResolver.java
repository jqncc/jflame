package org.jflame.context.dubbo;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * dubbo rest服务,定制jackson属性
 * 
 * @author yucan.zhang
 */
@Provider
public class JacksonContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;

    public JacksonContextResolver() {
        objectMapper = JsonMapper.builder()
                .configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
                .configure(Feature.IGNORE_UNKNOWN, true)
                .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)// 忽略@Transient或transient修改符成员
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .serializationInclusion(Include.NON_NULL)
                .build();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
