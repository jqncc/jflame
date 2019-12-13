package org.jflame.commons.zookeeper.zkclient;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import org.jflame.commons.common.Chars;
import org.jflame.commons.exception.DataAccessException;
import org.jflame.commons.zookeeper.AbstractZookeeperClient;
import org.jflame.commons.zookeeper.ChildNodeListener;
import org.jflame.commons.zookeeper.NodeDataListener;
import org.jflame.commons.zookeeper.StateListener;

/**
 * 基于zkclient客户端实现zookeeper操作类
 * 
 * @author yucan.zhang
 */
public class ZkclientZookeeperClient extends AbstractZookeeperClient {

    private final ZkClient client;

    private volatile KeeperState state = KeeperState.SyncConnected;

    /**
     * 构造函数,使用缺少超时
     * 
     * @param connUrl 连接串
     */
    public ZkclientZookeeperClient(String connUrl) {
        this(connUrl, null, DEFAULT_SESSION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * 构造函数
     * 
     * @param connUrl 连接串
     * @param sessionTimeout 会话超时
     * @param connectionTimeout 连接超时
     */
    public ZkclientZookeeperClient(String connUrl, String authority, int sessionTimeout, int connectionTimeout) {
        super(connUrl, sessionTimeout, connectionTimeout);
        client = new ZkClient(connUrl, sessionTimeout, connectionTimeout, new MyZkSerializer());
        if (authority != null && !authority.isEmpty()) {
            client.addAuthInfo(connUrl, null);
        }
        client.subscribeStateChanges(new IZkStateListener() {

            public void handleStateChanged(KeeperState state) throws Exception {
                ZkclientZookeeperClient.this.state = state;
                if (state == KeeperState.Disconnected) {
                    stateChanged(StateListener.DISCONNECTED);
                } else if (state == KeeperState.SyncConnected) {
                    stateChanged(StateListener.CONNECTED);
                }
            }

            public void handleNewSession() throws Exception {
                stateChanged(StateListener.RECONNECTED);
            }

            @Override
            public void handleSessionEstablishmentError(Throwable error) throws Exception {
                logger.error("zookeeper connection error!", error);
                throw new RuntimeException(error);
            }
        });
    }

    @Override
    public void delete(String path, boolean isDeleteChildren) {
        try {
            if (isDeleteChildren) {
                client.deleteRecursive(path);
            } else {
                client.delete(path);
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return client.getChildren(path);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public boolean isExist(String path) {
        return client.exists(path);
    }

    @Override
    public String create(String path, Serializable data, CreateMode createMode) {
        try {
            // 如果有父级目录先生成父级
            int c = StringUtils.countMatches(path, Chars.SLASH);
            if (c > 1) {
                String[] paths = StringUtils.split(path, Chars.SLASH);
                String parentPath = "";
                for (int i = 0; i < paths.length - 1; i++) {
                    parentPath = parentPath + Chars.SLASH + paths[0];
                    if (!isExist(parentPath)) {
                        client.create(parentPath, null, createMode);
                    }
                }
            }
            return client.create(path, data, createMode);
        } catch (ZkNodeExistsException e) {
            return path;
        } catch (RuntimeException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> T getData(String path) {
        try {
            return client.readData(path, true);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void writeDate(String path, Serializable data) {
        try {
            client.writeData(path, data);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public boolean isConnected() {
        return state == KeeperState.SyncConnected;
    }

    @Override
    public void doClose() {
        client.close();
    }

    private final ConcurrentMap<String,ConcurrentMap<ChildNodeListener,IZkChildListener>> childListeners = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,ConcurrentMap<NodeDataListener,IZkDataListener>> dataChildListeners = new ConcurrentHashMap<>();

    @Override
    public List<String> registerChildListener(String path, ChildNodeListener listener) {
        ConcurrentMap<ChildNodeListener,IZkChildListener> listeners = childListeners.get(path);
        if (listeners == null) {
            childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildNodeListener,IZkChildListener>());
            listeners = childListeners.get(path);
        }
        IZkChildListener targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener, new ChildListenerAdapter(listener));
            targetListener = listeners.get(listener);
        }

        return client.subscribeChildChanges(path, targetListener);
    }

    @Override
    public void unregisterChildListener(String path, ChildNodeListener listener) {
        ConcurrentMap<ChildNodeListener,IZkChildListener> listeners = childListeners.get(path);
        if (listeners != null) {
            IZkChildListener targetListener = listeners.remove(listener);
            if (targetListener != null) {
                client.unsubscribeChildChanges(path, targetListener);
            }
        }
    }

    /**
     * 注册节点数据监听器.
     * <ol>
     * <b>zkclient注意事项:</b>
     * <li>先修改数据,再马上删除节点,数据修改节点不会触发,但会触发两次删除事件.原因是在zkclient数据修改事件中如果发现节点不存在将不会再执行转而显式触发一次删除事件</li>
     * <li>先注册事件,再创建节点时会触发一次数据更新事件</li>
     * </ol>
     */
    @Override
    public void registerDataListener(String path, NodeDataListener listener) {
        ConcurrentMap<NodeDataListener,IZkDataListener> listeners = dataChildListeners.get(path);
        if (listeners == null) {
            dataChildListeners.putIfAbsent(path, new ConcurrentHashMap<NodeDataListener,IZkDataListener>());
            listeners = dataChildListeners.get(path);
        }
        IZkDataListener targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener, new DataListenerAdapter(listener));
            targetListener = listeners.get(listener);
        }
        client.subscribeDataChanges(path, targetListener);
    }

    @Override
    public void unregisterDataListener(String path, NodeDataListener listener) {
        ConcurrentMap<NodeDataListener,IZkDataListener> listeners = dataChildListeners.get(path);
        if (listeners != null) {
            IZkDataListener targetListener = listeners.remove(listener);
            if (targetListener != null) {
                client.unsubscribeDataChanges(path, targetListener);
            }
        }
    }

    /**
     * 添加节点修改监听
     * 
     * @param path 节点
     * @param listener ChildListener
     * @return IZkChildListener
     */
    /*public IZkChildListener createTargetChildListener(String path, final ChildNodeListener listener) {
        return new IZkChildListener() {
    
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                listener.childChanged(parentPath, currentChilds);
            }
        };
    }*/

    /* public List<String> addTargetChildListener(String path, final IZkChildListener listener) {
        return client.subscribeChildChanges(path, listener);
    }*/

    /*    public void addNodeDataListener(String path, final NodeDataListener listener) {
        IZkDataListener dataListener = new IZkDataListener() {
    
            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                listener.dataDeleted(dataPath);
            }
    
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                listener.dataChange(dataPath, data);
            }
        };
        client.subscribeDataChanges(path, dataListener);
    }
    */
    /*  public void removeTargetChildListener(String path, IZkChildListener listener) {
        client.unsubscribeChildChanges(path, listener);
    }*/

    public ZkClient getClient() {
        return client;
    }

    private class ChildListenerAdapter implements IZkChildListener {

        private ChildNodeListener listener;

        public ChildListenerAdapter(ChildNodeListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
            listener.childChanged(parentPath, currentChilds);
        }
    }

    private class DataListenerAdapter implements IZkDataListener {

        private NodeDataListener listener;

        public DataListenerAdapter(NodeDataListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleDataChange(String dataPath, Object data) throws Exception {
            listener.dataChange(dataPath, data);
        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            listener.dataDeleted(dataPath);
        }
    }

    class MyZkSerializer implements ZkSerializer {

        @Override
        public byte[] serialize(Object data) throws ZkMarshallingError {
            return SerializationUtils.serialize((Serializable) data);
        }

        @Override
        public Object deserialize(byte[] bytes) throws ZkMarshallingError {
            if (bytes == null) {
                return null;
            }
            return SerializationUtils.deserialize(bytes);
        }

    }

}
