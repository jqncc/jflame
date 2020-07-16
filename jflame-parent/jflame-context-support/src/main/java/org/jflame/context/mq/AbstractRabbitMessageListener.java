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

import com.rabbitmq.client.Channel;

import org.jflame.commons.util.StringHelper;
import org.jflame.context.cache.redis.RedisClient;

/**
 * 实现重复消息判断的消息接收监听器父类.
 * <p>
 * 通过缓存消息id,判断消息是否重复.
 * 
 * @author yucan.zhang
 */
public abstract class AbstractRabbitMessageListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 字符编码
     */
    private Charset charset = StandardCharsets.UTF_8;
    /**
     * 缓存前缀
     */
    protected String cacheKeyPrefix;

    protected RedisClient redisClient;

    public AbstractRabbitMessageListener(RedisClient redisClient) {
        this.redisClient = redisClient;
        cacheKeyPrefix = getClass().getPackage()
                .getName();
    }

    public enum MqAction {
        ACCEPT, // 处理成功
        RETRY, // 可以重试消息
        REJECT, // 无需重试消息,丢弃
    }

    public void onMessage(Message message, Channel channel) throws Exception {
        String msgId = message.getMessageProperties()
                .getMessageId();
        String cacheKey = null;
        if (StringHelper.isNotEmpty(msgId)) {
            cacheKey = cacheKeyPrefix + msgId;
            if (redisClient.exists(cacheKey)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("重复的消息:{}", msgId);
                }
                replyMq(cacheKey, message, channel, MqAction.REJECT);
                return;
            }
        }
        MqAction reply = null;
        String msgText = null;
        try {
            msgText = new String(message.getBody(), charset);
            if (logger.isDebugEnabled()) {
                logger.debug("接收消息,ID:{},内容:{}", msgId, msgText);
            }
            reply = handleMessage(msgText, msgId, message.getMessageProperties());
        } catch (Exception e) {
            logger.error("消息处理异常,消息:{},ex:{}", msgText, e);
            reply = exceptionHandle(e);
        }
        replyMq(cacheKey, message, channel, reply);
    }

    private void replyMq(String cacheKey, Message message, Channel channel, MqAction reply) throws IOException {
        long deliveryTag = message.getMessageProperties()
                .getDeliveryTag();
        switch (reply) {
            case ACCEPT:
                channel.basicAck(deliveryTag, false);
                redisClient.set(cacheKey, 1, 1, TimeUnit.DAYS);
                break;
            case RETRY:
                channel.basicNack(deliveryTag, false, true);// 重新入队
                redisClient.set(cacheKey, 1, 1, TimeUnit.DAYS);
                break;
            case REJECT:
                channel.basicReject(deliveryTag, false);// 丢弃
                break;
            default:
                channel.basicAck(deliveryTag, false);
                redisClient.set(cacheKey, 1, 1, TimeUnit.DAYS);
                break;
        }
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
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
