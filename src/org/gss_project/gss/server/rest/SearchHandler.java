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

import org.gss_project.gss.common.exceptions.ObjectNotFoundException;
import org.gss_project.gss.common.exceptions.RpcException;
import org.gss_project.gss.server.domain.FileHeader;
import org.gss_project.gss.server.domain.FileBody;
import org.gss_project.gss.server.domain.User;

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
        String path = getInnerPath(req, PATH_SEARCH);
        if(path.indexOf("?")!=-1){
        	path = path.substring(0,path.indexOf("?"));
        }
        
        if(path.indexOf("/")==-1){
        	
        }
        else{
			if(path.indexOf("/") == 0)
				path = path.substring(1);
			if(path.lastIndexOf("/") == path.length()-1 &&path.length()>1)
				path = path.substring(0,path.length()-1);
			if (!isValidResourceName(path)) {
	    		resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
	    		return;
	
			}
        }
		if (path.equals(""))
			path = "/";
    	if (!path.equals("/"))
			try {
		    	User user = getUser(req);
	        	JSONArray json = new JSONArray();
	        	int start =-1;
	        	if(req.getParameter("start")!=null){
	        		start=Integer.parseInt(req.getParameter("start"));
	        		JSONObject j = new JSONObject();
	        		j.put("length",getService().searchFilesCount(user.getId(), URLDecoder.decode(path,"UTF-8")));
	        		json.put(j);
	        	}
				List<FileHeader> fileHeaders = getService().searchFiles(user.getId(), URLDecoder.decode(path,"UTF-8"),start);
    	    	for (FileHeader f: fileHeaders) {
    	    		FileBody currentBody = f.getCurrentBody();
    	    		JSONObject j = new JSONObject();
    				j.put("name", f.getName()).
    					put("owner", f.getOwner().getUsername()).
    					put("deleted", f.isDeleted()).
    					put("version", currentBody.getVersion()).
    					put("size", currentBody.getFileSize()).
    					put("path", f.getFolder().getPath()).
						put("content", currentBody.getMimeType()).
						put("shared", f.getShared()).
    					put("versioned",f.isVersioned()).
    					put("creationDate", f.getAuditInfo().getCreationDate().getTime()).
    					put("modificationDate", f.getAuditInfo().getModificationDate().getTime()).
        				put("uri", getApiRoot() + f.getURI());
    				JSONObject jf = new JSONObject();
    				jf.put("uri", getApiRoot() + f.getFolder().getURI()).
    						put("name", URLEncoder.encode(f.getFolder().getName(),"UTF-8"));
    				j.put("folder", jf);
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
