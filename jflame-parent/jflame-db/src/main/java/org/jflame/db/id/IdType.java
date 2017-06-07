package org.jflame.db.id;

/**
 * id类型枚举
 * 
 * @author yucan.zhang
 */
public enum IdType {
    /**
     * 代码手动赋值
     */
    ASSIGN,
    /**
     * 数据库内置序列
     */
    SEQUENCE,
    /**
     * 表序列
     */
    TABLE,
    /**
     * 数据库自增长
     */
    IDENTITY,
    /**
     * snowflake算法生成唯一id
     */
    SNOWFLAKE_ID,
    /**
     * uui
     */
    UUID
}
