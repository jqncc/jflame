package org.jflame.toolkit.test;

import org.jflame.toolkit.crypto.BaseEncryptor.Algorithm;
import org.jflame.toolkit.crypto.DigestHelper;
import org.jflame.toolkit.crypto.SymmetricEncryptor;
import org.jflame.toolkit.util.CharsetHelper;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class DigestTest {

    @Test
    public void test() {
        System.out.println(DigestHelper.md5Hex("中国人"));
    }

    @Test
    public void test2() {
        String t = "{\"appId\":\"wx71ca2f344969081a\",\"callbackUrl\":\"http://mt.zpcx.cn/pay-demo/pay/orders\",\"clientIp\":\"113.102.165.125\",\"hmac\":\"UDCP5FVDYk1hqJZJZWXnyrpLXavWFZ1ySqyQOk4+udgOJdX/HQ0mf8BX5dqvZ5WGGV3201tSac9N2gSi4QjqcJluNO9vYjcBVI2ljagwnwLL+jRCzWAZqMzRKINrNXU7ro1OsuftsGqyP0nziSXyVCvye4mzFT8uKbYwrzkFi6RLKyG3v6LsoN7EUEs9WUtMtwbzakdp7d0r8yxeoxR6+qna74YBaFhWQHb8Xg66BmPZJmUIb/pTpj2QcOZEenhmZcNUt5H9tRF+KTWFEHagnTN4x75hRTDEzstfpRX+uOJG1tWLFgRBOuVnIZkQizwK+McvH/ltp2fHunEynRZinQ==\",\"merchantId\":\"890000593\",\"notifyUrl\":\"http://mt.zpcx.cn/payer/result/zpgo/payease_pay/wechat_mp_pay\",\"openId\":\"oWOdtwpb9JQZEgqSqWSDyZJdqI6I\",\"orderAmount\":\"50\",\"orderCurrency\":\"CNY\",\"payer\":{},\"paymentModeCode\":\"WECHAT-OFFICIAL_PAY-P2P\",\"productDetails\":[{\"amount\":10,\"name\":\"萝卜\",\"quantity\":1},{\"amount\":30,\"name\":\"菊花\",\"quantity\":2},{\"amount\":10,\"name\":\"神水\",\"quantity\":1}],\"requestId\":\"P0319061415515000589\",\"splitMark\":\"DO_SPLIT\",\"splitRecords\":[{\"merchantInfo\":\"890000593\",\"merchantMark\":\"MERCHANT_ID\",\"splitAmount\":45}],\"splitRule\":\"FIXED_AMOUNT\",\"timeout\":\"120\"}";
        String randomKey = RandomStringUtils.randomAlphabetic(16);
        String aes = AESUtils.encryptToBase64(t, randomKey);
        System.out.println(aes);
        SymmetricEncryptor aesutil = new SymmetricEncryptor(Algorithm.AES);
        aesutil.setEnableBase64UrlSafe(false);
        String aes1 = aesutil.encrytTextToBase64(t, CharsetHelper.getUtf8Bytes(randomKey), null);
        System.out.println(aes1);
        System.out.println(AESUtils.decryptFromBase64(aes1, randomKey));
    }
}
