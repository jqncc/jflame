package org.jflame.context.env;

import org.jflame.toolkit.crypto.DigestHelper;

/**
 * 将应用登记到一个统一的记录中心,并返回一个全局唯一id用于标识该应用身份,该id可用于单号或表id生成
 * 
 * @author yucan.zhang
 */
public interface WorkerIdAssigner {

    /**
     * 注册当前应用到登记中心,返回集群环境全局唯一ID
     * 
     * @param appCode 当前应用标识
     * @return 注册失败返回0
     */
    public int registerWorker(String appCode);

    /**
     * 获取当前应用路径,将路径作md5返回.同一应用在同一主机下部署多个可用绝对路径来区分
     * 
     * @return
     */
    default String workerPathMd5() {
        return DigestHelper.md5Hex(this.getClass()
                .getResource("/")
                .getPath());
    }
}
