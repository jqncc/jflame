package org.jflame.toolkit.zookeeper;

import java.io.Serializable;
import java.util.List;

import org.apache.zookeeper.CreateMode;

public interface ZookeeperClient {

    /**
     * 创建节点,默认持久节点
     * 
     * @param path 节点路径
     */
    String create(String path);

    /**
     * 创建节点
     * 
     * @param path 节点路径
     * @param mode zk模式
     * @return
     */
    String create(String path, CreateMode mode);

    /**
     * 创建节点,并存入数据
     * 
     * @param path 节点路径
     * @param data 节点数据,可序列化的对象
     * @param mode zk模式
     * @return 最终路径
     */
    String create(String path, Serializable data, CreateMode mode);

    /**
     * 删除节点
     * 
     * @param path 节点路径
     */
    void delete(String path);

    /**
     * 获取子节点
     * 
     * @param path 节点路径
     * @return
     */
    List<String> getChildren(String path);

    /**
     * 判断节点是否存在
     * 
     * @param path 节点路径
     * @return
     */
    boolean isExist(String path);

    /**
     * 获取节点数据
     * 
     * @param path 节点路径
     * @return
     */
    Object getData(String path);

    /**
     * 增加子节点修改监听器
     * 
     * @param path 节点路径
     * @param listener ChildListener
     * @return
     */
    List<String> addChildListener(String path, ChildListener listener);

    /**
     * 删除节点修改监听器
     * 
     * @param path 节点路径
     * @param listener ChildListener
     */
    void removeChildListener(String path, ChildListener listener);

    /**
     * 增加状态监听器
     * 
     * @param listener StateListener
     */
    void addStateListener(StateListener listener);

    /**
     * 删除状态监听器
     * 
     * @param listener StateListener
     */
    void removeStateListener(StateListener listener);

    /**
     * 是否已连接
     * 
     * @return
     */
    boolean isConnected();

    /**
     * 关闭连接
     */
    void close();

}
