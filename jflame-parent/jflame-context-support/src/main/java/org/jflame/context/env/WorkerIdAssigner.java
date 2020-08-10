package org.jflame.context.env;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jflame.commons.crypto.DigestHelper;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.StringHelper;

/**
 * 将应用登记到一个统一的记录中心,并返回一个全局唯一id用于标识该应用身份,该id可用于单号或表id生成
 * 
 * @author yucan.zhang
 */
public abstract class WorkerIdAssigner {

    protected final String CENTER_ROOT_KEY = "cluster_worker_center";
    protected String appNo;
    private static final AtomicInteger WOKER_ID = new AtomicInteger(0);
    private final String cacheIdFile = "workerid.cache";

    public WorkerIdAssigner(String appNo) {
        if (StringHelper.isEmpty(appNo)) {
            throw new IllegalArgumentException("parameter appNo not be null");
        }
        this.appNo = appNo;
    }

    /**
     * 注册当前应用到登记中心,返回集群环境全局唯一ID
     * 
     * @param appCode 当前应用标识
     * @return 注册失败返回0
     */
    protected abstract int registerWorker();

    /**
     * 获取当前应用路径,将路径作md5返回.同一应用在同一主机下部署多个可用绝对路径来区分
     * 
     * @return
     */
    protected String workerPathMd5() {
        return DigestHelper.md5Hex(this.getClass()
                .getResource("/")
                .getPath());
    }

    /**
     * 获取本地缓存的id.在注册中心连接失败时使用
     * 
     * @return
     * @throws URISyntaxException
     */
    int getCacheWorkerId() {
        try {
            Path path = Paths.get(this.getClass()
                    .getResource("/")
                    .toURI())
                    .resolve(cacheIdFile);
            if (Files.exists(path)) {
                List<String> txts = Files.readAllLines(path, StandardCharsets.UTF_8);
                if (CollectionHelper.isNotEmpty(txts)) {
                    return Integer.parseInt(txts.get(0)
                            .trim());
                }
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    void cacheWorkerId(int id) {
        try {
            Path path = Paths.get(this.getClass()
                    .getResource("/")
                    .toURI())
                    .resolve(cacheIdFile);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            Files.write(path, String.valueOf(id)
                    .getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public int getWorkerId() {
        if (WOKER_ID.get() == 0) {
            synchronized (WOKER_ID) {
                int wid = registerWorker();
                if (wid == 0) {
                    wid = getCacheWorkerId();
                } else {
                    cacheWorkerId(wid);
                }
                if (wid > 0) {
                    WOKER_ID.set(wid);
                } else {
                    throw new RuntimeException("获取workerId失败");
                }
            }
        }
        return WOKER_ID.get();
    }
}
