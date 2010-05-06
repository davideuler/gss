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
You may sign up for the service by filling and submitting the following form. All fields are required.
<center>
<form method="post" action="/pithos/register">
<%= request.getParameter("error") != null? URLDecoder.decode(request.getParameter("error"), "UTF-8"): "" %>
    <table>
        <tr>
            <td>Firstname:</td>
            <td><input type="text" name="firstname" title="Enter a firstname" value="<%= request.getParameter("firstname") != null? URLDecoder.decode(request.getParameter("firstname"), "UTF-8"): ""  %>"/></td>
        </tr>
        <tr>
            <td>Lastname:</td>
            <td><input type="text" name="lastname" title="Enter a lastname" value="<%= request.getParameter("lastname") != null? URLDecoder.decode(request.getParameter("lastname"), "UTF-8"): ""  %>"/></td>
        </tr>
        <tr>
            <td>E-Mail:</td>
            <td><input type="text" name="email" title="Enter an e-mail address" value="<%= request.getParameter("email") != null? URLDecoder.decode(request.getParameter("email"), "UTF-8"): "" %>"/></td>
        </tr>
        <tr>
            <td>Username:</td>
            <td><input type="text" name="username" title="Enter a username" value="<%= request.getParameter("username") != null? URLDecoder.decode(request.getParameter("username"), "UTF-8"): "" %>"/></td>
        </tr>
        <tr>
            <td>Password:</td>
            <td><input type="password" name="password" title="Enter a password"/></td>
        </tr>
        <tr>
            <td>Confirm Password:</td>
            <td><input type="password" name="password2" title="Enter the password again to confirm"/></td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="checkbox" name="accept" title="Accept terms and conditions"/>
                I have read and understood the <a href='/terms' target='_blank'>
                Terms and Conditions</a> of the Service and agree to abide by them
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input type="submit" value="Sign Up" />
            </td>
        </tr>
    </table>
</form>
</center>

</body>
</html>
