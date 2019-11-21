package org.jflame.context.filemanager;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.file.FileHelper;
import org.jflame.commons.util.StringHelper;

public abstract class BaseFileManager implements IFileManager {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 生成新的唯一文件名
     * 
     * @param fileName 原文件名
     * @param ext 扩展名
     * @return
     */
    protected String createNewFileName(String ext) {
        String name = StringUtils.reverse(StringHelper.millisAndRandomNo(3));
        if (StringHelper.isNotEmpty(ext)) {
            if (ext.charAt(0) == '.') {
                return name + ext;
            }
            return name + '.' + ext;
        }
        return name;
    }

    protected String getFileId(String filePath) {
        if (filePath.startsWith("http")) {
            URI uri = URI.create(filePath);
            filePath = uri.getPath();
        }
        if (filePath.charAt(0) == FileHelper.UNIX_SEPARATOR) {
            filePath = filePath.substring(1);
        }
        return filePath;
    }
}
