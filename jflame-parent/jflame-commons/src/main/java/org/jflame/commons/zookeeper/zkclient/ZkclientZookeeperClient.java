package org.jflame.commons.zookeeper.zkclient;

import java.io.Serializable;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import org.jflame.commons.exception.DataAccessException;
import org.jflame.commons.file.FileHelper;
import org.jflame.commons.zookeeper.AbstractZookeeperClient;
import org.jflame.commons.zookeeper.ChildListener;
import org.jflame.commons.zookeeper.StateListener;

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
            int c = StringUtils.countMatches(path, FileHelper.UNIX_SEPARATOR);
            if (c > 1) {
                String[] paths = StringUtils.split(path, FileHelper.UNIX_SEPARATOR);
                String parentPath = "";
                for (int i = 0; i < paths.length - 1; i++) {
                    parentPath = parentPath + FileHelper.UNIX_SEPARATOR + paths[0];
                    if (!isExist(parentPath)) {
                        client.create(parentPath, null, createMode);
                    }
                }
            }
            return client.create(path, data, createMode);
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
