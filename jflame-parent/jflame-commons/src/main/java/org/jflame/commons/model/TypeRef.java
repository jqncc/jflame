package org.jflame.commons.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.alibaba.fastjson.TypeReference;

public abstract class TypeRef<T> implements Comparable<TypeRef<T>> {

    final Type type;

    public TypeRef() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) { // sanity check, should never happen
            throw new IllegalArgumentException(
                    "Internal error: TypeReference constructed without actual type information");
        }
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public TypeReference<T> toFastJsonTypeRef() {
        return new TypeReference<T>() {
        };
    }

    public Type getType() {
        return type;
    }

    @Override
    public int compareTo(TypeRef<T> o) {
        return 0;
    }

}
