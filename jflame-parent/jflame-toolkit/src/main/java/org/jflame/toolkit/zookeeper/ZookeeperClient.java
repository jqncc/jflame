package org.jflame.toolkit.zookeeper;

import java.io.Serializable;
import java.util.List;

public interface ZookeeperClient {

    /**
     * 创建临时节点
     * 
     * @param path 节点路径
     * @param isSequential 是否顺序编号
     * @return
     */
    String createEphemeral(String path, boolean isSequential);

    /**
     * 创建持久节点
     * 
     * @param path 节点路径
     * @param isSequential 是否顺序编号
     * @return
     */
    String createPersistent(String path, boolean isSequential);

    /**
     * 创建临时节点,并存储数据
     * 
     * @param path 节点路径
     * @param data 节点数据,可序列化的对象
     * @param isSequential 是否顺序编号
     * @return
     */
    String createEphemeral(String path, Serializable data, boolean isSequential);

    /**
     * 创建持久节点,并存储数据
     * 
     * @param path 节点路径
     * @param data 节点数据,可序列化的对象
     * @param isSequential 是否顺序编号
     * @return
     */
    String createPersistent(String path, Serializable data, boolean isSequential);

    /**
     * 删除节点
     * 
     * @param isDeleteChildren 是否递归删除子节点
     * @param path 节点路径
     */
    void delete(String path, boolean isDeleteChildren);

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
    <T extends Serializable> T getData(String path);

    /**
     * 写数据
     * 
     * @param path 节点路径
     * @param data 待写入数据
     */
    void writeDate(String path, Serializable data);

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
