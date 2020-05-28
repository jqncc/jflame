package org.jflame.context.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import org.jflame.commons.exception.BusinessException;
import org.jflame.commons.key.IDHelper;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.util.file.FileHelper;

/**
 * FastDFS文件管理. 注:一个实例一个连接,非线程安全
 * 
 * @author yucan.zhang
 */
public class FastDFSFileManager extends BaseFileManager {

    private StorageClient1 storageClient = null;
    private TrackerClient trackerClient = null;
    private String serverUrl;

    public FastDFSFileManager(String configFile, String serverUrl) {
        try {
            String ext = FileHelper.getExtension(configFile, false);
            if ("properties".equals(ext)) {
                ClientGlobal.initByProperties(configFile);
            } else {
                ClientGlobal.init(configFile);
            }
            trackerClient = new TrackerClient(ClientGlobal.g_tracker_group);
        } catch (IOException | MyException e) {
            throw new RuntimeException("fastdfs client实例化失败", e);
        }
        this.serverUrl = serverUrl;
    }

    @Override
    public String save(File file, String saveDir, Map<String,String> fileMeta) throws IOException {
        String ext = FileHelper.getExtension(file.getName(), true);
        return save(new FileInputStream(file), saveDir, ext, fileMeta);
    }

    @Override
    public String save(InputStream fileStream, String saveDir, String extension, Map<String,String> fileMeta)
            throws IOException {
        try {
            byte[] fileBytes = IOHelper.readBytes(fileStream);
            return save(fileBytes, saveDir, extension, fileMeta);
        } catch (Exception e) {
            throw e;
        } finally {
            IOHelper.closeQuietly(fileStream);
        }
    }

    @Override
    public String save(byte[] fileBytes, String saveDir, String extension, Map<String,String> fileMeta)
            throws IOException {
        NameValuePair[] metaList = setMetas(fileMeta);
        try {
            initConnect();
            return storageClient.upload_file1(saveDir, fileBytes, extension, metaList);
        } catch (MyException e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public byte[] readBytes(String filePath) throws IOException {
        try {
            initConnect();
            byte[] content = storageClient.download_file1(getFileId(filePath));
            return content;
        } catch (MyException e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public File read(String filePath) throws IOException {
        byte[] fileBytes = readBytes(filePath);
        if (fileBytes != null) {
            String ext = FileHelper.getExtension(filePath, true);
            Path tmpPath = Files.createTempFile(IDHelper.millisAndRandomNo(3), ext);
            return Files.write(tmpPath, fileBytes)
                    .toFile();
        }
        throw new FileNotFoundException("文件不存在" + filePath);
    }

    @Override
    public int delete(String... filePaths) throws IOException {
        return delete(null, filePaths);
    }

    @Override
    public int delete(String groupName, String[] filePaths) throws IOException {
        if (filePaths.length == 0) {
            return 0;
        }
        List<String> fileKeys = Arrays.asList(filePaths);
        if (logger.isInfoEnabled()) {
            logger.info("fastdfs删除文件:{}", ArrayUtils.toString(fileKeys));
        }
        for (int i = 0; i < filePaths.length; i++) {
            fileKeys.set(i, getFileId(fileKeys.get(i)));
        }
        int i = 0;
        try {
            initConnect();
            for (String fileId : fileKeys) {
                i = i + storageClient.delete_file1(fileId);
            }
        } catch (MyException e) {
            throw new BusinessException(e);
        }
        return i;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public void close() {
        if (storageClient != null) {
            try {
                storageClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                storageClient = null;
            }
        }
    }

    private void initConnect() throws IOException {
        if (storageClient == null || !storageClient.isConnected()) {
            if (storageClient != null && storageClient.getTrackerServer() != null) {
                close();
            }
            TrackerServer trackerServer = trackerClient.getConnection();
            if (trackerServer == null) {
                throw new IllegalStateException("get fastDFS connection failed");
            }
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            if (storageServer == null) {
                throw new IllegalStateException("getStoreStorage return null");
            }
            storageClient = new StorageClient1();
            storageClient.setStorageServer(storageServer);
            storageClient.setTrackerServer(trackerServer);
        }
    }

    private NameValuePair[] setMetas(Map<String,String> fileMeta) {
        NameValuePair[] metaList = null;
        if (fileMeta != null) {
            int h = fileMeta.size();
            if (h > 0) {
                metaList = new NameValuePair[h];
                int i = 0;
                for (Entry<String,String> entry : fileMeta.entrySet()) {
                    metaList[i] = new NameValuePair(entry.getKey(), entry.getValue());
                    i++;
                }
            }
        }
        return metaList;
    }
}
