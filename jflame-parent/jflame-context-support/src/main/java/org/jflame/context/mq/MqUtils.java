package org.jflame.context.mq;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.alibaba.fastjson.JSON;

import org.jflame.commons.util.CharsetHelper;

/**
 * 消息队列工具类
 * 
 * @author yucan.zhang
 */
public final class MqUtils {

    /**
     * 消息对象MqMsg转为amqp text_plain Message
     * 
     * @param msg 消息对象MqMsg
     * @return
     */
    public static <T> Message toTextMessage(MqMsg<T> msg) {
        byte[] msgBytes = JSON.toJSONBytes(msg);
        return MessageBuilder.withBody(msgBytes)
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .setMessageId(msg.getMessageId())
                .build();
    }

    public static <T> Message toTextMessage(String msg, String messageId) {
        return MessageBuilder.withBody(CharsetHelper.getUtf8Bytes(msg))
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .setMessageId(messageId)
                .build();
    }

    /**
     * 消息对象MqMsg转为amqp text_plain Message,设置延迟时间
     * 
     * @param msg 消息对象MqMsg
     * @param delay 延迟时间,单位秒
     * @return
     */
    public static <T> Message toTextDelayMessage(MqMsg<T> msg, int delay) {
        byte[] msgBytes = JSON.toJSONBytes(msg);
        Message message = MessageBuilder.withBody(msgBytes)
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .setMessageId(msg.getMessageId())
                .build();
        if (delay < 0 || delay > Integer.MAX_VALUE / 1000) {
            throw new IllegalArgumentException("delay>0 and < Integer.MAX_VALUE/1000");
        }
        message.getMessageProperties()
                .setDelay(delay * 1000);
        return message;
    }

    public static <T> Message toTextDelayMessage(String msg, String messageId, int delay) {
        Message message = MessageBuilder.withBody(CharsetHelper.getUtf8Bytes(msg))
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .setMessageId(messageId)
                .build();
        if (delay < 0 || delay > Integer.MAX_VALUE / 1000) {
            throw new IllegalArgumentException("delay>0 and < Integer.MAX_VALUE/1000");
        }
        message.getMessageProperties()
                .setDelay(delay * 1000);
        return message;
    }

    /**
     * 发送消息到RabbitMQ
     * 
     * @param template RabbitTemplate必须已经指定exchange和routeKey
     * @param msg 统一消息对象
     */
    public static <T> void send(RabbitTemplate template, MqMsg<T> msg) throws AmqpException {
        template.send(toTextMessage(msg));
    }

    /**
     * 发送消息到RabbitMQ
     * 
     * @param template RabbitTemplate必须已经指定exchange和routeKey
     * @param exchange exchange
     * @param routingKey routing key
     * @param msg 统一消息对象
     */
    public static <T> void send(RabbitTemplate template, String exchange, String routingKey, MqMsg<T> msg)
            throws AmqpException {
        template.send(exchange, routingKey, toTextMessage(msg));
    }
}
