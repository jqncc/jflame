package org.jflame.toolkit.test;

import java.io.IOException;

import org.jflame.toolkit.util.FileHelper;
import org.junit.Test;

public class IOTest {

    @Test
    public void testReadString() throws IOException {

        //InputStream stream = Files.newInputStream(Paths.get("C:\\Users\\yucan.zhang\\Desktop\\new 2.txt"),
        //        StandardOpenOption.READ);
        //String text = IOHelper.readString(stream, "utf-8");
        String text =FileHelper.readText("C:\\Users\\yucan.zhang\\Desktop\\new 2.txt", "utf-8");
        System.out.println(text);
    }

    @Test
    public void testCreateDateDir() {
        System.out.println("绝对路径下创建年份文件夹:" + FileHelper.createDateDir("C:\\Users\\yucan.zhang\\Desktop", false));
        System.out.println("绝对路径下创建年月份文件夹:" + FileHelper.createDateDir("C:\\Users\\yucan.zhang\\Desktop", true));
        System.out.println("权限路径下创建年份文件夹:" + FileHelper.createDateDir("", false));
    }

}
