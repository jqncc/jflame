package org.jflame.web;

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

    public static ConcurrentMap<String,Object> serviceMap = new ConcurrentHashMap<>();

    /**
     * 加载接口第一个实现类,如果已经加载过返回原实例,实现类单例形式存在
     * 
     * @param serviceClazz 接口类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T loadSingleService(Class<T> serviceClazz) {
        String key = serviceClazz.getName();
        T instance = null;
        if (!serviceMap.containsKey(key)) {
            ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClazz);
            Iterator<T> it = serviceLoader.iterator();
            if (it.hasNext()) {
                instance = it.next();
                serviceMap.put(key, instance);
            }
        } else {
            instance = (T) serviceMap.get(key);
        }

        return instance;
    }

    /**
     * 加载接口第一个实现类,每次都是新实例
     * 
     * @param serviceClazz 接口类型
     * @return
     */
    public static <T> T loadService(Class<T> serviceClazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClazz);
        Iterator<T> it = serviceLoader.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    /**
     * 加载接口所有实现类
     * 
     * @param serviceClazz 接口类型
     * @return
     */
    public static <T> Iterator<T> loadServices(Class<T> serviceClazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClazz);
        return serviceLoader.iterator();
    }
}
