package org.jflame.web.util;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.InvalidFileNameException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * 文件上传工具类.基于apache-commons-upload包
 * 
 * @author yucan.zhang
 *
 */
public class UploadUtils {

    public static Map<String,List<String>> upload(HttpServletRequest request, UploadItem props) throws UploadException {
        if (!ServletFileUpload.isMultipartContent(request)) {
            return null;
        }
        if (StringUtils.isEmpty(props.getSavePath())) {
            throw new IllegalArgumentException("请指定文件保存路径");
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload fileUpload = new ServletFileUpload(factory);
        // 设置单个文件的最大上传值
        if (props.getFileSize() > 0)
            fileUpload.setFileSizeMax(props.getFileSize());
        // 设置整个request的最大值
        if (props.getFileTotalSize() > 0)
            fileUpload.setSizeMax(props.getFileTotalSize());
        fileUpload.setHeaderEncoding(request.getCharacterEncoding());
        List<FileItem> items = null;
        try {
            items = fileUpload.parseRequest(request);
        } catch (FileUploadException e) {
            throw new UploadException("上传失败", e);
        }

        Map<String,List<String>> resultMap = new HashMap<>();// 文件表单元素名和保存的新名map
        String ext;
        String newName;
        String savePath;
        int i = 1;
        if (props.isCreateDateDir()) {
            savePath = FileHelper.createDateDir(props.getSavePath(), true, true);
        } else {
            savePath = props.getSavePath();
            File dirFile = new File(savePath);
            if (!dirFile.exists())
                dirFile.mkdirs();
        }

        for (FileItem item : items) {
            if (!item.isFormField()) {
                // 超出文件个数忽略后面文件
                if (i++ > props.getFileCount()) {
                    item.delete();
                    continue;
                }

                try {
                    ext = FileHelper.getExtension(item.getName(), true);
                    if (props.isForceNewName()) {
                        newName = StringHelper.uuid() + ext;
                    } else {
                        newName = item.getName();
                        // 存在同名文件,在原名上加随机数
                        if (FileHelper.existSameNameFile(savePath, newName)) {
                            newName = newName + RandomStringUtils.random(4, true, true) + ext;
                        }
                    }
                } catch (InvalidFileNameException e) {
                    ext = FileHelper.getExtension(e.getName(), true);
                    newName = StringHelper.uuid() + ext;
                }
                if (ArrayUtils.isNotEmpty(props.getAllowedFiles())
                        && !ArrayUtils.contains(props.getAllowedFiles(), ext.substring(1))) {
                    throw new UploadException("不允许上传的文件类型:" + ext);
                }
                try {
                    item.write(Paths.get(savePath, newName).toFile());
                } catch (Exception e) {
                    throw new UploadException("上传保存出错" + item.getFieldName(), e);
                }

                if (resultMap.containsKey(item.getFieldName())) {
                    resultMap.get(item.getFieldName()).add(newName);
                } else {
                    List<String> lst = new ArrayList<>();
                    lst.add(newName);
                    resultMap.put(item.getFieldName(), lst);
                }
            }
        }
        return resultMap;
    }

}
