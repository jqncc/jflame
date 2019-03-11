package org.jflame.toolkit.zookeeper.zkclient;

import java.io.Serializable;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.jflame.toolkit.exception.SerializeException;
import org.jflame.toolkit.zookeeper.AbstractZookeeperClient;
import org.jflame.toolkit.zookeeper.ChildListener;
import org.jflame.toolkit.zookeeper.StateListener;

public class ZkclientZookeeperClient extends AbstractZookeeperClient<IZkChildListener> {

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
                throw new Exception(error);
            }
        });
    }

    @Override
    public void delete(String path) {
        try {
            client.delete(path);
        } catch (ZkNoNodeException e) {
            // ignore
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return client.getChildren(path);
        } catch (ZkNoNodeException e) {
            return null;
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

    /**
     * 添加节点修改监听
     * 
     * @param path 节点
     * @param listener ChildListener
     * @return IZkChildListener
     */
    public IZkChildListener createTargetChildListener(String path, final ChildListener listener) {
        return new IZkChildListener() {

            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                listener.childChanged(parentPath, currentChilds);
            }
        };
    }

    public List<String> addTargetChildListener(String path, final IZkChildListener listener) {
        return client.subscribeChildChanges(path, listener);
    }

    public void removeTargetChildListener(String path, IZkChildListener listener) {
        client.unsubscribeChildChanges(path, listener);
    }

    @Override
    public boolean isExist(String path) {
        return client.exists(path);
    }

    @Override
    public String create(String path, Serializable data, CreateMode createMode) {
        return client.create(path, data, createMode);
    }

    @Override
    public Object getData(String path) {
        try {
            return client.readData(path);
        } catch (ZkMarshallingError e) {
            throw new SerializeException(e);
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
