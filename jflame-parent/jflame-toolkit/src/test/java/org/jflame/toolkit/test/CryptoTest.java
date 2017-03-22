package org.jflame.toolkit.test;

import org.junit.Test;

import org.jflame.toolkit.crypto.RSAEncryption;

public class CryptoTest {

    @Test
    public void testRsa() {
        // String content = "中国字密钥谢谢合作";
        String content = "{\"joinerId\":null,\"joinerMobile\":\"18820268067\",\"userId\":\"fch990507\","
                + "\"eventId\":3,\"giftId\":27,\"giftQty\":1}";
        String pkey = "d:\\public.key";
        String priKey = "d:\\private.key";
        RSAEncryption rsa = new RSAEncryption();
        rsa.generateKeyPair(pkey, priKey);
        String cihper = rsa.encryptBase64(content, pkey);
        System.out.println("公钥加密:" + cihper);
        String plain = rsa.dencryptBase64(cihper, priKey);
        System.out.println("私钥解密:" + plain); //
        System.out.println("解密成功:");
    }

    
    @Test
    public void testSymmetric(){
        /*
         * SymmetricEncryption encryption = new SymmetricEncryption(ALGORITHM.AES); String cSrc =
         * "78450520e6785f6aeacb275e91e145be4693173cc8db60f5f85a6549914e9d5c978e020fe96a514a6b5e5c89df945ec7";
         * String aespwd = "Bat2@KEN+~-=1230"; String despwd = "Bat2@KEN"; String desedepwd =
         * "Bat2@KEN+~-=1230Bat2@KEN"; String aes_key2 = "Bat2@KEN+~-=4560";
         * encryption.setCurEncryptMode(ENCRYPT_MODE.CBC);
         * encryption.setCurPaddingMode(PADDING_MODE.PKCS7Padding); String desecd_p5_plain =
         * encryption.dencryptHEX(cSrc, aes_key2, "1312093828304813"); System.out.println(
         * "des ecd pad5 base64解密结果:" + desecd_p5_plain);
         */
        /*
         * // des String desecb_p5 = encryption.encrytBase64(cSrc, despwd, null);
         * System.out.println("des ecd pad5 base64加密结果:" + desecb_p5); String desecd_p5_plain =
         * encryption.dencryptBase64(desecb_p5, despwd, null); System.out.println(
         * "des ecd pad5 base64解密结果:" + desecd_p5_plain);
         * encryption.setCurEncryptMode(ENCRYPT_MODE.CBC); String descbc_p5 =
         * encryption.encrytBase64(cSrc, despwd, null); System.out.println(
         * "des cbc pad5 base64加密结果:" + descbc_p5); String descbc_p5_plain =
         * encryption.dencryptBase64(descbc_p5, despwd, null); System.out.println(
         * "des cbc pad5 base64解密结果:" + descbc_p5_plain); // aes
         * encryption.setCurAlgorithm(ALGORITHM.AES);
         * encryption.setCurEncryptMode(ENCRYPT_MODE.CBC);
         * encryption.setCurPaddingMode(PADDING_MODE.PKCS7Padding); String aesecb_p5 =
         * encryption.encrytBase64(cSrc, aespwd, "1312093828304813"); System.out.println(
         * "aes ecd pad7 base64加密结果:" + aesecb_p5); String aesecd_p5_plain =
         * encryption.dencryptBase64(aesecb_p5, aespwd, "1312093828304813"); System.out.println(
         * "aes ecd pad7 base64解密结果:" + aesecd_p5_plain);
         * encryption.setCurEncryptMode(ENCRYPT_MODE.CBC); String aescbc_p5 =
         * encryption.encrytBase64(cSrc, aespwd, aespwd); System.out.println(
         * "aes cbc pad5 base64加密结果:" + aescbc_p5); String aescbc_p5_plain =
         * encryption.dencryptBase64(aescbc_p5, aespwd, aespwd); System.out.println(
         * "aes cbc pad5 base64解密结果:" + aescbc_p5_plain);
         * encryption.setCurPaddingMode(PADDING_MODE.PKCS7Padding); String aescbc_p7 =
         * encryption.encrytBase64(cSrc, aespwd, aespwd); System.out.println(
         * "aes cbc pad7 base64加密结果:" + aescbc_p7); // 3des
         * encryption.setCurAlgorithm(ALGORITHM.DESede);
         * encryption.setCurEncryptMode(ENCRYPT_MODE.ECB);
         * encryption.setCurPaddingMode(PADDING_MODE.PKCS5Padding); String desedecbc_p5 =
         * encryption.encrytBase64(cSrc, desedepwd, desedepwd); System.out.println(
         * "3des ecb pad5 base64加密结果:" + desedecbc_p5); String desedecbc_p5_plain =
         * encryption.dencryptBase64(desedecbc_p5, desedepwd, desedepwd); System.out.println(
         * "3des ecb pad5 base64解密结果:" + desedecbc_p5_plain);
         */
    }
}
