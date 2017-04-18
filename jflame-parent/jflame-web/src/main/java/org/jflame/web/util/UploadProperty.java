package org.jflame.web.util;

/**
 * 上传文件参数
 * 
 * @author zyc
 */
public class UploadProperty {

    private long fileTotalSize = -1;// 允许上传的总文件最大值
    private long fileSize = -1;     // 允许上传的单个文件最大值
    private String[] allowedFiles;  // 允许上传的文件类型含扩展名
    private int fileCount = -1;     // 允许上传文件最大个数,默认-1表示无限制
    private String savePath;        // 保存路径
    private boolean forceNewName = false;   //强制重新命名

    public long getFileTotalSize() {
        return fileTotalSize;
    }

    public void setFileTotalSize(long fileTotalSize) {
        this.fileTotalSize = fileTotalSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String[] getAllowedFiles() {
        return allowedFiles;
    }

    public void setAllowedFiles(String[] allowedFiles) {
        this.allowedFiles = allowedFiles;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean isForceNewName() {
        return forceNewName;
    }

    public void setForceNewName(boolean forceNewName) {
        this.forceNewName = forceNewName;
    }

}
