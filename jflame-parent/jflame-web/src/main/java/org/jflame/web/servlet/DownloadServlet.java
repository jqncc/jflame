package org.jflame.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.toolkit.config.CommonConfigKeys;
import org.jflame.toolkit.config.PropertiesConfigHolder;
import org.jflame.toolkit.config.ServletParamConfig;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.IOHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.util.WebUtils.MimeImages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件下载servlet
 */
@SuppressWarnings("serial")
public class DownloadServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(DownloadServlet.class);
    private String savePath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletParamConfig servletParam = new ServletParamConfig(config);
        savePath = servletParam.getString(CommonConfigKeys.SAVE_PATH);
        if (StringHelper.isEmpty(savePath)) {
            try {
                savePath = PropertiesConfigHolder.getString(CommonConfigKeys.SAVE_PATH);
            } catch (NullPointerException e) {
                savePath = null;
            }
        }
        if (StringHelper.isEmpty(savePath)) {
            throw new ServletException("未设置图片加载路径");
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 从URL读取文件路径,拼接本地路径,读取文件流输出到客户端
        String imgRelativePath = request.getPathInfo();
        boolean isNotFound = true;
        if (StringHelper.isNotEmpty(imgRelativePath)) {
            Path imgPath = Paths.get(savePath, request.getPathInfo());
            File file = imgPath.toFile();
            if (file.exists()) {
                String ext = FileHelper.getExtension(file.getName(), false);
                if (MimeImages.support(ext)) {
                    response.setContentType(getMediaType(ext));
                    try (FileInputStream imgStream = new FileInputStream(file);
                            ServletOutputStream servletOutStream = response.getOutputStream();) {
                        IOHelper.copy(imgStream, servletOutStream);
                        isNotFound = false;
                    } catch (IOException e) {
                        log.error("输出图片失败:" + imgPath, e);
                        isNotFound = true;
                    }
                } else {
                    log.error("不支持的图片格式{}", imgRelativePath);
                }
            }
        }
        if (isNotFound) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            log.info("not found image:{}", imgRelativePath);
        }
    }

    String getMediaType(String fileExtName) {
        MimeImages mime = MimeImages.valueOf(fileExtName);
        return mime.getMime();
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public void destroy() {
        super.destroy();
    }
}
