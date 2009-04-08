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

import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A class that handles operations on the 'search' namespace.
 *
 * @author past
 */
public class SearchHandler extends RequestHandler {
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(SearchHandler.class);

	/**
     * Serve the 'search' namespace that contains results in queries to find files.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
     * @throws IOException if an input/output error occurs
	 */
	void serveSearchResults(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	String contextPath = getContextPath(req, true);
        String path = getInnerPath(req, PATH_SEARCH);
		if (path.equals(""))
			path = "/";

    	if (!path.equals("/"))
			try {
		    	User user = getUser(req);
	        	JSONArray json = new JSONArray();

				String pathInfo = req.getPathInfo();

				List<FileHeaderDTO> fileHeaders = getService().searchFiles(user.getId(), path.substring(1));
    	    	for (FileHeaderDTO f: fileHeaders) {
    				String parentUrl = contextPath.replaceFirst(pathInfo, "");
    				if (!parentUrl.endsWith("/"))
    					parentUrl += "/";
    				parentUrl = parentUrl+ f.getOwner().getUsername() +	PATH_FILES;
    	    		JSONObject j = new JSONObject();
    				j.put("name", f.getName()).
    					put("owner", f.getOwner().getUsername()).
    					put("deleted", f.isDeleted()).
    					put("version", f.getVersion()).
    					put("size", f.getFileSize()).
    					put("folder", f.getFolder().getURI()).
    					put("path", f.getFolder().getPath()).
    					put("creationDate", f.getAuditInfo().getCreationDate().getTime()).
        				put("uri", f.getURI());
    				json.put(j);
    	    	}
            	sendJson(req, resp, json.toString());
    		} catch (ObjectNotFoundException e) {
    			logger.error("User not found or search query not specified", e);
    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
    		} catch (RpcException e) {
    			logger.error("", e);
    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
			} catch (JSONException e) {
				logger.error("", e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No search query found");
			return;
    	}
	}

}
