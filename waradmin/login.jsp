<?xml version="1.0" encoding="UTF-8"?>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="gr.ebs.gss.server.configuration.GSSConfigurationFactory"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><%= GSSConfigurationFactory.getConfiguration().getString("serviceName") %> Administration</title>
</head>
<body bgcolor="#FFFFFF" text="#000000" link="#0066CC" vlink="#0966C4"
	alink="#0099CC"
	onload="document.forms[0].elements['j_username'].focus();">
<table width="100%" border="0" cellpadding="0"
	cellspacing="0">
	<tr>
		<td height="100%" colspan="2" align="center" valign="middle"><br />
		<table width="90%" border="0" cellpadding="0"
			cellspacing="0">
			<tr>
				<td width="100%" height="100%" align="center" valign="middle">
				<p>&nbsp;</p>
				<form method="post" action="/admin/j_security_check">
				<table width="300" border="0" cellspacing="0" cellpadding="0" class="jspTable">
					<tr>
						<td height="35" align="center" valign="middle" class="jspHeader"><%= GSSConfigurationFactory.getConfiguration().getString("serviceName") %> Administration</td>
					</tr>
					<tr>
						<td>
						<table width="100%" border="0" cellpadding="8" cellspacing="0">
							<tr>
								<td width="40%" align="right" valign="middle" class="jspText">Username</td>
								<td width="180" align="left" valign="top" class="jspText"><input
									name="j_username" type="text" class="jspField" size="20" /></td>
							</tr>
							<tr>

								<td align="right" valign="middle" class="jspText">Password</td>
								<td align="left" valign="top" class="jspText"><input
									name="j_password" type="password" class="jspField" size="20" /></td>
							</tr>
							<tr>

								<td colspan="2" align="center" valign="middle" class="jspText">
								<button type="submit" class="jspButton" name="button" value="login">Log into the system</button>
								</td>
							</tr>
						</table>
						</td>
					</tr>
				</table>
				</form>
				<p class="jspComment">Enter the username/password that you use on <%= GSSConfigurationFactory.getConfiguration().getString("serviceName") %></p>
				</td>
			</tr>
		</table>
		<p>&nbsp;</p>
		<p>&nbsp;</p>
		<p>&nbsp;</p>
		</td>
	</tr>
</table>
</body>
</html>