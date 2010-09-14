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
import gr.ebs.gss.server.ejb.TransactionHelper;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.inject.Singleton;

/**
 * The servlet that handles user registration.
 *
 * @author past
 */
@Singleton
public class Registration extends BaseServlet {
	/**
	 * The request parameter name for the acceptance flag.
	 */
	private static final String ACCEPT_PARAM = "accept";

	/**
	 * The request parameter name for the firstname.
	 */
	private static final String FIRSTNAME_PARAM = "firstname";

	/**
	 * The request parameter name for the lastname.
	 */
	private static final String LASTNAME_PARAM = "lastname";

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

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (getConfiguration().getBoolean("onlyRegisterWithCode", false))
			response.sendRedirect("invites.jsp");
		else
			response.sendRedirect("register.jsp");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String firstname = request.getParameter(FIRSTNAME_PARAM);
		final String lastname = request.getParameter(LASTNAME_PARAM);
		final String email = request.getParameter(EMAIL_PARAM);
		final String username = request.getParameter(USERNAME_PARAM);
		String password = request.getParameter(PASSWORD_PARAM);
		String password2 = request.getParameter(PASSWORD2_PARAM);
		String accept = request.getParameter(ACCEPT_PARAM);
		response.setContentType("text/html");

		// Validate input parameters.
		if (username == null || username.isEmpty()) {
			String error = encode("No username was specified");
			String errorUrl = "register.jsp?username=&error=" + error;
			errorUrl += "&firstname=" + (firstname == null? "": encode(firstname));
			errorUrl += "&lastname=" + (lastname == null? "": encode(lastname));
			errorUrl += "&email=" + (email == null? "": encode(email));
			response.sendRedirect(errorUrl);
			return;
		} else if (username.indexOf(' ') != -1) {
			String error = encode("Spaces in username are not allowed");
			String errorUrl = "register.jsp?username=&error=" + error;
			errorUrl += "&firstname=" + (firstname == null? "": encode(firstname));
			errorUrl += "&lastname=" + (lastname == null? "": encode(lastname));
			errorUrl += "&email=" + (email == null? "": encode(email));
			response.sendRedirect(errorUrl);
			return;
		} else if (firstname == null || firstname.isEmpty()) {
			String error = encode("No firstname was specified");
			String errorUrl = "register.jsp?firstname=&error=" + error;
			errorUrl += "&username=" + encode(username);
			errorUrl += "&lastname=" + (lastname == null? "": encode(lastname));
			errorUrl += "&email=" + (email == null? "": encode(email));
			response.sendRedirect(errorUrl);
			return;
		} else if (lastname == null || lastname.isEmpty()) {
			String error = encode("No lastname was specified");
			String errorUrl = "register.jsp?lastname=&error=" + error;
			errorUrl += "&username=" + encode(username);
			errorUrl += "&firstname=" + encode(firstname);
			errorUrl += "&email=" + (email == null? "": encode(email));
			response.sendRedirect(errorUrl);
			return;
		} else if (email == null || email.isEmpty()) {
			String error = encode("No e-mail was specified");
			String errorUrl = "register.jsp?email=&error=" + error;
			errorUrl += "&username=" + encode(username);
			errorUrl += "&firstname=" + encode(firstname);
			errorUrl += "&lastname=" + encode(lastname);
			response.sendRedirect(errorUrl);
			return;
		} else if (password == null || password.isEmpty()) {
			String error = encode("No password was specified");
			String errorUrl = "register.jsp?error=" + error;
			errorUrl += "&username=" + encode(username);
			errorUrl += "&firstname=" + encode(firstname);
			errorUrl += "&lastname=" + encode(lastname);
			errorUrl += "&email=" + encode(email);
			response.sendRedirect(errorUrl);
			return;
		} else if (!password.equals(password2)) {
			String error = encode("Passwords do not match");
			String errorUrl = "register.jsp?error=" + error;
			errorUrl += "&username=" + encode(username);
			errorUrl += "&firstname=" + encode(firstname);
			errorUrl += "&lastname=" + encode(lastname);
			errorUrl += "&email=" + encode(email);
			response.sendRedirect(errorUrl);
			return;
		} else if (!"on".equalsIgnoreCase(accept)) {
			String error = encode("You must accept the terms and conditions");
			String errorUrl = "register.jsp?error=" + error;
			errorUrl += "&username=" + encode(username);
			errorUrl += "&firstname=" + encode(firstname);
			errorUrl += "&lastname=" + encode(lastname);
			errorUrl += "&email=" + encode(email);
			response.sendRedirect(errorUrl);
			return;
		}

		User user = null;
		try {
			user = getService().findUser(username);
			if (user != null) {
				String error = encode("The username already exists");
				String errorUrl = "register.jsp?username=&error=" + error;
				errorUrl += "&firstname=" + encode(firstname);
				errorUrl += "&lastname=" + encode(lastname);
				errorUrl += "&email=" + encode(email);
				response.sendRedirect(errorUrl);
				return;
			}
			try {
				getService().createLdapUser(username, firstname, lastname, email, password);
			} catch (Exception e) {
				logger.error("", e);
				handleException(response, e.getMessage());
				return;
			}
			final UserDTO userDto = new TransactionHelper<UserDTO>().tryExecute(new Callable<UserDTO>() {
				@Override
				public UserDTO call() throws Exception {
					return getService().createUser(username, firstname + " " + lastname, email, "", "").getDTO();
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
			logger.error("", e);
			handleException(response, "An error occurred while communicating with the service");
		} catch (DuplicateNameException e) {
			// Can't happen, but this is more user-friendly than an assert.
			logger.error("", e);
			handleException(response, "The username already exists");
		} catch (ObjectNotFoundException e) {
			// Can't happen, but this is more user-friendly than an assert.
			logger.error("", e);
			handleException(response, "No username or name was specified");
		} catch (Exception e) {
			logger.error("", e);
			handleException(response, e.getMessage());
		}
	}

	private void handleException(HttpServletResponse response, String error) throws IOException {
		String msg = error != null ? encode(error) : "";
		String errorUrl = "register.jsp?username=&firstname=&lastname=&email=&error=" + msg;
		response.sendRedirect(errorUrl);
	}
}
