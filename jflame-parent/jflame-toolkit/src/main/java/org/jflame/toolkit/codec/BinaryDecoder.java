package org.jflame.toolkit.codec;

/**
 * 二进制解码接口
 * 
 * @author yucan.zhang
 */
public interface BinaryDecoder {

    byte[] decode(byte[] source) throws TranscodeException;
}
