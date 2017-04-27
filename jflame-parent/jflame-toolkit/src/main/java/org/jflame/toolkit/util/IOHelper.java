package org.jflame.toolkit.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.Socket;

/**
 * io工具类
 * 
 * @author yucan.zhang
 */
public final class IOHelper {

    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    /**
     * 使用系统默认编码将输入流InputStream包装为BufferedReader
     * 
     * @param inputStream 输入流
     * @return BufferedReader
     */
    public static BufferedReader toBufferedReader(final InputStream inputStream) {
        InputStreamReader inReader = new InputStreamReader(inputStream);
        return new BufferedReader(inReader);
    }

    /**
     * 使用指定字符编码将输入流InputStream包装为BufferedReader
     * 
     * @param inputStream 输入流
     * @return BufferedReader
     * @throws UnsupportedEncodingException 字符集不支持
     */
    public static BufferedReader toBufferedReader(final InputStream inputStream, String charset)
            throws UnsupportedEncodingException {
        InputStreamReader inReader = new InputStreamReader(inputStream, charset);
        return new BufferedReader(inReader);
    }

    /**
     * 从指定的<code>Reader</code>复制字符到<code>Writer</code>. 提供复制的buffer.<br />
     * <strong>注：请手动关闭输入流</strong>
     * 
     * @param input Reader字符输入流
     * @param output Writer字符输出流
     * @param buffer char []复制时使用的buffer
     * @return 复制的总字符数
     * @throws IOException
     */
    public static long copy(Reader input, Writer output, char[] buffer) throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * 从指定的<code>Reader</code>复制字符到<code>Writer</code>. 默认buffer大小为4096.<br />
     * <strong>注：请手动关闭输入流</strong>
     * 
     * @param input
     * @param output
     * @return 复制的总字符数
     * @throws IOException
     */
    public static long copy(Reader input, Writer output) throws IOException {
        return copy(input, output, new char[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * 从指定的InputStream复制字节到OutputStream.提供复制的buffer.<br />
     * <strong>注：请手动关闭输入流</strong>
     * 
     * @param input InputStream字节输入流
     * @param output OutputStream字节输出流
     * @param buffer byte[]复制时使用的buffer
     * @return 复制的总字节数
     * @throws IOException
     */
    public static long copy(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * 从指定的InputStream复制字节到OutputStream.默认buffer大小为4096.<br />
     * <strong>注：请手动关闭输入流</strong>
     * 
     * @param input InputStream字节输入流
     * @param output OutputStream字节输出流
     * @return 复制的总字节数
     * @throws IOException
     */
    public static long copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * 读取文件到输出流
     * @param file 文件
     * @param output 输出流
     * @return
     * @throws IOException
     */
    public static long copy(File file, OutputStream output) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            return copy(input, output, new byte[DEFAULT_BUFFER_SIZE]);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 从输入字节流读取字符串,指定字符编码.<strong>请手动关闭输入流</strong>
     * 
     * @param input InputStream 输入字节流
     * @param charset 字符编码
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String readText(InputStream input, String charset) throws IOException {
        StringWriter writer = new StringWriter();
        copy(toBufferedReader(input, charset), writer);
        return writer.toString();
    }

    /**
     * 从输入流读取，返回字节数组byte[]
     * 
     * @param input 输入字节流
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * 将byte[]数据字节输出流
     * 
     * @param data 待写入byte数组
     * @param output 字节输出流
     * @throws IOException
     */
    public static void write(byte[] data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    /**
     * 将byte[]数据写入字符输出流
     * 
     * @param data 待写入byte数组
     * @param output 字符输出流
     * @param encoding 字符编码.为null以系统默认编码
     * @throws IOException 字符编码或i/o异常
     */
    public static void write(byte[] data, Writer output, String encoding) throws IOException {
        if (data != null) {
            if (encoding == null) {
                output.write(new String(data));
            } else {
                output.write(new String(data, encoding));
            }
        }
    }

    /**
     * 将字符串写入输出流,使用系统默认编码
     * 
     * @param data 待写入字符串
     * @param output 输出流
     * @throws IOException 字符编码或i/o异常
     */
    public static void writeText(String data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(data.getBytes());
        }
    }

    /**
     * 将字符串写入输出流,指定字符编码
     * 
     * @param data 待写入字符串
     * @param output 输出流
     * @param encoding 编码
     * @throws IOException 字符编码或i/o异常
     */
    public static void writeText(String data, OutputStream output, String encoding) throws IOException {
        if (data != null) {
            if (encoding == null) {
                writeText(data, output);
            } else {
                output.write(data.getBytes(encoding));
            }
        }
    }

    /**
     * 静默关闭io流，失败不抛出异常
     * 
     * @param ioStream 实现接口Closeable的可关闭io流
     */
    public static void closeQuietly(Closeable ioStream) {
        try {
            if (ioStream != null) {
                ioStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 静默关闭socket，失败不抛出异常
     * 
     * @param socket Socket套接字
     */
    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
