package org.jflame.toolkit.zookeeper;

import java.util.List;

/**
 * 节点修改监听
 * 
 * @author yucan.zhang
 */
public interface ChildListener {

    void childChanged(String path, List<String> children);

}
