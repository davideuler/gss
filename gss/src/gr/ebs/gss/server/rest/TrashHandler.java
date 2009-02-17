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

import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A class that handles operations on the 'trash' namespace.
 *
 * @author past
 */
public class TrashHandler extends RequestHandler {
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(TrashHandler.class);

	/**
	 * Return the files and folders that are in the trash can.
	 *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
	 * @throws IOException if the response cannot be sent
	 * @throws ServletException
	 */
	void serveTrash(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		List<FileHeaderDTO> files = null;
		List<FolderDTO> folders = null;
    	User user = getUser(req);
    	User owner = getOwner(req);
    	if (!owner.equals(user)) {
    		resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    		return;
    	}
		try {
			files = getService().getDeletedFiles(user.getId());
			folders = getService().getDeletedRootFolders(user.getId());
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} catch (RpcException e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		if (files.isEmpty() && folders.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_NO_CONTENT);
			return;
		}

    	String parentUrl = getContextPath(req, true);
    	String pathInfo = req.getPathInfo();
		parentUrl = parentUrl.replaceFirst(pathInfo, "") + owner.getUsername() + PATH_FILES;
		JSONObject json = new JSONObject();
    	try {
    		List<String> trashFolders = new ArrayList<String>();
    		for (FolderDTO f: folders)
				trashFolders.add(parentUrl + f.getPath());
	    	json.put("subfolders", trashFolders);
	    	List<String> trashFiles = new ArrayList<String>();
	    	for (FileHeaderDTO f: files)
    			trashFiles.add(parentUrl + f.getPath());
	    	json.put("files", trashFiles);
		} catch (JSONException e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

    	sendJson(req, resp, json.toString());
	}

}
