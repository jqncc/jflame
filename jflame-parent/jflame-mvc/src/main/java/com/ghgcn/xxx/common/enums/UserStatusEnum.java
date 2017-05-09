package com.ghgcn.xxx.common.enums;

import org.jflame.toolkit.common.bean.pair.IIntKeyPair;

/**
 * 用户状态枚举:禁用,启用,锁定
 * 
 * @author yucan.zhang
 */
public enum UserStatusEnum implements IIntKeyPair {
    DISABLED("禁用"), ENABLED("启用"),LOCKED("锁定");

    private UserStatusEnum(String desc) {
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
