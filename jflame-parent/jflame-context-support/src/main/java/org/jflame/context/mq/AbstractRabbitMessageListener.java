package org.jflame.context.mq;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.rabbitmq.client.Channel;

import org.jflame.commons.cache.redis.RedisClient;
import org.jflame.commons.util.StringHelper;

/**
 * 消息接收监听器父类
 * <p>
 * 缓存消息id,判断消息是否重复
 * 
 * @author yucan.zhang
 */
public abstract class AbstractRabbitMessageListener implements ChannelAwareMessageListener, InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 字符编码
     */
    private Charset charset = StandardCharsets.UTF_8;
    /**
     * 缓存前缀
     */
    private String cacheKeyPrefix;
    /**
     * 消息最大重试次数
     */
    private int maxRetry = 5;

    protected RedisClient redisClient;

    public AbstractRabbitMessageListener(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public enum MqAction {
        ACCEPT, // 处理成功
        RETRY, // 可以重试消息
        REJECT, // 无需重试消息,丢弃
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MqAction reply = null;
        String msgId = message.getMessageProperties()
                .getMessageId();
        String msgText = new String(message.getBody(), charset);
        if (StringHelper.isEmpty(msgId)) {
            try {
                msgId = JSON.parseObject(msgText)
                        .getString("messageId");
            } catch (JSONException e) {

            }
        }
        String cacheKey = null;
        Integer retry = null;
        if (StringHelper.isNotEmpty(msgId)) {
            cacheKey = getCacheKeyPrefix() + msgId;
            retry = redisClient.get(cacheKey);
            if (retry == null) {
                retry = 0;
            }

            if (retry == 1) {
                // 处理过的重复消息
                replyMq(message, channel, MqAction.ACCEPT);
                cacheMq(MqAction.ACCEPT, cacheKey, retry);
                logger.error("重复的消息,id:{}", msgId);
                return;
            } else if (retry >= maxRetry) {
                // 重新入队次数超限,丢弃
                replyMq(message, channel, MqAction.REJECT);
                cacheMq(MqAction.REJECT, cacheKey, retry);
                logger.error("消息处理次数超限,id:{}", msgId);
                return;
            }

            try {
                logger.debug("接收消息,ID:{},内容:{}", msgId, msgText);
                if (StringHelper.isNotEmpty(msgText)) {
                    reply = handleMessage(msgText, msgId, message.getMessageProperties());
                } else {
                    reply = MqAction.REJECT;
                }
            } catch (JSONException e) {
                logger.error("消息JSON格式转换失败,内容:" + msgText, e);
                reply = MqAction.REJECT;
            } catch (Exception e) {
                logger.error("消息处理异常", e);
                reply = exceptionHandle(e);
            } finally {
                replyMq(message, channel, reply);
                cacheMq(reply, cacheKey, retry);
            }
        } else {
            logger.warn("未找到消息ID拒绝处理,消息内容:{}", msgText);
            replyMq(message, channel, MqAction.REJECT);
        }
    }

    /**
     * 重发消息
     * 
     * @param message 消息
     * @param channel 通道
     * @param reply 重发
     * @throws IOException
     */
    private void replyMq(Message message, Channel channel, MqAction reply) throws IOException {
        long deliveryTag = message.getMessageProperties()
                .getDeliveryTag();
        logger.debug("消息id:{},应答方式:{}", message.getMessageProperties()
                .getMessageId(), reply);
        if (reply == null) {
            return;
        }
        switch (reply) {
            case ACCEPT:
                channel.basicAck(deliveryTag, false);
                break;
            case RETRY:
                channel.basicNack(deliveryTag, false, true);// 重新入队
                break;
            case REJECT:
                channel.basicReject(deliveryTag, false);// 丢弃
                break;
            default:
                break;
        }
    }

    /**
     * 缓存消息id,用于判断是否是重复消息. 正确接收的新消息,id缓存1天 拒绝接收
     * 
     * @param reply
     * @param key
     * @param retry
     */
    private void cacheMq(MqAction reply, String key, Integer retry) {
        switch (reply) {
            case ACCEPT:
                if (retry == 0) {
                    // redisOpt.set(0, 1, TimeUnit.DAYS);
                    redisClient.set(key, 1, 1, TimeUnit.DAYS);// 新增的消息,缓存key
                }
                break;
            case REJECT:
                if (retry > 0) {
                    // redisOpt.expire(2, TimeUnit.SECONDS);// 拒绝的消息过期
                    redisClient.expire(key, 2);
                }
                break;
            case RETRY:
                if (retry == 1) {
                    // redisOpt.set(1, 1L, TimeUnit.DAYS);
                    redisClient.set(key, 1, 1, TimeUnit.DAYS);
                } else if (retry < maxRetry) {
                    // redisOpt.increment(1);
                    redisClient.incr(key);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.cacheKeyPrefix == null) {
            cacheKeyPrefix = getClass().getPackage()
                    .getName();
        }
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public String getCacheKeyPrefix() {
        return this.cacheKeyPrefix;
    }

    public void setCacheKeyPrefix(String cacheKeyPrefix) {
        this.cacheKeyPrefix = cacheKeyPrefix;
    }

    /**
     * 根据异常类型确定应答本条消息的行为
     * 
     * @param ex
     * @return
     */
    protected MqAction exceptionHandle(Throwable ex) {
        if (ex.getCause() instanceof SQLException) {
            return MqAction.REJECT;
        }
        return MqAction.ACCEPT;
    }

    /**
     * 消息处理业务逻辑
     * 
     * @param message
     * @param messageId
     * @param messageProperties
     * @return MqAction
     * @throws Exception
     */
    public abstract MqAction handleMessage(final String message, final String messageId,
            MessageProperties messageProperties) throws Exception;
}
