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

import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The servlet that handles authentication token retrieval.
 *
 * @author past
 */
public class TokenRetriever extends HttpServlet {
	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The request parameter name for the nonce.
	 */
	private static final String NONCE_PARAM = "nonce";

	/**
	 * The request parameter name for the user.
	 */
	private static final String USER_PARAM = "user";

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(TokenRetriever.class);

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
		String username = request.getParameter(USER_PARAM);
		String nonceEncoded = request.getParameter(NONCE_PARAM);
		User user = null;
		if (username == null) {
			String error = "No username supplied";
			logger.info(error);
			response.setContentType("text/html");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, error);
			return;
		}
		if (nonceEncoded == null) {
			String error = "No nonce supplied";
			logger.info(error);
			response.setContentType("text/html");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, error);
			return;
		}
		try {
			user = getService().findUser(username);
			if (user == null) {
				String error = "User was not found";
				logger.info(error);
				response.setContentType("text/html");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, error);
				return;
			}
			String nonce = user.getNonce();
			if (nonce == null || !nonce.equals(nonceEncoded))
				throw new ObjectNotFoundException("No match found");
		} catch (RpcException e) {
			String error = "An error occurred while communicating with the service";
			logger.error(error, e);
			response.setContentType("text/html");
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
			return;
		} catch (ObjectNotFoundException e) {
			logger.info(e.getMessage());
			response.setContentType("text/html");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
			return;
		}
		byte[] token = user.getAuthToken();
		if (token == null) {
			String error = "Authentication token invalid";
			logger.error(error);
			response.setContentType("text/html");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, error);
			return;
		}
		user.setNonce(null);
		user.setNonceExpiryDate(null);
		String tokenEncoded = new String(Base64.encodeBase64(token), "US-ASCII");
		response.setContentType("text/plain");
	    PrintWriter out = response.getWriter();
	    out.println(tokenEncoded);
	}
}
