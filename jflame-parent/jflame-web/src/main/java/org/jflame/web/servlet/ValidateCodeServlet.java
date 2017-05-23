package org.jflame.web.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jflame.toolkit.reflect.SpiFactory;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.config.ISysConfig;
import org.jflame.web.config.WebConstant.MimeImages;
import org.jflame.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 生成随机验证码图片servlet.
 * <p>
 * 1. 可使用请求参数定制生成: <br>
 * w=宽度(默认80),h=高度(默认24),c=生成的字符个数(默认4),n=随机码存储名称. 示例:<br>
 * 生成宽度为120高为30字符数5个的验证图片 /valid?w=120&h30&c=5<br>
 * 2. 随机码存储名称，默认"validcode"，必须是给定配置validcode.names参数中的名称，配置文件由ISysConfig接口读取<br>
 * 
 * @author yucan.zhang
 */
@WebServlet(value="/validcode")
@SuppressWarnings("serial")
public class ValidateCodeServlet extends HttpServlet {
    private final Logger log = LoggerFactory.getLogger(ValidateCodeServlet.class);

    private final int defaultWidth = 80;// 缺省图片宽
    private final int defaultHeight = 24;// 缺省图片高
    private final int defaultCount = 4;// 缺省字符个数
    private final String defaultCodeName = "validcode";
    private final String CODE_NAMES_CONFIGKEY = "validcode.names";
    private List<String> initCodeNames = new ArrayList<>();// 验证码限定名称，默认"validcode"
    private ThreadLocalRandom random = ThreadLocalRandom.current();
    
    public ValidateCodeServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int width;
        int height;
        int count;
        String codeName;
        String paramWidth = request.getParameter("w");
        String paramHeight = request.getParameter("h");
        String paramCount = request.getParameter("c");
        String paramCodeName = request.getParameter("n");
        if (StringHelper.isNotEmpty(paramCodeName)) {
            if (initCodeNames.contains(paramCodeName)) {
                codeName = paramCodeName;
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } else {
            codeName = initCodeNames.get(0);// 默认第一个
        }
        count = NumberUtils.toInt(paramCount, defaultCount);
        width = NumberUtils.toInt(paramWidth, defaultWidth);
        height = NumberUtils.toInt(paramHeight, defaultHeight);
        // 限制图片高宽及字符数
        if (count < 2 || count > 10) {
            count = defaultCount;
        }
        if (width < 50 || width > 300) {
            width = defaultWidth;
        }
        if (height < 20 || height > 200) {
            height = defaultHeight;
        }

        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();
        // 绘制背景
        drawBackground(g, width, height);
        // 绘制随机字符
        String randomCode = RandomStringUtils.random(count, true, true);
        drawString(randomCode, g, width, height);
        // 禁止图像缓存
        WebUtils.setDisableCacheHeader(response);
        response.setContentType(MimeImages.jpg.getMime());
        ServletOutputStream sos = response.getOutputStream();
        ImageIO.write(buffImg, MimeImages.jpg.name(), sos);
        sos.close();
        // 将验证码保存到session中
        HttpSession session = request.getSession();
        session.setAttribute(codeName, randomCode);
    }

    /**
     * 画背景
     * @param g
     * @param width
     * @param height
     */
    private void drawBackground(Graphics2D g, int width, int height) {
        g.setColor(getRandColor(220, 250, random));
        g.fillRect(0, 0, width, height);
        // 画边框
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, width - 1, height - 1);
        // 随机产生干扰线
        int start_x,start_y,end_x,end_y,i = 0;
        for (i = 0; i < 15; i++) {
            g.setColor(getRandColor(50, 150, random));
            start_x = random.nextInt(width);
            start_y = random.nextInt(height);
            end_x = random.nextInt(width);
            end_y = random.nextInt(height);
            g.drawLine(start_x, start_y, start_x + end_x, start_y + end_y);
        }
    }

    /**
     * 画随机码
     * @param randomCode
     * @param g
     * @param width
     * @param height
     */
    private void drawString(String randomCode, Graphics2D g, int width, int height) {
        int x = 0;
        g.setColor(new Color(150, 80 + random.nextInt(50), 50 + random.nextInt(30)));
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, height - 4);
        g.setFont(font);
        // 计算文字居中时x,y坐标
        FontMetrics metrics = g.getFontMetrics(font);
        x = (width - metrics.stringWidth(randomCode)) / 2;
        int ascent = metrics.getAscent();
        int descent = metrics.getDescent();
        int y = (height - (ascent + descent)) / 2 + ascent;
        g.drawString(randomCode, x, y);
    }

    private Color getRandColor(int f, int b, Random random) {
        return new Color(f + random.nextInt(b - f), f + random.nextInt(b - f), f + random.nextInt(b - f));
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        initCodeNames.add(defaultCodeName);
        ISysConfig sysConfig = SpiFactory.getSingleBean(ISysConfig.class);
        if (sysConfig != null) {
            String initNameParam = sysConfig.getTextParam(CODE_NAMES_CONFIGKEY);
            if (StringHelper.isNotEmpty(initNameParam)) {
                String[] names = initNameParam.trim().split(",");
                if (ArrayUtils.isNotEmpty(names)) {
                    Collections.addAll(initCodeNames, names);
                }
            }
        }else {
            log.error("未找到ISysConfig实现类");
        }
    }
}
