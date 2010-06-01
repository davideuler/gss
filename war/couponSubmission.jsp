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
<title><%= GSSConfigurationFactory.getConfiguration().getString("serviceName") %> Coupons</title>
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
<p class="blurb">The supplied coupon code is available for the user with the
data below. Please review the following information before clicking 'submit'.
Unauthorized use of coupons by other users is not permitted.</p>
<form method="post" action="/pithos/submitCoupon">
<div class="error">
    <%= request.getParameter("error") != null? URLDecoder.decode(request.getParameter("error"), "UTF-8"): "" %>
</div>
<input type="hidden" name="code" value="<%= request.getParameter("code") %>"/>
<input type="hidden" name="username" value="<%= request.getParameter("username") %>"/>
<input type="hidden" name="firstname" value="<%= request.getParameter("firstname") %>"/>
<input type="hidden" name="lastname" value="<%= request.getParameter("lastname") %>"/>
<input type="hidden" name="email" value="<%= request.getParameter("email") %>"/>
    <table>
        <tr>
            <td>Firstname:</td>
            <td><%= request.getParameter("firstname") != null? URLDecoder.decode(request.getParameter("firstname"), "UTF-8"): ""  %></td>
        </tr>
        <tr>
            <td>Lastname:</td>
            <td><%= request.getParameter("lastname") != null? URLDecoder.decode(request.getParameter("lastname"), "UTF-8"): ""  %></td>
        </tr>
        <tr>
            <td>E-Mail:</td>
            <td><%= request.getParameter("email") != null? URLDecoder.decode(request.getParameter("email"), "UTF-8"): "" %></td>
        </tr>
        <tr>
            <td>Coupon:</td>
            <td><%= request.getParameter("code") != null? URLDecoder.decode(request.getParameter("code"), "UTF-8"): "" %></td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="checkbox" name="verify" title="Verify coupon ownership"/>
                I have read and verified my information above and affirm that this coupon was issued to me
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input type="submit" value="Submit" />
            </td>
        </tr>
    </table>
</form>
</center>
</div>
<div class="footer"></div>
</div> <!-- wrapper -->
</body>
</html>
