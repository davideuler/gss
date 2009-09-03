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
import gr.ebs.gss.client.exceptions.GSSIOException;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.QuotaExceededException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.FileUploadStatus;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.domain.dto.FileBodyDTO;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;
import gr.ebs.gss.server.domain.dto.FolderDTO;
import gr.ebs.gss.server.domain.dto.GroupDTO;
import gr.ebs.gss.server.domain.dto.PermissionDTO;
import gr.ebs.gss.server.ejb.ExternalAPI;
import gr.ebs.gss.server.webdav.Range;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A class that handles operations on the 'files' namespace.
 *
 * @author past
 */
public class FilesHandler extends RequestHandler {
	/**
	 * The request parameter name for fetching a different version.
	 */
	private static final String VERSION_PARAM = "version";

	/**
	 * The request attribute containing the owner of the destination URI
	 * in a copy or move request.
	 */
	private static final String DESTINATION_OWNER_ATTRIBUTE = "destOwner";

	private static final int TRACK_PROGRESS_PERCENT = 5;

	/**
	 * The form parameter name that contains the signature in a browser POST upload.
	 */
	private static final String AUTHORIZATION_PARAMETER = "Authorization";

	/**
	 * The form parameter name that contains the date in a browser POST upload.
	 */
	private static final String DATE_PARAMETER = "Date";

	/**
	 * The request parameter name for making an upload progress request.
	 */
	private static final String PROGRESS_PARAMETER = "progress";

	/**
	 * The request parameter name for restoring a previous version of a file.
	 */
	private static final String RESTORE_VERSION_PARAMETER = "restoreVersion";

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(FilesHandler.class);

	/**
	 * The servlet context provided by the call site.
	 */
	private ServletContext context;

	/**
	 * @param servletContext
	 */
	public FilesHandler(ServletContext servletContext) {
		context = servletContext;
	}

