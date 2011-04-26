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
package org.gss_project.gss.server.rest;

import org.gss_project.gss.common.exceptions.ObjectNotFoundException;
import org.gss_project.gss.common.exceptions.RpcException;
import org.gss_project.gss.server.domain.User;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A class that handles authentication token renewal.
 *
 * @author past
 */
public class TokenHandler extends RequestHandler {
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(TokenHandler.class);

	/**
     * Invalidate the current authentication token and return a newly-issued one.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
     * @throws IOException if an input/output error occurs
	 */
	void newToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = getInnerPath(req, PATH_TOKEN);
		if (path.equals(""))
			path = "/";
		if (!"/".equals(path)) {
			String error = "Invalid request for new token";
			logger.info(error);
			resp.setContentType("text/html");
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, error);
			return;
		}

		try {
	    	User user = getUser(req);
	    	// The following can't happen, but it's better to be safe than sorry.
	    	if (user == null)
	    		throw new ObjectNotFoundException();
	    	user = getService().updateUserToken(user.getId());
			String tokenEncoded = new String(Base64.encodeBase64(user.getAuthToken()), "US-ASCII");
			resp.setContentType("text/plain");
		    PrintWriter out = resp.getWriter();
		    out.println(tokenEncoded);
		} catch (ObjectNotFoundException e) {
			logger.error("User not found", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		} catch (RpcException e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}

}
