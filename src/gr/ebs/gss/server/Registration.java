/*
 * Copyright 2010 Electronic Business Systems Ltd.
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
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.dto.UserDTO;
import gr.ebs.gss.server.ejb.ExternalAPI;
import gr.ebs.gss.server.ejb.TransactionHelper;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The servlet that handles user registration.
 *
 * @author past
 */
public class Registration extends HttpServlet {
	/**
	 * The request parameter name for the acceptance flag.
	 */
	private static final String ACCEPT_PARAM = "accept";

	/**
	 * The request parameter name for the name.
	 */
	private static final String NAME_PARAM = "name";

	/**
	 * The request parameter name for the username.
	 */
	private static final String USERNAME_PARAM = "username";

	/**
	 * The request parameter name for the e-mail.
	 */
	private static final String EMAIL_PARAM = "email";

	/**
	 * The request parameter name for the password.
	 */
	private static final String PASSWORD_PARAM = "password";

	/**
	 * The request parameter name for the password confirmation.
	 */
	private static final String PASSWORD2_PARAM = "password2";

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(Registration.class);

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

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (getConfiguration().getBoolean("onlyRegisterWithCode"))
			response.sendRedirect("invites.jsp");
		else
			response.sendRedirect("register.jsp");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String name = request.getParameter(NAME_PARAM);
		final String email = request.getParameter(EMAIL_PARAM);
		final String username = request.getParameter(USERNAME_PARAM);
		String password = request.getParameter(PASSWORD_PARAM);
		String password2 = request.getParameter(PASSWORD2_PARAM);
		String accept = request.getParameter(ACCEPT_PARAM);
		response.setContentType("text/html");

		// Validate input parameters.
		if (username == null || username.isEmpty()) {
			String error = URLEncoder.encode("No username was specified", "UTF-8");
			String errorUrl = "register.jsp?username=&error=" + error;
			errorUrl += "&name=" + (name == null? "": name);
			errorUrl += "&email=" + (email == null? "": email);
			response.sendRedirect(errorUrl);
			return;
		} else if (name == null || name.isEmpty()) {
			String error = URLEncoder.encode("No name was specified", "UTF-8");
			String errorUrl = "register.jsp?name=&error=" + error;
			errorUrl += "&username=" + username;
			errorUrl += "&email=" + (email == null? "": email);
			response.sendRedirect(errorUrl);
			return;
		} else if (email == null || email.isEmpty()) {
			String error = URLEncoder.encode("No e-mail was specified", "UTF-8");
			String errorUrl = "register.jsp?email=&error=" + error;
			errorUrl += "&username=" + username;
			errorUrl += "&name=" + name;
			response.sendRedirect(errorUrl);
			return;
		} else if (password == null || password.isEmpty()) {
			String error = URLEncoder.encode("No password was specified", "UTF-8");
			String errorUrl = "register.jsp?error=" + error;
			errorUrl += "&username=" + username;
			errorUrl += "&name=" + name;
			errorUrl += "&email=" + email;
			response.sendRedirect(errorUrl);
			return;
		} else if (!password.equals(password2)) {
			String error = URLEncoder.encode("Passwords do not match", "UTF-8");
			String errorUrl = "register.jsp?error=" + error;
			errorUrl += "&username=" + username;
			errorUrl += "&name=" + name;
			errorUrl += "&email=" + email;
			response.sendRedirect(errorUrl);
			return;
		} else if (!"on".equalsIgnoreCase(accept)) {
			String error = URLEncoder.encode("You must accept the terms and conditions", "UTF-8");
			String errorUrl = "register.jsp?error=" + error;
			errorUrl += "&username=" + username;
			errorUrl += "&name=" + name;
			errorUrl += "&email=" + email;
			response.sendRedirect(errorUrl);
			return;
		}

		User user = null;
		try {
			user = getService().findUser(username);
			if (user != null) {
				String error = URLEncoder.encode("The username already exists", "UTF-8");
				String errorUrl = "register.jsp?username=&error=" + error;
				errorUrl += "&name=" + name;
				errorUrl += "&email=" + email;
				response.sendRedirect(errorUrl);
				return;
			}
			try {
				getService().createLdapUser(username, name, email, password);
			} catch (Exception exc) {
				String error = "An error occurred while communicating with the Shibboleth IdP";
				logger.error(error, exc);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
				return;
			}
			final UserDTO userDto = new TransactionHelper<UserDTO>().tryExecute(new Callable<UserDTO>() {
				@Override
				public UserDTO call() throws Exception {
					return getService().createUser(username, name, email, "", "").getDTO();
				}

			});
			new TransactionHelper<Void>().tryExecute(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					getService().updateUserPolicyAcceptance(userDto.getId(), true);
					return null;
				}

			});
			response.sendRedirect("registered.jsp");
		} catch (RpcException e) {
			String error = "An error occurred while communicating with the service";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
		} catch (DuplicateNameException e) {
			// Can't happen, but this is more user-friendly than an assert.
			String error = URLEncoder.encode("The username already exists", "UTF-8");
			String errorUrl = "register.jsp?username=&name=&email=&error=" + error;
			response.sendRedirect(errorUrl);
		} catch (ObjectNotFoundException e) {
			// Can't happen, but this is more user-friendly than an assert.
			String error = URLEncoder.encode("No username or name was specified", "UTF-8");
			String errorUrl = "register.jsp?username=&name=&email=&error=" + error;
			response.sendRedirect(errorUrl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
