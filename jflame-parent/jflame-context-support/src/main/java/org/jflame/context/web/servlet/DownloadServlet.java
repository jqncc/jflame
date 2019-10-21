package org.jflame.context.web.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.context.env.BaseConfig;
import org.jflame.context.web.WebUtils;
import org.jflame.toolkit.config.ConfigKey;
import org.jflame.toolkit.config.PropertiesConfigHolder;
import org.jflame.toolkit.config.ServletParamConfig;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.IOHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * 文件下载servlet
 */
@SuppressWarnings("serial")
public class DownloadServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(DownloadServlet.class);
    private String[] allowDownFiles = ArrayUtils.addAll(WebUtils.IMAGE_EXTS,
            new String[] { "zip","tar","tar.gz","rar","gzip","pdf","doc","docx","xls","xlsx","ppt","pptx","csv" });
    private String savePath;

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
        savePath = getConfParam(servletParam, BaseConfig.CFG_SAVE_PATH);
        if (StringHelper.isEmpty(savePath)) {
            log.warn("未设置文件下载根路径,设为项目根目录");
            savePath = config.getServletContext()
                    .getRealPath("/");
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 从URL读取文件路径,拼接本地路径,读取文件流输出到客户端
        String imgRelativePath = request.getPathInfo();
        boolean isNotFound = true;
        if (StringHelper.isNotEmpty(imgRelativePath)) {
            Path downFilePath = Paths.get(savePath, request.getPathInfo());
            File file = downFilePath.toFile();
            if (file.exists()) {
                if (isAllowedDownload(file)) {
                    BufferedReader reader = null;
                    ServletOutputStream output = null;
                    try {
                        WebUtils.setFileDownloadHeader(response, file.getName(), file.length());
                        output = response.getOutputStream();
                        reader = Files.newBufferedReader(file.toPath());
                        IOHelper.copy(reader, output);
                        isNotFound = false;
                    } catch (IOException e) {
                        log.warn("下载文件异常:" + downFilePath, e);
                        response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "文件读取异常");
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                        if (output != null) {
                            output.close();
                        }
                    }
                } else {
                    log.warn("不支持的图片格式{}", imgRelativePath);
                }
            }
        }
        if (isNotFound) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            log.info("not found image:{}", imgRelativePath);
        }
    }

    /**
     * 是否允许下载
     * 
     * @param file
     * @return
     */
    boolean isAllowedDownload(File file) {
        String ext = FileHelper.getExtension(file.getName(), false);
        if (!ArrayUtils.contains(allowDownFiles, ext)) {
            return false;
        }
        return true;
    }

    public void destroy() {
        super.destroy();
    }
}
