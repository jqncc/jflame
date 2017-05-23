package com.ghgcn.xxx.action;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.jflame.mvc.support.MyCustomDateEditor;
import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.common.bean.CallResult;
import org.jflame.toolkit.common.bean.CallResult.ResultEnum;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.web.util.webfile.UploadDownException;
import org.jflame.web.util.webfile.UploadItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.multipart.MultipartFile;

/**
 * controller基类
 * 
 * @author yucan.zhang
 */
public abstract class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @InitBinder
    protected void ininBinder(WebDataBinder binder) {
        // 注册时间转换器
        MyCustomDateEditor dateEditor = new MyCustomDateEditor();
        binder.registerCustomEditor(java.util.Date.class, dateEditor);
        binder.registerCustomEditor(java.sql.Timestamp.class, dateEditor);
    }

    /**
     * 将errors对象转换为CallResult输出,验证错误转为map
     * 
     * @param error 错误对象
     * @param result
     * @see org.springframework.validation.Errors
     * @return
     */
    protected void convertError(Errors error, CallResult result) {
        if (error.hasErrors()) {
            result.setResult(ResultEnum.PARAM_ERROR);
            Map<String,String> errMap = new HashMap<String,String>();
            if (error.hasGlobalErrors()) {
                String globalMsg = "";
                for (ObjectError ferr : error.getGlobalErrors()) {
                    globalMsg = globalMsg + ferr.getDefaultMessage() + ";";
                }
                errMap.put("_global_msg", globalMsg);
            }
            if (error.hasFieldErrors()) {
                for (FieldError ferr : error.getFieldErrors()) {
                    errMap.put(ferr.getField(), ferr.getDefaultMessage());
                }
            }
            result.setData(errMap);
        }
    }

    /**
     * 保存文件,返回相对于给定保存文件夹的路径
     * 
     * @param uploadFile
     * @param props
     * @return
     * @throws UploadDownException
     */
    protected String saveFile(MultipartFile uploadFile, UploadItem props) throws UploadDownException {
        if (uploadFile.isEmpty()) {
            throw new UploadDownException("上传文件大小为0" + uploadFile.getName());
        }
        if (!props.checkSize(uploadFile.getSize())) {
            throw new UploadDownException("上传文件" + uploadFile.getName() + "大小超过" + props.getFileSize());
        }
        String newName = uploadFile.getOriginalFilename();// 最终文件名
        Path basePath = Paths.get(props.getSavePath());// 给定的根路径
        String ext = FileHelper.getExtension(newName, true);
        if (!props.checkFileType(ext.isEmpty() ? ext : ext.substring(1))) {
            throw new UploadDownException("不允许上传的文件类型:" + ext);
        }
        String savePath = props.createSavePath();// 最终保存路径
        newName = TranscodeHelper.urlencode(newName);// 扩展名
        // 存在同名文件,或编码后文件名太长，重命名
        if (FileHelper.existSameNameFile(savePath, newName) || newName.length() > 50) {
            newName = String.valueOf(newName.hashCode()) + System.nanoTime() + ext;
        }
        Path finalPath = Paths.get(savePath, newName);
        try {
            uploadFile.transferTo(finalPath.toFile());
        } catch (IllegalStateException | IOException e) {
            throw new UploadDownException("保存上传文件失败" + uploadFile.getOriginalFilename(), e);
        }
        String relativelyPath = basePath.relativize(finalPath).toString().replace(FileHelper.WIN_SEPARATOR,
                FileHelper.UNIX_SEPARATOR);
        return relativelyPath;
    }
}
