package org.jflame.toolkit.config;

/**
 * 配置参数封装类
 * 
 * @author yucan.zhang
 * @param <E> 参数值类型
 */
public final class ConfigKey<E> {

    private final String name;

    private final E defaultValue;

    public ConfigKey(final String name) {
        this(name, null);
    }

    public ConfigKey(final String name, final E defaultValue) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }

        this.name = name;
        this.defaultValue = defaultValue;
    }

    /**
     * 配置参数键名
     *
     * @return 配置参数名称. 不能为null
     */
    public String getName() {
        return this.name;
    }

    /**
     * 参数默认值,可选
     *
     * @return 返回默认值或null
     */
    public E getDefaultValue() {
        return this.defaultValue;
    }
}
