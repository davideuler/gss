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
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Formatter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
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
public class Login extends HttpServlet {
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
	private static final String AUTH_COOKIE = "_gss_a";

	/**
	 * The separator character for the authentication cookie.
	 */
	private static final char COOKIE_SEPARATOR = '|';

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(Login.class);

	/**
	 * A helper method that retrieves a reference to the ExternalAPI bean and
	 * stores it for future use.
	 *
	 * @return an ExternalAPI instance
	 * @throws RpcException in case an error occurs
	 */
	private ExternalAPI getService() throws RpcException {
		try {
			final Context ctx = new InitialContext();
			final Object ref = ctx.lookup(getConfiguration().getString("externalApiPath"));
			return (ExternalAPI) PortableRemoteObject.narrow(ref, ExternalAPI.class);
		} catch (final NamingException e) {
			logger.error("Unable to retrieve the ExternalAPI EJB", e);
			throw new RpcException("An error occurred while contacting the naming service");
		}
	}

	/**
	 * Return the name of the service.
	 */
	private String getServiceName() {
		return getConfiguration().getString("serviceName", "GSS");
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Fetch the next URL to display, if any.
		String nextUrl = request.getParameter(NEXT_URL_PARAM);
		// Fetch the supplied nonce, if any.
		String nonce = request.getParameter(NONCE_PARAM);
		String[] attrs = new String[] {"REMOTE_USER", "HTTP_SHIB_INETORGPERSON_DISPLAYNAME",
					"HTTP_SHIB_INETORGPERSON_GIVENNAME", "HTTP_SHIB_PERSON_COMMONNAME",
					"HTTP_SHIB_PERSON_SURNAME", "HTTP_SHIB_INETORGPERSON_MAIL",
					"HTTP_SHIB_EP_UNSCOPEDAFFILIATION"};
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
		Object userclassAttr = request.getAttribute("HTTP_SHIB_EP_UNSCOPEDAFFILIATION"); // Multi-valued
		if (usernameAttr == null) {
			String authErrorUrl = "authenticationError.jsp";
			authErrorUrl += "?name=" + (nameAttr==null? "-": nameAttr.toString());
			authErrorUrl += "&givenname=" + (givennameAttr==null? "-": givennameAttr.toString());
			authErrorUrl += "&sn=" + (snAttr==null? "-": snAttr.toString());
			authErrorUrl += "&cn=" + (cnAttr==null? "-": cnAttr.toString());
			authErrorUrl += "&mail=" + (mailAttr==null? "-": mailAttr.toString());
			authErrorUrl += "&userclass=" + (userclassAttr==null? "-": userclassAttr.toString());
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
		// XXX we are not using the user class currently
		String userclass = userclassAttr != null ? userclassAttr.toString() : "";
		if (userclass.indexOf(';') != -1)
			userclass = userclass.substring(0, userclass.indexOf(';'));
		try {
			user = getService().findUser(username);
			if (user == null)
				user = getService().createUser(username, name, mail);
			if (!user.hasAcceptedPolicy()) {
				String policyUrl = "policy.jsp";
				if (request.getQueryString() != null)
					policyUrl += "?user=" + username + "&" + request.getQueryString();
				response.sendRedirect(policyUrl);
				return;
			}
			// Update the user name and e-mail if modified.
			if (!user.getName().equals(name) || !user.getEmail().equals(mail))
				user = getService().updateUser(username, name, mail);
			if (user.getAuthToken() == null)
				user = getService().updateUserToken(user.getId());
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
		if (nextUrl != null) {
			URL next = new URL(nextUrl);
			String domain = next.getHost();
			String path = next.getPath();
			Cookie cookie = new Cookie(AUTH_COOKIE, userEncoded + COOKIE_SEPARATOR +
						tokenEncoded);
			cookie.setMaxAge(-1);
			cookie.setDomain(domain);
			cookie.setPath(path);
		    response.addCookie(cookie);
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

	/**
	 * Decode the request attribute provided by the container to a UTF-8
	 * string, since GSS assumes all data to be encoded in UTF-8. The
	 * servlet container's encoding can be specified in gss.properties.
	 */
	private String decodeAttribute(Object attribute) throws UnsupportedEncodingException {
		return new String(attribute.toString().getBytes(getConfiguration().getString("requestAttributeEncoding")), "UTF-8");
	}

	/**
	 * A helper method that converts a byte buffer to a printable list of
	 * hexadecimal numbers.
	 */
	private String getHexString(byte[] buffer) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		for (int i=0; i<buffer.length; i++)
			formatter.format("0x%x, ", buffer[i]);
		return sb.toString();
	}

}
