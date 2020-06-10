package org.jflame.context.filemanager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.jflame.commons.util.UrlHelper;
import org.jflame.commons.util.file.FileHelper;
import org.jflame.context.filemanager.FileManagerFactory.FileManagerMode;

/**
 * 本地硬盘或NFS文件管理
 * 
 * @author yucan.zhang
 */
public class LocalFileManager extends BaseFileManager {

    private String serverUrl;
    private String basePath;

    public LocalFileManager(String basePath, String serverUrl) {
        this.basePath = basePath.trim();
        Path path = Paths.get(basePath.trim());
        if (!path.isAbsolute()) {
            throw new IllegalArgumentException(basePath + "不是绝对路径");
        }
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(basePath + "不存在");
        }
        this.serverUrl = serverUrl;
    }

    @Override
    public String save(File file, String saveDir, Map<String,String> fileMeta) throws IOException {
        String extension = FileHelper.getExtension(file.getName(), true);
        return save(new FileInputStream(file), saveDir, extension, fileMeta);
    }

    @Override
    public String save(InputStream fileStream, String saveDir, String extension, Map<String,String> fileMeta)
            throws IOException {
        String newName = createNewFileName(extension);
        Path finalPath = Paths.get(basePath, saveDir, newName);
        Files.copy(fileStream, finalPath);
        if (logger.isDebugEnabled()) {
            logger.debug("保存上传文件到:{}", finalPath);
        }
        // 计算相对路径,统一分隔符为/
        String relativelyPath = FileHelper.separatorsToUnix(Paths.get(basePath)
                .relativize(finalPath)
                .toString());
        return relativelyPath;
    }

    @Override
    public String save(byte[] fileBytes, String saveDir, String extension, Map<String,String> fileMeta)
            throws IOException {
        return save(new ByteArrayInputStream(fileBytes), saveDir, extension, fileMeta);
    }

    @Override
    public byte[] readBytes(String filePath) throws IOException {
        Path downFile = null;
        if (!UrlHelper.isAbsoluteUri(filePath)) {
            downFile = Paths.get(basePath, filePath);
        } else {
            downFile = Paths.get(filePath);
        }
        if (Files.exists(downFile)) {
            return Files.readAllBytes(downFile);
        } else {
            throw new FileNotFoundException("文件不存在" + downFile);
        }
    }

    @Override
    public File read(String filePath) throws IOException {
        Path downFile = null;
        if (!UrlHelper.isAbsoluteUri(filePath)) {
            downFile = Paths.get(basePath, filePath);
        } else {
            downFile = Paths.get(filePath);
        }
        if (Files.exists(downFile)) {
            return downFile.toFile();
        } else {
            throw new FileNotFoundException("文件不存在" + downFile);
        }
    }

    @Override
    public int delete(String... filePaths) throws IOException {
        return delete(null, filePaths);
    }

    @Override
    public int delete(String parent, String[] filePaths) throws IOException {
        Path delFile = null;
        boolean result;
        int i = 0;
        for (String filePath : filePaths) {
            if (parent != null) {
                delFile = Paths.get(basePath, parent, filePath);
            } else {
                delFile = Paths.get(basePath, filePath);
            }
            logger.info("删除文件{}", delFile);
            result = Files.deleteIfExists(delFile);
            if (!result) {
                logger.warn("删除文件失败{}", filePath);
                continue;
            }
            i++;
        }
        return i;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public void close() {
    }

    @Override
    public FileManagerMode getFileManagerMode() {
        return FileManagerMode.local;
    }
}
