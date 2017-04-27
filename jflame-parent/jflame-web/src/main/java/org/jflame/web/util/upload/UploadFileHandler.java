package org.jflame.web.util.upload;

import java.io.File;

/**
 * 上传文件处理回调处理接口
 * @author yucan.zhang
 *
 */
public interface UploadFileHandler {
    /**
     * 文件上传后对文件执行操作
     * @param fieldName 表单域名
     * @param uploadedFile 已上传文件
     */
    void afterUpload(String fieldName,File uploadedFile);
}
