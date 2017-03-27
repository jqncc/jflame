package org.jflame.toolkit.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;

/**
 * zip文件操作工具类
 * 
 * @author zyc
 */
public final class ZipHelper {

    /**
     * 递归压缩文件夹
     * 
     * @param srcRootDir 压缩文件夹根目录的子路径
     * @param file 当前递归压缩的文件或目录对象
     * @param zos 压缩文件存储对象
     * @throws IOException
     */
    private static void zip(String srcRootDir, File file, ZipOutputStream zos) throws IOException {
        if (file == null) {
            return;
        }

        // 如果是文件，则直接压缩该文件
        if (file.isFile()) {
            int count;
            int bufferLen = 1024;
            byte[] data = new byte[bufferLen];

            // 获取文件相对于压缩文件夹根目录的子路径
            String subPath = file.getAbsolutePath();
            int index = subPath.indexOf(srcRootDir);
            if (index != -1) {
                subPath = subPath.substring(srcRootDir.length() + File.separator.length());
            }
            ZipEntry entry = new ZipEntry(subPath);
            zos.putNextEntry(entry);
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
                while ((count = bis.read(data, 0, bufferLen)) != -1) {
                    zos.write(data, 0, count);
                }
            }
            zos.closeEntry();
        } else {
            // 如果是目录，则压缩整个目录. 压缩目录中的文件或子目录
            File[] childFileList = file.listFiles();
            for (int n = 0; n < childFileList.length; n++) {
                childFileList[n].getAbsolutePath().indexOf(file.getAbsolutePath());
                zip(srcRootDir, childFileList[n], zos);
            }
        }
    }

    /**
     * 对文件或文件目录进行压缩
     * 
     * @param srcPath 要压缩的源文件路径。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径
     * @param newZipPath 压缩文件保存的路径。注意：zipPath不能是srcPath路径下的子文件夹
     * @param newZipFileName 压缩文件名
     * @param isDelSameZipFile 如果已存在同名的压缩文件是否删除,不删除则抛出异常
     * @throws IOException
     */
    public static void zip(String srcPath, String newZipPath, String newZipFileName, boolean isDelSameZipFile)
            throws IOException {
        if (StringUtils.isEmpty(srcPath) || StringUtils.isEmpty(newZipPath) || StringUtils.isEmpty(newZipFileName)) {
            throw new IllegalArgumentException("参数不能为空");
        }
        CheckedOutputStream cos = null;
        ZipOutputStream zos = null;
        try {
            File srcFile = new File(srcPath);
            // 判断压缩文件保存的路径是否为源文件路径的子文件夹，防止无限递归压缩
            if (srcFile.isDirectory() && newZipPath.indexOf(srcPath) != -1) {
                throw new IllegalArgumentException("保存路径zipPath不能是待压缩文件路径srcPath下的子目录 ");
            }

            // 判断压缩文件保存的路径是否存在，如果不存在，则创建目录
            File zipDir = new File(newZipPath);
            if (!zipDir.exists() || !zipDir.isDirectory()) {
                zipDir.mkdirs();
            }

            // 创建压缩文件保存的文件对象
            String zipFilePath = newZipPath + File.separator + newZipFileName;
            File zipFile = new File(zipFilePath);
            if (zipFile.exists()) {
                // 检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
                SecurityManager securityManager = new SecurityManager();
                securityManager.checkDelete(zipFilePath);
                // 删除已存在的目标文件
                zipFile.delete();
            }

            cos = new CheckedOutputStream(new FileOutputStream(zipFile), new CRC32());
            zos = new ZipOutputStream(cos);

            // 如果只是压缩一个文件，则需要截取该文件的父目录
            String srcRootDir = srcPath;
            if (srcFile.isFile()) {
                int index = srcPath.lastIndexOf(File.separator);
                if (index != -1) {
                    srcRootDir = srcPath.substring(0, index);
                }
            }
            // 调用递归压缩方法进行目录或文件压缩
            zip(srcRootDir, srcFile, zos);
            zos.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                zos = null;
                e.printStackTrace();
            }
        }
    }

    /**
     * 将输入的压缩文件流解压到解压目录(unzipDir)
     * 
     * @param unzipDir 解压目录
     * @param in 压缩文件流
     * @throws IOException
     */
    public static void unzip(File unzipDir, InputStream in) throws IOException {
        unzipDir.mkdirs();
        ZipEntry entry = null;
        try (ZipInputStream zin = new ZipInputStream(in);) {
            while ((entry = zin.getNextEntry()) != null) {
                File path = new File(unzipDir, entry.getName());
                if (entry.isDirectory()) {
                    path.mkdirs();
                } else {
                    File parentFile = path.getAbsoluteFile().getParentFile();
                    if (parentFile != null && !parentFile.equals(path.getAbsoluteFile())) {
                        parentFile.mkdirs();
                    }
                    try (FileOutputStream output = new FileOutputStream(path)) {
                        byte[] buf = new byte[1024];
                        int n = 0;
                        while ((n = zin.read(buf)) != -1) {
                            output.write(buf, 0, n);
                        }
                    } catch (IOException e) {
                        throw e;
                    }
                }
            }
        }catch (IOException e) {
            throw e;
        }
    }

    /**
     * 解压文件
     * 
     * @param unzipDir 解压目录
     * @param zipFile 压缩文件
     * @throws IOException
     */
    public static void unzip(File unzipDir, File zipFile) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(zipFile))) {
            unzip(unzipDir, in);
        } catch (IOException e) {
            throw e;
        }
    }

}