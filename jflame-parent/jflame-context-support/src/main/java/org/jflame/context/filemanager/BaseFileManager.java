package org.jflame.context.filemanager;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.key.IDHelper;
import org.jflame.commons.model.Chars;
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
        String name = StringUtils.reverse(IDHelper.millisAndRandomNo(3));
        if (StringHelper.isNotEmpty(ext)) {
            if (ext.charAt(0) == '.') {
                return name + ext;
            }
            return name + '.' + ext;
        }
        return name;
    }

    protected String getFileId(String filePath) {
        String serverUrl = getServerUrl();
        if (StringHelper.isNotEmpty(serverUrl) && filePath.startsWith(serverUrl)) {
            filePath = StringUtils.removeFirst(filePath, serverUrl);
        } else {
            if (filePath.startsWith("http")) {
                URI uri = URI.create(filePath);
                filePath = uri.getPath();
            }
        }
        if (filePath.charAt(0) == Chars.BACKSLASH) {
            filePath = filePath.substring(1);
        }
        return filePath;
    }
}
