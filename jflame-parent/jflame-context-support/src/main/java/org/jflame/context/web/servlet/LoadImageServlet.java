package org.jflame.context.web.servlet;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.context.env.BaseConfig;
import org.jflame.context.web.WebUtils.MimeImages;
import org.jflame.toolkit.config.PropertiesConfigHolder;
import org.jflame.toolkit.config.ServletParamConfig;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.IOHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * 读取本地图片输出servlet.
 */
@SuppressWarnings("serial")
public class LoadImageServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(LoadImageServlet.class);
    private String savePath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletParamConfig servletParam = new ServletParamConfig(config);
        savePath = servletParam.getString(BaseConfig.CFG_SAVE_PATH);
        if (savePath == null) {
            savePath = PropertiesConfigHolder.getString(BaseConfig.CFG_SAVE_PATH);
        }
        if (StringHelper.isEmpty(savePath)) {
            log.warn("未图片保存路径,设为项目根目录");
            savePath = config.getServletContext()
                    .getRealPath("/");
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
                    log.warn("不支持的图片格式{}", imgRelativePath);
                }
            }
        }
        if (isNotFound) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            if (log.isDebugEnabled()) {
                log.debug("图片不存在:{}", imgRelativePath);
            }
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
