package org.jflame.commons.net;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 使用特定证书文件实现SSL通信验证管理器
 * 
 * @author yucan.zhang
 */
public class CertX509TrustManager implements X509TrustManager {

    private X509TrustManager trustManager;

    /**
     * 构造函数
     * 
     * @param certFilePath 证书全路径
     * @param password 证书密码
     * @param keyStoreType 证书类型pkcs12/jks/jceks
     * @throws Exception
     */
    public CertX509TrustManager(String certFilePath, String password, String keyStoreType) throws Exception {
        KeyStore ks = KeyStore.getInstance(keyStoreType);
        try (FileInputStream keyStream = new FileInputStream(certFilePath)) {
            ks.load(keyStream, password.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
        tmf.init(ks);
        TrustManager[] tms = tmf.getTrustManagers();
        for (TrustManager tm : tms) {
            if (tm instanceof X509TrustManager) {
                trustManager = (X509TrustManager) tm;
                return;
            }
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        trustManager.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        trustManager.checkServerTrusted(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return trustManager.getAcceptedIssuers();
    }

}
