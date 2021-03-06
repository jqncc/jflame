package org.jflame.context.filemanager;

import org.apache.commons.lang3.ArrayUtils;

import org.jflame.commons.util.StringHelper;

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

    /**
     * 上传文件总大小,单位byte
     * 
     * @return
     */
    public long getFileTotalSize() {
        return fileTotalSize;
    }

    /**
     * 设置上传文件的总大小,单位byte
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
     * 设置单个文件最大值,单位byte
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

    /**
     * 检查单个文件是否超过大小
     * 
     * @param fileSize 文件大小
     * @return
     */
    public boolean checkSize(final long fileSize) {
        if (this.fileSize > 0) {
            return this.fileSize >= fileSize;
        }
        return true;
    }

    /**
     * 检查所有上传文件是否超过大小
     * 
     * @param totalFileSize 单位byte
     * @return
     */
    public boolean checkTotalSize(final long totalFileSize) {
        if (this.fileTotalSize > 0) {
            return this.fileTotalSize >= totalFileSize;
        }
        return true;
    }

    /**
     * 检查上传文件类型
     * 
     * @param fileExtension 文件扩展名
     * @return
     */
    public boolean checkFileType(final String fileExtension) {
        if (StringHelper.isEmpty(fileExtension)) {
            if (ArrayUtils.isEmpty(allowedFiles)) {
                return true;
            }
        } else {
            if (ArrayUtils.isNotEmpty(allowedFiles) && (ArrayUtils.contains(allowedFiles, fileExtension))) {
                return true;
            }
        }
        return false;
    }
}
