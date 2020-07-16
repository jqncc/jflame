package org.jflame.context.zookeeper;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zookeeper操作抽象父类.
 * 
 * @author yucan.zhang
 */
public abstract class AbstractZookeeperClient implements ZookeeperClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<StateListener>();

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

    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    public void removeStateListener(StateListener listener) {
        stateListeners.remove(listener);
    }

    public Set<StateListener> getSessionListeners() {
        return stateListeners;
    }

    /*   @Override
    public List<String> registerChildListener(String path, final ChildNodeListener listener) {
        ConcurrentMap<ChildNodeListener,T> listeners = childListeners.get(path);
        if (listeners == null) {
            childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildNodeListener,T>());
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
    public void unregisterChildListener(String path, ChildNodeListener listener) {
        ConcurrentMap<ChildNodeListener,T> listeners = childListeners.get(path);
        if (listeners != null) {
            T targetListener = listeners.remove(listener);
            if (targetListener != null) {
                removeTargetChildListener(path, targetListener);
            }
        }
    }*/

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

    /**
     * 创建节点.如果节点已经存在视为成功不抛出异常
     * 
     * @param path
     * @param data
     * @param mode
     * @return
     */
    protected abstract String create(String path, Serializable data, CreateMode mode);

    // protected abstract T createTargetChildListener(String path, ChildNodeListener listener);

    // protected abstract List<String> addTargetChildListener(String path, T listener);

    // protected abstract void removeTargetChildListener(String path, T listener);

}
