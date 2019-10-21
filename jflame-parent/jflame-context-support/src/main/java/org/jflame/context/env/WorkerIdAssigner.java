package org.jflame.context.env;

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
     * 获取当前JVM进程名,只取@前部分
     * 
     * @return
     */
    default String processName() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean()
                .getName();
        int atIndex = processName.indexOf('@');
        if (atIndex > -1) {
            return processName.substring(0, atIndex);
        }
        return processName;
    }
}
