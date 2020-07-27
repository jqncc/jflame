package org.jflame.context.filemanager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jflame.commons.config.BaseConfig;
import org.jflame.commons.util.UrlHelper;

public final class FileManagerFactory {

    private static IFileManager currentManager = null;

    public enum FileManagerMode {
        local,
        fastdfs,
        alioss
    }

    /**
     * 获取当前启用的文件管理类实例
     * 
     * @return
     */
    public static IFileManager getCurrentManager() {
        Lock lock = new ReentrantLock();
        try {
            lock.lock();
            if (currentManager == null) {
                FileManagerMode currentMethod = Enum.valueOf(FileManagerMode.class, BaseConfig.getFileManagerMode());
                if (currentMethod == FileManagerMode.fastdfs) {
                    currentManager = createFastDFSManager();
                } else if (currentMethod == FileManagerMode.alioss) {
                    currentManager = createAliOssManager();
                } else {
                    currentManager = createLocalManager();
                }
            }
        } finally {
            lock.unlock();
        }
        return currentManager;
    }

    /**
     * 创建Alioss文件管理实例
     * 
     * @return
     */
    public static IFileManager createAliOssManager() {
        String endpoint = BaseConfig.getFileServer();
        String accessId = BaseConfig.getAliOSSAccessId();
        String accessSecret = BaseConfig.getAliOSSAccessSecret();
        String bucket = BaseConfig.getAliOSSBucket();
        return new AliOssFileManager(endpoint, accessId, accessSecret, bucket);
    }

    /**
     * 创建FastDFS文件管理实例
     * 
     * @return
     */
    public static IFileManager createFastDFSManager() {
        String confFileName = BaseConfig.getFastDFSConfigFile();
        String fileServer = BaseConfig.getFileServer();
        return new FastDFSFileManager(confFileName, fileServer);
    }

    /**
     * 创建本地磁盘文件管理实例
     * 
     * @return
     */
    public static IFileManager createLocalManager() {
        return new LocalFileManager(BaseConfig.getFileSavePath(), BaseConfig.getFileServer());
    }

    /**
     * 返回当前文件服务器根地址
     * 
     * @return
     */
    public static String getFileServer() {
        return getCurrentManager().getServerUrl();
    }

    /**
     * 给定文件相对路径,返回在当前文件管理系统的绝对路径
     * 
     * @param filePath 文件相对路径
     * @return
     */
    public static String getAbsolutePath(String filePath) {
        if (!UrlHelper.isAbsoluteUri(filePath)) {
            return UrlHelper.mergeUrl(getFileServer(), filePath);
        } else {
            return filePath;
        }
    }

}
