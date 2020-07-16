package org.jflame.context.zookeeper;

import java.util.List;

/**
 * zookeeper节点修改监听
 * 
 * @author yucan.zhang
 */
public interface ChildNodeListener {

    /**
     * 子节点改变事件处理
     * 
     * @param path 监听的节点路径
     * @param children 最新的子节点列表
     */
    void childChanged(String path, List<String> children);

}
