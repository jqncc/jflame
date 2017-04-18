package org.jflame.toolkit.file;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.jflame.toolkit.util.IOHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * 图片文件处理工具类，使用java原生ImageIO操作.支持格式rgb格式的jpg,gif,png,bmp
 * <p>
 * <strong> 注：由于java内置ImageIO支持图片格式有限，所以会有图片失真问题，如类似CMYK格式jpg压缩后加了一层红色.解决该问题：</strong><br>
 * 1. 试着升级到jdk1.8以上，会支持更多格式<br>
 * 2. 使用JAI(java advanced imagi/ojava高级图片处理工具),可在系统安装JAI tools<br>
 * 3. 修改本类使用第三方图片读取解析包读取图片，如commons imaging<br>
 * 4. 使用第三方图片处理包,如ali simpleimage工具包,thumbnails包也有该问题
 * 
 * @author zyc
 */
public final class ImageHelper {

    /**
     * 等比缩放图片到指定最大高宽范围内 .新文件名等于"源图名_w宽_h高"
     * 
     * @param srcImgUrl 源图片全路径
     * @param newImgPath 缩放图片保存目录,为空表示与原图片同一目录
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 图片最终名
     * @throws IOException
     */
    public static String scale(String srcImgUrl, String newImgPath, int maxWidth, int maxHeight) throws IOException {
        return scale(srcImgUrl, newImgPath, null, maxWidth, maxHeight);
    }

    /**
     * 等比缩放图片到指定最大高宽范围内 .
     * <p>
     * 缩放规则:最大高宽,其中一个为零时将以不为零的为标准.都大于零时以与原尺寸最小差为标准.
     * 
     * @param srcImgUrl 源图片全路径
     * @param newImgPath 缩放图片保存目录,为空表示与原图片同一目录
     * @param newImgName 缩放图片新名称,不含扩展名.如果为空将新文件名等于"源图名_w宽_h高"
     * @param maxWidth 最大宽度.
     * @param maxHeight 最大高度.
     * @return 图片最终名
     * @throws IOException IOException
     */
    public static String scale(String srcImgUrl, String newImgPath, String newImgName, int maxWidth, int maxHeight)
            throws IOException {
        File srcFile = new File(srcImgUrl);
        if (!srcFile.exists()) {
            throw new IllegalArgumentException(srcImgUrl + "文件不存在");
        }
        if (maxWidth <= 0 || maxHeight <= 0) {
            throw new IllegalArgumentException("图片最大宽度和最大高度必须有一个大于0");
        }

        // 未指定保存路径,默认与原图片路径相同
        if (newImgPath == null || newImgPath.isEmpty()) {
            newImgPath = srcFile.getParent();
        }
        if (StringHelper.isEmpty(newImgName)) {
            String oldName = srcFile.getName();
            newImgName = StringHelper.insertAt(oldName, "_w" + maxWidth + "_h" + maxHeight, oldName.lastIndexOf("."));
        } else {
            newImgName = newImgName + FileHelper.getExtension(srcImgUrl, true);
        }
        FileInputStream imageStream = null;
        BufferedImage srcImage = null;
        int newWidth = 0;
        int newHeight = 0;
        try {
            imageStream = new FileInputStream(srcImgUrl);
            // srcImage = Imaging.getBufferedImage(imageStream);使用commons imaging包读取支持更多格式
            srcImage = ImageIO.read(imageStream);
            if (null == srcImage) {
                throw new IllegalArgumentException("文件不能以图片方式读入" + srcImgUrl);
            }
            int oldWidth = srcImage.getWidth();// 原宽
            int oldHeight = srcImage.getHeight();// 原长

            if (oldWidth > maxWidth || oldHeight > maxHeight) {
                if (((float) maxHeight / oldHeight) < ((float) maxWidth / oldWidth)) {
                    newHeight = maxHeight > oldHeight ? oldHeight : maxHeight;
                    newWidth = (int) Math.round(((float) maxHeight / oldHeight) * oldWidth);
                    if (newWidth > maxWidth) {
                        newWidth = maxWidth;
                    }
                } else {
                    newWidth = maxWidth > oldWidth ? oldWidth : maxWidth;
                    newHeight = (int) Math.round(((float) maxWidth / oldWidth) * oldHeight);
                    if (newHeight > maxHeight) {
                        newHeight = maxHeight;
                    }
                }
                // 缩放生成新图
                BufferedImage newImageBuf = zoomedFixedSize(srcImage, newWidth, newHeight);
                return saveImage(newImgPath, newImgName, newImageBuf);
            } else {
                // 如果不需要缩放.直接复制文件
                File toFile = Paths.get(newImgPath, newImgName).toFile();
                FileHelper.copyFile(srcFile, toFile, true);
                return newImgName;
            }
        } catch (IOException e) {
            throw e;
        } finally {
            IOHelper.closeQuietly(imageStream);
        }
    }