	/**
     * Serve the specified resource, optionally including the data content.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     * @param content Should the content be included?
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     * @throws RpcException
     * @throws InsufficientPermissionsException
     * @throws ObjectNotFoundException
     */
	@Override
	protected void serveResource(HttpServletRequest req, HttpServletResponse resp, boolean content)
    		throws IOException, ServletException {
		boolean authDeferred = getAuthDeferred(req);
        String path = getInnerPath(req, PATH_FILES);
		if (path.equals(""))
			path = "/";
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (IllegalArgumentException e) {
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
    	String progress = req.getParameter(PROGRESS_PARAMETER);

    	if (logger.isDebugEnabled())
			if (content)
    			logger.debug("Serving resource '" +	path + "' headers and data");
    		else
    			logger.debug("Serving resource '" +	path + "' headers only");

    	User user = getUser(req);
    	User owner = getOwner(req);
    	if (user == null) user = owner;
        boolean exists = true;
        Object resource = null;
        FileHeaderDTO file = null;
        FolderDTO folder = null;
        try {
        	resource = getService().getResourceAtPath(owner.getId(), path, false);
        } catch (ObjectNotFoundException e) {
            exists = false;
        } catch (RpcException e) {
        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}

    	if (!exists) {
			if (authDeferred) {
				// We do not want to leak information if the request
				// was not authenticated.
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
	    	// A request for upload progress.
	    	if (progress != null && content) {
	    		serveProgress(req, resp, progress, user, null);
				return;
	    	}

    		resp.sendError(HttpServletResponse.SC_NOT_FOUND, req.getRequestURI());
    		return;
    	}

    	if (resource instanceof FolderDTO)
    		folder = (FolderDTO) resource;
    	else
    		file = (FileHeaderDTO) resource;

    	// Now it's time to perform the deferred authentication check.
		// Since regular signature checking was already performed,
		// we need to check the read-all flag or the signature-in-parameters.
		if (authDeferred)
			if (file != null && !file.isReadForAll() && content) {
				// Check for GET with the signature in the request parameters.
				String auth = req.getParameter(AUTHORIZATION_PARAMETER);
				String dateParam = req.getParameter(DATE_PARAMETER);
				if (auth == null || dateParam == null) {
					resp.sendError(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

		    	long timestamp;
				try {
					timestamp = DateUtil.parseDate(dateParam).getTime();
				} catch (DateParseException e) {
		    		resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
		    		return;
				}
		    	if (!isTimeValid(timestamp)) {
		    		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		    		return;
		    	}

				// Fetch the Authorization parameter and find the user specified in it.
				String[] authParts = auth.split(" ");
				if (authParts.length != 2) {
		    		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		    		return;
		    	}
				String username = authParts[0];
				String signature = authParts[1];
				user = null;
				try {
					user = getService().findUser(username);
				} catch (RpcException e) {
		        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
					return;
				}
				if (user == null) {
		    		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		    		return;
		    	}
				req.setAttribute(USER_ATTRIBUTE, user);

				// Remove the servlet path from the request URI.
				String p = req.getRequestURI();
				String servletPath = req.getContextPath() + req.getServletPath();
				p = p.substring(servletPath.length());
				// Validate the signature in the Authorization parameter.
				String data = req.getMethod() + dateParam + p;
				if (!isSignatureValid(signature, user, data)) {
		    		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		    		return;
		    	}
			} else if (file != null && !file.isReadForAll() || file == null) {
				// Check for a read-for-all file request.
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

    	// If the resource is not a collection, and the resource path
    	// ends with "/" or "\", return NOT FOUND.
    	if (folder == null)
			if (path.endsWith("/") || path.endsWith("\\")) {
    			resp.sendError(HttpServletResponse.SC_NOT_FOUND, req.getRequestURI());
    			return;
    		}

    	// Workaround for IE's broken caching behavior.
    	if (folder != null)
    		resp.setHeader("Expires", "-1");

    	// A request for upload progress.
    	if (progress != null && content) {
    		if (file == null) {
    			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    			return;
    		}
    		serveProgress(req, resp, progress, user, file);
			return;
    	}

		// Fetch the version to retrieve, if specified.
		String verStr = req.getParameter(VERSION_PARAM);
		int version = 0;
		FileBodyDTO oldBody = null;
		if (verStr != null && file != null)
			try {
				version = Integer.valueOf(verStr);
			} catch (NumberFormatException e) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, req.getRequestURI());
    			return;
			}
		if (version > 0)
			try {
				oldBody = getService().getFileVersion(user.getId(), file.getId(), version);
			} catch (RpcException e) {
	        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
				return;
			} catch (ObjectNotFoundException e) {
    			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    			return;
			} catch (InsufficientPermissionsException e) {
    			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    			return;
			}

    	// Check if the conditions specified in the optional If headers are
    	// satisfied. Doing this for folders would require recursive checking
    	// for all of their children, which in turn would defy the purpose of
    	// the optimization.
    	if (folder == null)
			// Checking If headers.
    		if (!checkIfHeaders(req, resp, file, oldBody))
				return;

    	// Find content type.
    	String contentType = null;
    	if (file != null) {
        	contentType = version>0 ? oldBody.getMimeType() : file.getMimeType();
        	if (contentType == null) {
        		contentType = context.getMimeType(file.getName());
        		file.setMimeType(contentType);
        	}
    	} else
			contentType = "application/json;charset=UTF-8";

    	ArrayList ranges = null;
    	long contentLength = -1L;

    	if (file != null) {
    		// Parse range specifier.
    		ranges = parseRange(req, resp, file, oldBody);
    		// ETag header
    		resp.setHeader("ETag", getETag(file, oldBody));
    		// Last-Modified header.
    		String lastModified = oldBody == null ?
    					getLastModifiedHttp(file.getAuditInfo()) :
    					getLastModifiedHttp(oldBody.getAuditInfo());
    		resp.setHeader("Last-Modified", lastModified);
    		// X-GSS-Metadata header.
    		try {
				resp.setHeader("X-GSS-Metadata", renderJson(user, file, oldBody));
			} catch (InsufficientPermissionsException e) {
				resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	        	return;
	        }
    		// Get content length.
    		contentLength = version>0 ? oldBody.getFileSize() : file.getFileSize();
    		// Special case for zero length files, which would cause a
    		// (silent) ISE when setting the output buffer size.
    		if (contentLength == 0L)
				content = false;
    	} else
    		// Set the folder X-GSS-Metadata header.
    		try {
				resp.setHeader("X-GSS-Metadata", renderJsonMetadata(user, folder));
			} catch (InsufficientPermissionsException e) {
				resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	        	return;
	        }

    	ServletOutputStream ostream = null;
    	PrintWriter writer = null;

    	if (content)
			try {
    			ostream = resp.getOutputStream();
    		} catch (IllegalStateException e) {
    			// If it fails, we try to get a Writer instead if we're
    			// trying to serve a text file
    			if ( contentType == null
    						|| contentType.startsWith("text")
    						|| contentType.endsWith("xml") )
					writer = resp.getWriter();
				else
					throw e;
    		}

    	if (folder != null
    				|| (ranges == null || ranges.isEmpty())
    							&& req.getHeader("Range") == null
    							|| ranges == FULL) {
    		// Set the appropriate output headers
    		if (contentType != null) {
    			if (logger.isDebugEnabled())
    				logger.debug("contentType='" + contentType + "'");
    			resp.setContentType(contentType);
    		}
    		if (file != null && contentLength >= 0) {
    			if (logger.isDebugEnabled())
    				logger.debug("contentLength=" + contentLength);
    			if (contentLength < Integer.MAX_VALUE)
					resp.setContentLength((int) contentLength);
				else
					// Set the content-length as String to be able to use a long
    				resp.setHeader("content-length", "" + contentLength);
    		}

    		InputStream renderResult = null;
    		if (folder != null)
				if (content)
					// Serve the directory browser
    				try {
						renderResult = renderJson(user, folder);
					} catch (InsufficientPermissionsException e) {
						resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			        	return;
					}
    		// Copy the input stream to our output stream (if requested)
    		if (content) {
    			try {
    				resp.setBufferSize(output);
    			} catch (IllegalStateException e) {
    				// Silent catch
    			}
    			try {
    				if(file != null)
						if (needsContentDisposition(req))
    						resp.setHeader("Content-Disposition","attachment; filename*=UTF-8''"+URLEncoder.encode(file.getName(),"UTF-8"));
    					else
    						resp.setHeader("Content-Disposition","inline; filename*=UTF-8''"+URLEncoder.encode(file.getName(),"UTF-8"));
	    			if (ostream != null)
						copy(file, renderResult, ostream, req, oldBody);
					else
						copy(file, renderResult, writer, req, oldBody);
	    			if (file!=null) getService().updateAccounting(owner, new Date(), contentLength);
        		} catch (ObjectNotFoundException e) {
        			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        			return;
        		} catch (InsufficientPermissionsException e) {
        			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        			return;
        		} catch (RpcException e) {
        			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        			return;
        		}
    		}
    	} else {
    		if (ranges == null || ranges.isEmpty())
    			return;
    		// Partial content response.
    		resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

    		if (ranges.size() == 1) {
    			Range range = (Range) ranges.get(0);
    			resp.addHeader("Content-Range", "bytes "
    						+ range.start
    						+ "-" + range.end + "/"
    						+ range.length);
    			long length = range.end - range.start + 1;
    			if (length < Integer.MAX_VALUE)
					resp.setContentLength((int) length);
				else
					// Set the content-length as String to be able to use a long
    				resp.setHeader("content-length", "" + length);

    			if (contentType != null) {
    				if (logger.isDebugEnabled())
    					logger.debug("contentType='" + contentType + "'");
    				resp.setContentType(contentType);
    			}

    			if (content) {
    				try {
    					resp.setBufferSize(output);
    				} catch (IllegalStateException e) {
    					// Silent catch
    				}
    				try {
	    				if (ostream != null)
							copy(file, ostream, range, req, oldBody);
						else
							copy(file, writer, range, req, oldBody);
	    				getService().updateAccounting(owner, new Date(), contentLength);
    	    		} catch (ObjectNotFoundException e) {
    	    			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    	    			return;
    	    		} catch (InsufficientPermissionsException e) {
    	    			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    	    			return;
    	    		} catch (RpcException e) {
    	    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	    			return;
    	    		}
    			}
    		} else {
    			resp.setContentType("multipart/byteranges; boundary=" + mimeSeparation);
    			if (content) {
    				try {
    					resp.setBufferSize(output);
    				} catch (IllegalStateException e) {
    					// Silent catch
    				}
    				try {
	    				if (ostream != null)
							copy(file, ostream, ranges.iterator(), contentType, req, oldBody);
						else
							copy(file, writer, ranges.iterator(), contentType, req, oldBody);
	    				getService().updateAccounting(owner, new Date(), contentLength);
    	    		} catch (ObjectNotFoundException e) {
    	    			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    	    			return;
    	    		} catch (InsufficientPermissionsException e) {
    	    			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    	    			return;
    	    		} catch (RpcException e) {
    	    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	    			return;
    	    		}
    			}
    		}
    	}
    }

	/**
	 * Determines whether the user agent needs the Content-Disposition
	 * header to be set, in order to properly download a file.
	 *
	 * @param req the HTTP request
	 * @return true if the Content-Disposition HTTP header must be set
	 */
	private boolean needsContentDisposition(HttpServletRequest req) {
		/*String agent = req.getHeader("user-agent");
		if (agent != null && agent.contains("MSIE"))
			return true;*/
		String dl = req.getParameter("dl");
		if ("1".equals(dl))
			return true;
		return false;
	}

	/**
	 * Sends a progress update on the amount of bytes received until now for
	 * a file that the current user is currently uploading.
	 *
	 * @param req the HTTP request
	 * @param resp the HTTP response
	 * @param parameter the value for the progress request parameter
	 * @param user the current user
	 * @param file the file being uploaded, or null if the request is about a new file
	 * @throws IOException if an I/O error occurs
	 */
	private void serveProgress(HttpServletRequest req, HttpServletResponse resp,
				String parameter, User user, FileHeaderDTO file)	throws IOException {
		String filename = file == null ? parameter : file.getName();
		try {
			FileUploadStatus status = getService().getFileUploadStatus(user.getId(), filename);
			if (status == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			JSONObject json = new JSONObject();
			json.put("bytesUploaded", status.getBytesUploaded()).
				put("bytesTotal", status.getFileSize());
			sendJson(req, resp, json.toString());

			// Workaround for IE's broken caching behavior.
    		resp.setHeader("Expires", "-1");
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		} catch (JSONException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			return;
		}
	}

	/**
	 * Server a POST request to create/modify a file or folder.
	 *
	 * @param req the HTTP request
	 * @param resp the HTTP response
     * @exception IOException if an input/output error occurs
	 */
	void postResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		boolean authDeferred = getAuthDeferred(req);
    	if (!authDeferred && req.getParameterMap().size() > 1) {
    		resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		return;
    	}
        String path = getInnerPath(req, PATH_FILES);
    	path = path.endsWith("/")? path: path + '/';
		try {
	    	path = URLDecoder.decode(path, "UTF-8");
		} catch (IllegalArgumentException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
    	// We only defer authenticating multipart POST requests.
    	if (authDeferred) {
			if (!ServletFileUpload.isMultipartContent(req)) {
	    		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
	    		return;
	    	}
			handleMultipart(req, resp, path);
			return;
		}

    	String newName = req.getParameter(NEW_FOLDER_PARAMETER);
    	boolean hasUpdateParam = req.getParameterMap().containsKey(RESOURCE_UPDATE_PARAMETER);
    	boolean hasTrashParam = req.getParameterMap().containsKey(RESOURCE_TRASH_PARAMETER);
    	boolean hasRestoreParam = req.getParameterMap().containsKey(RESOURCE_RESTORE_PARAMETER);
    	String copyTo = req.getParameter(RESOURCE_COPY_PARAMETER);
    	String moveTo = req.getParameter(RESOURCE_MOVE_PARAMETER);
    	String restoreVersion = req.getParameter(RESTORE_VERSION_PARAMETER);

    	if (newName != null)
			createFolder(req, resp, path, newName);
    	else if (hasUpdateParam)
			updateResource(req, resp, path);
		else if (hasTrashParam)
			trashResource(req, resp, path);
		else if (hasRestoreParam)
			restoreResource(req, resp, path);
		else if (copyTo != null)
			copyResource(req, resp, path, copyTo);
		else if (moveTo != null)
			moveResource(req, resp, path, moveTo);
		else if (restoreVersion != null)
			restoreVersion(req, resp, path, restoreVersion);
		else
			// IE with Gears uses POST for multiple uploads.
			putResource(req, resp);
	}

	/**
	 * Restores a previous version for a file.
	 *
	 * @param req the HTTP request
	 * @param resp the HTTP response
	 * @param path the resource path
	 * @param version the version number to restore
	 * @throws IOException if an I/O error occurs
	 */
	private void restoreVersion(HttpServletRequest req, HttpServletResponse resp, String path, String version) throws IOException {
		User user = getUser(req);
		User owner = getOwner(req);
		Object resource = null;
		try {
			resource = getService().getResourceAtPath(owner.getId(), path, true);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}
		if (resource instanceof FolderDTO) {
			resp.sendError(HttpServletResponse.SC_CONFLICT);
			return;
		}

		try {
			FileHeaderDTO file = (FileHeaderDTO) resource;
			int oldVersion = Integer.parseInt(version);
			getService().restoreVersion(user.getId(), file.getId(), oldVersion);
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
		} catch (GSSIOException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (QuotaExceededException e) {
			resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, e.getMessage());
		} catch (NumberFormatException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}

	/**
	 * A method for handling multipart POST requests for uploading
	 * files from browser-based JavaScript clients.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @param path the resource path
	 * @throws IOException in case an error occurs writing to the
	 * 		response stream
	 */
	private void handleMultipart(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
    	if (logger.isDebugEnabled())
   			logger.debug("Multipart POST for resource: " + path);

    	User owner = getOwner(request);
    	boolean exists = true;
        Object resource = null;
        FileHeaderDTO file = null;
        try {
        	resource = getService().getResourceAtPath(owner.getId(), path, false);
        } catch (ObjectNotFoundException e) {
            exists = false;
        } catch (RpcException e) {
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}

        if (exists)
			if (resource instanceof FileHeaderDTO) {
    			file = (FileHeaderDTO) resource;
    			if (file.isDeleted()) {
    				response.sendError(HttpServletResponse.SC_CONFLICT, file.getName() + " is in the trash");
    	    		return;
    			}
			} else {
	        	response.sendError(HttpServletResponse.SC_CONFLICT, path + " is a folder");
	    		return;
	        }

        FolderDTO folder = null;
    	Object parent;
    	String parentPath = null;
		try {
			parentPath = getParentPath(path);
			parent = getService().getResourceAtPath(owner.getId(), parentPath, true);
		} catch (ObjectNotFoundException e) {
    		response.sendError(HttpServletResponse.SC_NOT_FOUND, parentPath);
    		return;
		} catch (RpcException e) {
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}
    	if (!(parent instanceof FolderDTO)) {
    		response.sendError(HttpServletResponse.SC_CONFLICT);
    		return;
    	}
   		folder = (FolderDTO) parent;
    	String fileName = getLastElement(path);

		FileItemIterator iter;
		File uploadedFile = null;
		try {
			// Create a new file upload handler.
			ServletFileUpload upload = new ServletFileUpload();
			StatusProgressListener progressListener = new StatusProgressListener(getService());
			upload.setProgressListener(progressListener);
			iter = upload.getItemIterator(request);
			String dateParam = null;
			String auth = null;
			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String name = item.getFieldName();
				InputStream stream = item.openStream();
				if (item.isFormField()) {
					final String value = Streams.asString(stream);
					if (name.equals(DATE_PARAMETER))
						dateParam = value;
					else if (name.equals(AUTHORIZATION_PARAMETER))
						auth = value;

					if (logger.isDebugEnabled())
						logger.debug(name + ":" + value);
				} else {
					// Fetch the timestamp used to guard against replay attacks.
			    	if (dateParam == null) {
			    		response.sendError(HttpServletResponse.SC_FORBIDDEN, "No Date parameter");
			    		return;
			    	}

			    	long timestamp;
					try {
						timestamp = DateUtil.parseDate(dateParam).getTime();
					} catch (DateParseException e) {
			    		response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
			    		return;
					}
			    	if (!isTimeValid(timestamp)) {
			    		response.sendError(HttpServletResponse.SC_FORBIDDEN);
			    		return;
			    	}

					// Fetch the Authorization parameter and find the user specified in it.
			    	if (auth == null) {
			    		response.sendError(HttpServletResponse.SC_FORBIDDEN, "No Authorization parameter");
			    		return;
			    	}
					String[] authParts = auth.split(" ");
					if (authParts.length != 2) {
			    		response.sendError(HttpServletResponse.SC_FORBIDDEN);
			    		return;
			    	}
					String username = authParts[0];
					String signature = authParts[1];
					User user = null;
					try {
						user = getService().findUser(username);
					} catch (RpcException e) {
			        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
						return;
					}
					if (user == null) {
			    		response.sendError(HttpServletResponse.SC_FORBIDDEN);
			    		return;
			    	}
					request.setAttribute(USER_ATTRIBUTE, user);

					// Remove the servlet path from the request URI.
					String p = request.getRequestURI();
					String servletPath = request.getContextPath() + request.getServletPath();
					p = p.substring(servletPath.length());
					// Validate the signature in the Authorization parameter.
					String data = request.getMethod() + dateParam + p;
					if (!isSignatureValid(signature, user, data)) {
			    		response.sendError(HttpServletResponse.SC_FORBIDDEN);
			    		return;
			    	}

					progressListener.setUserId(user.getId());
					progressListener.setFilename(fileName);
					String contentType = item.getContentType();

					try {
						uploadedFile = getService().uploadFile(stream, user.getId());
					} catch (IOException ex) {
						throw new GSSIOException(ex, false);
					}
					FileHeaderDTO fileDTO = null;
					if (file == null)
						fileDTO = getService().createFile(user.getId(), folder.getId(), fileName, contentType, uploadedFile.getCanonicalFile().length(), uploadedFile.getAbsolutePath());
					else
						fileDTO = getService().updateFileContents(user.getId(), file.getId(), contentType, uploadedFile.getCanonicalFile().length(), uploadedFile.getAbsolutePath());
					getService().updateAccounting(owner, new Date(), fileDTO.getFileSize());
					getService().removeFileUploadProgress(user.getId(), fileName);
				}
			}
			// We can't return 204 here since GWT's onSubmitComplete won't fire.
			response.setContentType("text/html");
            response.getWriter().print("<pre></pre>");
		} catch (FileUploadException e) {
			String error = "Error while uploading file";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
		} catch (GSSIOException e) {
			if (uploadedFile != null && uploadedFile.exists())
				uploadedFile.delete();
			String error = "Error while uploading file";
			if (e.logAsError())
				logger.error(error, e);
			else
				logger.debug(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
		} catch (DuplicateNameException e) {
			if (uploadedFile != null && uploadedFile.exists())
				uploadedFile.delete();
			String error = "The specified file name already exists in this folder";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_CONFLICT, error);

		} catch (InsufficientPermissionsException e) {
			if (uploadedFile != null && uploadedFile.exists())
				uploadedFile.delete();
			String error = "You don't have the necessary permissions";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, error);

		} catch (QuotaExceededException e) {
			if (uploadedFile != null && uploadedFile.exists())
				uploadedFile.delete();
			String error = "Not enough free space available";
			if (logger.isDebugEnabled())
				logger.debug(error, e);
			response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, error);

		} catch (ObjectNotFoundException e) {
			if (uploadedFile != null && uploadedFile.exists())
				uploadedFile.delete();
			String error = "A specified object was not found";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, error);
		} catch (RpcException e) {
			if (uploadedFile != null && uploadedFile.exists())
				uploadedFile.delete();
			String error = "An error occurred while communicating with the service";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
		}
	}

	private String getBackupFilename(FileHeaderDTO file, String filename){
		List<FileHeaderDTO> deletedFiles = new ArrayList<FileHeaderDTO>();
		try{
			deletedFiles = getService().getDeletedFiles(file.getOwner().getId());
		}
		catch(ObjectNotFoundException e){

		} catch (RpcException e) {

		}
		List<FileHeaderDTO> filesInSameFolder = new ArrayList<FileHeaderDTO>();
		for(FileHeaderDTO deleted : deletedFiles)
			if(deleted.getFolder().getId().equals(file.getFolder().getId()))
				filesInSameFolder.add(deleted);
		int i=1;
		String filenameToCheck = filename;
		for(FileHeaderDTO same : filesInSameFolder)
			if(same.getName().startsWith(filename)){
				String toCheck=same.getName().substring(filename.length(),same.getName().length());
				if(toCheck.startsWith(" ")){
					int test =-1;
					try{
						test = Integer.valueOf(toCheck.replace(" ",""));
					}
					catch(NumberFormatException e){
						//do nothing since string is not a number
					}
					if(test>=i)
						i = test+1;
				}
			}

		return filename+" "+i;
	}

	/**
	 * Move the resource in the specified path to the specified destination.
	 *
	 * @param req the HTTP request
	 * @param resp the HTTP response
	 * @param path the path of the resource
	 * @param moveTo the destination of the move procedure
	 * @throws IOException if an input/output error occurs
	 */
	private void moveResource(HttpServletRequest req, HttpServletResponse resp, String path, String moveTo) throws IOException {
		User user = getUser(req);
		User owner = getOwner(req);
		Object resource = null;
		try {
			resource = getService().getResourceAtPath(owner.getId(), path, true);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}

        String destination = null;
        User destOwner = null;
		boolean exists = true;
		try {
			destination = getDestinationPath(req, encodePath(moveTo));
			destination = URLDecoder.decode(destination, "UTF-8");
			destOwner = getDestinationOwner(req);
			getService().getResourceAtPath(destOwner.getId(), destination, true);
		} catch (ObjectNotFoundException e) {
			exists = false;
		} catch (URISyntaxException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, destination);
			return;
		}
		if (exists) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, destination + " already exists");
			return;
		}

		try {
			if (resource instanceof FolderDTO) {
				FolderDTO folder = (FolderDTO) resource;
				getService().moveFolderToPath(user.getId(), destOwner.getId(), folder.getId(), destination);
			} else {
				FileHeaderDTO file = (FileHeaderDTO) resource;
				getService().moveFileToPath(user.getId(), destOwner.getId(), file.getId(), destination);
			}
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, destination);
		} catch (DuplicateNameException e) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
		} catch (GSSIOException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (QuotaExceededException e) {
			resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, e.getMessage());
		}
	}

	/**
	 * Copy the resource in the specified path to the specified destination.
	 *
	 * @param req the HTTP request
	 * @param resp the HTTP response
	 * @param path the path of the resource
	 * @param copyTo the destination of the copy procedure
	 * @throws IOException if an input/output error occurs
	 */
	private void copyResource(HttpServletRequest req, HttpServletResponse resp, String path, String copyTo) throws IOException {
		User user = getUser(req);
		User owner = getOwner(req);
		Object resource = null;
		try {
			resource = getService().getResourceAtPath(owner.getId(), path, true);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}

        String destination = null;
        User destOwner = null;
		boolean exists = true;
		try {
			String destinationEncoded = getDestinationPath(req, encodePath(copyTo));
			destination = URLDecoder.decode(destinationEncoded, "UTF-8");
			destOwner = getDestinationOwner(req);
			getService().getResourceAtPath(destOwner.getId(), destinationEncoded, true);
		} catch (ObjectNotFoundException e) {
			exists = false;
		} catch (URISyntaxException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, destination);
			return;
		}
		if (exists) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, destination + " already exists");
			return;
		}

		try {
			if (resource instanceof FolderDTO) {
				FolderDTO folder = (FolderDTO) resource;
				getService().copyFolderStructureToPath(user.getId(), destOwner.getId(), folder.getId(), destination);
			} else {
				FileHeaderDTO file = (FileHeaderDTO) resource;
				getService().copyFileToPath(user.getId(), destOwner.getId(), file.getId(), destination);
			}
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, destination);
		} catch (DuplicateNameException e) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
		} catch (GSSIOException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (QuotaExceededException e) {
			resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, e.getMessage());
		}
	}

	private String encodePath(String path) throws UnsupportedEncodingException{
		StringTokenizer str = new StringTokenizer(path, "/:", true);
		String result = new String();
		while(str.hasMoreTokens()){
			String token = str.nextToken();
			if(!token.equals("/") && !token.equals(":"))
				token = URLEncoder.encode(token,"UTF-8");
			result = result + token;
		}
		return result;
	}
	/**
	 * A helper method that extracts the relative resource path,
	 * after removing the 'files' namespace.
	 * The path returned is <i>not</i> URL-decoded.
	 *
	 * @param req the HTTP request
	 * @param path the specified path
	 * @return the path relative to the root folder
	 * @throws URISyntaxException
	 * @throws RpcException in case an error occurs while communicating
	 * 						with the backend
	 * @throws UnsupportedEncodingException
	 */
	private String getDestinationPath(HttpServletRequest req, String path) throws URISyntaxException, RpcException, UnsupportedEncodingException {
		URI uri = new URI(path);
		String dest = uri.getRawPath();
		// Remove the context path from the destination URI.
		String contextPath = req.getContextPath();
		if (!dest.startsWith(contextPath))
			throw new URISyntaxException(dest, "Destination path does not start with " + contextPath);
		dest = dest.substring(contextPath.length());
		// Remove the servlet path from the destination URI.
		String servletPath = req.getServletPath();
		if (!dest.startsWith(servletPath))
			throw new URISyntaxException(dest, "Destination path does not start with " + servletPath);
		dest = dest.substring(servletPath.length());
    	// Strip the username part
		if (dest.length() < 2)
			throw new URISyntaxException(dest, "No username in the destination URI");
		int slash = dest.substring(1).indexOf('/');
		if (slash == -1)
			throw new URISyntaxException(dest, "No username in the destination URI");
		// Decode the user to get the proper characters (mainly the @)
		String owner = URLDecoder.decode(dest.substring(1, slash + 1), "UTF-8");
		User o;
		o = getService().findUser(owner);
		if (o == null)
			throw new URISyntaxException(dest, "User " + owner + " not found");

		req.setAttribute(DESTINATION_OWNER_ATTRIBUTE, o);
		dest = dest.substring(slash + 1);

		// Chop the resource namespace part
		dest = dest.substring(RequestHandler.PATH_FILES.length());

    	dest = dest.endsWith("/")? dest: dest + '/';
		return dest;
	}

	/**
	 * Move the resource in the specified path to the trash bin.
	 *
	 * @param req the HTTP request
	 * @param resp the HTTP response
	 * @param path the path of the resource
	 * @throws IOException if an input/output error occurs
	 */
	private void trashResource(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
		User user = getUser(req);
		User owner = getOwner(req);
		Object resource = null;
		try {
			resource = getService().getResourceAtPath(owner.getId(), path, true);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}

		try {
			if (resource instanceof FolderDTO) {
				FolderDTO folder = (FolderDTO) resource;
				getService().moveFolderToTrash(user.getId(), folder.getId());
			} else {
				FileHeaderDTO file = (FileHeaderDTO) resource;
				getService().moveFileToTrash(user.getId(), file.getId());
			}
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
		}
	}

	/**
	 * Restore the resource in the specified path from the trash bin.
	 *
	 * @param req the HTTP request
	 * @param resp the HTTP response
	 * @param path the path of the resource
	 * @throws IOException if an input/output error occurs
	 */
	private void restoreResource(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
		User user = getUser(req);
		User owner = getOwner(req);
		Object resource = null;
		try {
			resource = getService().getResourceAtPath(owner.getId(), path, false);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}

		try {
			if (resource instanceof FolderDTO) {
				FolderDTO folder = (FolderDTO) resource;
				getService().removeFolderFromTrash(user.getId(), folder.getId());
			} else {
				FileHeaderDTO file = (FileHeaderDTO) resource;
				getService().removeFileFromTrash(user.getId(), file.getId());
			}
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
		}
	}

	/**
	 * Update the resource in the specified path.
	 *
	 * @param req the HTTP request
	 * @param resp the HTTP response
	 * @param path the path of the resource
	 * @throws IOException if an input/output error occurs
	 */
	private void updateResource(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
		final User user = getUser(req);
		User owner = getOwner(req);
		Object resource = null;
		try {
			resource = getService().getResourceAtPath(owner.getId(), path, false);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}
		//use utf-8 encoding for reading request
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream(),"UTF-8"));
		StringBuffer input = new StringBuffer();
		String line = null;
		JSONObject json = null;
		while ((line = reader.readLine()) != null)
			input.append(line);
		reader.close();
		try {
			json = new JSONObject(input.toString());
			if (logger.isDebugEnabled())
				logger.debug("JSON update: " + json);
			if (resource instanceof FolderDTO) {
				final FolderDTO folder = (FolderDTO) resource;
				String name = json.optString("name");
				if (!name.isEmpty()){
					try {
						name = URLDecoder.decode(name, "UTF-8");
					} catch (IllegalArgumentException e) {
						resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
						return;
					}
					final String fName = name;
					FolderDTO folderUpdated = new TransactionHelper<FolderDTO>().tryExecute(new Callable<FolderDTO>() {
						@Override
						public FolderDTO call() throws Exception {
							return getService().modifyFolder(user.getId(), folder.getId(), fName);
						}

					});
					String parentUrl =URLDecoder.decode(getContextPath(req, true),"UTF-8");
					String fpath = URLDecoder.decode(req.getPathInfo(), "UTF-8");
					parentUrl = parentUrl.replaceAll(fpath, "");
					if(!parentUrl.endsWith("/"))
						parentUrl = parentUrl+"/";
					parentUrl = parentUrl+folderUpdated.getOwner().getUsername()+PATH_FILES+folderUpdated.getPath();
					resp.getWriter().println(parentUrl);
				}

				JSONArray permissions = json.optJSONArray("permissions");
				if (permissions != null) {
					final Set<PermissionDTO> perms = parsePermissions(user, permissions);
					new TransactionHelper<Object>().tryExecute(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							getService().setFolderPermissions(user.getId(), folder.getId(), perms);
							return null;
						}

					});
				}
			} else {
				final FileHeaderDTO file = (FileHeaderDTO) resource;
				String name = null;
				if (json.opt("name") != null)
					name = json.optString("name");
				JSONArray tagset = json.optJSONArray("tags");
				String tags= null;
				StringBuffer t = new StringBuffer();
				if (tagset != null) {
					for (int i = 0; i < tagset.length(); i++)
						t.append(tagset.getString(i) + ',');
					tags = t.toString();
				}
				if (name != null || tags != null) {
					final String fName = name;
					final String fTags = tags;
					new TransactionHelper<Object>().tryExecute(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							getService().updateFile(user.getId(), file.getId(), fName, fTags);
							return null;
						}

					});
				}

				JSONArray permissions = json.optJSONArray("permissions");
				Set<PermissionDTO> perms = null;
				if (permissions != null)
					perms = parsePermissions(user, permissions);
				Boolean readForAll = null;
				if (json.opt("readForAll") != null)
					readForAll = json.optBoolean("readForAll");
				if (perms != null || readForAll != null) {
					final Boolean fReadForAll = readForAll;
					final Set<PermissionDTO> fPerms = perms;
					new TransactionHelper<Object>().tryExecute(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							getService().setFilePermissions(user.getId(), file.getId(), fReadForAll, fPerms);
							return null;
						}

					});
				}

				if (json.opt("versioned") != null) {
					final boolean versioned = json.getBoolean("versioned");
					new TransactionHelper<Object>().tryExecute(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							getService().toggleFileVersioning(user.getId(), file.getId(), versioned);
							return null;
						}

					});
				}
			}
		} catch (JSONException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} catch (DuplicateNameException e) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}
	}

	/**
	 * Helper method to convert a JSON array of permissions into a set of
	 * PermissionDTO objects.
	 *
	 * @param user the current user
	 * @param permissions the JSON array to parse
	 * @return the parsed set of permissions
	 * @throws JSONException if there was an error parsing the JSON object
	 * @throws RpcException if there was an error communicating with the EJB
	 * @throws ObjectNotFoundException if the user could not be found
	 */
	private Set<PermissionDTO> parsePermissions(User user, JSONArray permissions)
			throws JSONException, RpcException, ObjectNotFoundException {
		if (permissions == null)
			return null;
		Set<PermissionDTO> perms = new HashSet<PermissionDTO>();
		for (int i = 0; i < permissions.length(); i++) {
			JSONObject j = permissions.getJSONObject(i);
			PermissionDTO perm = new PermissionDTO();
			perm.setModifyACL(j.optBoolean("modifyACL"));
			perm.setRead(j.optBoolean("read"));
			perm.setWrite(j.optBoolean("write"));
			String permUser = j.optString("user");
			if (!permUser.isEmpty()) {
				User u = getService().findUser(permUser);
				if (u == null)
					throw new ObjectNotFoundException("User " + permUser + " not found");
				perm.setUser(u.getDTO());
			}
			String permGroup = j.optString("group");
			if (!permGroup.isEmpty()) {
				GroupDTO g = getService().getGroup(user.getId(), permGroup);
				perm.setGroup(g);
			}
			if (permUser.isEmpty() && permGroup.isEmpty() ||
						permUser.isEmpty() && permGroup.isEmpty())
				throw new JSONException("A permission must correspond to either a user or a group");
			perms.add(perm);
		}
		return perms;
	}

	/**
	 * Creates a new folder with the specified name under the folder in the provided path.
	 *
	 * @param req the HTTP request
	 * @param resp the HTTP response
	 * @param path the parent folder path
	 * @param folderName the name of the new folder
	 * @throws IOException if an input/output error occurs
	 */
	private void createFolder(HttpServletRequest req, HttpServletResponse resp, String path, final String folderName) throws IOException {
		if (logger.isDebugEnabled())
   			logger.debug("Creating folder " + folderName + " in '" + path);

    	final User user = getUser(req);
    	User owner = getOwner(req);
        boolean exists = true;
        try {
        	getService().getResourceAtPath(owner.getId(), path + folderName, false);
        } catch (ObjectNotFoundException e) {
            exists = false;
        } catch (RpcException e) {
        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path + folderName);
			return;
		}

        if (exists) {
            resp.addHeader("Allow", METHOD_GET + ", " + METHOD_DELETE +
            			", " + METHOD_HEAD);
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

		Object parent;
		try {
			parent = getService().getResourceAtPath(owner.getId(), path, true);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_CONFLICT);
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path + folderName);
			return;
		}
		try {
			if (parent instanceof FolderDTO) {
				final FolderDTO folder = (FolderDTO) parent;
				FolderDTO newFolder = new TransactionHelper<FolderDTO>().tryExecute(new Callable<FolderDTO>() {
					@Override
					public FolderDTO call() throws Exception {
						return getService().createFolder(user.getId(), folder.getId(), folderName);
					}

				});
	        	String newResource = getApiRoot() + newFolder.getURI();
	        	resp.setHeader("Location", newResource);
	        	resp.setContentType("text/plain");
	    	    PrintWriter out = resp.getWriter();
	    	    out.println(newResource);
			} else {
				resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	    		return;
			}
		} catch (DuplicateNameException e) {
			resp.sendError(HttpServletResponse.SC_CONFLICT);
    		return;
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    		return;
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_CONFLICT);
			return;
		} catch (RpcException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path + folderName);
			return;
		} catch (Exception e) {
        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}
    	resp.setStatus(HttpServletResponse.SC_CREATED);
	}

	/**
	 * @param req
	 * @param resp
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	void putResource(HttpServletRequest req, HttpServletResponse resp) throws IOException, FileNotFoundException {
        String path = getInnerPath(req, PATH_FILES);
		try {
	    	path = URLDecoder.decode(path, "UTF-8");
		} catch (IllegalArgumentException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
    	if (logger.isDebugEnabled())
   			logger.debug("Updating resource: " + path);

    	final User user = getUser(req);
    	User owner = getOwner(req);
    	boolean exists = true;
        Object resource = null;
        FileHeaderDTO file = null;
        try {
        	resource = getService().getResourceAtPath(owner.getId(), path, false);
        } catch (ObjectNotFoundException e) {
            exists = false;
        } catch (RpcException e) {
        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}

        if (exists)
			if (resource instanceof FileHeaderDTO)
    			file = (FileHeaderDTO) resource;
			else {
	        	resp.sendError(HttpServletResponse.SC_CONFLICT, path + " is a folder");
	    		return;
	        }
        boolean result = true;

        // Temporary content file used to support partial PUT.
        File contentFile = null;

        Range range = parseContentRange(req, resp);

        InputStream resourceInputStream = null;

        // Append data specified in ranges to existing content for this
        // resource - create a temporary file on the local filesystem to
        // perform this operation.
        // Assume just one range is specified for now
        if (range != null) {
            try {
				contentFile = executePartialPut(req, range, path);
			} catch (RpcException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
				return;
			} catch (ObjectNotFoundException e) {
				resp.sendError(HttpServletResponse.SC_CONFLICT);
        		return;
			} catch (InsufficientPermissionsException e) {
				resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        		return;
			}
            resourceInputStream = new FileInputStream(contentFile);
        } else
			resourceInputStream = req.getInputStream();

        try {
        	FolderDTO folder = null;
        	Object parent = getService().getResourceAtPath(owner.getId(), getParentPath(path), true);
        	if (!(parent instanceof FolderDTO)) {
        		resp.sendError(HttpServletResponse.SC_CONFLICT);
        		return;
        	}
       		folder = (FolderDTO) parent;
        	final String name = getLastElement(path);
        	final String mimeType = context.getMimeType(name);
        	File uploadedFile = null;
        	try {
				uploadedFile = getService().uploadFile(resourceInputStream, user.getId());
			} catch (IOException ex) {
				throw new GSSIOException(ex, false);
			}
        	FileHeaderDTO fileDTO = null;
            if (exists)
            	fileDTO = getService().updateFileContents(user.getId(), file.getId(), mimeType, uploadedFile.getCanonicalFile().length(), uploadedFile.getAbsolutePath());
			else {
				final File uploadedf = uploadedFile;
				final FolderDTO parentf = folder;
				fileDTO = new TransactionHelper<FileHeaderDTO>().tryExecute(new Callable<FileHeaderDTO>() {
					@Override
					public FileHeaderDTO call() throws Exception {
						return getService().createFile(user.getId(), parentf.getId(), name, mimeType, uploadedf.getCanonicalFile().length(), uploadedf.getAbsolutePath());
					}

				});
			}
            getService().updateAccounting(owner, new Date(), fileDTO.getFileSize());
			getService().removeFileUploadProgress(user.getId(), fileDTO.getName());
        } catch(ObjectNotFoundException e) {
            result = false;
        } catch (RpcException e) {
        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
        } catch (IOException e) {
        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		} catch (GSSIOException e) {
        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		} catch (DuplicateNameException e) {
			resp.sendError(HttpServletResponse.SC_CONFLICT);
    		return;
		} catch (InsufficientPermissionsException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    		return;
		} catch (QuotaExceededException e) {
			resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, e.getMessage());
    		return;
		} catch (Exception e) {
        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path);
			return;
		}

        if (result) {
            if (exists)
				resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			else
				resp.setStatus(HttpServletResponse.SC_CREATED);
        } else
			resp.sendError(HttpServletResponse.SC_CONFLICT);
	}

    /**
     * Delete a resource.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
	 * @throws IOException if the response cannot be sent
     */
    void deleteResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = getInnerPath(req, PATH_FILES);
    	if (logger.isDebugEnabled())
   			logger.debug("Deleting resource '" + path);
    	path = URLDecoder.decode(path, "UTF-8");
    	User user = getUser(req);
    	User owner = getOwner(req);
    	boolean exists = true;
    	Object object = null;
    	try {
    		object = getService().getResourceAtPath(owner.getId(), path, false);
    	} catch (ObjectNotFoundException e) {
    		exists = false;
    	} catch (RpcException e) {
    		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

    	if (!exists) {
    		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    		return;
    	}

    	FolderDTO folder = null;
    	FileHeaderDTO file = null;
    	if (object instanceof FolderDTO)
    		folder = (FolderDTO) object;
    	else
    		file = (FileHeaderDTO) object;

    	if (file != null)
			try {
				getService().deleteFile(user.getId(), file.getId());
	        } catch (InsufficientPermissionsException e) {
	        	resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				return;
    		} catch (ObjectNotFoundException e) {
    			// Although we had already found the object, it was
    			// probably deleted from another thread.
    			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    			return;
    		} catch (RpcException e) {
    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
    		}
		else if (folder != null)
			try {
    			getService().deleteFolder(user.getId(), folder.getId());
	        } catch (InsufficientPermissionsException e) {
	        	resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	        	return;
    		} catch (ObjectNotFoundException e) {
	        	resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        	return;
    		} catch (RpcException e) {
	        	resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        	return;
    		}
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    	return;
    }

	/**
     * Return an InputStream to a JSON representation of the contents
     * of this directory.
     *
	 * @param user the user that made the request
     * @param folder the specified directory
     * @return an input stream with the rendered contents
	 * @throws IOException if the response cannot be sent
     * @throws ServletException
	 * @throws InsufficientPermissionsException if the user does not have
	 * 			the necessary privileges to read the directory
     */
    private InputStream renderJson(User user, FolderDTO folder) throws IOException,
    		ServletException, InsufficientPermissionsException {
    	JSONObject json = new JSONObject();
    	try {
			json.put("name", folder.getName()).
					put("owner", folder.getOwner().getUsername()).
					put("createdBy", folder.getAuditInfo().getCreatedBy().getUsername()).
					put("creationDate", folder.getAuditInfo().getCreationDate().getTime()).
					put("deleted", folder.isDeleted());
			if (folder.getAuditInfo().getModifiedBy() != null)
				json.put("modifiedBy", folder.getAuditInfo().getModifiedBy().getUsername()).
						put("modificationDate", folder.getAuditInfo().getModificationDate().getTime());
			if (folder.getParent() != null) {
				JSONObject j = new JSONObject();
				j.put("uri", getApiRoot() + folder.getParent().getURI());
				j.put("name", folder.getParent().getName());
				json.put("parent", j);
			}
	    	List<JSONObject> subfolders = new ArrayList<JSONObject>();
	    	for (FolderDTO f: folder.getSubfolders())
				if (!f.isDeleted()) {
					JSONObject j = new JSONObject();
					j.put("name", f.getName()).
						put("uri", getApiRoot() + f.getURI());
					subfolders.add(j);
				}
	    	json.put("folders", subfolders);
	    	List<JSONObject> files = new ArrayList<JSONObject>();
	    	List<FileHeaderDTO> fileHeaders = getService().getFiles(user.getId(), folder.getId(), false);
	    	for (FileHeaderDTO f: fileHeaders) {
	    		JSONObject j = new JSONObject();
				j.put("name", f.getName()).
					put("owner", f.getOwner().getUsername()).
					put("deleted", f.isDeleted()).
					put("version", f.getVersion()).
					put("content", f.getMimeType()).
					put("size", f.getFileSize()).
					put("creationDate", f.getAuditInfo().getCreationDate().getTime()).
					put("path", f.getFolder().getPath()).
					put("uri", getApiRoot() + f.getURI());
				if (f.getAuditInfo().getModificationDate() != null)
					j.put("modificationDate", f.getAuditInfo().getModificationDate().getTime());
				files.add(j);
	    	}
	    	json.put("files", files);
	    	Set<PermissionDTO> perms = getService().getFolderPermissions(user.getId(), folder.getId());
	    	json.put("permissions", renderJson(perms));
		} catch (JSONException e) {
			throw new ServletException(e);
		} catch (ObjectNotFoundException e) {
			throw new ServletException(e);
		} catch (RpcException e) {
			throw new ServletException(e);
		}

    	// Prepare a writer to a buffered area
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	OutputStreamWriter osWriter = new OutputStreamWriter(stream, "UTF8");
    	PrintWriter writer = new PrintWriter(osWriter);

    	// Return an input stream to the underlying bytes
    	writer.write(json.toString());
    	writer.flush();
    	return new ByteArrayInputStream(stream.toByteArray());
    }

	/**
     * Return a String with a JSON representation of the metadata
     * of the specified folder.
	 * @throws RpcException
	 * @throws InsufficientPermissionsException
	 * @throws ObjectNotFoundException
     */
    private String renderJsonMetadata(User user, FolderDTO folder)
    		throws ServletException, InsufficientPermissionsException {
    	// Check if the user has read permission.
		try {
			if (!getService().canReadFolder(user.getId(), folder.getId()))
				throw new InsufficientPermissionsException();
		} catch (ObjectNotFoundException e) {
			throw new ServletException(e);
		} catch (RpcException e) {
			throw new ServletException(e);
		}

    	JSONObject json = new JSONObject();
    	try {
			json.put("name", folder.getName()).
			put("owner", folder.getOwner().getUsername()).
			put("createdBy", folder.getAuditInfo().getCreatedBy().getUsername()).
			put("creationDate", folder.getAuditInfo().getCreationDate().getTime()).
			put("deleted", folder.isDeleted());
			if (folder.getAuditInfo().getModifiedBy() != null)
				json.put("modifiedBy", folder.getAuditInfo().getModifiedBy().getUsername()).
						put("modificationDate", folder.getAuditInfo().getModificationDate().getTime());
		} catch (JSONException e) {
			throw new ServletException(e);
		}
    	return json.toString();
    }

	/**
     * Return a String with a JSON representation of the metadata
     * of the specified file. If an old file body is provided, then
     * the metadata of that particular version will be returned.
     *
	 * @param user the user that made the request
     * @param file the specified file header
     * @param oldBody the version number
     * @return the JSON-encoded file
     * @throws ServletException
	 * @throws InsufficientPermissionsException if the user does not have
	 * 			the necessary privileges to read the directory
     */
    private String renderJson(User user, FileHeaderDTO file, FileBodyDTO oldBody)
    		throws ServletException, InsufficientPermissionsException {
    	JSONObject json = new JSONObject();
    	try {
    		// Need to encode file name in order to properly display it in the web client.
			json.put("name", URLEncoder.encode(file.getName(),"UTF-8")).
					put("owner", file.getOwner().getUsername()).
					put("versioned", file.isVersioned()).
					put("version", oldBody != null ? oldBody.getVersion() : file.getVersion()).
					put("readForAll", file.isReadForAll()).
					put("tags", renderJson(file.getTags())).
					put("path", file.getFolder().getPath()).
    				put("uri", getApiRoot() + file.getURI()).
					put("deleted", file.isDeleted());
			JSONObject j = new JSONObject();
			j.put("uri", getApiRoot() + file.getFolder().getURI()).
					put("name", URLEncoder.encode(file.getFolder().getName(),"UTF-8"));
			json.put("folder", j);
			if (oldBody != null)
				json.put("createdBy", oldBody.getAuditInfo().getCreatedBy().getUsername()).
						put("creationDate", oldBody.getAuditInfo().getCreationDate().getTime()).
						put("modifiedBy", oldBody.getAuditInfo().getModifiedBy().getUsername()).
						put("modificationDate", oldBody.getAuditInfo().getModificationDate().getTime()).
						put("content", oldBody.getMimeType()).
						put("size", oldBody.getFileSize());
			else
				json.put("createdBy", file.getAuditInfo().getCreatedBy().getUsername()).
						put("creationDate", file.getAuditInfo().getCreationDate().getTime()).
						put("modifiedBy", file.getAuditInfo().getModifiedBy().getUsername()).
						put("modificationDate", file.getAuditInfo().getModificationDate().getTime()).
						put("content", file.getMimeType()).
						put("size", file.getFileSize());
	    	Set<PermissionDTO> perms = getService().getFilePermissions(user.getId(), file.getId());
	    	json.put("permissions", renderJson(perms));
		} catch (JSONException e) {
			throw new ServletException(e);
		} catch (ObjectNotFoundException e) {
			throw new ServletException(e);
		} catch (RpcException e) {
			throw new ServletException(e);
		} catch (UnsupportedEncodingException e) {
			throw new ServletException(e);
		}

    	return json.toString();
    }

	/**
	 * Return a String with a JSON representation of the
	 * specified set of permissions.
     *
	 * @param permissions the set of permissions
	 * @return the JSON-encoded object
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	private JSONArray renderJson(Set<PermissionDTO> permissions) throws JSONException, UnsupportedEncodingException {
		JSONArray perms = new JSONArray();
		for (PermissionDTO p: permissions) {
			JSONObject permission = new JSONObject();
			permission.put("read", p.hasRead()).put("write", p.hasWrite()).put("modifyACL", p.hasModifyACL());
			if (p.getUser() != null)
				permission.put("user", p.getUser().getUsername());
			if (p.getGroup() != null)
				permission.put("group", URLEncoder.encode(p.getGroup().getName(),"UTF-8"));
			perms.put(permission);
		}
		return perms;
	}

	/**
	 * Return a String with a JSON representation of the
	 * specified collection of tags.
     *
	 * @param tags the collection of tags
	 * @return the JSON-encoded object
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	private JSONArray renderJson(Collection<String> tags) throws JSONException, UnsupportedEncodingException {
		JSONArray tagArray = new JSONArray();
		for (String t: tags)
			tagArray.put(URLEncoder.encode(t,"UTF-8"));
		return tagArray;
	}

	/**
	 * Retrieves the user who owns the destination namespace, for a
	 * copy or move request.
	 *
	 * @param req the HTTP request
	 * @return the owner of the namespace
	 */
	protected User getDestinationOwner(HttpServletRequest req) {
		return (User) req.getAttribute(DESTINATION_OWNER_ATTRIBUTE);
	}

	/**
	 * A helper inner class for updating the progress status of a file upload.
	 *
	 * @author kman
	 */
	public static class StatusProgressListener implements ProgressListener {
		private int percentLogged = 0;
		private long bytesTransferred = 0;

		private long fileSize = -100;

		private long tenKBRead = -1;

		private Long userId;

		private String filename;

		private ExternalAPI service;

		public StatusProgressListener(ExternalAPI aService) {
			service = aService;
		}

		/**
		 * Modify the userId.
		 *
		 * @param aUserId the userId to set
		 */
		public void setUserId(Long aUserId) {
			userId = aUserId;
		}

		/**
		 * Modify the filename.
		 *
		 * @param aFilename the filename to set
		 */
		public void setFilename(String aFilename) {
			filename = aFilename;
		}

		public void update(long bytesRead, long contentLength, int items) {
			//monitoring per percent of bytes uploaded
			bytesTransferred = bytesRead;
			if (fileSize != contentLength)
				fileSize = contentLength;
			int percent = new Long(bytesTransferred * 100 / fileSize).intValue();

			if (percent < 5 || percent % TRACK_PROGRESS_PERCENT == 0 )
				if (percent != percentLogged){
					percentLogged = percent;
					try {
						if (userId != null && filename != null)
							service.createFileUploadProgress(userId, filename, bytesTransferred, fileSize);
					} catch (ObjectNotFoundException e) {
						// Swallow the exception since it is going to be caught
						// by previously called methods
					}
				}
		}
	}
}
