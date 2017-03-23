package org.jflame.toolkit.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jflame.toolkit.codec.Hex;
import org.jflame.toolkit.codec.TranscodeException;
import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * 对称加密,支持算法des,3des,aes. 基于bouncyCastle加密库封装
 * <p>
 * 字符串默认utf-8编码. AES只支持128密钥长度
 * 
 * @author zyc
 */
public class SymmetricEncryption extends AbstractEncryption {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 构造函数,使用默认加密填充方式 ecb/PKCS5Padding.
     * 
     * @param algorithm 算法名称
     */
    public SymmetricEncryption(Algorithm algorithm) {
        curAlgorithm = algorithm;
        curEncryptMode = EncryptMode.ECB;
        curPaddingMode = PaddingMode.PKCS5Padding;

    }

    /**
     * 构造函数,指定加密算法,填充模式.
     * 
     * @param algorithm 算法名
     * @param encMode 加密方式
     * @param paddingMode 填充方式
     */
    public SymmetricEncryption(Algorithm algorithm, EncryptMode encMode, PaddingMode paddingMode) {
        curAlgorithm = algorithm;
        setCurEncryptMode(encMode);
        setCurPaddingMode(paddingMode);
    }

    /**
     * 加密.
     * 
     * @param content 明文
     * @param keybytes 密钥
     * @param ivParam 向量,无需向量或使用默认向量传null
     * @return 密文byte[]
     * @throws EncryptException 加密异常
     */
    public byte[] encrypt(final byte[] content, final byte[] keybytes, byte[] ivParam) throws EncryptException {
        return doCompute(content, keybytes, ivParam, Cipher.ENCRYPT_MODE);
    }

    /**
     * 解密.
     * 
     * @param data byte[]密文
     * @param keybytes byte[]密钥
     * @param ivParam byte[]向量,无需向量或使用默认向量传null
     * @return byte[]明文
     * @throws EncryptException 解密异常
     */
    public byte[] decrypt(final byte[] data,final byte[] keybytes, byte[] ivParam) throws EncryptException {
        return doCompute(data, keybytes, ivParam, Cipher.DECRYPT_MODE);
    }

    
    /**
     * 加密字符串,返回base64密文.
     * 
     * @param plainText 明文
     * @param password 密钥
     * @param ivParam 向量,无需向量传null
     * @return 密文,base64字符串
     * @throws EncryptException 编码转换或加密异常
     */
    public String encrytToBase64(String plainText, String password, String ivParam) throws EncryptException {
        if (StringHelper.isEmpty(plainText)) {
            return plainText;
        }
        return TranscodeHelper.encodeBase64String(doEncryptString(plainText, password, ivParam));
    }

    /**
     * 加密字符串,返回十六进制密文.
     * 
     * @param plainText 明文
     * @param password 密钥
     * @param ivParam 向量,无需向量传null
     * @return 密文,十六进制字符串
     * @throws EncryptException 编码转换或加密异常
     */
    public String encrytToHex(String plainText, String password, String ivParam) throws EncryptException {
        if (StringHelper.isEmpty(plainText)) {
            return plainText;
        }
        return TranscodeHelper.encodeHexString(doEncryptString(plainText, password, ivParam));
    }

    /**
     * 加密字符串
     * @param plainText 明文
     * @param password 密钥
     * @param ivParam 向量
     * @return
     * @throws EncryptException 编码转换或解密异常
     */
    private byte[] doEncryptString(String plainText, String password, String ivParam) throws EncryptException {
        byte[] contBytes = null;
        byte[] keyBytes = null;
        byte[] ivBytes = null;
        try {
            contBytes = plainText.getBytes(getCharset());
            keyBytes = password.getBytes(getCharset());
            if (ivParam != null) {
                ivBytes = ivParam.getBytes(getCharset());
            }
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException("加密时,字符编码错误" + charset, e);
        }
        return encrypt(contBytes, keyBytes, ivBytes);
    }

    /**
     * 解密字符串,密文为base64编码.
     * 
     * @param cipherBase64Text 密文
     * @param password 密钥
     * @param ivParam 向量
     * @return 解密后字符串
     * @throws EncryptException 编码转换或解密异常
     */
    public String decryptBase64(String cipherBase64Text, String password, String ivParam) throws EncryptException {
        if (StringHelper.isEmpty(cipherBase64Text)) {
            return cipherBase64Text;
        }
        byte[] cipherBytes = null;
        try {
            cipherBytes = TranscodeHelper.dencodeBase64(cipherBase64Text);
            return doDencrypt(cipherBytes, password, ivParam);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException("解密异常", e);
        }
    }

    /**
     * 解密字符串,密文为16进制编码.
     * 
     * @param ciphertext 密文
     * @param password 密钥
     * @param ivParam 向量
     * @return 解密后字符串
     * @throws EncryptException 字符编码转换或解密异常
     */
    public String dencryptHex(String ciphertext, String password, String ivParam) throws EncryptException {
        if (StringHelper.isEmpty(ciphertext)) {
            return ciphertext;
        }
        byte[] cipherBytes = null;
        try {
            cipherBytes = Hex.decodeHex(ciphertext.toCharArray());
            return doDencrypt(cipherBytes, password, ivParam);
        } catch (TranscodeException|UnsupportedEncodingException e) {
            throw new EncryptException("解密异常", e);
        }
    }

