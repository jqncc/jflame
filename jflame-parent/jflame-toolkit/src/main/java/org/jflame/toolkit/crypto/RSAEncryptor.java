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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.jflame.toolkit.codec.Base64;
import org.jflame.toolkit.codec.Hex;
import org.jflame.toolkit.codec.TranscodeException;
import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * RSA非对称加密算法.<br />
 * RSA, ECB, NOPADDING/PKCS1PADDING/OAEPWITHMD5ANDMGF1PADDING<br />
 * <br />
 * JDK1.7默认ECB/PKCS1PADDING
 */
public class RSAEncryptor extends BaseEncryptor {

    /**
     * 构造函数,ras默认
     */
    public RSAEncryptor() {
        super(Algorithm.RSA, OpMode.ECB, Padding.PKCS1PADDING);
        // curAlgorithm = Algorithm.RSA; OpMode.ECB,Padding.PKCS1PADDING
    }

    /**
     * 构造函数
     * 
     * @param encMode
     * @param paddingMode 填充模式
     */
    public RSAEncryptor(OpMode encMode, Padding paddingMode) {
        super(Algorithm.RSA, encMode, paddingMode);
    }

    /**
     * 随机生成公私密钥对,存入密钥文件,密钥长度1024.
     * 
     * @param publicKeyFile 公钥文件路径
     * @param privateKeyFile 私钥文件路径
     * @throws EncryptException 加解密异常
     */
    public KeyPair generateKeyPair(String publicKeyFile, String privateKeyFile) throws EncryptException {
        // RSA算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance(Algorithm.RSA.name());
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptException(e);
        }
        kpg.initialize(1024, sr);
        // 生成密匙对
        KeyPair keyPair = kpg.generateKeyPair();
        Key publicKey = keyPair.getPublic();
        Key privateKey = keyPair.getPrivate();
        // 用对象流将生成的密钥写入文件
        if (publicKey != null && privateKeyFile != null) {
            try (ObjectOutputStream pubKeyOutStream = new ObjectOutputStream(new FileOutputStream(publicKeyFile));
                    ObjectOutputStream priKeyOutStream = new ObjectOutputStream(new FileOutputStream(privateKeyFile))) {
                pubKeyOutStream.writeObject(publicKey);
                priKeyOutStream.writeObject(privateKey);
            } catch (IOException e) {
                throw new EncryptException("写入密钥文件失败", e);
            }
        }

