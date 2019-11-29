package org.jflame.toolkit.test;

import java.io.IOException;

import org.junit.Test;

import org.jflame.commons.util.file.FileHelper;

public class IOTest {

    @Test
    public void testCreateDateDir() {
        System.out.println("绝对路径下创建年份文件夹:" + FileHelper.createDateDir("C:\\Users\\yucan.zhang\\Desktop", true, false));
        System.out.println("绝对路径下创建年月份文件夹:" + FileHelper.createDateDir("C:\\Users\\yucan.zhang\\Desktop", true, true));
        System.out.println("权限路径下创建月份文件夹:" + FileHelper.createDateDir("", false, true));
    }

    @Test
    public void testFileHepler() throws IOException {
        System.out.println("classpath根路径:" + FileHelper.getClassPath());
        System.out.println("获取路径中目录部分(E:\\abc):" + FileHelper.getDir("E:\\abc"));
        System.out.println("获取路径中目录部分(E:\\abc\\ab.jpg):" + FileHelper.getDir("E:\\abc\\ab.jpg"));
        System.out.println("获取路径中目录部分(/data):" + FileHelper.getDir("/data"));
        System.out.println("获取路径中目录部分(/data/abc.jpg):" + FileHelper.getDir("/data/abc.jpg"));
        System.out.println("获取扩展名(/data/abc.jpg):" + FileHelper.getExtension("/data/abc.jpg", false));
        System.out.println("获取扩展名(/data/abc.jpg):" + FileHelper.getExtension("/data/abc.jpg", true));

        // String text = FileHelper.readText("C:\\Users\\Desktop\\new.txt", CharsetHelper.GBK_18030.name());
        // System.out.println("读取文本:" + text);

        System.out.println("获取当前classpath下文件的绝对路径:" + FileHelper.toAbsolutePath("org"));
    }
}
