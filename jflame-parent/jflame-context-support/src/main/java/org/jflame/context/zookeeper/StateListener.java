package org.jflame.context.zookeeper;

/**
 * 状态监听
 * 
 * @author yucan.zhang
 */
public interface StateListener {

    int DISCONNECTED = 0;

    int CONNECTED = 1;

    int RECONNECTED = 2;

    void stateChanged(int connected);

}
