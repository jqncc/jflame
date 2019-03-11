package org.jflame.toolkit.zookeeper;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Zookeeper Client
 * 
 * @author yucan.zhang
 * @param <T>
 */
public abstract class AbstractZookeeperClient<T> implements ZookeeperClient {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractZookeeperClient.class);

    private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<StateListener>();
    private final ConcurrentMap<String,ConcurrentMap<ChildListener,T>> childListeners = new ConcurrentHashMap<>();

    protected static final int DEFAULT_SESSION_TIMEOUT = 60 * 1000;
    protected static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    protected String connUrl;
    protected int sessionTimeout = 0;
    protected int connectionTimeout = 0;
    private volatile boolean closed = false;

    /**
     * 构造函数,使用缺少超时
     * 
     * @param connUrl 连接串
     */
    public AbstractZookeeperClient(String connUrl) {
        this.connUrl = connUrl;
        sessionTimeout = DEFAULT_SESSION_TIMEOUT;
        connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    }

    /**
     * 构造函数
     * 
     * @param connUrl 连接串
     * @param sessionTimeout 会话超时
     * @param connectionTimeout 连接超时
     */
    public AbstractZookeeperClient(String connUrl, int sessionTimeout, int connectionTimeout) {
        this.connUrl = connUrl;
        this.sessionTimeout = sessionTimeout > 0 ? sessionTimeout : DEFAULT_SESSION_TIMEOUT;
        this.connectionTimeout = connectionTimeout > 0 ? connectionTimeout : DEFAULT_CONNECTION_TIMEOUT;
    }

    public String create(String path) {
        return createPersistent(path, null, false);
    }

    /**
     * 创建临时节点
     * 
     * @param path 节点路径
     * @param isSequential 是否顺序编号
     * @return
     */
    public String createEphemeral(String path, boolean isSequential) {
        return createEphemeral(path, null, isSequential);
    }

    /**
     * 创建持久节点
     * 
     * @param path 节点路径
     * @param isSequential 是否顺序编号
     * @return
     */
    public String createPersistent(String path, boolean isSequential) {
        return createPersistent(path, null, isSequential);
    }

    /**
     * 创建临时节点,并存储数据
     * 
     * @param path 节点路径
     * @param data 节点数据,可序列化的对象
     * @param isSequential 是否顺序编号
     * @return
     */
    public String createEphemeral(String path, Serializable data, boolean isSequential) {
        return create(path, data, isSequential ? CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.EPHEMERAL);
    }

    /**
     * 创建持久节点,并存储数据
     * 
     * @param path 节点路径
     * @param data 节点数据,可序列化的对象
     * @param isSequential 是否顺序编号
     * @return
     */
    public String createPersistent(String path, Serializable data, boolean isSequential) {
        return create(path, data, isSequential ? CreateMode.PERSISTENT_SEQUENTIAL : CreateMode.PERSISTENT);
    }

    /**
     * 创建节点
     * 
     * @param path 节点路径
     * @param mode zk模式
     * @return
     */
    /* public String create(String path, CreateMode mode) {
        return create(path, null, mode);
    }*/

    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    public void removeStateListener(StateListener listener) {
        stateListeners.remove(listener);
    }

    public Set<StateListener> getSessionListeners() {
        return stateListeners;
    }

    @Override
    public List<String> addChildListener(String path, final ChildListener listener) {
        ConcurrentMap<ChildListener,T> listeners = childListeners.get(path);
        if (listeners == null) {
            childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildListener,T>());
            listeners = childListeners.get(path);
        }
        T targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener, createTargetChildListener(path, listener));
            targetListener = listeners.get(listener);
        }
        return addTargetChildListener(path, targetListener);
    }

    @Override
    public void removeChildListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener,T> listeners = childListeners.get(path);
        if (listeners != null) {
            T targetListener = listeners.remove(listener);
            if (targetListener != null) {
                removeTargetChildListener(path, targetListener);
            }
        }
    }

    protected void stateChanged(int state) {
        for (StateListener sessionListener : getSessionListeners()) {
            sessionListener.stateChanged(state);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            doClose();
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }

    protected abstract void doClose();

    // protected abstract void createPersistent(String path);

    protected abstract String create(String path, Serializable data, CreateMode mode);

    protected abstract T createTargetChildListener(String path, ChildListener listener);

    protected abstract List<String> addTargetChildListener(String path, T listener);

    protected abstract void removeTargetChildListener(String path, T listener);

}
