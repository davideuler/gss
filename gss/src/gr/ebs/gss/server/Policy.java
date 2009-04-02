/*
 * Copyright 2009 Electronic Business Systems Ltd.
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

import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.io.IOException;

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
 * The servlet that handles user policy acceptance.
 *
 * @author past
 */
public class Policy extends HttpServlet {
	/**
	 * The request parameter name for the acceptance flag.
	 */
	private static final String ACCEPT_PARAM = "accept";

	/**
	 * The request parameter name for the original query string to the jsp.
	 */
	private static final String QUERY_PARAM = "queryString";

	/**
	 * The request parameter name for the username.
	 */
	private static final String USER_PARAM = "user";

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(Policy.class);

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
			final Object ref = ctx.lookup("gss/ExternalAPIBean/local");
			return (ExternalAPI) PortableRemoteObject.narrow(ref, ExternalAPI.class);
		} catch (final NamingException e) {
			logger.error("Unable to retrieve the ExternalAPI EJB", e);
			throw new RpcException("An error occurred while contacting the naming service");
		}
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String queryString = request.getParameter(QUERY_PARAM);
		String accept = request.getParameter(ACCEPT_PARAM);
		String username = request.getParameter(USER_PARAM);
		response.setContentType("text/html");
		if (username == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		User user = null;
		try {
			user = getService().findUser(username);
			if (user == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			if ("on".equalsIgnoreCase(accept))
				user = getService().updateUserPolicyAcceptance(user.getId(), true);
			response.sendRedirect(request.getContextPath()+ "/login?" + queryString);
		} catch (RpcException e) {
			String error = "An error occurred while communicating with the service";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
			return;
		} catch (ObjectNotFoundException e) {
			String error = "User " + username + " was not found";
			logger.warn(error, e);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, error);
			return;
		}
	}
}
