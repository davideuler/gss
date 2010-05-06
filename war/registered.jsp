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
<link href="/pithos/gss.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>

<center>
<p>
Your account was successfully created. You may now
<a href="<%= request.getContextPath()+ "/login?next=" + GSSConfigurationFactory.getConfiguration().getString("serviceURL") %>">
login</a> to <%= GSSConfigurationFactory.getConfiguration().getString("serviceName") %>
</center>

</body>
</html>
