package org.jflame.commons.zookeeper;

import java.io.Closeable;
import java.io.Serializable;
import java.util.List;

import org.apache.zookeeper.data.Stat;

public interface ZookeeperClient extends Closeable {

    int DEFAULT_SESSION_TIMEOUT = 60 * 1000;
    int DEFAULT_CONNECTION_TIMEOUT = 5000;

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
    <T extends Serializable> T readData(String path);

    /**
     * 读取节点数据,并返回节点Stat信息
     * 
     * @param path 节点路径
     * @param stat 用于存储Stat信息
     * @return
     */
    <T extends Serializable> T readData(String path, Stat stat);

    /**
     * 写数据
     * 
     * @param path 节点路径
     * @param data 待写入数据
     */
    void writeDate(String path, Serializable data);

    /**
     * 注册子节点修改监听器
     * 
     * @param path 节点路径
     * @param listener ChildListener
     * @return
     */
    List<String> registerChildListener(String path, ChildNodeListener listener);

    /**
     * 删除子节点监听器
     * 
     * @param path 节点路径
     * @param listener ChildListener
     */
    void unregisterChildListener(String path, ChildNodeListener listener);

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
     * 注册节点数据监听器
     * 
     * @param path 要监听的节点路径
     * @param listener
     */
    void registerDataListener(String path, NodeDataListener listener);

    /**
     * 删除节点数据监听器
     * 
     * @param path 节点路径
     * @param listener ChildListener
     */
    void unregisterDataListener(String path, NodeDataListener listener);

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
