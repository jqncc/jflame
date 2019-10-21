package org.jflame.context.filemanager;

import org.jflame.context.env.BaseConfig;

public final class FileManagerFactory {

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
        IFileManager fileManager;
        FileManagerMode currentMethod = BaseConfig.getFileManagerMode();
        if (currentMethod == FileManagerMode.fastdfs) {
            fileManager = createFastDFSManager();
        } else if (currentMethod == FileManagerMode.alioss) {
            fileManager = createAliOssManager();
        } else {
            fileManager = createLocalManager();
        }

        return fileManager;
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
        return new FastDFSFileManager(fileServer, confFileName);
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

}
