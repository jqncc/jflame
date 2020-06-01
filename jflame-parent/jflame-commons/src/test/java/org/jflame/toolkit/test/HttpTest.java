package org.jflame.toolkit.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.junit.Test;

import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.pair.NameValuePair;
import org.jflame.commons.net.CertX509TrustManager;
import org.jflame.commons.net.HttpHelper;
import org.jflame.commons.net.HttpHelper.HttpMethod;
import org.jflame.commons.net.http.HttpResponse;
import org.jflame.commons.net.http.handler.JsonResponseHandler;
import org.jflame.commons.util.CharsetHelper;

public class HttpTest {

    @Test
    public void testGet() {
        HttpResponse result = HttpHelper.get("https://www.baidu.com/s?wd=keyword");
        System.out.println("请求状态：" + result.getStatus());
        System.out.println("返回消息描述：" + result.getMessage());
        System.out.println("返回数据：" + result.getResponseAsText());
    }

    @Test
    public void testPost() {
        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new NameValuePair("email", "jq@163.com"));
        pairs.add(new NameValuePair("password", "123456"));
        HttpHelper.post("http://www.tuicool.com/login", pairs);
    }

    /**
     * 使用json解析返回结果
     */
    @Test
    public void testJsonResponse() {
        List<NameValuePair> pairs = new ArrayList<>();
        HttpResponse result = HttpHelper.post("http://127.0.0.1:88/user/1", pairs);
        if (result.success()) {
            CallResult info = result.getResponse(new JsonResponseHandler<>(CallResult.class));
        }
        //
        result.getResponseAsJson(CallResult.class);// 结果json解析为bean
        result.getResponseAsXml(CallResult.class);// xml解析为bean
        // 构造JSON反序列复合对象list
        // TypeReference<List<MemberInfo>> type = JsonHelper.buildListType(MemberInfo.class);
        // List<MemberInfo> list = result.getResponse(new JsonResponseHandler<>(type));
    }

    /**
     * 模拟登录
     */
    @Test
    public void testFull() {
        HttpHelper httpHelper = new HttpHelper();
        httpHelper.setRequestUrl("http://localhost:9090/zp-admin/login")
                .setCharset(CharsetHelper.GBK_18030.name())
                .setMethod(HttpMethod.POST);
        // 保持cookie,必须是同一httpHelper实例
        // 登录 请求
        // Map<String,String> header=new HashMap<>();
        // header.put("accept","*/*");带header

        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new NameValuePair("user", "jq@163.com"));
        pairs.add(new NameValuePair("password", "123456"));
        HttpResponse result = httpHelper.sendRequest(pairs);
        // 返回结果,反序列化json

        System.out.println(result.getResponseAsJson(CallResult.class));// 结果json转为bean
        // httpHelper.sendJsonRequest(null, null)
        // 登录成功后请求有身份证验证的页面
        httpHelper.setRequestUrl("http://localhost:9090/zp-admin/topMenu")
                .setMethod(HttpMethod.GET);
        result = httpHelper.sendRequest();
        System.out.println(result.getResponseAsText());

    }

    /**
     * 自定义SSL证书验证
     */
    @Test
    public void testSSl() {
        HttpHelper httpHelper = new HttpHelper();
        httpHelper.setCharset(CharsetHelper.GBK_18030.name());
        SSLSocketFactory mySSLFactory;
        try {
            mySSLFactory = HttpHelper.initSSLSocketFactory("TLS",
                    new TrustManager[] { new CertX509TrustManager("d://x.p12", "passwd", "PKCS12") });
            httpHelper.setSslSocketFactory(mySSLFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件域上传
     */
    @Test
    public void testUpload() {
        HttpHelper httpHelper = new HttpHelper();
        String shopAdminUrl = "http://localhost:9092/activity/a/client/upload";
        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new NameValuePair("username", "jq@163.com"));
        Map<String,File> upl = new HashMap<>();
        upl.put("uploadZip", new File("D:\\电脑软件\\Notepad++\\readme.txt"));
        httpHelper.setRequestUrl(shopAdminUrl);
        HttpResponse result = httpHelper.sendRequest(pairs, upl);
        System.out.println(result);
    }
}
