package org.jflame.commons.zookeeper;

/**
 * zookeeper节点内容改变事件监听
 * 
 * @author yucan.zhang
 */
public interface NodeDataListener {

    public void dataDeleted(String path) throws Exception;

    public void dataChange(String path, Object data) throws Exception;
}
