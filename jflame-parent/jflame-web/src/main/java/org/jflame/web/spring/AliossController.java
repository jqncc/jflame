package org.jflame.web.spring;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.jflame.commons.common.Chars;
import org.jflame.commons.common.bean.CallResult;
import org.jflame.commons.config.BaseConfig;
import org.jflame.commons.util.StringHelper;
import org.jflame.context.filemanager.AliOssFileManager;
import org.jflame.context.filemanager.FileManagerFactory;
import org.jflame.context.filemanager.IFileManager;

@Controller
@RequestMapping("${file.alioss.signurl:/alioss}")
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
            if (dir.charAt(0) == Chars.SLASH) {
                dir = dir.substring(1);
            }
            if (dir.charAt(dir.length() - 1) != Chars.SLASH) {
                dir = dir + Chars.SLASH;
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
