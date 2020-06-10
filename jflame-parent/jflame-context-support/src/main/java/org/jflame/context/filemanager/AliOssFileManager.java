package org.jflame.context.filemanager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PolicyConditions;

import org.jflame.commons.exception.BusinessException;
import org.jflame.commons.key.IDHelper;
import org.jflame.commons.model.Chars;
import org.jflame.commons.util.CharsetHelper;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.util.MapHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.file.FileHelper;
import org.jflame.context.filemanager.FileManagerFactory.FileManagerMode;

/**
 * Ali OSS云存储文件管理
 * 
 * @author yucan.zhang
 */
public class AliOssFileManager extends BaseFileManager {

    private final static String ossRootDomain = "aliyuncs.com";
    private String ossEndpoint;
    private String ossAccessId;
    private String ossAccessSecret;
    private String currentBucket;
    private OSSClient ossClient = null;

    public AliOssFileManager(String ossEndpoint, String ossAccessId, String ossAccessSecret, String bucketName) {
        this.ossEndpoint = ossEndpoint;
        this.ossAccessId = ossAccessId;
        this.ossAccessSecret = ossAccessSecret;
        this.currentBucket = bucketName;
        if (StringHelper.isEmpty(currentBucket)) {
            throw new IllegalArgumentException("currentBucket not be null!");
        }
        initOSSClient();
    }

    @Override
    public String save(File file, String saveDir, Map<String,String> fileMeta) throws IOException {
        String fileName = file.getName();
        String ext = FileHelper.getExtension(fileName, true);
        String newName = createNewFileName(saveDir, ext);
        try {
            if (MapHelper.isNotEmpty(fileMeta)) {
                ObjectMetadata meta = new ObjectMetadata();
                meta.setUserMetadata(fileMeta);
                ossClient.putObject(currentBucket, newName, file, meta);
            } else {
                ossClient.putObject(currentBucket, newName, file);
            }
        } catch (OSSException | ClientException e) {
            throw new BusinessException("上传文件到alioss失败,文件:" + fileName, e);
        }
        return newName;
    }

    @Override
    public String save(InputStream fileStream, String saveDir, String extension, Map<String,String> fileMeta)
            throws IOException {
        String newName = createNewFileName(saveDir, extension);
        try {
            if (MapHelper.isNotEmpty(fileMeta)) {
                ObjectMetadata meta = new ObjectMetadata();
                meta.setUserMetadata(fileMeta);
                ossClient.putObject(currentBucket, newName, fileStream, meta);
            } else {
                ossClient.putObject(currentBucket, newName, fileStream);
            }
        } catch (OSSException | ClientException e) {
            throw new BusinessException("上传到alioss失败", e);
        }
        return newName;
    }

    @Override
    public String save(byte[] fileBytes, String saveDir, String extension, Map<String,String> fileMeta)
            throws IOException {
        return save(new ByteArrayInputStream(fileBytes), saveDir, extension, fileMeta);
    }

    @Override
    public byte[] readBytes(String filePath) throws IOException {
        InputStream contentStream = null;
        try {
            Optional<String> bucket = extractBucketName(filePath);
            String key = getFileId(filePath);
            OSSObject ossObject = ossClient.getObject(bucket.isPresent() ? bucket.get() : currentBucket, key);
            contentStream = ossObject.getObjectContent();
            if (contentStream != null) {
                return IOHelper.readBytes(contentStream);
            }
            throw new FileNotFoundException("文件不存在" + filePath);
        } catch (OSSException | ClientException e) {
            throw new BusinessException(e);
        } finally {
            IOHelper.closeQuietly(contentStream);
        }
    }

    @Override
    public File read(String filePath) throws IOException {
        InputStream contentStream = null;
        try {
            Optional<String> bucket = extractBucketName(filePath);
            String key = getFileId(filePath);
            OSSObject ossObject = ossClient.getObject(bucket.isPresent() ? bucket.get() : currentBucket, key);
            contentStream = ossObject.getObjectContent();
            if (contentStream != null) {
                String ext = FileHelper.getExtension(filePath, true);
                Path tmpPath = Files.createTempFile(IDHelper.millisAndRandomNo(3), ext);
                Files.copy(contentStream, tmpPath);
                return tmpPath.toFile();
            }
            throw new FileNotFoundException("文件不存在" + filePath);
        } catch (OSSException | ClientException e) {
            throw new BusinessException(e);
        } finally {
            IOHelper.closeQuietly(contentStream);
        }
    }

