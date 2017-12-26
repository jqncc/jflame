<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib uri="/jflame-ui" prefix="jf" %>
<%@taglib uri="/jflame-fun" prefix="jfn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>自定义标签示范页</title>
</head>
<body>
<p>
图片标签:
<% 
request.setAttribute("img","/2017/4/gg.jpg");
request.setAttribute("ctx",request.getContextPath());
%><br>
<jf:img src="/2017/4/gg.jpg" />
<jf:img src="/2017/4/gg.jpg" id="abc" name="imgname" width="100" height="100" alt="图片标题" /><br>
调用静态方法标签显示图片服务器路径:${jfn:baseimg()}
<img alt="" src="${ctx}${jfn:baseimg()}/2017/4/gg.jpg">
</p>
<hr>
<p>
验证码1:<jf:validcode /><br>
验证码2:<jf:validcode codeName="regcode" width="100" height="32" id="regcode" />
</p>
</body>
</html>