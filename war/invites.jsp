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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="gr.ebs.gss.server.configuration.GSSConfigurationFactory"%>
<%@page import="java.net.URLDecoder"%>
<html>
<head>
<title><%= GSSConfigurationFactory.getConfiguration().getString("serviceName") %> Registration</title>
<link href="/pithos/gss.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>

Welcome to <%= GSSConfigurationFactory.getConfiguration().getString("serviceName") %>.
<%= GSSConfigurationFactory.getConfiguration().getString("invitesIntro") %>
<center>
<form method="post" action="/pithos/invites">
<%= request.getParameter("error") != null? URLDecoder.decode(request.getParameter("error"), "UTF-8"): "" %>
    <table>
        <tr>
            <td>Code:</td>
            <td><input type="text" name="code" title="Enter your code"/></td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input type="submit" value="Submit" />
            </td>
        </tr>
    </table>
</form>
</center>

</body>
</html>
