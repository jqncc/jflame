package org.jflame.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import org.jflame.context.filemanager.FastDFSFileManager;

public class FileManagerTest {

    @Test
    public void testFdfs() throws IOException {

        FastDFSFileManager fdfs = new FastDFSFileManager("D:\\fastdfs.properties", "http://10.18.200.96:8000");
        fdfs.save(new File("C:\\Users\\yucan.zhang\\Pictures\\1-26.jpg"), null, null);
        fdfs.close();
    }

}
