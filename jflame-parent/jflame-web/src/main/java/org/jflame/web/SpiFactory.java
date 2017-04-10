package org.jflame.web;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SpiFactory {

    public static ConcurrentMap<String,Object> serviceMap = new ConcurrentHashMap<>();

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

    public static <T> T loadService(Class<T> serviceClazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClazz);
        Iterator<T> it = serviceLoader.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public static <T> Iterator<T> loadServices(Class<T> serviceClazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClazz);
        return serviceLoader.iterator();
    }
}
