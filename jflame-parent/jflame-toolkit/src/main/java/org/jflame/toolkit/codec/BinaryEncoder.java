package org.jflame.toolkit.codec;

/**
 * 二进制编码接口
 * @author yucan.zhang
 *
 */
public interface BinaryEncoder {

    byte[] encode(byte[] source) throws TranscodeException;
}
