package org.jflame.context.filemanager;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 附件管理接口,实现文件的上传,下载
 * 
 * @author yucan.zhang
 */
public interface IFileManager extends Closeable {

    /**
     * 保存文件
     * 
     * @param file 要保存的文件File
     * @param saveDir 保存目录,相对于文件服务根目录,可为null.
     * @param fileMeta 文件元数据或附带属性,只适用于可保存无数据的文件服务,可为null.
     * @return 返回生成的文件名
     * @throws IOException
     */
    String save(File file, String saveDir, Map<String,String> fileMeta) throws IOException;

    /**
     * 保存文件,从输入流中读取文件
     * 
     * @param fileStream 文件输入流
     * @param saveDir 保存目录,相对于文件服务根目录,可为null.
     * @param extension 文件扩展名
     * @param fileMeta 文件元数据或附带属性,只适用于可保存无数据的文件服务,可为null.
     * @return 返回生成的文件名
     * @throws IOException
     */
    String save(InputStream fileStream, String saveDir, String extension, Map<String,String> fileMeta)
            throws IOException;

    /**
     * 保存文件
     * 
     * @param fileBytes 文件byte[]
     * @param saveDir 保存目录,相对于文件服务根目录,可为null.
     * @param extension 文件扩展名
     * @param fileMeta 文件元数据或附带属性,只适用于可保存无数据的文件服务,可为null.
     * @return 返回生成的文件名
     * @throws IOException
     */
    String save(byte[] fileBytes, String saveDir, String extension, Map<String,String> fileMeta) throws IOException;

    /**
     * 读取文件,返回文件 的字节数组byte[]
     * 
     * @param filePath 文件路径
     * @return 文件byte[]
     * @throws IOException 文件不存在或读取出错
     */
    byte[] readBytes(String filePath) throws IOException;

    /**
     * 读取文件,返回File对象
     * 
     * @param filePath 文件路径
     * @return File对象
     * @throws IOException 文件不存在或读取出错
     */
    File read(String filePath) throws IOException;;

    /**
     * 删除文件
     * 
     * @param fileKey 文件id或路径
     * @return 返回成功删除的数量
     */
    int delete(String... fileKey) throws IOException;

    /**
     * 指定父级目录，删除下面的文件
     * 
     * @param parent 父级目录或分组
     * @param fileKeys 文件id
     * @return 返回成功删除的数量
     */
    int delete(String parent, String[] fileKeys) throws IOException;

    /**
     * 返回文件服务器地址
     * 
     * @return
     */
    String getServerUrl();

}
