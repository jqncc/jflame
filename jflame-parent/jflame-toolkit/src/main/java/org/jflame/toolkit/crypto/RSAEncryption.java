package org.jflame.toolkit.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * RSA非对称加密算法,基于BouncyCastle加密库实现.
 */
public class RSAEncryption extends AbstractEncryption {

    /**
     * 构造函数,ras默认填充NoPadding.
     */
    public RSAEncryption() {
        curAlgorithm = Algorithm.RSA;
        curEncryptMode = EncryptMode.NONE;
        curPaddingMode = PaddingMode.NoPadding;
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 随机生成公私密钥对,存入密钥文件.
     * 
     * @param publicKeyFile 公钥文件路径
     * @param privateKeyFile 私钥文件路径
     * @throws EncryptException 加解密异常
     */
    public void generateKeyPair(String publicKeyFile, String privateKeyFile) throws EncryptException {
        // RSA算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance(curAlgorithm.name(), "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new EncryptException(e);
        }
        kpg.initialize(1024, sr);
        // 生成密匙对
        KeyPair kp = kpg.generateKeyPair();
        Key publicKey = kp.getPublic();
        Key privateKey = kp.getPrivate();
        // 用对象流将生成的密钥写入文件
        try (ObjectOutputStream pubKeyOutStream = new ObjectOutputStream(new FileOutputStream(publicKeyFile));
                ObjectOutputStream priKeyOutStream = new ObjectOutputStream(new FileOutputStream(privateKeyFile))) {
            pubKeyOutStream.writeObject(publicKey);
            priKeyOutStream.writeObject(privateKey);
        } catch (IOException e) {
            throw new EncryptException("写入密钥文件失败", e);
        }

    }

    /**
     * 加密二进制内容.
     * 
     * @param content 明文，byte数组
     * @param keyFileOrStr base64密钥或密钥文件路径,文件路径必须提供绝对路径
     * @return 密文，byte数组
     * @throws EncryptException 加解密异常
     */
    public byte[] encrypt(final byte[] content, final String keyFileOrStr) throws EncryptException {
        Key key = generateKey(keyFileOrStr);
        return docrypt(key, content, CipherMode.ENCRYPT);
    }

    /**
     * 解密.
     * 
     * @param cipher 密文
     * @param keyFileOrStr base64密钥或密钥文件路径,文件路径必须提供绝对路径
     * @return 明文，byte数组
     * @throws EncryptException 加解密异常
     */
    public byte[] dencrypt(final byte[] cipher, final String keyFileOrStr) throws EncryptException {
        Key key = generateKey(keyFileOrStr);
        return docrypt(key, cipher, CipherMode.DENCRYPT);
    }

    /**
     * 加密字符串,返回base64密文.
     * 
     * @param content 明文
     * @param keyFileOrStr base64密钥或密钥文件路径,文件路径必须提供绝对路径
     * @return 密文,base64
     * @throws EncryptException 加解密异常
     */
    public String encryptBase64(final String content, final String keyFileOrStr) throws EncryptException {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        return Base64.encodeBase64String(doencrypt(content, keyFileOrStr));
    }

    /**
     * 加密字符串,返回16进制密文.
     * 
     * @param content 明文
     * @param keyFileOrStr base64密钥或密钥文件路径,文件路径必须提供绝对路径
     * @return 密文,16进制 字符串
     * @throws EncryptException 加解密异常
     */
    public String encryptHEX(final String content, final String keyFileOrStr) throws EncryptException {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        return Hex.encodeHexString(doencrypt(content, keyFileOrStr));
    }

    /**
     * 解密base64字符串密文.
     * 
     * @param cipherBase64 密文,base64字符串
     * @param keyFileOrStr base64密钥或密钥文件路径,文件路径必须提供绝对路径
     * @return 明文
     * @throws EncryptException 加解密异常
     */
    public String dencryptBase64(final String cipherBase64, final String keyFileOrStr) throws EncryptException {
        if (StringUtils.isEmpty(cipherBase64)) {
            return cipherBase64;
        }
        byte[] cipherBytes = Base64.decodeBase64(cipherBase64);
        byte[] plainBytes = dencrypt(cipherBytes, keyFileOrStr);
        try {
            return new String(plainBytes, charset);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException(e);
        }
    }

    private byte[] doencrypt(final String content, final String keyFileOrStr) throws EncryptException {
        Key key = generateKey(keyFileOrStr);
        byte[] contBytes = null;
        try {
            contBytes = content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException(e);
        }
        return docrypt(key, contBytes, CipherMode.ENCRYPT);
    }

    private Key generateKey(String keyFileOrString) throws EncryptException {
        boolean fileKey = false;
        Path keyFilePath = null;
        // 无路径分隔符且无文件扩展名视为非密钥文件
        if (keyFileOrString.indexOf(File.separator) > -1 || keyFileOrString.indexOf(".") > -1) {
            try {
                keyFilePath = Paths.get(keyFileOrString);
                if (keyFilePath.isAbsolute()) {
                    fileKey = true;
                }
            } catch (InvalidPathException e) {
                fileKey = false;
            }
        }

        if (fileKey) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyFilePath.toFile()))) {
                return (Key) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new EncryptException("密钥生成异常", e);
            }
        }
        {
            try {
                byte[] keyBytes = Base64.decodeBase64(keyFileOrString);
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance(curAlgorithm.name());
                return keyFactory.generatePrivate(x509KeySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new EncryptException("密钥生成异常", e);
            }

        }
    }

    private byte[] docrypt(Key decryptKey, byte[] cryptBytes, CipherMode cipherMode) {
        byte[] resultBytes;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");// getCurCipher()
            cipher.init(cipherMode.getValue(), decryptKey);
            resultBytes = cipher.doFinal(cryptBytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException | NoSuchProviderException e) {
            throw new EncryptException(e);
        }
        return resultBytes;
    }

    @Override
    public void setCurEncryptMode(EncryptMode curEncryptMode) {
    }

    @Override
    public void setCurAlgorithm(Algorithm curAlgorithm) {
    }

}