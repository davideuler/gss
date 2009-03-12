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
import gr.ebs.gss.server.domain.dto.FolderDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A class that handles operations on the 'others' namespace.
 *
 * @author past
 */
public class OthersHandler extends RequestHandler {
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(OthersHandler.class);

	/**
     * Serve the 'others' namespace for the user.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
     * @throws IOException if an input/output error occurs
	 */
	void serveOthers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	String parentUrl = getContextPath(req, true);
        String path = getInnerPath(req, PATH_OTHERS);
		if (path.equals(""))
			path = "/";

    	User user = getUser(req);
    	User owner = getOwner(req);
    	if (!owner.equals(user)) {
    		resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    		return;
    	}
    	if (path.equals("/"))
			try {
	    		// Request to retrieve the other users who have shared resources to this user
	        	JSONArray json = new JSONArray();

    	    	List<UserDTO> others = getService().getUsersSharingFoldersForUser(owner.getId());
		    	for (UserDTO u: others) {
		    		JSONObject j = new JSONObject();
		    		j.put("username", u.getUsername()).
		    			put("uri", parentUrl + u.getUsername());
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
		else
			try {
	    		// Request to retrieve the files and folders shared by the other user
	    		// Chop any trailing slash
	        	path = path.endsWith("/")? path.substring(0, path.length()-1): path;
	        	// Chop any leading slash
	        	path = path.startsWith("/")? path.substring(1): path;

	        	User other = getService().findUser(path);
	        	JSONObject json = new JSONObject();

				String pathInfo = req.getPathInfo();
				parentUrl = parentUrl.replaceFirst(pathInfo, "");
				if (!parentUrl.endsWith("/"))
					parentUrl += "/";
				parentUrl = parentUrl + path + PATH_FILES;

				List<JSONObject> subfolders = new ArrayList<JSONObject>();
    	    	List<FolderDTO> folders = getService().getSharedRootFolders(other.getId(), owner.getId());
        		for (FolderDTO f: folders) {
        			JSONObject j = new JSONObject();
        			j.put("name", f.getName()).
        				put("uri", parentUrl + f.getPath());
    				subfolders.add(j);
        		}
    			json.put("folders", subfolders);

    	    	List<JSONObject> files = new ArrayList<JSONObject>();
    	    	List<FileHeaderDTO> fileHeaders = getService().getSharedFiles(other.getId(), owner.getId());
    	    	for (FileHeaderDTO f: fileHeaders) {
    	    		JSONObject j = new JSONObject();
    				j.put("name", f.getName()).
    					put("owner", f.getOwner().getUsername()).
    					put("deleted", f.isDeleted()).
    					put("version", f.getVersion()).
    					put("size", f.getFileSize()).
    					put("creationDate", f.getAuditInfo().getCreationDate().getTime()).
        				put("uri", parentUrl + f.getPath());
        			files.add(j);
    	    	}
    	    	json.put("files", files);

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
	}

}
