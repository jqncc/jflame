package org.jflame.toolkit.test;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class DrawTest {

    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @Test
    public void test() throws IOException {
        int width = 200,height = 80;
        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();
        // 绘制背景
        drawBackground(g, width, height);
        // 绘制随机字符
        String randomCode = RandomStringUtils.random(4, "0123456789qwertyupkhgfdsazxcvbnm");
        drawString(randomCode, g, width, height);
        Path img = Paths.get("D:\\datacenter\\rnd.jpg");
        /* if (!Files.exists(img)) {
            Files.createFile(img);
        }*/
        try (OutputStream output = Files.newOutputStream(img, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            ImageIO.write(buffImg, "jpg", output);
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 画背景
     * 
     * @param g
     * @param width
     * @param height
     */
    private void drawBackground(Graphics2D g, int width, int height) {
        g.setColor(getRandColor(200, 250, random));
        g.fillRect(0, 0, width, height);
        // 画边框
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, width - 1, height - 1);
        // 随机产生干扰线
        int start_x,start_y,end_x,end_y,i = 0;
        for (i = 0; i < 60; i++) {
            g.setColor(getRandColor(80, 120, random));
            start_x = random.nextInt(width - 30);
            start_y = random.nextInt(height);
            end_x = random.nextInt(width);
            end_y = random.nextInt(height);
            g.drawLine(start_x, start_y, start_x + end_x, start_y + end_y);
        }
    }

    /**
     * 画随机码
     * 
     * @param randomCode
     * @param g
     * @param width
     * @param height
     */
    private void drawString(String randomCode, Graphics2D g, int width, int height) {
        int x = 0;
        g.setColor(new Color(90, 80 + random.nextInt(30), 60 + random.nextInt(30)));
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, height - 4);
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
}
