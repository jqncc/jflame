package org.jflame.web.spring;

import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;
import org.jflame.commons.model.Chars;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.file.FileHelper;
import org.jflame.context.filemanager.FileManagerFactory;
import org.jflame.context.filemanager.UploadItem;
import org.jflame.web.WebUtils;

public class UploadController extends BaseController {

    /**
     * 保存文件,返回相对于给定保存文件夹的路径
     * 
     * @param uploadFile
     * @param props
     * @return
     */
    protected CallResult<String> uploadFile(MultipartFile uploadFile, UploadItem props) {
        String originalFileName = uploadFile.getOriginalFilename();
        // 部分浏览器可能带路径,删除路径部分
        if (originalFileName.indexOf(Chars.SLASH) >= 0) {
            originalFileName = FileHelper.getFilename(originalFileName);
        }
        CallResult<String> result = new CallResult<>();
        String ext = FileHelper.getExtension(originalFileName, false);
        Optional<String> error = checkFile(ext, uploadFile.getSize(), props);
        if (error.isPresent()) {
            result.setResult(ResultEnum.PARAM_ERROR.getStatus(), error.get());
            return result;
        }
        if (StringHelper.isEmpty(props.getSavePath())) {
            props.setSavePath(YearMonth.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMM")));
        }
        try {
            String newFile = FileManagerFactory.getCurrentManager()
                    .save(uploadFile.getBytes(), props.getSavePath(), ext, null);
            result.setData(newFile);
        } catch (IOException e) {
            logger.error("文件上传异常" + originalFileName, e);
        }
        return result;
    }

    /**
     * 上传图片
     * 
     * @param imgFile 图片
     * @param maxSize 图片最大尺寸 单位K
     * @return 上传后相对路径
     * @throws UploadDownException 上传文件不符合要求或传输异常
     */
    protected CallResult<String> uploadImage(MultipartFile imgFile, int maxSize) {
        UploadItem props = new UploadItem();
        props.setAllowedFiles(WebUtils.IMAGE_EXTS);
        props.setFileSize(maxSize * 1024);
        return uploadFile(imgFile, props);
    }

    /**
     * 检测上传文件是否符合要求
     * 
     * @param ext 文件类,不含'.'号
     * @param fileSize 文件大小
     * @param uploadProperty 上传属性
     * @return 校验有错返回错误描述
     */
    protected Optional<String> checkFile(String ext, long fileSize, UploadItem uploadProperty) {
        String errorMsg = null;
        if (!uploadProperty.checkFileType(ext)) {
            errorMsg = "不允许上传的文件类型:";
        }
        if (fileSize == 0) {
            errorMsg = "上传文件大小为0";
        }
        if (!uploadProperty.checkSize(fileSize)) {
            errorMsg = "文件超过限制大小:" + uploadProperty.getFileSize();
        }
        return Optional.ofNullable(errorMsg);
    }
}
