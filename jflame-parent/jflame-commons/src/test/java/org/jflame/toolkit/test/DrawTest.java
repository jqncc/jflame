package org.jflame.toolkit.test;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class DrawTest {

    private ThreadLocalRandom random = ThreadLocalRandom.current();
    private boolean isBorder = true;

    @Test
    public void test() throws IOException {
        int width = 200,height = 80;
        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();
        // 绘制背景
        drawBackground(g, width, height);
        // 绘制随机字符
        String randomCode = RandomStringUtils.random(4, "23456789wertyupkhgfdsazxcvbnm");
        // drawString(randomCode, g, width, height);
        render(randomCode, g, width, height);
        g.dispose();
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
        g.setColor(getRandColor(160, 250));
        g.fillRect(0, 0, width, height);
        // 画边框
        if (isBorder) {
            g.setColor(g.getColor()
                    .darker());
            g.drawRect(0, 0, width - 1, height - 1);
        } else {
            g.drawRect(0, 0, width, height);
        }
        // 随机产生干扰线
        int start_x,start_y,end_x,end_y,i = 0;
        Color[] lineColors = { getRandColor(),getRandColor(),getRandColor() };
        int rndNum = random.nextInt(20, 50);
        for (i = 0; i < rndNum; i++) {
            g.setColor(lineColors[i % 3]);
            start_x = random.nextInt(width);
            start_y = random.nextInt(height);
            end_x = random.nextInt(height);
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
        g.setColor(getRandColor(60, 150));
        Font font = new Font(Font.SANS_SERIF, Font.ITALIC, height - 10);
        Font font1 = new Font(Font.SERIF, Font.PLAIN, height - 4);

        List<Font> _fonts = Arrays.asList(font, font1);
        g.setFont(font);
        // 计算文字居中时x,y坐标
        FontMetrics metrics = g.getFontMetrics(font);
        x = (width - metrics.stringWidth(randomCode)) / 2;
        int ascent = metrics.getAscent();
        int descent = metrics.getDescent();
        int y = (height - (ascent + descent)) / 2 + ascent;
        g.drawString(randomCode, x, y);
    }

    public void render(String word, Graphics2D g, int width, int height) {
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g.setRenderingHints(hints);

        Font font = new Font(Font.SANS_SERIF, Font.ITALIC, height - 10);
        Font font1 = new Font(Font.SERIF, Font.PLAIN, height - 4);

        List<Font> _fonts = Arrays.asList(font, font1);

        FontRenderContext frc = g.getFontRenderContext();
        int startPosX = 25;
        char[] wc = word.toCharArray();
        Random generator = new Random();
        for (char element : wc) {
            char[] itchar = new char[] { element };
            int choiceFont = generator.nextInt(_fonts.size());
            Font itFont = _fonts.get(choiceFont);
            g.setFont(itFont);
            g.setColor(getRandColor(50, 150));
            GlyphVector gv = itFont.createGlyphVector(frc, itchar);
            double charWitdth = gv.getVisualBounds()
                    .getWidth();

            int y = (int) gv.getVisualBounds()
                    .getHeight();
            g.drawChars(itchar, 0, itchar.length, startPosX, y);
            startPosX = startPosX + (int) charWitdth;
        }
    }

    private Color getRandColor() {
        return getRandColor(0, 255);
    }

    private Color getRandColor(int start, int end) {
        if (start < 0) {
            start = 0;
        }
        if (start > 255) {
            start = 200;
        }
        if (end < 0 || end > 255) {
            end = 255;
        }
        return new Color(random.nextInt(start, end), random.nextInt(start, end), random.nextInt(start, end));
    }
}
