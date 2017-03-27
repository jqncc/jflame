package org.jflame.toolkit.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jflame.toolkit.common.bean.NameValuePair;
import org.jflame.toolkit.net.HttpHelper;
import org.jflame.toolkit.net.HttpHelper.HttpMethod;
import org.jflame.toolkit.net.HttpResponse;
import org.jflame.toolkit.util.CharsetHelper;
import org.junit.Test;

public class HttpTest {
    @Test
    public void testGet(){
       HttpResponse result= HttpHelper.get("https://ww.baidu.com/s?wd=keyword");
       System.out.println("请求状态："+result.getStatus());
       System.out.println("返回消息描述："+result.getMessage());
       System.out.println("返回数据："+result.getData());
    }
    
    @Test
    public void testPost(){
       List<NameValuePair> pairs=new ArrayList<>();
       pairs.add(new NameValuePair("email", "jq@163.com"));
       pairs.add(new NameValuePair("password", "123456"));
       HttpResponse result= HttpHelper.post("http://www.tuicool.com/login",pairs);
       System.out.println("请求状态："+result.getStatus());
       System.out.println("返回消息描述："+result.getMessage());
       System.out.println("返回数据："+result.getData());
    }
    
    @Test
    public void testFull() {
        HttpHelper httpHelper = new HttpHelper();
        httpHelper.setCharset(CharsetHelper.GBK);
        boolean isOk = httpHelper.initConnect("http://www.tuicool.com/login", HttpMethod.POST);
        if (isOk) {
            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new NameValuePair("email", "jq@163.com"));
            pairs.add(new NameValuePair("password", "123456"));
            HttpResponse result = httpHelper.sendRequest(pairs);
            System.out.println(result);
        }else {
            System.out.println("建立连接失败，请确认请求url可用");
        }
    }
    
    @Test
    public void testUpload(){
        HttpHelper httpHelper = new HttpHelper();
        String shopAdminUrl="http://localhost:9092/activity/a/client/upload";
        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new NameValuePair("username", "jq@163.com"));
        Map<String,File> upl=new HashMap<>();
        upl.put("uploadZip", new File("D:\\电脑软件\\Notepad++\\readme.txt"));
        boolean isOk = httpHelper.initConnect(shopAdminUrl, HttpMethod.POST);
        if (isOk) {
            HttpResponse result = httpHelper.sendRequest(pairs, upl);
            System.out.println(result);
        }
    }
}
