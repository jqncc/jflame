package org.jflame.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.IOHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.config.WebConstant.MimeImages;
import org.jflame.web.util.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读取本地图片输出servlet.
 * <p>
 * 图片读取路径,优先从ISysConfig接口实现类取配置参数"save.path.image"，如果获取失败使用本应用要根目录下upload/images目录.<br>
 * ISysConfig实现类使用SPI方式接入
 */
@SuppressWarnings("serial")
public class LoadImageServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(LoadImageServlet.class);
    private String savePath;

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

    public void init() throws ServletException {
        savePath = FunctionUtils.getImgSavePath();
        if (StringHelper.isEmpty(savePath)) {
            savePath = this.getServletContext().getRealPath("/upload/images");
        }
        log.info("set image save path:{}", savePath);
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
