package org.jflame.context.env;

import java.util.Arrays;

import org.jflame.commons.net.IPAddressHelper;
import org.jflame.context.cache.redis.RedisClient;

/**
 * 基于redis的应用登记中心实现
 * 
 * @author yucan.zhang
 */
public class RedisWorkerIdAssigner extends WorkerIdAssigner {

    private RedisClient redisClient;

    public RedisWorkerIdAssigner(String appNo, RedisClient redisClient) {
        super(appNo);
        this.redisClient = redisClient;
    }

    protected int registerWorker() {
        // 以zset存储应用注册数据
        String zsetKey = CENTER_ROOT_KEY + '_' + appNo;
        String ip = IPAddressHelper.getHostIP();
        String identifyNodeFix = ip + '_' + workerPathMd5();
        // 脚本实现登记逻辑
        String workerIdStr = redisClient.runScript(registerScript(), Arrays.asList(zsetKey),
                Arrays.asList(identifyNodeFix), String.class);
        return Integer.parseInt(workerIdStr);
    }

    private String registerScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call('EXISTS',KEYS[1])==0 then ");
        sb.append("   redis.call('ZADD',KEYS[1],1,ARGV[1]) ");
        sb.append("   return '\"1\"' ");
        sb.append("end ");
        sb.append("local wokerscore=redis.call('ZSCORE',KEYS[1],ARGV[1]) ");
        sb.append("if wokerscore ~=false then ");
        sb.append("   return '\"'..tostring(wokerscore)..'\"' ");
        sb.append("end ");
        sb.append("local maxscores=redis.call('ZRANGE',KEYS[1],-1,-1,'WITHSCORES') ");
        sb.append("if maxscores[1]~=nil then ");
        sb.append("   local wokerscore=maxscores[2]+1 ");
        sb.append("   redis.call('ZADD',KEYS[1],wokerscore,ARGV[1]) ");
        sb.append("   return '\"'..tostring(wokerscore)..'\"' ");
        sb.append("else ");
        sb.append("   redis.call('ZADD',KEYS[1],1,ARGV[1]) ");
        sb.append("   return '\"1\"' ");
        sb.append("end ");
        return sb.toString();
    }

}
