package org.jflame.web.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.commons.config.BaseConfig;
import org.jflame.commons.config.ConfigKey;
import org.jflame.commons.config.ServletParamConfig;
import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.Chars;
import org.jflame.commons.util.StringHelper;
import org.jflame.context.filemanager.AliOssFileManager;
import org.jflame.context.filemanager.FileManagerFactory;
import org.jflame.context.filemanager.IFileManager;
import org.jflame.web.WebUtils;

/**
 * alioss 客户端直传签名获取Servlet
 * 
 * @author yucan.zhang
 */
@SuppressWarnings("serial")
public class AliossServlet extends HttpServlet {

    private String defaultDir;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletParamConfig servletParam = new ServletParamConfig(config);
        defaultDir = servletParam.getString(new ConfigKey<String>("file.alioss.upload-dir"));
        if (StringHelper.isNotEmpty(defaultDir)) {
            defaultDir = cleanPath(defaultDir);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final int expireTime = 300;
        String dir = request.getParameter("dir");
        CallResult<Map<String,String>> result = new CallResult<>();
        // 目录不以/开头但以/结尾
        if (StringHelper.isNotEmpty(dir)) {
            dir = cleanPath(dir);
        } else {
            if (BaseConfig.isDebugMode()) {
                dir = "test/";
            } else {
                dir = defaultDir;
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
        WebUtils.outJson(response, result);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    private String cleanPath(String dir) {
        if (dir.charAt(0) == Chars.SLASH) {
            dir = dir.substring(1);
        }
        if (dir.charAt(dir.length() - 1) != Chars.SLASH) {
            dir = dir + Chars.SLASH;
        }
        return dir;
    }
}
