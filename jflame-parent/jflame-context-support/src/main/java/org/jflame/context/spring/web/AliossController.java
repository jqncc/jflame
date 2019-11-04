package org.jflame.context.spring.web;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.jflame.context.env.BaseConfig;
import org.jflame.context.filemanager.AliOssFileManager;
import org.jflame.context.filemanager.FileManagerFactory;
import org.jflame.context.filemanager.IFileManager;
import org.jflame.toolkit.common.bean.CallResult;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.StringHelper;

@Controller
@RequestMapping("${file.alioss.signurl:/alioss}}")
public class AliossController extends BaseController {

    /**
     * 前端获取ali oss 直传签名
     * 
     * @return
     */
    @RequestMapping("policySign")
    @ResponseBody
    public CallResult<Map<String,String>> aliossUploadSignature(
            @RequestParam(required = false, name = "dir", defaultValue = "") String dir, HttpServletResponse response) {
        final int expireTime = 300;
        CallResult<Map<String,String>> result = new CallResult<>();
        // 目录不以/开头但以/结尾
        if (StringHelper.isNotEmpty(dir)) {
            if (dir.charAt(0) == FileHelper.UNIX_SEPARATOR) {
                dir = dir.substring(1);
            }
            if (dir.charAt(dir.length() - 1) != FileHelper.UNIX_SEPARATOR) {
                dir = dir + FileHelper.UNIX_SEPARATOR;
            }
        } else {
            if (BaseConfig.isDebugMode()) {
                dir = "test/";
            }
        }

        IFileManager fileManager = FileManagerFactory.getCurrentManager();
        if (fileManager instanceof AliOssFileManager) {
            Map<String,String> respMap = ((AliOssFileManager) fileManager).generatePostSignature(dir, expireTime);
            result.setData(respMap);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST");
        } else {
            result.error()
                    .message("当前文件管理不是ALI OSS");
        }

        return result;
    }
}
