package org.jflame.commons.reflect;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 加载自定义jar包工具类
 */
public final class ClassLoaderHelper {

    private final static Logger log = LoggerFactory.getLogger(ClassLoaderHelper.class);

    /** URLClassLoader的addURL方法 */
    private static Method addURL = initAddMethod();
    private static URLClassLoader system = (URLClassLoader) ClassLoader.getSystemClassLoader();

    /** 初始化方法 */
    private static final Method initAddMethod() {
        try {
            Method add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{ URL.class });
            add.setAccessible(true);
            return add;
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * 循环遍历目录，找出所有的JAR包
     */
    private static final void loopFiles(File file, List<File> files) {
        if (file.isDirectory()) {
            File[] tmps = file.listFiles();
            for (File tmp : tmps) {
                loopFiles(tmp, files);
            }
        } else {
            if (file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".zip")) {
                files.add(file);
            }
        }
    }

    /**
     * 加载jar文件
     *
     * @param file
     * @throws MalformedURLException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static final void loadJarFile(File file)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
        if (!file.exists()) {
            throw new IllegalArgumentException("指定文件不存在" + file.getName());
        }
        addURL.invoke(system, new Object[]{ file.toURI().toURL() });
        log.info("加载JAR包：" + file.getAbsolutePath());
    }

    /**
     * 加载指定目录下的所有JAR文件
     *
     * @param path 目录
     */
    public static final void loadJarPath(String path) {
        List<File> files = new ArrayList<File>();
        File lib = new File(path);
        loopFiles(lib, files);
        if (!files.isEmpty()) {
            for (File file : files) {
                try {
                    loadJarFile(file);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | MalformedURLException e) {
                    log.error("加载jar失败:" + file.getName(), e);
                }
            }
        } else {
            log.warn("路径{}下不存在可用的jar文件", path);
        }
    }
}