    @Override
    public int delete(String... filePaths) throws IOException {
        Optional<String> bucket = extractBucketName(filePaths[0]);
        return delete(bucket.isPresent() ? bucket.get() : null, filePaths);
    }

    @Override
    public int delete(String bucketName, String[] filePaths) throws IOException {
        if (ArrayUtils.isEmpty(filePaths)) {
            throw new IllegalArgumentException();
        }
        List<String> fileKeys = Arrays.asList(filePaths);
        if (logger.isInfoEnabled()) {
            logger.info("删除文件:{}", ArrayUtils.toString(fileKeys));
        }

        for (int i = 0; i < filePaths.length; i++) {
            fileKeys.set(i, getFileId(fileKeys.get(i)));
        }

        try {
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(
                    StringHelper.isEmpty(bucketName) ? currentBucket : bucketName).withKeys(fileKeys);
            deleteRequest.setQuiet(true);// 只返回出错的结果
            DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteRequest);
            List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
            return deletedObjects.size();
        } catch (OSSException | ClientException e) {
            throw new BusinessException(e);
        }
    }

    public Map<String,String> generatePostSignature(String dir, int expireTime) {
        long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
        Date expiration = new Date(expireEndTime);
        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        if (dir != null) {
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
        }
        String host = getBucketUrl(ossEndpoint, currentBucket);
        String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
        String encodedPolicy = java.util.Base64.getEncoder()
                .encodeToString(CharsetHelper.getUtf8Bytes(postPolicy));
        String postSignature = ossClient.calculatePostSignature(postPolicy);

        Map<String,String> respMap = new LinkedHashMap<String,String>();
        respMap.put("accessid", ossAccessId);
        respMap.put("policy", encodedPolicy);
        respMap.put("signature", postSignature);
        respMap.put("dir", dir);
        respMap.put("host", host);
        respMap.put("expire", String.valueOf(expireEndTime / 1000));
        return respMap;
    }

    @Override
    public String getServerUrl() {
        return ossEndpoint;
    }

    public String getOssEndpoint() {
        return ossEndpoint;
    }

    public String getCurrentBucket() {
        return currentBucket;
    }

    public void setCurrentBucket(String currentBucket) {
        this.currentBucket = currentBucket;
    }

    @Override
    public void close() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    private void initOSSClient() {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setSupportCname(true);// 开启支持CNAME选项
        conf.setIdleConnectionTime(10000);// 1000*10
        ossClient = new OSSClient(this.ossEndpoint, this.ossAccessId, this.ossAccessSecret, conf);
    }

    protected String createNewFileName(String saveDir, String ext) {
        String newName = super.createNewFileName(ext);
        if (StringHelper.isNotEmpty(saveDir)) {
            if (saveDir.charAt(0) == Chars.SLASH) {
                newName = saveDir.substring(1) + Chars.SLASH + newName;
            } else {
                newName = saveDir + Chars.SLASH + newName;
            }
        }
        return newName;
    }

    /**
     * 获取bucket的访问地址
     * 
     * @param endpoint bucket所在endpoint
     * @param bucket bucket名称
     * @return
     */
    public static String getBucketUrl(String endpoint, String bucket) {
        // 使用原始ali地址组装endpoint和bucket,使用绑定域名无需组装
        if (endpoint.indexOf(ossRootDomain) >= 0) {
            int i = endpoint.indexOf("://") + 3;
            return StringHelper.insertAt(endpoint, bucket + ".", i);
        }
        return endpoint;
    }

    /**
     * 从文件url中提取不等于所属的bucket,如果该bucket不等于当前bucket则返回.
     * <p>
     * 只有未配置过独立域名转接的资源地址可以,资源地址域名组成是: bucket.区域标识.aliyuncs.com. 如:<br>
     * <code>http://prezpcx.oss-cn-shenzhen.aliyuncs.com/x.jpg</code>
     * 
     * @param fileUrl 文件url
     * @return
     */
    private Optional<String> extractBucketName(String fileUrl) {
        URI uri = URI.create(fileUrl);
        if (uri.isAbsolute()) {
            String domain = URI.create(fileUrl)
                    .getHost();
            if (domain.endsWith(ossRootDomain)) {
                String bucket = StringHelper.substringBeforeIgnoreCase(domain, ".");
                if (!bucket.equals(currentBucket)) {
                    return Optional.of(bucket);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public FileManagerMode getFileManagerMode() {
        return FileManagerMode.alioss;
    }
}
