package org.jflame.commons.reflect;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jflame.commons.codec.TranscodeHelper;
import org.jflame.commons.model.Chars;

/**
 * 类文件class操作工具类.
 * 
 * @see org.apache.commons.lang3.ClassUtils
 * @author yucan.zhang
 */
public final class ClassHelper {

    /**
     * 取得指定包下所有实现指定接口的类,不包含接口和抽象类
     * 
     * @param interfaceClazz 指定接口class对象
     * @param packageName 包名
     * @param <T> 泛型参数
     * @return class对象列表
     * @throws IOException IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Class<T>> findImplClassesOfInterface(Class<T> interfaceClazz, String packageName)
            throws IOException, ClassNotFoundException {
        List<Class<T>> resultList = null;

        if (interfaceClazz.isInterface()) {
            // 获取当前包下以及子包下所有的类
            List<Class<?>> allClass = getAllClassesInPackage(packageName);
            if (allClass != null) {
                resultList = new ArrayList<Class<T>>();
                for (Class<?> classes : allClass) {
                    // 判断是否是同一个接口
                    if (interfaceClazz.isAssignableFrom(classes) && !classes.isInterface()
                            && !Modifier.isAbstract(classes.getModifiers())) {
                        // 本身不加入进去
                        if (!interfaceClazz.equals(classes)) {
                            resultList.add((Class<T>) classes);
                        }
                    }
                }
            }
        }

        return resultList;
    }

    /**
     * 取得某一类所在包的所有类名,不递归
     * 
     * @param classLocation 类位置
     * @param packageName 包名
     * @return 返回类名数组
     */
    public static String[] getAllClassNamesInPackage(String classLocation, String packageName) {
        String[] packagePathSplit = packageName.split("[.]");// 将packageName分解
        String realClassLocation = classLocation;
        int packageLength = packagePathSplit.length;
        for (int i = 0; i < packageLength; i++) {
            realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
        }
        File packeageDir = new File(realClassLocation);
        if (packeageDir.isDirectory()) {
            String[] allClassName = packeageDir.list();
            return allClassName;
        }
        return null;
    }

    /**
     * 从包package中获取所有的Class
     * 
     * @param packageName 包名
     * @return 查找到的class列表
     * @throws IOException IOException
     * @throws ClassNotFoundException
     */
    public static List<Class<?>> getAllClassesInPackage(String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;

        char point = '.';
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace(point, Chars.SLASH);

        Enumeration<URL> dirs = Thread.currentThread()
                .getContextClassLoader()
                .getResources(packageDirName);
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            String protocol = url.getProtocol();
            // 普通文件
            if ("file".equals(protocol)) {
                String filePath = TranscodeHelper.urlDecode(url.getFile());// 获取包的物理路径
                findClassesInPackage(packageName, filePath, recursive, classes);
            } else if ("jar".equals(protocol)) {
                // 如果是jar包文件
                JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();

                Enumeration<JarEntry> entries = jar.entries();// 返回jar包类的文件迭代
                while (entries.hasMoreElements()) {
                    // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    // 如果是以/开头的
                    if (name.charAt(0) == Chars.SLASH) {
                        name = name.substring(1);
                    }
                    // 如果前半部分和定义的包名相同
                    if (name.startsWith(packageDirName)) {
                        int idx = name.lastIndexOf(Chars.SLASH);
                        // 如果以"/"结尾 是一个包
                        if (idx != -1) {
                            packageName = name.substring(0, idx)
                                    .replace(Chars.SLASH, point);
                        }
                        // 如果可以迭代下去 并且是一个包
                        if ((idx != -1) || recursive) {
                            if (name.endsWith(".class") && !entry.isDirectory()) {
                                String className = name.substring(packageName.length() + 1, name.length() - 6);
                                classes.add(Class.forName(packageName + point + className));
                            }
                        }
                    }
                }
            }
        }

        return classes;
    }

    /**
     * 以class文件的形式来获取包下的所有Class对象，递归子文件夹
     * 
     * @param packageName 包全名
     * @param packagePath 包路径
     * @param recursive 是否递归
     * @param classes 结果存放List
     * @throws ClassNotFoundException
     */
    private static void findClassesInPackage(String packageName, String packagePath, final boolean recursive,
            List<Class<?>> classes) throws ClassNotFoundException {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {

            // 自定义过滤规则 如果可以循环(包含子目录) 或是.class文件
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName()
                        .endsWith(".class"));
            }
        });

        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findClassesInPackage(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // java类文件 取类名
                String className = file.getName()
                        .substring(0, file.getName()
                                .length() - 6);
                classes.add(Class.forName(packageName + '.' + className));
            }
        }
    }

    /**
     * 返回缺省的类加载器
     * 
     * @return
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread()
                    .getContextClassLoader();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        if (cl == null) {
            cl = ClassHelper.class.getClassLoader();
        }
        return cl;
    }
}