    private String doDencrypt(byte[] cipherBytes, String password, String ivParam) throws UnsupportedEncodingException {
        byte[] keyBytes = null;
        byte[] ivBytes = null;
        keyBytes = password.getBytes(getCharset());
        if (ivParam != null) {
            ivBytes = ivParam.getBytes(getCharset());
        }

        byte[] plainBytes = decrypt(cipherBytes, keyBytes, ivBytes);
        return new String(plainBytes, charset);
    }
    
    /**
     * 执行加解密算法计算
     * @param data byte[] 内容
     * @param keybytes 密钥
     * @param ivParam 向量
     * @param cipherMode 计算方式,即加密或解密，取值:Cipher.ENCRYPT_MODE,Cipher.DECRYPT_MODE
     * @return
     * @throws EncryptException
     */
    private byte[] doCompute(final byte[] data, final byte[] keybytes, byte[] ivParam, int cipherMode)
            throws EncryptException {
        if (data == null) {
            return null;
        }
        Key key;
        checkKeyOrIv(keybytes, ivParam);
        if (curEncryptMode != EncryptMode.ECB && ivParam == null) {
            ivParam = initIvParam();
        }
        try {
            key = new SecretKeySpec(keybytes, curAlgorithm.name());
            Cipher in = Cipher.getInstance(getCurCipher(), "BC");
            if (curEncryptMode == EncryptMode.ECB) {
                in.init(cipherMode, key);
            } else {
                in.init(cipherMode, key, new IvParameterSpec(ivParam));
            }
            byte[] cipherText = in.doFinal(data);
            return cipherText;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
                | NoSuchProviderException e) {
            throw new EncryptException(cipherMode==Cipher.ENCRYPT_MODE?"加密异常":"解密异常", e);
        }
    }

    private void checkKeyOrIv(final byte[] keybytes, byte[] ivParam) throws EncryptException {
        String errmsg;
        errmsg = checkKey(keybytes);
        if (errmsg != null) {
            throw new EncryptException(errmsg);
        }
        if (curEncryptMode != EncryptMode.ECB) {
            errmsg = checkIvParam(ivParam);
            if (errmsg != null) {
                throw new EncryptException(errmsg);
            }
        }
    }


    String checkKey(byte[] keyBytes) {
        if (curAlgorithm == Algorithm.DES) {
            if (keyBytes.length != 8) {
                return "DES密钥长度为8 bytes";
            }
        } else if (curAlgorithm == Algorithm.DESede) {
            if (keyBytes.length != 24) {
                return "3DES密钥长度为24 bytes";
            }
        } else if (curAlgorithm == Algorithm.AES) {
            if (keyBytes.length != 16) {
                return "AES密钥长度为16 bytes";
            }
        }
        return null;
    }

    String checkIvParam(byte[] ivBytes) {
        if (ivBytes != null) {
            if (curAlgorithm == Algorithm.DES) {
                if (ivBytes.length != 8) {
                    return "DES向量长度为8 bytes";
                }
            } else if (curAlgorithm == Algorithm.DESede) {
                if (ivBytes.length != 24) {
                    return "3DES向量长度为24 bytes";
                }
            } else if (curAlgorithm == Algorithm.AES) {
                if (ivBytes.length != 16) {
                    return "AES向量长度为16 bytes";
                }
            }
        }
        return null;
    }

    byte[] initIvParam() {
        if (curAlgorithm == Algorithm.DES) {
            return new byte[8];
        } else if (curAlgorithm == Algorithm.AES) {
            return new byte[16];
        } else if (curAlgorithm == Algorithm.DESede) {
            return new byte[24];
        }
        return null;
    }

    @Override
    public void setCurEncryptMode(EncryptMode curEncryptMode) {
        if (curEncryptMode != EncryptMode.CBC && curEncryptMode != EncryptMode.ECB && curEncryptMode != EncryptMode.OFB
                && curEncryptMode != EncryptMode.CFB) {
            throw new EncryptException(curEncryptMode + "不适合des或aes加密算法");
        }
        this.curEncryptMode = curEncryptMode;
    }

    @Override
    public void setCurPaddingMode(PaddingMode curPaddingMode) {
        if (curPaddingMode != PaddingMode.NoPadding && curPaddingMode != PaddingMode.PKCS5Padding
                && curPaddingMode != PaddingMode.PKCS7Padding) {
            throw new EncryptException(curPaddingMode + "不适合des或aes加密算法");
        }
        this.curPaddingMode = curPaddingMode;
    }

    @Override
    public void setCurAlgorithm(Algorithm curAlgorithm) {
        if (curAlgorithm != Algorithm.AES && curAlgorithm != Algorithm.DES && curAlgorithm != Algorithm.DESede) {
            throw new EncryptException("当前类只支持des,aes,3des算法");
        }
        this.curAlgorithm = curAlgorithm;
    }

}
