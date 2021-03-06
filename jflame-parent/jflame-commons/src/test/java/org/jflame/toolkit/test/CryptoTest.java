package org.jflame.toolkit.test;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import org.jflame.commons.codec.TranscodeHelper;
import org.jflame.commons.crypto.BaseEncryptor.Algorithm;
import org.jflame.commons.crypto.BaseEncryptor.OpMode;
import org.jflame.commons.crypto.BaseEncryptor.Padding;
import org.jflame.commons.crypto.RSAEncryptor;
import org.jflame.commons.crypto.SymmetricEncryptor;

public class CryptoTest {

    @Test
    public void testRsa() {
        String content = "中国字密钥谢谢合作";
        // String content = "{\"joinerId\":null,\"joinerMobile\":\"18820268067\",\"userId\":\"fch990507\","
        // + "\"eventId\":3,\"giftId\":27,\"giftQty\":1}";
        // String pkey = "d:\\public.key";
        // String priKey = "d:\\private.key";
        // RSAEncryptor rsa = new RSAEncryptor();
        // KeyPair kv=rsa.generateKeyPair(pkey, priKey);
        // String cihper = rsa.encryptBase64(content, pkey);
        // System.out.println("公钥加密:" + cihper);
        // String plain = rsa.dencryptBase64(cihper, priKey);
        // System.out.println("私钥解密:" + plain); //
        // ECB/OAEPWITHMD5ANDMGF1PADDING
        RSAEncryptor rsa1 = new RSAEncryptor(OpMode.ECB, Padding.OAEPWITHMD5ANDMGF1PADDING);
        KeyPair kv1 = rsa1.generateKeyPair(null, null);
        String pubKey1 = TranscodeHelper.encodeBase64String(kv1.getPublic()
                .getEncoded());
        String priKey1 = TranscodeHelper.encodeBase64String(kv1.getPrivate()
                .getEncoded());
        String cihper1 = rsa1.encryptToHex(content, pubKey1);
        System.out.println("公钥加密:" + cihper1);
        String plain1 = rsa1.dencryptHex(cihper1, priKey1);
        System.out.println("私钥解密:" + plain1);

    }

    @Test
    public void testRsa1() {
        String pubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBpIHQL9FCsSJ/WAWBPNHJK5Cif7QplZIlQRrREMPbewPEVYOGMmfjTb94ZnbdsnQfsWXuvNPMAN9mYjfruUSoTiZLQqyorb4rG7tuQM+xaXdvmvDULJf+NnFls5Ws6Bsn3RdhnXOPaHaPhv8O+cTS6J1uZsyx+grTci0JYVDeXQIDAQAB";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String plain = now.format(formatter)
                + "中国字中国字中国字中国字中国字中国字中国字中国字中国字yyyyMMddHHmmssMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBpIHQL9FCsSasdfsdfsdfsdfffffffffffffffffffffffff";

        RSAEncryptor encryptor = new RSAEncryptor();
        KeyPair keyPair = encryptor.generateKeyPair(1024);
        String result = encryptor.encryptToBase64(plain, keyPair.getPublic());
        System.out.println(result);

        String decr = encryptor.dencryptBase64(result, keyPair.getPrivate());
        System.out.println(decr);

        RSAEncryptor bcencryptor = new RSAEncryptor(new BouncyCastleProvider(), "BC");
        String bresult = bcencryptor.encryptToBase64(plain, pubKey);
        System.out.println(bresult);
    }

    @Test
    public void testSymmetric() {
        byte[] passwd = "0123456789123456".getBytes();
        byte[] iv = new byte[16];

        SymmetricEncryptor encryptor = new SymmetricEncryptor(Algorithm.AES);
        String a = encryptor.encrytTextToBase64("中国人字符串envi", passwd, iv);
        System.out.println("aes默认加密:" + a);// LH5ABqeVCIhuyGW1coZGxU9cG6JXdcdoILJWS0pVhoY
        String pa = encryptor.decryptBase64(a, passwd, iv);
        System.out.println("aes默认解密:" + pa);

        SymmetricEncryptor encryptor1 = new SymmetricEncryptor(Algorithm.AES, OpMode.CBC, Padding.PKCS5Padding);
        String a1 = encryptor1.encrytTextToBase64("中国人字符串envi", passwd, iv);
        System.out.println("aes cbc pkcs5加密:" + a1);// LH5ABqeVCIhuyGW1coZGxXB5Ot9mWGsdQdbQdq3G768
        String pa1 = encryptor1.decryptBase64(a1, passwd, iv);
        System.out.println("aes cbc pkcs5解密:" + pa1);

        byte[] passwd24 = "012345678901234567891234".getBytes();
        byte[] iv24 = new byte[8];

        SymmetricEncryptor encryptor2 = new SymmetricEncryptor(Algorithm.DESede, OpMode.CBC, Padding.PKCS5Padding);
        String a2 = encryptor2.encrytTextToBase64("中国人字符串envi", passwd24, iv24);
        System.out.println("3des cbc pkcs5加密:" + a2);
        String pa2 = encryptor2.decryptBase64(a2, passwd24, iv24);
        System.out.println("3des cbc pkcs5解密" + pa2);
        //
        byte[] passwd8 = "01234567".getBytes();
        byte[] iv8 = new byte[8];

        SymmetricEncryptor encryptor3 = new SymmetricEncryptor(Algorithm.DES, OpMode.CBC, Padding.ISO10126PADDING);
        String a3 = encryptor3.encrytTextToBase64("中国人字符串envi", passwd8, iv8);
        System.out.println("des cbc pkcs5加密:" + a3);
        String pa3 = encryptor3.decryptBase64(a3, passwd8, iv8);
        System.out.println("des cbc pkcs5解密" + pa3);

    }

    @Test
    public void testSymmetric2() {

        byte[] passwd = "0123456789123456".getBytes();
        byte[] iv = new byte[16];

        SymmetricEncryptor encryptor = new SymmetricEncryptor(Algorithm.AES);
        String a = encryptor.encrytTextToBase64("中国人字符串envi", passwd, null);
        System.out.println("aes默认加密:" + a);// LH5ABqeVCIhuyGW1coZGxU9cG6JXdcdoILJWS0pVhoY
        String pa = encryptor.decryptBase64(a, passwd, iv);
        System.out.println("aes默认解密:" + pa);
    }

    @Test
    public void testBCProvider() {

        byte[] passwd = "0123456789123456".getBytes();
        byte[] iv = new byte[16];

        SymmetricEncryptor encryptor = new SymmetricEncryptor(Algorithm.AES, OpMode.ECB, Padding.PKCS7Padding,
                new BouncyCastleProvider(), "BC");
        String a = encryptor.encrytTextToBase64("中国人字符串envi", passwd, null);
        System.out.println("aes默认加密:" + a);// LH5ABqeVCIhuyGW1coZGxU9cG6JXdcdoILJWS0pVhoY
        String pa = encryptor.decryptBase64(a, passwd, iv);
        System.out.println("aes默认解密:" + pa);
    }

}
