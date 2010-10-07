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
package gr.ebs.gss.server.rest;

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.Login;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.dto.StatsDTO;
import gr.ebs.gss.server.service.ExternalAPI;
import gr.ebs.gss.server.service.TransactionHelper;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A class that handles operations on the user's root namespace.
 *
 * @author past
 */
public class UserHandler extends RequestHandler {

	/**
	 * The reset WebDAV password parameter name.
	 */
	protected static final String RESET_WEBDAV_PARAMETER = "resetWebDAV";

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(UserHandler.class);

	public UserHandler(ExternalAPI aService) {
		service = aService;
	}

    /**
     * Serve the root namespace for the user.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
     * @throws IOException if an input/output error occurs
	 */
	void serveUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	String parentUrl = getContextPath(req, false);

    	User user = getUser(req);
    	User owner = getOwner(req);
    	if (!owner.equals(user)) {
    		resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    		return;
    	}

    	JSONObject json = new JSONObject();
    	try {
    		StatsDTO stats = service.getUserStatistics(owner.getId());
    		JSONObject statistics = new JSONObject();
    		statistics.put("totalFiles", stats.getFileCount()).put("totalBytes", stats.getFileSize()).
    				put("bytesRemaining", stats.getQuotaLeftSize());
			json.put("name", owner.getName()).put("firstname", owner.getFirstname()).
					put("lastname", owner.getLastname()).put("username", owner.getUsername()).
					put("creationDate", owner.getAuditInfo().getCreationDate().getTime()).
					put("modificationDate", owner.getAuditInfo().getModificationDate().getTime()).
					put("email", owner.getEmail()).put("fileroot", parentUrl + PATH_FILES).
					put("groups", parentUrl + PATH_GROUPS).put("trash", parentUrl + PATH_TRASH).
					put("shared", parentUrl + PATH_SHARED).put("others", parentUrl + PATH_OTHERS).
					put("quota", statistics).put("tags", parentUrl + PATH_TAGS);
			String announcement = getConfiguration().getString("announcement");
			if (announcement != null && !announcement.isEmpty())
				json.put("announcement", announcement);
			if (owner.getLastLogin() != null)
				json.put("lastLogin", owner.getLastLogin().getTime());
		} catch (JSONException e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			return;
		}

    	sendJson(req, resp, json.toString());
	}


	/**
	 * Handle POST requests in the users namespace.
	 *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
     * @throws IOException if an input/output error occurs
	 */
	void postUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
	    	final User user = getUser(req);
        	User owner = getOwner(req);
        	if (!owner.equals(user))
        		throw new InsufficientPermissionsException("User " + user.getUsername()
        					+ " does not have permission to modify "
        					+ owner.getUsername());
        	boolean hasResetWebDAVParam = req.getParameterMap().containsKey(RESET_WEBDAV_PARAMETER);
        	if (hasResetWebDAVParam) {
        		String newPassword = new TransactionHelper<String>().tryExecute(new Callable<String>() {
					@Override
					public String call() throws Exception {
						return service.resetWebDAVPassword(user.getId());
					}
				});

    			// Set the cookie again to send new value
    			Cookie cookie = new Cookie(Login.WEBDAV_COOKIE, newPassword);
    			cookie.setMaxAge(-1);
    			String domain = req.getRemoteHost();
    			String path = req.getContextPath();
    			cookie.setDomain(domain);
    			cookie.setPath(path);
    		    resp.addCookie(cookie);
        	}
	    	// Workaround for IE's broken caching behavior.
			resp.setHeader("Expires", "-1");
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} catch (RpcException e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.getMessage());
		} catch (Exception e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
