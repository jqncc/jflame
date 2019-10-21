package org.jflame.context.env;

import java.io.Serializable;
import java.util.Map;

import org.jflame.toolkit.cache.RedisClient;
import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.net.IPAddressHelper;
import org.jflame.toolkit.util.MapHelper;
import org.jflame.toolkit.util.StringHelper;

public class RedisWorkerIdAssigner implements WorkerIdAssigner {

    private final String CENTER_ROOT_KEY = "cluster_worker_center";
    private RedisClient redisClient;

    public RedisWorkerIdAssigner(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public int registerWorker(String appCode) {
        if (StringHelper.isEmpty(appCode)) {
            throw new IllegalArgumentException("parameter appCode not be null");
        }
        String zsetKey = CENTER_ROOT_KEY + "_" + appCode;

        // 顺序节点组成:cluster_worker_center/appCode/ip&classpath&001
        // 主机ip
        String ip = IPAddressHelper.getHostIP();
        // 当前应用所在路径,同一应用在同一主机下部署多个用绝对路径来区分,重启或重新部署可重用编号
        String classpath = TranscodeHelper.urlencode(this.getClass()
                .getResource("/")
                .getPath());
        String identifyNodeFix = ip + classpath;
        Double score = redisClient.zscore(zsetKey, identifyNodeFix);
        if (score != null) {
            return score.intValue();
        }
        Map<? extends Serializable,Double> maxWorderId = redisClient.zsrangeWithScores(zsetKey, -1, -1);
        if (MapHelper.isNotEmpty(maxWorderId)) {

        }
        int myWorkerId;
        return 0;
    }

}
