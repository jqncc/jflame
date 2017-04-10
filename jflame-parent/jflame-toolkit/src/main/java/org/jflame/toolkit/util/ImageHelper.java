package org.jflame.toolkit.util;

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

/**
 * 图片缩放处理工具类.不支持背景透明图片
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
    public static String zoomScale(String srcImgUrl, String newImgPath, int maxWidth, int maxHeight)
            throws IOException {
        return zoomScale(srcImgUrl, newImgPath, null, maxWidth, maxHeight);
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
    public static String zoomScale(String srcImgUrl, String newImgPath, String newImgName, int maxWidth, int maxHeight)
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
            newImgName = StringHelper.insertAt(oldName, "_w" + maxWidth + "_h" + maxHeight,
                    oldName.lastIndexOf(".") - 1);
        } else {
            newImgName = newImgName + FileHelper.getExtension(srcImgUrl, true);
        }
        FileInputStream imageStream = null;
        BufferedImage srcImage = null;
        int newWidth = 0;
        int newHeight = 0;
        try {
            imageStream = new FileInputStream(srcImgUrl);
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

    private static BufferedImage zoomedFixedSize(BufferedImage src, int newWidth, int newHeight) {
        BufferedImage newImg = new BufferedImage(newWidth, newHeight, src.getType());
        Graphics2D g = newImg.createGraphics();
        // 根据图片尺寸压缩比得到新图的尺寸
        g.drawImage(src.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();
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

    /*
     * public static void main(String args[]) throws IOException { String srcImg =
     * "C:\\Users\\yucan.zhang\\Pictures\\5399392f729c9.jpg"; String srcImg1 =
     * "C:\\Users\\yucan.zhang\\Downloads\\20161208164434.png"; String tarDir = "D:\\datacenter";
     * System.out.println("固定缩放=" + zoomFixed(srcImg1, null, null, 500, 400)); System.out.println("等比缩放xxx1=" +
     * zoomScale(srcImg, tarDir, "xxxx1", 500, 600)); System.out.println("等比缩放=" + zoomScale(srcImg, tarDir, null, 500,
     * 600)); }
     */
}
