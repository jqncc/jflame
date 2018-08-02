package org.jflame.toolkit.reflect;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 使用SPI机制加载实现类工厂
 * 
 * @author yucan.zhang
 */
public class SpiFactory {

    private static ConcurrentMap<String,Object> serviceBeanMap = new ConcurrentHashMap<>();

    /**
     * 加载接口第一个实现类,如果已经加载过返回原实例,实现类单例形式存在
     * 
     * @param serviceClazz 接口类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSingleBean(Class<T> serviceClazz) {
        String key = serviceClazz.getName();
        T instance = null;
        if (!serviceBeanMap.containsKey(key)) {
            ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClazz);
            Iterator<T> it = serviceLoader.iterator();
            if (it.hasNext()) {
                instance = it.next();
                serviceBeanMap.put(key, instance);
            }
        } else {
            instance = (T) serviceBeanMap.get(key);
        }

        return instance;
    }

    /**
     * 加载接口第一个实现类,每次都是新实例
     * 
     * @param serviceClazz 接口类型
     * @return T
     */
    public static <T> T getBean(Class<T> serviceClazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClazz);
        if (serviceLoader != null) {
            Iterator<T> it = serviceLoader.iterator();
            if (it.hasNext()) {
                return it.next();
            }
        }
        return null;
    }

    /**
     * 加载接口第一个实现类,每次都是新实例,如果未指定实现类使用传入实现类生成新实例
     * 
     * @param interfaceClazz 接口类型
     * @param ifnullDefaultClass 默认实现类型
     * @return T
     */
    public static <T> T getBean(Class<T> interfaceClazz, Class<? extends T> ifnullDefaultClass) {
        T t = getBean(interfaceClazz);
        if (t == null) {
            try {
                t = ifnullDefaultClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    /**
     * 加载接口所有实现类
     * 
     * @param serviceClazz 接口类型
     * @return
     */
    public static <T> Iterator<T> getBeans(Class<T> serviceClazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClazz);
        return serviceLoader.iterator();
    }
}
