package org.jflame.commons.model.pair;

public interface IIntKeyPair extends IKeyValuePair<Integer,String> {

    default boolean equalKey(Integer obj) {
        return obj == null ? false : getKey() == obj;
    }
}
