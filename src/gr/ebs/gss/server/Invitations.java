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
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.Invitation;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.io.IOException;
import java.net.URLEncoder;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The servlet that handles registration invitations.
 *
 * @author past
 */
public class Invitations extends Registration {
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
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String code = request.getParameter(CODE_PARAM);
		response.setContentType("text/html");

		// Validate input parameters.
		if (code == null || code.isEmpty()) {
			String error = URLEncoder.encode("No code was specified", "UTF-8");
			String errorUrl = "invites.jsp?error=" + error;
			response.sendRedirect(errorUrl);
			return;
		}

		try {
			Invitation invite = getService().findInvite(code);
			if (invite == null) {
				String error = URLEncoder.encode("The specified code was not found", "UTF-8");
				String errorUrl = "invites.jsp?code=&error=" + error;
				response.sendRedirect(errorUrl);
				return;
			}
			String firstname = invite.getFirstname() == null? "": invite.getFirstname();
			String lastname = invite.getLastname() == null? "": invite.getLastname();
			String email = invite.getEmail() == null? "": invite.getEmail();
			response.sendRedirect("register.jsp?firstname=" + firstname +
					"&lastname=" + lastname + "&email=" + email);
		} catch (RpcException e) {
			String error = "An error occurred while communicating with the service";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
		}
	}
}