        return keyPair;
    }

    /**
     * 加密二进制内容.
     * 
     * @param content 明文byte[]
     * @param publicKey 公钥,base64密钥串或密钥文件路径
     * @return byte[]密文
     * @throws EncryptException 加解密异常
     */
    public byte[] encrypt(final byte[] content, final String publicKey) throws EncryptException {
        Key key = generateKey(publicKey, true);
        return docrypt(key, content, Cipher.ENCRYPT_MODE);
    }

    /**
     * 解密.
     * 
     * @param cipher 密文
     * @param privateKey 私钥,base64密钥串或密钥文件路径
     * @return 明文，byte数组
     * @throws EncryptException 加解密异常
     */
    public byte[] decrypt(final byte[] cipher, final String privateKey) throws EncryptException {
        Key key = generateKey(privateKey, false);
        return docrypt(key, cipher, Cipher.DECRYPT_MODE);
    }

    /**
     * 加密字符串,返回base64密文.
     * 
     * @param plainText 明文
     * @param publicKey 公钥,base64密钥串或密钥文件路径
     * @return base64密文
     * @throws EncryptException 加解密异常
     */
    public String encryptToBase64(final String plainText, final String publicKey) throws EncryptException {
        if (StringHelper.isEmpty(plainText)) {
            return plainText;
        }
        return Base64.encodeBase64String(doencrypt(plainText, publicKey));
    }

    /**
     * 加密字符串,返回16进制密文.
     * 
     * @param content 明文
     * @param publicKey 公钥,base64密钥串或密钥文件路径
     * @return 密文,16进制字符串
     * @throws EncryptException 加解密异常
     */
    public String encryptToHex(final String content, final String publicKey) throws EncryptException {
        if (StringHelper.isEmpty(content)) {
            return content;
        }
        return Hex.encodeHexString(doencrypt(content, publicKey));
    }

    /**
     * 解密base64字符串密文.
     * 
     * @param cipherBase64 密文,base64字符串
     * @param privateKey 私钥,base64密钥串或密钥文件路径
     * @return 明文
     * @throws EncryptException 加解密异常
     */
    public String dencryptBase64(final String cipherBase64, final String privateKey) throws EncryptException {
        if (StringHelper.isEmpty(cipherBase64)) {
            return cipherBase64;
        }
        byte[] cipherBytes = Base64.decodeBase64(cipherBase64);
        byte[] plainBytes = decrypt(cipherBytes, privateKey);
        try {
            return new String(plainBytes, getCharset());
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException("字符编码错误" + getCharset(), e);
        }
    }

    /**
     * 解密hex字符串密文.
     * 
     * @param cipherHex 密文,hex字符串
     * @param privateKey 私钥,base64密钥串或密钥文件路径
     * @return 明文
     * @throws EncryptException 加解密异常
     */
    public String dencryptHex(final String cipherHex, final String privateKey) throws EncryptException {
        if (StringHelper.isEmpty(cipherHex)) {
            return cipherHex;
        }

        try {
            byte[] cipherBytes = TranscodeHelper.dencodeHex(cipherHex);
            byte[] plainBytes = decrypt(cipherBytes, privateKey);
            return new String(plainBytes, getCharset());
        } catch (UnsupportedEncodingException | TranscodeException e) {
            throw new EncryptException("字符编码错误" + getCharset(), e);
        }
    }

    private byte[] doencrypt(final String content, final String privateKey) throws EncryptException {
        Key key = generateKey(privateKey, true);
        byte[] contBytes = null;
        try {
            contBytes = content.getBytes(getCharset());
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException("字符编码错误" + getCharset(), e);
        }
        return docrypt(key, contBytes, Cipher.ENCRYPT_MODE);
    }

    /**
     * 生成密钥Key
     * 
     * @param keyOrFile 字符串密钥或密钥文件路径.含路径分隔符和扩展名
     * @param isPublicKey 生成公钥还是私钥 true=公钥,keyOrFile为密钥串时生效
     * @return Key
     * @throws EncryptException
     */
    private Key generateKey(String keyOrFile, boolean isPublicKey) throws EncryptException {
        boolean fileKey = false;
        Path keyFilePath = null;
        // 无路径分隔符且无文件扩展名视为非密钥文件
        if (keyOrFile.indexOf(".") > 0) {
            try {
                keyFilePath = Paths.get(keyOrFile);
                fileKey = true;
            } catch (InvalidPathException e) {
                fileKey = false;
            }
        }
        // 文件
        if (fileKey) {
            File keyFile = keyFilePath.toFile();
            if (keyFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyFile))) {
                    return (Key) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new EncryptException("密钥生成异常", e);
                }
            }
        }
        // 文本
        try {
            byte[] keyBytes = Base64.decodeBase64(keyOrFile);
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.name());
            if (isPublicKey) {
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
                return keyFactory.generatePublic(x509KeySpec);
            } else {
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                return keyFactory.generatePrivate(keySpec);
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EncryptException("密钥生成异常" + keyOrFile, e);
        }
    }

    private byte[] docrypt(Key decryptKey, byte[] cryptBytes, int cipherMode) {
        byte[] resultBytes;
        Cipher cipher = null;
        try {
            if (StringHelper.isNotEmpty(providerName)) {
                cipher = Cipher.getInstance(getCipherStr(), providerName);
            } else {
                cipher = Cipher.getInstance(getCipherStr());// RSA/ECB/PKCS1Padding
            }
            cipher.init(cipherMode, decryptKey);
            resultBytes = cipher.doFinal(cryptBytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new EncryptException(e);
        } catch (NoSuchProviderException e) {
            throw new EncryptException("未找到名为" + providerName + "的Provider", e);
        }
        return resultBytes;
    }

    @Override
    public boolean isSupport() {
        return curAlgorithm == Algorithm.RSA;
    }

}