package org.jflame.commons.json;

import java.io.IOException;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;

/**
 * 将大于9007199254740992L的数字转字符串序列化,解决js大数字精度丢失问题
 * 
 * @author yucan.zhang
 */
public class BigNumberToStrSerializer extends NumberSerializer {

    private static final long serialVersionUID = 1L;
    static final long JS_MAX_NUMBER_VALUE = 9007199254740992L;

    public BigNumberToStrSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    @Override
    public void serialize(Number value, JsonGenerator g, SerializerProvider provider) throws IOException {
        if (value instanceof BigInteger || value instanceof Long) {
            if (value.longValue() > JS_MAX_NUMBER_VALUE) {
                g.writeString(value.toString());
                return;
            }
        }
        NumberSerializer.instance.serialize(value, g, provider);
    }
}
