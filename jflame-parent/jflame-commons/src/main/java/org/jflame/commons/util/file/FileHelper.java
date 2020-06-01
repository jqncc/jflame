package org.jflame.commons.util.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import org.jflame.commons.model.Chars;
import org.jflame.commons.reflect.ClassHelper;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.util.StringHelper;

/**
 * 文件和文件名操作工具类.另参见:java.nio.Files
 * 
 * @author zyc
 */
public final class FileHelper {

    /**
     * 返回文件路径的目录部分. 如果指定的文件存在，使用文件属性判断<br>
     * 如果文件不存在,则以路径结构判断,有扩展名视为文件，没有视为目录. 示例：
     * 
     * <pre>
     * <code>
     * 文件存在：
     * e:\\a\\b.txt --&gt;  e:\\a\\
     * /usr/local/sh --&gt; /usr/local (sh是文件)
     * /usr/local/sh --&gt; /usr/local/sh (sh是目录)
     * 
     * 文件不存在：
     * e:\\a\\b.txt --&gt;  e:\\a\\
     * /usr/local/sh --&gt; /usr/local/sh
     * </code>
     * </pre>
     * 
     * @param filePath 文件路径
     * @return
     */
    public static String getDir(String filePath) {
        File tmpFile = new File(filePath);
        if (tmpFile.exists()) {
            return tmpFile.isDirectory() ? filePath : tmpFile.getParent();
        } else {
            int i = filePath.lastIndexOf(Chars.SLASH);
            if (i < 0) {
                i = filePath.lastIndexOf(Chars.BACKSLASH);
            }
            if (i < 0) {
                return filePath;
            } else {
                if (filePath.indexOf('.', i) == -1) {
                    return filePath;
                }
                return filePath.substring(0, i + 1);
            }
        }
    }

    /**
     * 取得文件名. 示例：
     * 
     * <pre>
     * <code>
     * a/b/c.txt --&gt; c.txt
     * a.txt --&gt; a.txt
     * e:\\a\\b.txt --&gt; b.txt
     * </code>
     * </pre>
     * 
     * @param filePath 文件路径
     * @return
     */
    public static String getFilename(final String filePath) {
        Path p = Paths.get(filePath);
        return p.getFileName()
                .toString();
    }

