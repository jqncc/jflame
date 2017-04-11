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
import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.ISysConfig;
import org.jflame.web.SpiFactory;
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

    public final Map<String,String> imgMediaType = new HashMap<String,String>();
    private final String SAVE_PATH_IMAGE_CONFIGKEY = "save.path.image";
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
        imgMediaType.put("png", "image/png");
        imgMediaType.put("gif", "image/gif");
        imgMediaType.put("jpg", "image/jpeg");
        imgMediaType.put("ico", "image/x-icon");
        imgMediaType.put("bmp", "application/x-bmp");
        ISysConfig sysConfig = SpiFactory.loadSingleService(ISysConfig.class);
        if (sysConfig != null) {
            savePath = (String) sysConfig.getConfigParam(SAVE_PATH_IMAGE_CONFIGKEY);
        } else {
            log.error("未找到ISysConfig实现类");
        }
        if (StringHelper.isEmpty(savePath)) {
            savePath = this.getServletContext().getRealPath("/upload/images");
            log.info("set image save path:{}", savePath);
        }
    }

    String getMediaType(String fileExtName) {
        String mt = imgMediaType.get(fileExtName.toLowerCase());
        return mt == null ? imgMediaType.get("jpg") : mt;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public void destroy() {
        super.destroy();
    }
}
