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

import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;
import gr.ebs.gss.server.domain.dto.FolderDTO;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A class that handles operations on the 'shared' namespace.
 *
 * @author past
 */
public class SharedHandler extends RequestHandler {
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(SharedHandler.class);

	/**
     * Serve the 'shared' namespace for the user.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
     * @throws IOException if an input/output error occurs
	 */
	void serveShared(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	String parentUrl = getContextPath(req, true);
        String path = getInnerPath(req, PATH_SHARED);
		if (path.equals(""))
			path = "/";

    	if (path.equals("/"))
			try {
		    	User user = getUser(req);
	        	User owner = getOwner(req);
	        	if (!owner.equals(user))
	        		throw new InsufficientPermissionsException("User " + user.getUsername()
	        					+ " does not have permission to view the resources shared by "
	        					+ owner.getUsername());
	        	JSONObject json = new JSONObject();

				String pathInfo = req.getPathInfo();
				parentUrl = parentUrl.replaceFirst(pathInfo, "");
				if (!parentUrl.endsWith("/"))
					parentUrl += "/";
				parentUrl = parentUrl + owner.getUsername() +	PATH_FILES;

				List<JSONObject> subfolders = new ArrayList<JSONObject>();
    	    	List<FolderDTO> folders = getService().getSharedFolders(owner.getId());
    	    	for (FolderDTO f: folders) {
        			JSONObject j = new JSONObject();
        			j.put("name", f.getName()).
        				put("uri", parentUrl + f.getPath());
        			if (f.getParent() != null)
        				json.put("parent", f.getParent().getURI());
    				subfolders.add(j);
        		}
    			json.put("folders", subfolders);

    	    	List<FileHeaderDTO> fileHeaders = getService().getSharedFilesNotInSharedFolders(owner.getId());
    	    	List<JSONObject> files = new ArrayList<JSONObject>();
    	    	for (FileHeaderDTO f: fileHeaders) {
    	    		JSONObject j = new JSONObject();
    				j.put("name", f.getName()).
    					put("owner", f.getOwner().getUsername()).
    					put("deleted", f.isDeleted()).
    					put("version", f.getVersion()).
    					put("size", f.getFileSize()).
    					put("content", f.getMimeType()).
    					put("path", f.getFolder().getPath()).
    					put("creationDate", f.getAuditInfo().getCreationDate().getTime()).
        				put("uri", parentUrl + f.getPath());
    				JSONObject jf = new JSONObject();
    				jf.put("uri", f.getFolder().getURI()).
    						put("name", URLEncoder.encode(f.getFolder().getName(),"UTF-8"));
    				j.put("folder", jf);
        			files.add(j);
    	    	}
    	    	json.put("files", files);

            	sendJson(req, resp, json.toString());
    		} catch (ObjectNotFoundException e) {
    			logger.error("User not found", e);
    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    		} catch (RpcException e) {
    			logger.error("", e);
    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (JSONException e) {
				logger.error("", e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (InsufficientPermissionsException e) {
				resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.getMessage());
			}
		else
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

}
