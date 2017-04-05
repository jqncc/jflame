package org.jflame.web.servlet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.toolkit.util.FileHelper;
import org.jflame.toolkit.util.PropertiesHelper;
import org.jflame.toolkit.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读取本地图片输出到客户端
 */
@SuppressWarnings("serial")
public class LoadImageServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(LoadImageServlet.class);

    public final Map<String,String> IMG_MEDIA_TYPE = new HashMap<String,String>();
    private String savePath;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 从URL读取文件路径,拼接本地路径,读取文件流输出到客户端
        String imgRelativePath = request.getPathInfo();
        if (StringHelper.isNotEmpty(imgRelativePath)) {
            Path imgPath = Paths.get(savePath, request.getPathInfo());
            File file = imgPath.toFile();
            if (file.exists()) {
                BufferedImage buffImg = ImageIO.read(file);
                if (buffImg != null) {
                    String ext = FileHelper.getExtension(file.getName(), false);
                    if (StringHelper.isNotEmpty(ext)) {
                        response.setContentType(getMediaType(ext));
                        try (ServletOutputStream servletOutStream = response.getOutputStream();) {
                            ImageIO.write(buffImg, ext, servletOutStream);
                            return;
                        } catch (IOException e) {
                            log.error("输出图片失败:" + imgPath, e);
                        }
                    }
                }
            } else {
                log.info("图片不存在:{}", imgPath);
            }
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    public void init() throws ServletException {
        IMG_MEDIA_TYPE.put("png", "image/png");
        IMG_MEDIA_TYPE.put("gif", "image/gif");
        IMG_MEDIA_TYPE.put("jpg", "image/jpeg");
        IMG_MEDIA_TYPE.put("ico", "image/x-icon");
        IMG_MEDIA_TYPE.put("bmp", "application/x-bmp");
        // 读取system.properties配置
        try {
            this.getInitParameter(savePath);
            PropertiesHelper ploader = new PropertiesHelper("/system.properties");
            savePath = ploader.getProperty("sys.upload.path");
        } catch (IOException e) {
            // throw new ServletException("配置文件system.properties不存在",e);
            savePath = this.getServletContext().getRealPath("/");
            e.printStackTrace();
        }
    }

    String getMediaType(String fileExtName) {
        String mt = IMG_MEDIA_TYPE.get(fileExtName.toLowerCase());
        return mt == null ? IMG_MEDIA_TYPE.get("jpg") : mt;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public void destroy() {
        super.destroy();
    }
}
