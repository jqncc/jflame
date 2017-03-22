package org.jflame.toolkit.reflect;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * 加载自定义jar包
 */
public final class ClassLoaderUtil {

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
            e.printStackTrace();
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
     * <pre>
     * 加载JAR文件
     * </pre>
     *
     * @param file
     * @throws MalformedURLException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static final void loadJarFile(File file)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
        addURL.invoke(system, new Object[]{ file.toURI().toURL() });
        System.out.println("加载JAR包：" + file.getAbsolutePath());

    }

    /**
     * <pre>
     * 从一个目录加载所有JAR文件
     * </pre>
     *
     * @param path
     */
    public static final void loadJarPath(String path) {
        List<File> files = new ArrayList<File>();
        File lib = new File(path);
        loopFiles(lib, files);
        for (File file : files) {
            try {
                loadJarFile(file);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
