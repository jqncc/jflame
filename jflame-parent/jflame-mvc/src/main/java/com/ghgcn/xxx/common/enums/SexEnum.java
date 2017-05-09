package com.ghgcn.xxx.common.enums;

import org.jflame.toolkit.common.bean.pair.IIntKeyPair;

/**
 * 性别枚举
 * 
 * @author yucan.zhang
 */
public enum SexEnum implements IIntKeyPair {
    NONE("未设置"), MALE("男"), FEMALE("女");

    private SexEnum(String sex) {
        this.value = sex;
    }

    private String value;

    @Override
    public Integer getKey() {
        return ordinal();
    }

    @Override
    public String getValue() {
        return value;
    }
}
