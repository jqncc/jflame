package org.jflame.web.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.codec.TranscodeHelper;
import org.jflame.commons.config.ConfigKey;
import org.jflame.commons.config.PropertiesConfigHolder;
import org.jflame.commons.config.ServletParamConfig;
import org.jflame.commons.util.ArrayHelper;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.UrlHelper;
import org.jflame.commons.util.file.FileHelper;
import org.jflame.context.filemanager.FileManagerFactory;
import org.jflame.web.WebUtils;

/**
 * 文件下载servlet
 */
@SuppressWarnings("serial")
public class DownloadServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(DownloadServlet.class);
    private String[] allowDownFiles = { "zip","rar","xls","xlsx","doc","docx","ppt","pptx" };
    // private String savePath;

    /**
     * 取得配置参数,先从servlet配置获取没有再从配置文件获取
     * 
     * @param servletParam
     * @param name
     * @return
     */
    String getConfParam(ServletParamConfig servletParam, ConfigKey<String> configKey) {
        String paramValue = servletParam.getString(configKey);
        if (paramValue == null) {
            paramValue = PropertiesConfigHolder.getString(configKey);
        }
        return paramValue;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletParamConfig servletParam = new ServletParamConfig(config);
        /*savePath = getConfParam(servletParam, BaseConfig.CFG_SAVE_PATH);
        if (StringHelper.isEmpty(savePath)) {
           log.warn("未设置文件下载根路径,设为项目根目录");
           savePath = config.getServletContext()
                   .getRealPath("/");
        }*/
        allowDownFiles = servletParam.getStringArray(new ConfigKey<String[]>("file.allowDownFiles"));

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String downFile = request.getParameter("file");
        if (StringHelper.isEmpty(downFile)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        downFile = TranscodeHelper.urlDecode(downFile);
        if (!isAllowedDownload(downFile)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        ServletOutputStream output = null;
        try {
            byte[] fileBytes;
            if (UrlHelper.isURL(downFile)) {
                fileBytes = FileManagerFactory.getCurrentManager()
                        .readBytes(downFile);
            } else {
                fileBytes = FileManagerFactory.createLocalManager()
                        .readBytes(downFile);
            }

            WebUtils.setFileDownloadHeader(response, FileHelper.getFilename(downFile), (long) fileBytes.length);
            output = response.getOutputStream();
            IOHelper.write(fileBytes, output);
            output.flush();
        } catch (FileNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (IOException e) {
            log.warn("下载文件异常:" + downFile, e);
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "文件读取异常");
        } finally {
            if (output != null) {
                output.close();
            }
        }

    }

    /**
     * 是否允许下载
     * 
     * @param filePath
     * @return
     */
    boolean isAllowedDownload(String filePath) {
        if (ArrayHelper.isEmpty(allowDownFiles)) {
            return true;
        }
        String ext = FileHelper.getExtension(filePath, false);
        if (!ArrayUtils.contains(allowDownFiles, ext)) {
            return false;
        }
        return true;
    }

    public void destroy() {
        super.destroy();
    }
}