    /**
     * 取得文件扩展名,小写
     * 
     * @param filename 文件名
     * @param includePoint 返回的扩展名是否包含.号
     * @return 无扩展名将返回空字符串
     */
    public static String getExtension(final String filename, final boolean includePoint) {
        if (StringHelper.isNotEmpty(filename)) {
            int i = filename.lastIndexOf('.');

            if ((i > 0) && (i < (filename.length() - 1))) {
                return includePoint ? (filename.substring(i)).toLowerCase() : (filename.substring(i + 1)).toLowerCase();
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 在指定的文件夹下创建当前以年/月命名的子文件夹，如果存在则直接返回路径,不存在则创建.
     * <p>
     * 以当前时间为2011年3月,rootDir=/home/user示例：<br>
     * createYearDir=true createMonthDir=true,返回路径为/home/user/2011/3<br>
     * createYearDir=true createMonthDir=false,返回路径为/home/user/2011<br>
     * createYearDir=false createMonthDir=true,返回路径为/home/user/3<br>
     *
     * @param rootDir 指定的根目录.如果是相对路径仍然会创建，空字符串则相对于项目路径
     * @param createYearDir 是否创建当前年份名文件夹
     * @param createMonthDir 是否创建当前月份名文件夹
     * @return 返回全路径
     */
    public static String createDateDir(String rootDir, boolean createYearDir, boolean createMonthDir) {
        if (rootDir == null) {
            throw new IllegalArgumentException("参数错误 rootDir不能为null");
        }
        Calendar now = Calendar.getInstance();
        Path newPath = null;
        if (createYearDir) {
            if (createMonthDir) {
                newPath = Paths.get(rootDir, String.valueOf(now.get(Calendar.YEAR)),
                        String.valueOf(now.get(Calendar.MONTH) + 1));
            } else {
                newPath = Paths.get(rootDir, String.valueOf(now.get(Calendar.YEAR)));
            }
        } else {
            newPath = Paths.get(rootDir, String.valueOf(now.get(Calendar.MONTH) + 1));
        }
        File todayFile = newPath.toFile();
        if (!todayFile.exists()) {
            todayFile.mkdirs();
        }

        return newPath.toString();
    }

    /**
     * 转换所有路径分隔符为unix分隔符/
     * 
     * @param path 文件路径
     * @return
     */
    public static String separatorsToUnix(String path) {
        if (path == null || path.indexOf(Chars.BACKSLASH) == -1) {
            return path;
        }
        return path.replace(Chars.BACKSLASH, Chars.SLASH);
    }

    /**
     * 一次性读取文件内容作为字符串返回
     * 
     * @param filePath 文件路径
     * @param charset 字符集
     * @return
     * @throws InvalidPathException 路径不正确
     * @throws IOException 文件不存在或i/o异常
     */
    public static String readText(String filePath, String charset) throws IOException {
        try (InputStream stream = Files.newInputStream(Paths.get(filePath))) {
            return IOHelper.readText(stream, charset);
        }
    }

    /**
     * 复制文件.如果目标文件存在则替换
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param isReplaceOnExit 如果目标文件存在是否替换
     * @throws IOException 读写或替换文件异常
     */
    public static void copyFile(File sourceFile, File targetFile, boolean isReplaceOnExit) throws IOException {
        if (isReplaceOnExit) {
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(sourceFile.toPath(), targetFile.toPath());
        }
    }

    /**
     * 复制文件.如果目标文件存在则替换
     * 
     * @param sourceFile 源文件路径
     * @param targetFile 目标文件路径
     * @param isReplaceOnExit 如果目标文件存在是否替换
     * @throws IOException 读写或替换文件异常
     */
    public static void copyFile(String sourceFile, String targetFile, boolean isReplaceOnExit) throws IOException {
        if (isReplaceOnExit) {
            Files.copy(Paths.get(sourceFile), Paths.get(targetFile), StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(Paths.get(sourceFile), Paths.get(targetFile));
        }
    }

    /**
     * 检测指定目录下是否已存在同名文件
     * 
     * @param dir 待检测目录
     * @param fileName 文件名
     * @return true存在
     */
    public static boolean existSameNameFile(String dir, String fileName) {
        return Files.exists(Paths.get(dir, fileName));
    }

    /**
     * 清空一个文件夹里的文件，但不删除文件夹本身
     *
     * @param directory 要清空的文件夹
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) { // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * 删除文件，如果文件是目录同时删除下面的文件.
     * <p>
     * 如果其中一个文件没有被删除会抛出异常
     *
     * @param file file or directory to delete, must not be <code>null</code>
     * @throws NullPointerException if the directory is <code>null</code>
     * @throws FileNotFoundException if the file was not found
     * @throws IOException in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * 递归删除目录.
     *
     * @param directory 要删除的目录
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!Files.isSymbolicLink(directory.toPath())) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * 静默删除文件,不抛出异常. 如果文件是目录同时删除下面的文件.
     * 
     * @param file file or directory to delete, can be <code>null</code>
     * @return true成功删除, 否则false
     */
    public static boolean deleteQuietly(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            try {
                if (file.isDirectory()) {
                    cleanDirectory(file);
                }
            } catch (Exception ignored) {
                // 忽略异常
                ignored.printStackTrace();
            }

            try {
                return file.delete();
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    /**
     * 静默删除文件,不抛出异常
     * 
     * @see #deleteDirectory(File)
     * @param filePath 文件路径
     * @return true成功删除, 否则false
     */
    public static boolean deleteQuietly(String filePath) {
        if (StringHelper.isNotEmpty(filePath)) {
            return deleteQuietly(new File(filePath));
        }
        return false;
    }

    /**
     * 获取classpath根路径
     * 
     * @return
     */
    public static Path getClassPath() {
        return toAbsolutePath(StringUtils.EMPTY);
    }

    /**
     * 获取classpath下的文件的绝对路径
     * 
     * @param classPathFile 文件相对于classpath的相对路径,不以/开头
     * @return
     */
    public static Path toAbsolutePath(String classPathFile) {
        if (classPathFile == null) {
            return null;
        }
        ClassLoader classLoader = ClassHelper.getDefaultClassLoader();
        if (!classPathFile.isEmpty() && classPathFile.charAt(0) == Chars.SLASH) {
            classPathFile = classPathFile.substring(1);
        }
        try {
            return Paths.get(classLoader.getResource(classPathFile)
                    .toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取classpath相对路径文件
     * 
     * @param filePath classpath相对路径文件,不以/开头
     * @return 返回文件流InputStream
     */
    public static InputStream readFileFromClassPath(String filePath) {
        ClassLoader classLoader = ClassHelper.getDefaultClassLoader();
        // 修正下路径,classLoader不以/开头
        if (filePath.charAt(0) == Chars.SLASH) {
            filePath = filePath.substring(1);
        }
        return classLoader.getResourceAsStream(filePath);
    }

    /**
     * 从classpath读取文件,返回文本字符串
     * 
     * @param filePath
     * @param charset 字符编码,null使用系统默认编码
     * @return
     * @throws IOException
     */
    public static String readTextFromClassPath(String filePath, Charset charset) throws IOException {
        InputStream stream = readFileFromClassPath(filePath);
        return IOHelper.readText(stream, charset == null ? Charset.defaultCharset() : charset);
    }

}
