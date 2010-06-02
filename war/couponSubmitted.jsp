<%--

Copyright 2010 Electronic Business Systems Ltd.

This file is part of GSS.

GSS is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GSS is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GSS.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="gr.ebs.gss.server.configuration.GSSConfigurationFactory"%><html>
<head>
<title><%= GSSConfigurationFactory.getConfiguration().getString("serviceName") %> Authentication</title>
<link href="/pithos/main.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
<div class="wrapper" >
<div class="header"></div>
<div class="image_logo">
<table><tr>
  <td><a href="/"><img src="/pithos/images/service-logo.png"></img></a>
</tr></table>
</div> <!-- image_logo -->
<div style="clear: both; "> </div>
<div class="page_main">
<center>
<p>
Your account quota was successfully upgraded. You now have <%= request.getParameter("newQuota") %> of storage. You may now
<a href="<%= request.getContextPath()+ "/login?next=" + GSSConfigurationFactory.getConfiguration().getString("serviceURL") %>">
login</a> to <%= GSSConfigurationFactory.getConfiguration().getString("serviceName") %>
</center>
</div>
<div class="footer"></div>
</div> <!-- wrapper -->
</body>
</html>
