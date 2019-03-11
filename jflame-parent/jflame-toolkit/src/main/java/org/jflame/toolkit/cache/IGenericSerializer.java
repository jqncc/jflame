package org.jflame.toolkit.cache;

import org.jflame.toolkit.exception.SerializeException;

public interface IGenericSerializer {

    byte[] serialize(Object t) throws SerializeException;

    Object deserialize(byte[] bytes) throws SerializeException;
}
