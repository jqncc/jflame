package org.jflame.toolkit.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.jflame.toolkit.common.bean.CallResult;
import org.jflame.toolkit.common.bean.pair.NameValuePair;
import org.jflame.toolkit.net.CertX509TrustManager;
import org.jflame.toolkit.net.HttpHelper;
import org.jflame.toolkit.net.HttpHelper.HttpMethod;
import org.jflame.toolkit.net.http.HttpResponse;
import org.jflame.toolkit.net.http.handler.JsonResponseHandler;
import org.jflame.toolkit.util.CharsetHelper;
import org.junit.Test;

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
        HttpResponse result = HttpHelper.post("http://127.0.0.1:88/user/1", null);
        if (result.success()) {
            CallResult info = result.getResponse(new JsonResponseHandler<>(CallResult.class));
        }
        //
        // result.getResponseAsJson(CallResult.class);//结果json解析为bean
        // result.getResponseAsXml(CallResult.class);xml解析为bean
        // 构造JSON反序列复合对象list
        // TypeReference<List<MemberInfo>> type=JsonHelper.buildListType(MemberInfo.class);
        // List<MemberInfo> list=result.getResponse(new JsonResponseHandler<>(type));
    }

    /**
     * 模拟登录
     */
    @Test
    public void testFull() {
        HttpHelper httpHelper = new HttpHelper();
        httpHelper.setCharset(CharsetHelper.GBK.name());
        // 保持cookie,必须是同一httpHelper实例
        // 登录 请求
        // Map<String,String> header=new HashMap<>();
        // header.put("accept","*/*");带header
        // boolean isOk = httpHelper.initConnect("http://localhost:9090/zp-admin/login", HttpMethod.POST,header);
        boolean isOk = httpHelper.initConnect("http://localhost:9090/zp-admin/login", HttpMethod.POST);
        if (isOk) {
            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new NameValuePair("user", "jq@163.com"));
            pairs.add(new NameValuePair("password", "123456"));
            HttpResponse result = httpHelper.sendRequest(pairs);
            System.out.println(result.getResponseAsJson(CallResult.class));// 结果json转为bean
            // httpHelper.sendJsonRequest(null, null)
            // 登录成功后请求有身份证验证的页面
            httpHelper.initConnect("http://localhost:9090/zp-admin/topMenu", HttpMethod.GET);
            result = httpHelper.sendRequest();
            System.out.println(result.getResponseAsText());

        } else {
            System.out.println("建立连接失败，请确认请求url可用");
        }
    }

    /**
     * 自定义SSL证书验证
     */
    @Test
    public void testSSl() {
        HttpHelper httpHelper = new HttpHelper();
        httpHelper.setCharset(CharsetHelper.GBK.name());
        SSLSocketFactory mySSLFactory;
        try {
            mySSLFactory = HttpHelper.initSSLSocketFactory("TLS",
                    new TrustManager[]{ new CertX509TrustManager("d://x.p12", "passwd", "PKCS12") });
            httpHelper.setSslSocketFactory(mySSLFactory);
            boolean isOk = httpHelper.initConnect("http://localhost:9090/zp-admin/xxx", HttpMethod.POST);
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
        boolean isOk = httpHelper.initConnect(shopAdminUrl, HttpMethod.POST);
        if (isOk) {
            HttpResponse result = httpHelper.sendRequest(pairs, upl);
            System.out.println(result);
        }
    }
}