    /**
     * 按固定宽高缩放图片
     * 
     * @param srcImgUrl 源图片全路径
     * @param newImgPath 缩放图片保存目录.为空表示与原图片同一目录
     * @param newImgName 缩放图片新名称.如果为空将新文件名等于"源图名_w宽_h高"
     * @param newWidth 新宽度
     * @param newHeight 新高度
     * @return 最终图片名
     * @throws IOException IOException
     */
    public static String zoomFixed(String srcImgUrl, String newImgPath, String newImgName, int newWidth, int newHeight)
            throws IOException {
        File srcFile = new File(srcImgUrl);
        if (!srcFile.exists()) {
            throw new IllegalArgumentException(srcImgUrl + "文件不存在");
        }
        if (newWidth < 0 || newHeight < 0) {
            throw new IllegalArgumentException("宽度和高度必须大于0");
        }
        // 未指定保存路径,默认与原图片路径相同
        if (newImgPath == null || newImgPath.isEmpty()) {
            newImgPath = srcFile.getParent();
        }
        if (StringHelper.isEmpty(newImgName)) {
            String oldName = srcFile.getName();
            newImgName = StringHelper.insertAt(oldName, "_w" + newWidth + "_h" + newHeight,
                    oldName.lastIndexOf(".") - 1);
        } else {
            newImgName = newImgName + FileHelper.getExtension(srcImgUrl, true);
        }
        FileInputStream imageStream = null;
        BufferedImage srcImage = null;
        try {
            imageStream = new FileInputStream(srcImgUrl);

            srcImage = ImageIO.read(imageStream);
            if (null == srcImage) {
                throw new IllegalArgumentException("文件不能以图片方式读入" + srcImgUrl);
            }
            BufferedImage newImageBuf = zoomedFixedSize(srcImage, newWidth, newHeight);
            return saveImage(newImgPath, newImgName, newImageBuf);
        } catch (IOException e) {
            throw e;
        } finally {
            IOHelper.closeQuietly(imageStream);
        }
    }

    private static BufferedImage zoomedFixedSize(BufferedImage originalImage, int newWidth, int newHeight) {
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        Graphics2D g = null;
        BufferedImage newImg = new BufferedImage(newWidth, newHeight, type);
        try {
            g = newImg.createGraphics();
            // 根据图片尺寸压缩比得到新图的尺寸
            g.drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
        return newImg;
    }

    /**
     * 保存文件,存在同名文件则取随机名称,返回最终图片名
     * 
     * @param saveDir 图片保存目录
     * @param imgName 图片名称
     * @return 最终图片名
     * @throws IOException
     */
    private static String saveImage(String saveDir, String imgName, BufferedImage newImg) throws IOException {
        File file = Paths.get(saveDir, imgName).toFile();
        if (file.exists()) {
            file.delete();
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        ImageIO.write(newImg, FileHelper.getExtension(imgName, false), file);
        return imgName;
    }

    /**
     * 对图片裁剪，并把裁剪新图片保存 .
     * 
     * @param srcImgPath 原始图片
     * @param newImgPath 新图片路径
     * @param x 起始点x坐标
     * @param y 起始点y坐标
     * @param width 截取宽度
     * @param height 截取长度
     * @throws IOException
     */
    public static void cutImage(String srcImgPath, String newImgPath, int x, int y, int width, int height)
            throws IOException {
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("长度或宽度应大于0");
        }

        FileInputStream inStream = null;
        ImageInputStream imgStream = null;
        String extName = FileHelper.getExtension(srcImgPath, false);

        try {
            // 读取图片文件
            inStream = new FileInputStream(srcImgPath);
            Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(extName);
            if (it.hasNext()) {
                ImageReader reader = it.next();
                // 获取图片流
                imgStream = ImageIO.createImageInputStream(inStream);
                reader.setInput(imgStream, true);
                ImageReadParam param = reader.getDefaultReadParam();
                Rectangle rect = new Rectangle(x, y, width, height);
                param.setSourceRegion(rect);
                BufferedImage bi = reader.read(0, param);
                // 保存新图片
                ImageIO.write(bi, extName, new File(newImgPath));
            } else {
                throw new IllegalArgumentException("长度或宽度应大于0");
            }
        } catch (IOException e) {
            throw e;
        } finally {
            IOHelper.closeQuietly(inStream);
            IOHelper.closeQuietly(imgStream);
        }
    }

    /**
     * 添加文字水印操作,返回BufferedImage对象
     * 
     * @param imgPath 待处理图片
     * @param markText 水印文字
     * @param font 水印字体信息 不写默认值为宋体
     * @param color 水印字体颜色
     * @param x 水印位于图片左上角的 x 坐标值
     * @param y 水印位于图片左上角的 y 坐标值
     * @param alpha 水印透明度 0.1f ~ 1.0f
     * @throws IOException
     */
    public static void textWatermark(String imgPath, String markText, Font font, Color color, float x, float y,
            float alpha) throws IOException {
        BufferedImage targetImage = null;
        BufferedImage originalImage;
        Graphics2D g = null;
        try {
            originalImage = ImageIO.read(new File(imgPath));
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
            targetImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), type);
            g = targetImage.createGraphics();
            g.drawImage(originalImage, 0, 0, null);
            g.setColor(color);
            g.setFont(font);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            g.drawString(markText, x, y);
            ImageIO.write(targetImage, FileHelper.getExtension(imgPath, false), new File(imgPath));
        } catch (IOException e) {
            throw e;
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }

