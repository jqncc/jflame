package org.jflame.context.env;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.jflame.commons.model.Chars;
import org.jflame.commons.net.IPAddressHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.context.zookeeper.ZookeeperClient;

/**
 * 基于zookeeper的应用登记中心实现
 * 
 * @author yucan.zhang
 */
public class ZkWorkerIdAssigner extends WorkerIdAssigner {

    private final String CENTER_ROOT_NODE = "/" + CENTER_ROOT_KEY;
    private ZookeeperClient zkClient;

    public ZkWorkerIdAssigner(String appNo, ZookeeperClient zkClient) {
        super(appNo);
        this.zkClient = zkClient;
    }

    protected int registerWorker() {
        // 顺序节点组成:/cluster_worker_center/appCode/ip_应用安装路径md5&001
        String ip = IPAddressHelper.getHostIP();// 主机ip
        String identifyNodeFix = ip + '_' + workerPathMd5() + Chars.AND;
        // 应用标识节点
        String appNode = StringHelper.join(Chars.SLASH, CENTER_ROOT_NODE, appNo);
        int myWorkerId;

        if (!zkClient.isExist(appNode)) {
            zkClient.createPersistent(appNode, false);
        }
        List<String> children = zkClient.getChildren(appNode);
        String[] tmpArr;
        if (children != null) {
            for (String nodename : children) {
                if (nodename.startsWith(identifyNodeFix)) {
                    tmpArr = StringUtils.split(nodename, Chars.AND);
                    myWorkerId = Integer.parseInt(tmpArr[1]);
                    return myWorkerId;
                }
            }
        }
        String myNodeName = zkClient.createPersistent(identifyNodeFix, true);
        tmpArr = StringUtils.split(myNodeName, Chars.AND);
        return Integer.parseInt(tmpArr[1]) + 1;// 从1开始

    }

}
