package org.jflame.toolkit.zookeeper.curator;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.jflame.toolkit.exception.SerializeException;
import org.jflame.toolkit.zookeeper.AbstractZookeeperClient;
import org.jflame.toolkit.zookeeper.ChildListener;
import org.jflame.toolkit.zookeeper.StateListener;

public class CuratorZookeeperClient extends AbstractZookeeperClient<CuratorWatcher> {

    private final CuratorFramework client;

    public CuratorZookeeperClient(String url) {
        this(url, null, DEFAULT_SESSION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }

    public CuratorZookeeperClient(String url, int sessionTimeout, int connectionTimeout) {
        this(url, null, sessionTimeout, connectionTimeout);
    }

    /**
     * 构造函数
     * 
     * @param url zookeeper连接url
     * @param authority digest模式用户名密码 user:pwd
     * @param sessionTimeout
     * @param connectionTimeout
     */
    public CuratorZookeeperClient(String url, String authority, int sessionTimeout, int connectionTimeout) {
        super(url);
        Builder builder = CuratorFrameworkFactory.builder()
                .connectString(url)
                .retryPolicy(new RetryNTimes(1000 * 60 * 60 * 2, 2000))
                .connectionTimeoutMs(connectionTimeout)
                .sessionTimeoutMs(sessionTimeout);
        if (authority != null && !authority.isEmpty()) {
            builder = builder.authorization("digest", authority.getBytes());
        }
        client = builder.build();
        client.getConnectionStateListenable()
                .addListener(new ConnectionStateListener() {

                    public void stateChanged(CuratorFramework client, ConnectionState state) {
                        if (state == ConnectionState.LOST) {
                            CuratorZookeeperClient.this.stateChanged(StateListener.DISCONNECTED);
                        } else if (state == ConnectionState.CONNECTED) {
                            CuratorZookeeperClient.this.stateChanged(StateListener.CONNECTED);
                        } else if (state == ConnectionState.RECONNECTED) {
                            CuratorZookeeperClient.this.stateChanged(StateListener.RECONNECTED);
                        }
                    }
                });
        client.start();
    }

    @Override
    public void delete(String path) {
        try {
            client.delete()
                    .forPath(path);
        } catch (NoNodeException e) {
            // ignore
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return client.getChildren()
                    .forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return client.getZookeeperClient()
                .isConnected();
    }

    @Override
    public void doClose() {
        client.close();
    }

    private class CuratorWatcherImpl implements CuratorWatcher {

        private volatile ChildListener listener;

        public CuratorWatcherImpl(ChildListener listener) {
            this.listener = listener;
        }

        public void unwatch() {
            this.listener = null;
        }

        public void process(WatchedEvent event) throws Exception {
            if (listener != null) {
                listener.childChanged(event.getPath(), client.getChildren()
                        .usingWatcher(this)
                        .forPath(event.getPath()));
            }
        }
    }

    public CuratorWatcher createTargetChildListener(String path, ChildListener listener) {
        return new CuratorWatcherImpl(listener);
    }

    @Override
    public List<String> addTargetChildListener(String path, CuratorWatcher listener) {
        try {
            return client.getChildren()
                    .usingWatcher(listener)
                    .forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void removeTargetChildListener(String path, CuratorWatcher listener) {
        ((CuratorWatcherImpl) listener).unwatch();
    }

    @Override
    public boolean isExist(String path) {
        try {
            return client.checkExists()
                    .forPath(path) != null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public String create(String path, Serializable data, CreateMode mode) {
        try {
            if (data != null) {
                return client.create()
                        .withMode(mode)
                        .forPath(path);
            } else {
                return client.create()
                        .withMode(mode)
                        .forPath(path, SerializationUtils.serialize(data));
            }
        } catch (NodeExistsException e) {
            return path;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public Object getData(String path) {
        try {
            byte[] nodeData = client.getData()
                    .forPath(path);
            if (nodeData != null) {
                return SerializationUtils.deserialize(nodeData);
            }
            return null;
        } catch (SerializationException e) {
            throw new SerializeException(e);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public CuratorFramework getClient() {
        return client;
    }

}
