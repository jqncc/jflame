package com.ghgcn.xxx.common.enums;

import org.jflame.toolkit.common.bean.pair.IIntKeyPair;

/**
 * 普通状态枚举:禁用/启用
 * 
 * @author yucan.zhang
 */
public enum StatusEnum implements IIntKeyPair {
    DISABLED("禁用"), ENABLED("启用");

    private StatusEnum(String desc) {
        this.value = desc;
    }

    private String value;;

    @Override
    public Integer getKey() {
        return ordinal();
    }

    @Override
    public String getValue() {
        return value;
    }
}
