/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.ebs.gss.server;

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.Nonce;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.UserLogin;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The servlet that handles user logins.
 *
 * @author past
 */
public class Login extends BaseServlet {
	/**
	 * The request parameter name for the nonce.
	 */
	private static final String NONCE_PARAM = "nonce";

	/**
	 * The request parameter name for the URL to redirect
	 * to after authentication.
	 */
	private static final String NEXT_URL_PARAM = "next";

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The name of the authentication cookie.
	 */
	public static final String AUTH_COOKIE = "_gss_a";

	/**
	 * The separator character for the authentication cookie.
	 */
	public static final char COOKIE_SEPARATOR = '|';

	/**
	 * The name of the the webdav cookie.
	 */
	public static final String WEBDAV_COOKIE = "_gss_wd";

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(Login.class);

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Fetch the next URL to display, if any.
		String nextUrl = request.getParameter(NEXT_URL_PARAM);
		// Fetch the supplied nonce, if any.
		String nonce = request.getParameter(NONCE_PARAM);
		String[] attrs = new String[] {"REMOTE_USER", "HTTP_SHIB_INETORGPERSON_DISPLAYNAME",
					"HTTP_SHIB_INETORGPERSON_GIVENNAME", "HTTP_SHIB_PERSON_COMMONNAME",
					"HTTP_SHIB_PERSON_SURNAME", "HTTP_SHIB_INETORGPERSON_MAIL",
					"HTTP_SHIB_EP_UNSCOPEDAFFILIATION", "HTTP_PERSISTENT_ID"};
		StringBuilder buf = new StringBuilder("Shibboleth Attributes\n");
		for (String attr: attrs)
			buf.append(attr+": ").append(request.getAttribute(attr)).append('\n');
		logger.info(buf);
		if (logger.isDebugEnabled()) {
			buf = new StringBuilder("Shibboleth Attributes as bytes\n");
			for (String attr: attrs)
				if (request.getAttribute(attr) != null)
					buf.append(attr+": ").append(getHexString(request.getAttribute(attr).toString().getBytes("UTF-8"))).append('\n');
			logger.debug(buf);
		}
		User user = null;
		response.setContentType("text/html");
		Object usernameAttr = request.getAttribute("REMOTE_USER");
		Object nameAttr = request.getAttribute("HTTP_SHIB_INETORGPERSON_DISPLAYNAME");
		Object givennameAttr = request.getAttribute("HTTP_SHIB_INETORGPERSON_GIVENNAME"); // Multi-valued
		Object cnAttr = request.getAttribute("HTTP_SHIB_PERSON_COMMONNAME"); // Multi-valued
		Object snAttr = request.getAttribute("HTTP_SHIB_PERSON_SURNAME"); // Multi-valued
		Object mailAttr = request.getAttribute("HTTP_SHIB_INETORGPERSON_MAIL"); // Multi-valued
		Object persistentIdAttr = request.getAttribute("HTTP_PERSISTENT_ID");
		// Use a configured test username if found, as a shortcut for development deployments.
		String gwtServer = null;
		if (getConfiguration().getString("testUsername") != null) {
			usernameAttr = getConfiguration().getString("testUsername");
			// Fetch the GWT code server URL, if any.
			gwtServer = request.getParameter(GWT_SERVER_PARAM);
		}
		if (usernameAttr == null) {
			String authErrorUrl = "authenticationError.jsp";
			authErrorUrl += "?name=" + (nameAttr==null? "-": nameAttr.toString());
			authErrorUrl += "&givenname=" + (givennameAttr==null? "-": givennameAttr.toString());
			authErrorUrl += "&sn=" + (snAttr==null? "-": snAttr.toString());
			authErrorUrl += "&cn=" + (cnAttr==null? "-": cnAttr.toString());
			authErrorUrl += "&mail=" + (mailAttr==null? "-": mailAttr.toString());
			response.sendRedirect(authErrorUrl);
			return;
		}
		String username = decodeAttribute(usernameAttr);
		String name;
		if (nameAttr != null && !nameAttr.toString().isEmpty())
			name = decodeAttribute(nameAttr);
		else if (cnAttr != null && !cnAttr.toString().isEmpty()) {
			name = decodeAttribute(cnAttr);
			if (name.indexOf(';') != -1)
				name = name.substring(0, name.indexOf(';'));
		} else if (givennameAttr != null && snAttr != null && !givennameAttr.toString().isEmpty() && !snAttr.toString().isEmpty()) {
			String givenname = decodeAttribute(givennameAttr);
			if (givenname.indexOf(';') != -1)
				givenname = givenname.substring(0, givenname.indexOf(';'));
			String sn = decodeAttribute(snAttr);
			if (sn.indexOf(';') != -1)
				sn = sn.substring(0, sn.indexOf(';'));
			name = givenname + ' ' + sn;
		} else if (givennameAttr == null && snAttr != null && !snAttr.toString().isEmpty()) {
			name = decodeAttribute(snAttr);
			if (name.indexOf(';') != -1)
				name = name.substring(0, name.indexOf(';'));
		} else
			name = username;
		String mail = mailAttr != null ? mailAttr.toString() : username;
		if (mail.indexOf(';') != -1)
			mail = mail.substring(0, mail.indexOf(';'));
		String persistentId = persistentIdAttr != null ? persistentIdAttr.toString() : "";
		String idp = "";
		String idpid = "";
		if (!persistentId.isEmpty()) {
			int bang = persistentId.indexOf('!');
			if (bang > -1) {
				idp = persistentId.substring(0, bang);
				idpid = persistentId.substring(bang + 1);
			}
		}
		try {
			user = getService().findUser(username);
			if (user == null)
				user = getService().createUser(username, name, mail, idp, idpid);
			if (!user.isActive()) {
				logger.info("Disabled user " + username + " tried to login.");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "This account is disabled");
				return;
			}
			if (!user.hasAcceptedPolicy()) {
				String policyUrl = "policy.jsp";
				if (request.getQueryString() != null)
					policyUrl += "?user=" + username + "&" + request.getQueryString();
				response.sendRedirect(policyUrl);
				return;
			}
			user.setName(name);
			user.setEmail(mail);
			user.setIdentityProvider(idp);
			user.setIdentityProviderId(idpid);
			
