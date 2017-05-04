<html>
<body>
<h2>Hello World!</h2>
</body>
<a href="NewFile.jsp">xxx</a>
<a href="test.jsp">test</a>

<hr>
<div>
<form name="frm" action="<%=request.getContextPath()%>/upload" method="post" enctype="multipart/form-data">
<input name="file" type="file" value="upload"><br>
<input name="file" type="file" value="upload">
<input name="file1" type="file" value="upload">
<button type="submit">upload</button>
</form>
</div>
</html>