    /**
     * 文字水印，使用默认字体(sans_serif bold 18)，在右下角加入水印
     * 
     * @param imgPath 图片路径
     * @param markText 水印文字
     * @param color 字体颜色
     * @throws IOException
     */
    public static void textWatermark(String imgPath, String markText, Color color) throws IOException {
        BufferedImage targetImage = null;
        BufferedImage originalImage;
        Graphics2D g = null;
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 18);
        float alpha = 0.3f;
        try {
            originalImage = ImageIO.read(new File(imgPath));
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
            targetImage = new BufferedImage(width, height, type);
            g = targetImage.createGraphics();
            g.drawImage(originalImage, 0, 0, null);
            g.setColor(color);
            g.setFont(font);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(markText, width - metrics.stringWidth(markText) - 20, height - metrics.getHeight() - 20);
            ImageIO.write(targetImage, FileHelper.getExtension(imgPath, false), new File(imgPath));
        } catch (IOException e) {
            throw e;
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }

    /**
     * 添加图片水印操作,返回BufferedImage对象
     * 
     * @param imgPath 待处理图片
     * @param markPath 水印图片
     * @param x 水印位于图片左上角的 x 坐标值
     * @param y 水印位于图片左上角的 y 坐标值
     * @param alpha 水印透明度 0.1f ~ 1.0f
     * @return 处理后的图片对象
     * @throws Exception
     */
    public static BufferedImage addWaterMark(String imgPath, String markPath, int x, int y, float alpha)
            throws Exception {
        BufferedImage targetImage = null;
        try {
            // 加载待处理图片文件
            Image img = ImageIO.read(new File(imgPath));

            // 创建目标图象文件
            targetImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = targetImage.createGraphics();
            g.drawImage(img, 0, 0, null);

            // 加载水印图片文件
            Image markImg = ImageIO.read(new File(markPath));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            g.drawImage(markImg, x, y, null);
            g.dispose();
        } catch (Exception e) {
            throw new RuntimeException("添加图片水印操作异常");
        }
        return targetImage;

    }

    public static void main(String[] args) throws Exception {
        String srcImg = "C:\\Users\\yucan.zhang\\Documents\\4.jpg";
        // textWatermark(srcImg, "水印", new Font(Font.SERIF, Font.BOLD, 20),Color.black, 10f, 300, 0.3f);

        textWatermark(srcImg, "大水印", Color.black);
    }

    // public static void main(String args[]) throws IOException {
    // String srcImg = "C:\\Users\\yucan.zhang\\Documents\\3.jpg";
    // String srcImg1 = "C:\\Users\\yucan.zhang\\Documents\\8.jpg";
    // String srcImg2 = "C:\\Users\\yucan.zhang\\Documents\\20161208164434.png";
    // // String tarDir = "D:\\datacenter";
    // // System.out.println("等比缩放xxx1=" + zoomScale(srcImg, tarDir, "xxxx1", 500, 600));
    // System.out.println("等比缩放=" + zoomScale(srcImg, null, null, 400, 600));
    // System.out.println("固定缩放=" + zoomFixed(srcImg1, null, "xxx", 500, 500));
    // System.out.println("固定缩放=" + zoomFixed(srcImg2, null, "xxx11", 500, 500));
    // }

}