			UserLogin userLogin = new UserLogin();
			userLogin.setLoginDate(new Date());
			userLogin.setUser(user);
			if (user.getAuthToken() == null)
				user = getService().updateUserToken(user.getId());
			// Set WebDAV password to token if it's never been set.
			if (user.getWebDAVPassword() == null || user.getWebDAVPassword().length() == 0) {
				String tokenEncoded = new String(Base64.encodeBase64(user.getAuthToken()), "US-ASCII");
				user.setWebDAVPassword(tokenEncoded);
			}
			// Set the default user class if none was set.
			if (user.getUserClass() == null)
				user.setUserClass(getService().getUserClasses().get(0));			
			getService().updateUser(user);
			getService().addUserLogin(userLogin);		
		} catch (RpcException e) {
			String error = "An error occurred while communicating with the service";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
			return;
		} catch (DuplicateNameException e) {
			String error = "User with username " + username + " already exists";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
			return;
		} catch (ObjectNotFoundException e) {
			String error = "No username was provided";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
			return;
		}
		String tokenEncoded = new String(Base64.encodeBase64(user.getAuthToken()), "US-ASCII");
		String userEncoded = URLEncoder.encode(user.getUsername(), "US-ASCII");
		if (logger.isDebugEnabled())
			logger.debug("user: "+userEncoded+" token: "+tokenEncoded);
		if (nextUrl != null && !nextUrl.isEmpty()) {
			URI next;
			if (gwtServer != null)
				nextUrl += '?' + GWT_SERVER_PARAM + '=' + gwtServer;
			try {
				next = new URI(nextUrl);
			} catch (URISyntaxException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
				return;
			}
			if ("x-gr-ebs-igss".equalsIgnoreCase(next.getScheme()))
				nextUrl += "?u=" + userEncoded + "&t=" + tokenEncoded;
			else {
				String domain = next.getHost();
				String path = getServletContext().getContextPath() + '/';
				Cookie cookie = new Cookie(AUTH_COOKIE, userEncoded + COOKIE_SEPARATOR +
							tokenEncoded);
				cookie.setMaxAge(-1);
				cookie.setDomain(domain);
				cookie.setPath(path);
			    response.addCookie(cookie);
			    cookie = new Cookie(WEBDAV_COOKIE, user.getWebDAVPassword());
				cookie.setMaxAge(-1);
				cookie.setDomain(domain);
				cookie.setPath(path);
				response.addCookie(cookie);
			}
		    response.sendRedirect(nextUrl);
		} else if (nonce != null) {
			nonce = URLEncoder.encode(nonce, "US-ASCII");
			Nonce n = null;
			try {
				if (logger.isDebugEnabled())
					logger.debug("user: "+user.getId()+" nonce: "+nonce);
				n = getService().getNonce(nonce, user.getId());
			} catch (ObjectNotFoundException e) {
			    PrintWriter out = response.getWriter();
			    out.println("<HTML>");
			    out.println("<HEAD><TITLE>" + getServiceName() + " Authentication</TITLE>" +
			    		"<LINK TYPE='text/css' REL='stylesheet' HREF='gss.css'></HEAD>");
			    out.println("<BODY><CENTER><P>");
			    out.println("The supplied nonce could not be found!");
			    out.println("</CENTER></BODY></HTML>");
			    return;
			} catch (RpcException e) {
				String error = "An error occurred while communicating with the service";
				logger.error(error, e);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
				return;
			}
			try {
				getService().activateUserNonce(user.getId(), nonce, n.getNonceExpiryDate());
			} catch (ObjectNotFoundException e) {
				String error = "Unable to find user";
				logger.error(error, e);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
				return;
			} catch (RpcException e) {
				String error = "An error occurred while communicating with the service";
				logger.error(error, e);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
				return;
			}
			try {
				getService().removeNonce(n.getId());
			} catch (ObjectNotFoundException e) {
				logger.info("Nonce already removed!", e);
			} catch (RpcException e) {
				logger.warn("Could not remove nonce from data store", e);
			}
		    PrintWriter out = response.getWriter();
		    out.println("<HTML>");
		    out.println("<HEAD><TITLE>" + getServiceName() + " Authentication</TITLE>" +
		    		"<LINK TYPE='text/css' REL='stylesheet' HREF='gss.css'></HEAD>");
		    out.println("<BODY><CENTER><P>");
		    out.println("You can now close this browser window and return to your application.");
		    out.println("</CENTER></BODY></HTML>");
		} else {
		    PrintWriter out = response.getWriter();
		    out.println("<HTML>");
		    out.println("<HEAD><TITLE>" + getServiceName() + " Authentication</TITLE>" +
		    		"<LINK TYPE='text/css' REL='stylesheet' HREF='gss.css'></HEAD>");
		    out.println("<BODY><CENTER><P>");
		    out.println("Name: " + user.getName() + "<BR>");
		    out.println("E-mail: " + user.getEmail() + "<BR><P>");
		    out.println("Username: " + user.getUsername() + "<BR>");
		    out.println("Athentication token: " + tokenEncoded + "<BR>");
		    out.println("</CENTER></BODY></HTML>");
		}
	}
}
