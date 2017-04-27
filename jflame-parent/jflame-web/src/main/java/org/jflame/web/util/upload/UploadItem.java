package org.jflame.web.util;

/**
 * 上传文件参数
 * 
 * @author zyc
 */
public class UploadItem {

    private long fileTotalSize = 0;// 允许上传的总文件最大值
    private long fileSize = 0; // 允许上传的单个文件最大值
    private String[] allowedFiles; // 允许上传的文件扩展名,不含.
    private int fileCount = 0; // 允许上传文件最大个数,默认0表示无限制
    private String savePath; // 保存路径
    private boolean createDateDir = false; // 是否在savePath建立当前年月的文件夹作为最终保存路径
    private boolean forceNewName = false; // 强制重新命名

    /**
     * 上传文件总大小
     * 
     * @return
     */
    public long getFileTotalSize() {
        return fileTotalSize;
    }

    /**
     * 设置上传文件的总大小
     * 
     * @param fileTotalSize
     */
    public void setFileTotalSize(long fileTotalSize) {
        this.fileTotalSize = fileTotalSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    /**
     * 设置单个文件最大值
     * 
     * @param fileSize
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String[] getAllowedFiles() {
        return allowedFiles;
    }

    /**
     * 设置允许上传的文件扩展名,不含.
     * 
     * @param allowedFiles
     */
    public void setAllowedFiles(String[] allowedFiles) {
        this.allowedFiles = allowedFiles;
    }

    public int getFileCount() {
        return fileCount;
    }

    /**
     * 设置上传文件最大个数
     * 
     * @param fileCount
     */
    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public String getSavePath() {
        return savePath;
    }

    /**
     * 设置文件保存路径
     * 
     * @param savePath 保存路径,绝对路径
     */
    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean isForceNewName() {
        return forceNewName;
    }

    /**
     * 设置是否重命名文件
     * 
     * @param forceNewName true重命名文件,将生成一个uuid重新命名文件
     */
    public void setForceNewName(boolean forceNewName) {
        this.forceNewName = forceNewName;
    }

    public boolean isCreateDateDir() {
        return createDateDir;
    }

    /**
     * 是否在保存路径savePath下按年月创建文件夹作为最终保存路径
     * 
     * @param createDateDir
     */
    public void setCreateDateDir(boolean createDateDir) {
        this.createDateDir = createDateDir;
    }

}
