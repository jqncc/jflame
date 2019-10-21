package org.jflame.context.env;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.common.bean.Chars;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.net.IPAddressHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.toolkit.zookeeper.ZookeeperClient;
import org.jflame.toolkit.zookeeper.curator.CuratorZookeeperClient;

/**
 * 基于zookeeper的登记中心实现
 * 
 * @author yucan.zhang
 */
public class ZkWorkerIdAssigner implements WorkerIdAssigner {

    private final String CENTER_ROOT_NODE = "/cluster_worker_center";
    private ZookeeperClient zkClient;

    public ZkWorkerIdAssigner(String zkUrl) {
        this.zkClient = new CuratorZookeeperClient(zkUrl);
    }

    public ZkWorkerIdAssigner(ZookeeperClient zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public int registerWorker(String appCode) {
        if (StringHelper.isEmpty(appCode)) {
            throw new IllegalArgumentException("parameter appCode not be null");
        }
        // 顺序节点组成:/cluster_worker_center/appCode/ip&classpath&001
        // 主机ip
        String ip = IPAddressHelper.getHostIP();
        // 当前应用所在路径,同一应用在同一主机下部署多个用绝对路径来区分,重启或重新部署可重用编号
        String classpath = TranscodeHelper.urlencode(this.getClass()
                .getResource("/")
                .getPath());
        String identifyNodeFix = ip + classpath + Chars.AND;
        // 应用标识节点
        String appNode = StringHelper.join(FileHelper.UNIX_SEPARATOR, CENTER_ROOT_NODE, appCode);
        int myWorkerId;
        try {
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
            myWorkerId = Integer.parseInt(tmpArr[1]) + 1;
            return myWorkerId;
        } catch (Exception e) {
            throw e;
        } finally {
            zkClient.close();
        }
    }

}
