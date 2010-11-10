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

import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.Invitation;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The servlet that handles registration invitations.
 *
 * @author past
 */
public class Invitations extends BaseServlet {
	/**
	 * The request parameter name for the invitation code.
	 */
	private static final String CODE_PARAM = "code";

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(Invitations.class);

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String code = request.getParameter(CODE_PARAM);
		response.setContentType("text/html");

		// Validate input parameters.
		if (code == null || code.isEmpty()) {
			response.sendRedirect("invites.jsp?error=" + encode("No code was specified"));
			return;
		}

		try {
			Invitation invite = getService().findInvite(code);
			if (invite == null) {
				response.sendRedirect("invites.jsp?code=&error=" + encode("The specified code was not found"));
				return;
			}
			String firstname = invite.getFirstname() == null? "": invite.getFirstname();
			String lastname = invite.getLastname() == null? "": invite.getLastname();
			String email = invite.getEmail() == null? "": invite.getEmail();
			response.sendRedirect("register.jsp?firstname=" + encode(firstname) +
					"&lastname=" + encode(lastname) + "&email=" + encode(email));
		} catch (RpcException e) {
			logger.error(e);
			handleException(response, encode("An error occurred while communicating with the service"));
		}
	}

	private void handleException(HttpServletResponse response, String error) throws IOException {
		String errorUrl = "invites.jsp?username=&firstname=&lastname=&email=&error=" + error;
		response.sendRedirect(errorUrl);
	}
}
