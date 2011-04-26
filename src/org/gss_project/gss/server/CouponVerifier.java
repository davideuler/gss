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
package org.gss_project.gss.server;

import org.gss_project.gss.server.domain.UserClass;
import org.gss_project.gss.server.ejb.TransactionHelper;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The servlet that handles user registration.
 *
 * @author past
 */
public class CouponVerifier extends BaseServlet {
	/**
	 * The request parameter name for the verification flag.
	 */
	private static final String VERIFY_PARAM = "verify";

	/**
	 * The request parameter name for the coupon code.
	 */
	private static final String CODE_PARAM = "code";

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
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String firstname = request.getParameter(FIRSTNAME_PARAM);
		String lastname = request.getParameter(LASTNAME_PARAM);
		String email = request.getParameter(EMAIL_PARAM);
		final String username = request.getParameter(USERNAME_PARAM);
		final String code = request.getParameter(CODE_PARAM);
		String verify = request.getParameter(VERIFY_PARAM);
		response.setContentType("text/html");

		// Validate input parameters.
		if (username == null || username.isEmpty()) {
			handleException(response, "No username was specified");
			return;
		} else if (firstname == null || firstname.isEmpty()) {
			handleException(response, "No firstname was specified");
			return;
		} else if (lastname == null || lastname.isEmpty()) {
			handleException(response, "No lastname was specified");
			return;
		} else if (email == null || email.isEmpty()) {
			handleException(response, "No email was specified");
			return;
		} else if (code == null || code.isEmpty()) {
			handleException(response, "No code was specified");
			return;
		} else if (!"on".equalsIgnoreCase(verify)) {
			String error = encode("You must verify that the coupon belongs to you");
			String errorUrl = "couponSubmission.jsp?error=" + error;
			errorUrl += "&username=" + username;
			errorUrl += "&firstname=" + firstname;
			errorUrl += "&lastname=" + lastname;
			errorUrl += "&code=" + code;
			errorUrl += "&email=" + email;
			response.sendRedirect(errorUrl);
			return;
		}

		try {
			UserClass newClass = new TransactionHelper<UserClass>().tryExecute(new Callable<UserClass>() {
				@Override
				public UserClass call() throws Exception {
					return getService().upgradeUserClass(username, code);
				}

			});
			response.sendRedirect("couponSubmitted.jsp?newQuota=" + newClass.getQuotaAsString());
		} catch (Exception e) {
			handleException(response, e.getMessage());
		}
	}

	private void handleException(HttpServletResponse response, String error) throws IOException {
		String errorUrl = "coupon.jsp?error=" + error;
		response.sendRedirect(errorUrl);
	}
}
