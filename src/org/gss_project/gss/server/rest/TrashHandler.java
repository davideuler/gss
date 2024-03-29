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
package org.gss_project.gss.server.rest;

import org.gss_project.gss.common.exceptions.InsufficientPermissionsException;
import org.gss_project.gss.common.exceptions.ObjectNotFoundException;
import org.gss_project.gss.common.exceptions.RpcException;
import org.gss_project.gss.server.domain.FileHeader;
import org.gss_project.gss.server.domain.Folder;
import org.gss_project.gss.server.domain.User;
import org.gss_project.gss.server.ejb.TransactionHelper;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
        String path = getInnerPath(req, PATH_TRASH);
		if (path.equals(""))
			path = "/";

    	if (!path.equals("/")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
    	}

    	List<FileHeader> files = null;
		List<Folder> folders = null;
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

		JSONObject json = new JSONObject();
    	try {
    		List<JSONObject> trashFolders = new ArrayList<JSONObject>();
    		for (Folder f: folders) {
    			JSONObject j = new JSONObject();
    			j.put("name", f.getName()).
    				put("uri", getApiRoot() + f.getURI());
    			if (f.getParent() != null)
    				j.put("parent", getApiRoot() + f.getParent().getURI());
				trashFolders.add(j);
    		}
	    	json.put("folders", trashFolders);
	    	List<JSONObject> trashFiles = new ArrayList<JSONObject>();
	    	for (FileHeader f: files) {
	    		JSONObject j = new JSONObject();
				j.put("name", f.getName()).
					put("owner", f.getOwner().getUsername()).
					put("deleted", f.isDeleted()).
					put("version", f.getCurrentBody().getVersion()).
					put("size", f.getCurrentBody().getFileSize()).
					put("content", f.getCurrentBody().getMimeType()).
					put("shared", f.getShared()).
				    put("versioned",f.isVersioned()).
					put("path", f.getFolder().getPath()).
					put("creationDate", f.getAuditInfo().getCreationDate().getTime()).
					put("modificationDate", f.getAuditInfo().getModificationDate().getTime()).
    				put("uri", getApiRoot() + f.getURI());
				JSONObject p = new JSONObject();
				p.put("uri", getApiRoot() + f.getFolder().getURI()).
						put("name", URLEncoder.encode(f.getFolder().getName(),"UTF-8"));
				j.put("folder", p);
				trashFiles.add(j);
	    	}
	    	json.put("files", trashFiles);
		} catch (JSONException e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		// Workaround for IE's broken caching behavior.
   		resp.setHeader("Expires", "-1");
    	sendJson(req, resp, json.toString());
	}

	/**
	 * Empties the trash can from any currently stored resource,
	 * making all trashed files and folders permanently deleted.
	 *
     * @param req The HTTP request we are processing
     * @param resp The HTTP response we are processing
	 * @throws IOException
     * @throws IOException if an input/output error occurs
	 */
	public void emptyTrash(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = getInnerPath(req, PATH_TRASH);
		if (path.equals(""))
			path = "/";

    	if (!path.equals("/")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
    	}
		try {
			User user = getUser(req);
			final User owner = getOwner(req);
			if (!owner.equals(user))
				throw new InsufficientPermissionsException("User " + user.getUsername()
							+ " does not have permission to empty the trash can owned by "
							+ owner.getUsername());
			if (logger.isDebugEnabled())
				logger.debug("Emptying trash for user " + owner.getUsername());
			new TransactionHelper<Void>().tryExecute(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					getService().emptyTrash(owner.getId());
					return null;
				}
			});
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} catch (RpcException e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.getMessage());
		} catch (Exception e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
