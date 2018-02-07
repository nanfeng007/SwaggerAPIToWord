<%-- text/html：正常的html显示  application/msword：html页面直接转word--%>
<%@ page contentType="application/msword" pageEncoding="UTF-8"
	language="java"%>
<%--<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>tool</title>
<style type="text/css">
.bg {
	background-color: rgb(84, 127, 177);
}

tr {
	height: 20px;
	font-size: 12px;
}

.specialHeight {
	height: 40px;
}
</style>
</head>
<body>
	<div style="width: 800px; margin: 0 auto">
		
			<h4>${t.title}</h4>
			<%--这个是类的说明--%>
			<h5>${t.tag}</h5>
			<%--这个是每个请求的说明，方便生成文档后进行整理--%>
			<table border="1" cellspacing="0" cellpadding="0" width="100%">
				
				<tr class="bg" align="center">
					<td colspan="4"><b>接口名称</b></td>
					<td colspan="4"><b>内部/外部接口</b></td>
				</tr>
				<c:forEach items="${table}" var="t">
					<tr align="center">
						<td colspan="4">${t.tag}</td>
						<td colspan="4"></td>
					</tr>

				</c:forEach>
			</table>
			<br>

	</div>
</body>
</html>
