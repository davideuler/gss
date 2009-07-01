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

import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.dto.GroupDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A class that handles operations on the 'groups' namespace.
 *
 * @author past
 */
public class GroupsHandler extends RequestHandler {
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(GroupsHandler.class);

    /**
     * Serve the groups namespace for the user.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
     * @throws IOException if an input/output error occurs
	 */
	void serveGroups(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	String parentUrl = getContextPath(req, true);
        String path = getInnerPath(req, PATH_GROUPS);
		if (path.equals(""))
			path = "/";

    	User user = getUser(req);
    	User owner = getOwner(req);
    	if (!owner.equals(user)) {
    		resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    		return;
    	}
    	if (path.equals("/"))
			// Request to serve all groups
        	try {
            	List<GroupDTO> groups = getService().getGroups(owner.getId());
            	JSONArray json = new JSONArray();
            	for (GroupDTO group: groups) {
            		JSONObject j = new JSONObject();
            		j.put("name", group.getName()).
            			put("uri", parentUrl + URLEncoder.encode(group.getName(),"UTF-8"));
        			json.put(j);
            	}

            	sendJson(req, resp, json.toString());
    		} catch (ObjectNotFoundException e) {
    			logger.error("User not found", e);
    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
    		} catch (RpcException e) {
    			logger.error("", e);
    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
    		} catch (JSONException e) {
    			logger.error("", e);
    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
			}
		else {
    		// Chop any trailing slash
        	path = path.endsWith("/")? path.substring(0, path.length()-1): path;
        	// Chop any leading slash
        	path = path.startsWith("/")? path.substring(1): path;
        	int slash = path.indexOf('/');
			try {
	        	if (slash != -1) {
	        		// Request to serve group member
	        		if (logger.isDebugEnabled())
	        			logger.debug("Serving member " + path.substring(slash + 1) +
	        						" from group " + path.substring(0, slash));
	        		GroupDTO group = getService().getGroup(owner.getId(), URLDecoder.decode(path.substring(0, slash),"UTF-8"));
	        		for (UserDTO u: group.getMembers())
	        			if (u.getUsername().equals(path.substring(slash + 1))) {
	    					// Build the proper parent URL
	    					String pathInfo = req.getPathInfo();
	    					parentUrl = parentUrl.replaceFirst(pathInfo, "");
	    		        	JSONObject json = new JSONObject();
    		    			json.put("username", u.getUsername()).put("name", u.getName()).
    		    					put("home", parentUrl + u.getUsername());

	    		        	sendJson(req, resp, json.toString());
	        			}
	        	} else {
	        		// Request to serve group
	        		if (logger.isDebugEnabled())
	        			logger.debug("Serving group " + path);
	    			GroupDTO group = getService().getGroup(owner.getId(), URLDecoder.decode(path,"UTF-8"));
		        	JSONArray json = new JSONArray();
		        	for (UserDTO u: group.getMembers())
		    			json.put(parentUrl + u.getUsername());

		        	sendJson(req, resp, json.toString());
	        	}
			} catch (ObjectNotFoundException e) {
    			logger.error("", e);
    			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    			return;
			} catch (RpcException e) {
    			logger.error("", e);
    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
			} catch (JSONException e) {
				logger.error("", e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
    	}
    	// Workaround for IE's broken caching behavior.
		resp.setHeader("Expires", "-1");
	}

	/**
	 * Handle POST requests in the groups namespace.
	 *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
     * @throws IOException if an input/output error occurs
	 */
	void postGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	// Identify the requested resource path
        String path = getInnerPath(req, PATH_GROUPS);
		if (path.equals(""))
			path = "/";

		try {
	    	User user = getUser(req);
        	User owner = getOwner(req);
        	if (!owner.equals(user))
        		throw new InsufficientPermissionsException("User " + user.getUsername()
        					+ " does not have permission to modify the groups owned by "
        					+ owner.getUsername());
	    	if (path.equals("/")) {
	        	// Request to add group
	    		String group = req.getParameter(GROUP_PARAMETER);
	    		if (logger.isDebugEnabled())
	    			logger.debug("Adding group " + group);
					getService().createGroup(owner.getId(), group);
	        		resp.setStatus(HttpServletResponse.SC_CREATED);
	    	} else {
        		// Request to add group member
        		String username = req.getParameter(USERNAME_PARAMETER);
	    		// Chop any trailing slash
	        	path = path.endsWith("/")? path.substring(0, path.length()-1): path;
	        	// Chop any leading slash
	        	path = path.startsWith("/")? path.substring(1): path;
        		if (logger.isDebugEnabled())
        			logger.debug("Adding member " + username +
        						" to group " + path);
        		GroupDTO group = getService().getGroup(owner.getId(), URLDecoder.decode(path,"UTF-8"));
        		User member = getService().findUser(username);
        		if (member == null) {
        			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User " + username + " not found");
        			return;
        		}
        		getService().addUserToGroup(owner.getId(), group.getId(), member.getId());
        		resp.setStatus(HttpServletResponse.SC_CREATED);
	    	}
	    	// Workaround for IE's broken caching behavior.
			resp.setHeader("Expires", "-1");
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} catch (DuplicateNameException e) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
		} catch (RpcException e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.getMessage());
		}
	}

	/**
	 * Handle DELETE requests in the groups namespace.
	 *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
     * @throws IOException if an input/output error occurs
	 */
	void deleteGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = getInnerPath(req, PATH_GROUPS);
		if (path.equals(""))
			path = "/";

    	if (path.equals("/"))
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, METHOD_GET + ", " + METHOD_POST);
		else {
    		// Chop any trailing slash
        	path = path.endsWith("/")? path.substring(0, path.length()-1): path;
        	// Chop any leading slash
        	path = path.startsWith("/")? path.substring(1): path;
        	int slash = path.indexOf('/');
        	try {
            	User user = getUser(req);
            	User owner = getOwner(req);
            	if (!owner.equals(user))
            		throw new InsufficientPermissionsException("User " + user.getUsername()
            					+ " does not have permission to modify the groups owned by "
            					+ owner.getUsername());
            	if (slash != -1) {
            		// Request to delete group member
            		if (logger.isDebugEnabled())
            			logger.debug("Removing member " + path.substring(slash + 1) +
            						" from group " + path.substring(0, slash));
            		GroupDTO group = getService().getGroup(owner.getId(), path.substring(0, slash));
            		for (UserDTO u: group.getMembers())
            			if (u.getUsername().equals(path.substring(slash + 1)))
            				getService().removeMemberFromGroup(owner.getId(), group.getId(), u.getId());
            	} else {
            		if (logger.isDebugEnabled())
            			logger.debug("Removing group " + path);
        			GroupDTO group = getService().getGroup(owner.getId(), URLDecoder.decode(path,"UTF-8"));
        			getService().deleteGroup(owner.getId(), group.getId());
            	}
        		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        		// Workaround for IE's broken caching behavior.
    			resp.setHeader("Expires", "-1");
        	} catch (RpcException e) {
    			logger.error("", e);
    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	} catch (ObjectNotFoundException e) {
    			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			} catch (InsufficientPermissionsException e) {
				resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.getMessage());
			}
    	}
	}

}
