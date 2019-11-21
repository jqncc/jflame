package org.jflame.context.mq;

import java.io.Serializable;

import org.jflame.commons.key.IDHelper;

/**
 * mq消息
 * 
 * @author yucan.zhang
 * @param <T> 存放的业务数据类型
 */
public class MqMsg<T> implements Serializable {

    private static final long serialVersionUID = -2812516205061473241L;

    private String messageId;// 消息唯一id
    private String opt;// 操作类型
    private String obj;// 操作对象
    private T data;// 附带数据
    private long version = 1;// 数据版本号

    public MqMsg() {

    }

    /*public MqMsg(MqOpt opt, String obj, T data) {
        this(opt.name(), obj, data);
    }
    
    public MqMsg(MqOpt opt, T data) {
        this(opt, (String) null, data);
    }*/

    public MqMsg(String opt, T data) {
        this(opt, null, data);
    }

    public MqMsg(String opt, String obj, T data) {
        this.messageId = IDHelper.uuid();
        this.obj = obj;
        this.opt = opt;
        this.data = data;
    }

    /*public MqMsg(MqOpt opt, MqObj obj, T data) {
        this.messageId = StringHelper.uuid();
        this.obj = obj.name();
        this.opt = opt.name();
        this.data = data;
    }*/

    public MqMsg(String opt, String obj, T data, long version) {
        this(opt, obj, data);
        this.version = version;
    }

    /* public MqMsg(MqOpt opt, String obj, T data, long version) {
        this(opt, obj, data);
        this.version = version;
    }*/

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    public String getObj() {
        return obj;
    }

    public void setObj(String obj) {
        this.obj = obj;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "MqMsg [messageId=" + messageId + ", opt=" + opt + ", obj=" + obj + ", data=" + data + ", version="
                + version + "]";
    }

}
