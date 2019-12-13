package org.jflame.commons.zookeeper.curator;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import org.jflame.commons.exception.DataAccessException;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.zookeeper.AbstractZookeeperClient;
import org.jflame.commons.zookeeper.ChildNodeListener;
import org.jflame.commons.zookeeper.NodeDataListener;
import org.jflame.commons.zookeeper.StateListener;

/**
 * 基于curator客户端实现zookeeper操作类
 * 
 * @author yucan.zhang
 */
public class CuratorZookeeperClient extends AbstractZookeeperClient {

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
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
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
    public void delete(String path, boolean isDeleteChildren) {
        try {
            DeleteBuilder deleteBuilder = client.delete();
            if (isDeleteChildren) {
                deleteBuilder.deletingChildrenIfNeeded();
            }
            deleteBuilder.forPath(path);
        } catch (NoNodeException e) {
            // ignore
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage(), e);
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
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isExist(String path) {
        try {
            return client.checkExists()
                    .forPath(path) != null;
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public String create(String path, Serializable data, CreateMode mode) {
        try {
            if (data != null) {
                return client.create()
                        .creatingParentsIfNeeded()
                        .withMode(mode)
                        .forPath(path);
            } else {
                return client.create()
                        .creatingParentsIfNeeded()
                        .withMode(mode)
                        .forPath(path, SerializationUtils.serialize(data));
            }
        } catch (NodeExistsException e) {
            return path;
        } catch (SerializationException e) {
            throw new DataAccessException(e.getMessage());
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public <T extends Serializable> T getData(String path) {
        try {
            byte[] nodeData = client.getData()
                    .forPath(path);
            if (nodeData != null) {
                return SerializationUtils.deserialize(nodeData);
            }
            return null;
        } catch (SerializationException e) {
            throw new DataAccessException(e.getMessage());
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void writeDate(String path, Serializable data) {
        try {
            client.setData()
                    .forPath(path, SerializationUtils.serialize(data));
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public boolean isConnected() {
        return client.getZookeeperClient()
                .isConnected();
    }

    @Override
    public void doClose() {
        if (!childNodeCaches.isEmpty()) {
            childNodeCaches.forEach((k, v) -> {
                IOHelper.closeQuietly(v);
            });
        }
        if (!nodeDataCaches.isEmpty()) {
            nodeDataCaches.forEach((k, v) -> {
                IOHelper.closeQuietly(v);
            });
        }
        client.close();
    }

    private final ConcurrentMap<String,ConcurrentMap<ChildNodeListener,PathChildrenCacheListener>> childListeners = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,ConcurrentMap<NodeDataListener,NodeCacheListener>> dataChildListeners = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,PathChildrenCache> childNodeCaches = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,NodeCache> nodeDataCaches = new ConcurrentHashMap<>();

    @Override
    public List<String> registerChildListener(String path, ChildNodeListener listener) {
        ConcurrentMap<ChildNodeListener,PathChildrenCacheListener> pathListeners = childListeners.get(path);
        if (pathListeners == null) {
            childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildNodeListener,PathChildrenCacheListener>());
            pathListeners = childListeners.get(path);
        }
        PathChildrenCache pathChildrenCache = childNodeCaches.get(path);
        if (pathChildrenCache == null) {
            try {
                pathChildrenCache = new PathChildrenCache(client, path, true);
                pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);// 启动后对已有节点不触发事件
            } catch (Exception e) {
                throw new DataAccessException("创建PathChildrenCache异常", e);
            }
            childNodeCaches.put(path, pathChildrenCache);
        }
        PathChildrenCacheListener targetListener = pathListeners.get(listener);
        if (targetListener == null) {
            pathListeners.putIfAbsent(listener, new ChildListenerAdapter(listener));
            targetListener = pathListeners.get(listener);
        }
        pathChildrenCache.getListenable()
                .addListener(targetListener);

        if (pathChildrenCache.getCurrentData() != null) {
            return pathChildrenCache.getCurrentData()
                    .stream()
                    .map(ChildData::getPath)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void unregisterChildListener(String path, ChildNodeListener listener) {
        ConcurrentMap<ChildNodeListener,PathChildrenCacheListener> listeners = childListeners.get(path);
        if (listeners != null) {
            PathChildrenCacheListener targetListener = listeners.remove(listener);
            if (targetListener != null) {
                PathChildrenCache pathChildrenCache = childNodeCaches.get(path);
                pathChildrenCache.getListenable()
                        .removeListener(targetListener);
                // 没有监听器了直接删除
                if (pathChildrenCache.getListenable()
                        .size() == 0) {
                    childNodeCaches.remove(path);
                    IOHelper.closeQuietly(pathChildrenCache);
                }
            }
        }
    }

    @Override
    public void registerDataListener(String path, NodeDataListener listener) {
        ConcurrentMap<NodeDataListener,NodeCacheListener> nodeListeners = dataChildListeners.get(path);
        if (nodeListeners == null) {
            dataChildListeners.putIfAbsent(path, new ConcurrentHashMap<NodeDataListener,NodeCacheListener>());
            nodeListeners = dataChildListeners.get(path);
        }
        NodeCache nodeCache = nodeDataCaches.get(path);
        if (nodeCache == null) {
            try {
                nodeCache = new NodeCache(client, path);
                nodeCache.start();// nodeCache.start(true)时如果先监听再创建节点会出现异常
            } catch (Exception e) {
                throw new DataAccessException(path + " nodeCache启动异常", e);
            }
            nodeDataCaches.put(path, nodeCache);
        }
        NodeCacheListener targetListener = nodeListeners.get(listener);
        if (targetListener == null) {
            nodeListeners.putIfAbsent(listener, new DataListenerAdapter(path, nodeCache, listener));
            targetListener = nodeListeners.get(listener);
        }

        nodeCache.getListenable()
                .addListener(targetListener);
    }

    @Override
    public void unregisterDataListener(String path, NodeDataListener listener) {
        ConcurrentMap<NodeDataListener,NodeCacheListener> listeners = dataChildListeners.get(path);
        if (listeners != null) {
            NodeCacheListener targetListener = listeners.remove(listener);
            if (targetListener != null) {
                NodeCache nodeCache = nodeDataCaches.get(path);
                nodeCache.getListenable()
                        .removeListener(targetListener);
                // 没有监听器了直接删除
                if (nodeCache.getListenable()
                        .size() == 0) {
                    nodeDataCaches.remove(path);
                    IOHelper.closeQuietly(nodeCache);
                }
            }
        }
    }

    /* private class CuratorWatcherImpl implements CuratorWatcher {
    
        private volatile ChildNodeListener listener;
    
        public CuratorWatcherImpl(ChildNodeListener listener) {
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
    
    public CuratorWatcher createTargetChildListener(String path, ChildNodeListener listener) {
        return new CuratorWatcherImpl(listener);
    }*/

    private class ChildListenerAdapter implements PathChildrenCacheListener {

        private ChildNodeListener listener;

        public ChildListenerAdapter(ChildNodeListener listener) {
            this.listener = listener;
        }

        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            ChildData childData = event.getData();
            if (childData != null) {
                // System.out.println(childData.getPath() + " 事件:" + event.getType());
                List<String> childs = null;
                switch (event.getType()) {
                    case CHILD_ADDED:
                    case CHILD_UPDATED:
                        childs = getChildren(childData.getPath());
                        listener.childChanged(childData.getPath(), childs);
                        break;
                    case CHILD_REMOVED:
                        listener.childChanged(childData.getPath(), null);
                        break;
                    default:
                        break;
                }
                // System.out.println(childData.getPath() + ":" + childs);
                // listener.childChanged(childData.getPath(), childs);
            }
        }
    }

    private class DataListenerAdapter implements NodeCacheListener {

        private NodeDataListener listener;
        private NodeCache cache = null;
        private String path;
        private boolean nodeIsExist = false;

        /**
         * 构造函数,被迫传入事件关联的对象,curator事件傻吊什么也没传
         * 
         * @param path
         * @param cache
         * @param listener
         */
        public DataListenerAdapter(String path, NodeCache cache, NodeDataListener listener) {
            this.cache = cache;
            this.listener = listener;
            this.path = path;
            nodeIsExist = isExist(path);
        }

        @Override
        public void nodeChanged() throws Exception {
            ChildData childData = cache.getCurrentData();
            if (childData != null) {
                nodeIsExist = true;
                String path = cache.getCurrentData()
                        .getPath();
                Object data = null;
                if (cache.getCurrentData()
                        .getData() != null) {
                    data = SerializationUtils.deserialize(cache.getCurrentData()
                            .getData());
                }
                listener.dataChange(path, data);
            } else {
                if (nodeIsExist) {
                    listener.dataDeleted(path);
                }
            }
        }

    }
    /*@Override
    public List<String> addTargetChildListener(String path, CuratorWatcher listener) {
        try {
            return client.getChildren()
                    .usingWatcher(listener)
                    .forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
    
    public void removeTargetChildListener(String path, CuratorWatcher listener) {
        ((CuratorWatcherImpl) listener).unwatch();
    }*/

    public CuratorFramework getClient() {
        return client;
    }

}
