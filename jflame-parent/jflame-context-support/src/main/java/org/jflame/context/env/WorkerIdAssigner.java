package org.jflame.context.env;

import java.util.concurrent.atomic.AtomicInteger;

import org.jflame.commons.crypto.DigestHelper;
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

    public int getWorkerId() {
        if (WOKER_ID.get() == 0) {
            synchronized (WOKER_ID) {
                WOKER_ID.set(registerWorker());
            }
        }
        return WOKER_ID.get();
    }
}
