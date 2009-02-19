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
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.webdav.Webdav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The servlet that handles requests for the REST API.
 *
 * @author past
 */
public class RequestHandler extends Webdav {
	/**
	 * The request attribute containing the flag that will be used to indicate an
	 * authentication bypass has occurred. We will have to check for authentication
	 * later. This is a shortcut for dealing with publicly-readable files.
	 */
	protected static final String AUTH_DEFERRED_ATTR = "authDeferred";

	/**
	 * The path for the search subsystem.
	 */
	protected static final String PATH_SEARCH = "/search";

	/**
	 * The path for the resource manipulation subsystem.
	 */
	protected static final String PATH_FILES = FileHeaderDTO.PATH_FILES;

	/**
	 * The path for the trash virtual folder.
	 */
	protected static final String PATH_TRASH = "/trash";

	/**
	 * The path for the subsystem that deals with the user attributes.
	 */
	protected static final String PATH_GROUPS = "/groups";

	/**
	 * The path for the shared resources virtual folder.
	 */
	protected static final String PATH_SHARED = "/shared";

	/**
	 * The path for the other users' shared resources virtual folder.
	 */
	protected static final String PATH_OTHERS = "/others";

	/**
	 * The path for tags created by the user.
	 */
	protected static final String PATH_TAGS = "/tags";

	/**
	 * The GSS-specific header for the request timestamp.
	 */
	private static final String GSS_DATE_HEADER = "X-GSS-Date";

	/**
	 * The RFC 2616 date header.
	 */
	private static final String DATE_HEADER = "Date";

	/**
	 * The Authorization HTTP header.
	 */
	private static final String AUTHORIZATION_HEADER = "Authorization";

	/**
	 * The group parameter name.
	 */
	protected static final String GROUP_PARAMETER = "name";

	/**
	 * The username parameter name.
	 */
	protected static final String USERNAME_PARAMETER = "name";

	/**
	 * The "new folder name" parameter name.
	 */
	protected static final String NEW_FOLDER_PARAMETER = "new";

	/**
	 * The resource update parameter name.
	 */
	protected static final String RESOURCE_UPDATE_PARAMETER = "update";

	/**
	 * The resource trash parameter name.
	 */
	protected static final String RESOURCE_TRASH_PARAMETER = "trash";

	/**
	 * The resource restore parameter name.
	 */
	protected static final String RESOURCE_RESTORE_PARAMETER = "restore";

	/**
	 * The resource copy parameter name.
	 */
	protected static final String RESOURCE_COPY_PARAMETER = "copy";

	/**
	 * The resource move parameter name.
	 */
	protected static final String RESOURCE_MOVE_PARAMETER = "move";

	/**
	 * The HMAC-SHA1 hash name.
	 */
	private static final String HMAC_SHA1 = "HmacSHA1";

	/**
	 * The amount of milliseconds a request's timestamp may differ
	 * from the current system time.
	 */
	private static final int TIME_SKEW = 600000;

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(RequestHandler.class);

	/**
	 * Create a mapping between paths and allowed HTTP methods for fast lookup.
	 */
	private final Map<String, String> methodsAllowed = new HashMap<String, String>(7);

	@Override
	public void init() throws ServletException {
		super.init();
		methodsAllowed.put(PATH_FILES, METHOD_GET + ", " + METHOD_POST +
					", " + METHOD_DELETE + ", " + METHOD_PUT + ", " + METHOD_HEAD);
		methodsAllowed.put(PATH_GROUPS, METHOD_GET + ", " + METHOD_POST +
					", " + METHOD_DELETE);
		methodsAllowed.put(PATH_OTHERS, METHOD_GET);
		methodsAllowed.put(PATH_SEARCH, METHOD_GET);
		methodsAllowed.put(PATH_SHARED, METHOD_GET);
		methodsAllowed.put(PATH_TAGS, METHOD_GET);
		methodsAllowed.put(PATH_TRASH, METHOD_GET + ", " + METHOD_POST +
					", " + METHOD_DELETE);
	}

	@Override
	public void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        String method = request.getMethod();
		String path = getRelativePath(request);

		if (logger.isDebugEnabled())
            logger.debug("[" + method + "] " + path);

		if (!isRequestValid(request)) {
			if (!method.equals(METHOD_GET) && !method.equals(METHOD_HEAD)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			// Raise a flag to indicate we will have to check for
			// authentication later. This is a shortcut for dealing
			// with publicly-readable files.
			request.setAttribute(AUTH_DEFERRED_ATTR, true);
		}

		// Dispatch to the appropriate method handler.
		if (method.equals(METHOD_GET))
			doGet(request, response);
		else if (method.equals(METHOD_POST))
			doPost(request, response);
		else if (method.equals(METHOD_PUT))
			doPut(request, response);
		else if (method.equals(METHOD_DELETE))
			doDelete(request, response);
		else if (method.equals(METHOD_HEAD))
			doHead(request, response);
		else
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		boolean authDeferred = getAuthDeferred(req);
    	// Strip the username part
    	String path;
		try {
			path = getUserPath(req);
		} catch (ObjectNotFoundException e) {
			if (authDeferred) {
				// We do not want to leak information if the request
				// was not authenticated.
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			return;
		}
		if (authDeferred && !path.startsWith(PATH_FILES)) {
			// Only files may be open to the public.
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		if (path.startsWith(PATH_GROUPS)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_GROUPS));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_OTHERS)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_OTHERS));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_SEARCH)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_SEARCH));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_SHARED)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_SHARED));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_TAGS)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_TAGS));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_TRASH)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_TRASH));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_FILES))
			// Serve the requested resource, without the data content
			new FilesHandler(getServletContext()).serveResource(req, resp, false);
		else
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, req.getRequestURI());
	}

	/**
	 * Handle storing and updating file resources.
	 *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
	 * @throws IOException if the response cannot be sent
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// TODO: fix code duplication between doPut() and Webdav.doPut()
    	// Strip the username part
    	String path;
		try {
			path = getUserPath(req);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			return;
		}

    	if (path.startsWith(PATH_GROUPS)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_GROUPS));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_OTHERS)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_OTHERS));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_SEARCH)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_SEARCH));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_SHARED)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_SHARED));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_TAGS)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_TAGS));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_TRASH)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_TRASH));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_FILES))
			new FilesHandler(getServletContext()).putResource(req, resp);
		else
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, req.getRequestURI());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		boolean authDeferred = getAuthDeferred(req);
    	// Strip the username part
    	String path;
		try {
			path = getUserPath(req);
		} catch (ObjectNotFoundException e) {
			if (authDeferred) {
				// We do not want to leak information if the request
				// was not authenticated.
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			return;
		}
		if (authDeferred && !path.startsWith(PATH_FILES)) {
			// Only files may be open to the public.
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

    	// Dispatch according to the specified namespace
    	if (path.equals("") || path.equals("/"))
			new UserHandler().serveUser(req, resp);
		else if (path.startsWith(PATH_FILES))
			// Serve the requested resource, including the data content
   			new FilesHandler(getServletContext()).serveResource(req, resp, true);
		else if (path.startsWith(PATH_TRASH))
			new TrashHandler().serveTrash(req, resp);
		else if (path.startsWith(PATH_SEARCH))
			new SearchHandler().serveSearchResults(req, resp);
		else if (path.startsWith(PATH_GROUPS))
			new GroupsHandler().serveGroups(req, resp);
		else if (path.startsWith(PATH_SHARED))
			new SharedHandler().serveShared(req, resp);
		else if (path.startsWith(PATH_OTHERS))
			new OthersHandler().serveOthers(req, resp);
		else if (path.startsWith(PATH_TAGS))
			new TagsHandler().serveTags(req, resp);
		else
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, req.getRequestURI());
	}

    /**
     * Handle a Delete request.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are processing
	 * @throws IOException if the response cannot be sent
     */
    @Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
    		throws IOException {
    	// Strip the username part
    	String path;
		try {
			path = getUserPath(req);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			return;
		}

		if (path.startsWith(PATH_OTHERS)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_OTHERS));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_SEARCH)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_SEARCH));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_SHARED)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_SHARED));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_TAGS)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_TAGS));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_GROUPS))
			new GroupsHandler().deleteGroup(req, resp);
		else if (path.startsWith(PATH_TRASH)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_TRASH));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_FILES))
			new FilesHandler(getServletContext()).deleteResource(req, resp);
		else
    		resp.sendError(HttpServletResponse.SC_NOT_FOUND, req.getRequestURI());
    }

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	// Strip the username part
    	String path;
		try {
			path = getUserPath(req);
		} catch (ObjectNotFoundException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			return;
		}

		if (path.startsWith(PATH_OTHERS)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_OTHERS));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_SEARCH)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_SEARCH));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_SHARED)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_SHARED));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_TAGS)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_TAGS));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_GROUPS))
			new GroupsHandler().postGroup(req, resp);
		else if (path.startsWith(PATH_TRASH)) {
            resp.addHeader("Allow", methodsAllowed.get(PATH_TRASH));
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} else if (path.startsWith(PATH_FILES))
			new FilesHandler(getServletContext()).postResource(req, resp);
		else
    		resp.sendError(HttpServletResponse.SC_NOT_FOUND, req.getRequestURI());
	}

	/**
	 * Return the path inside the user namespace.
	 *
	 * @param req the HTTP request
	 * @return the path after the username part has been removed
	 * @throws ObjectNotFoundException if the namespace owner was not found
	 */
	private String getUserPath(HttpServletRequest req) throws ObjectNotFoundException {
        String path = getRelativePath(req);
		if (path.length() < 2)
			return path;
		int slash = path.substring(1).indexOf('/');
		if (slash == -1)
			return path;
		String owner = path.substring(1, slash + 1);
		User o;
		try {
			o = getService().findUser(owner);
		} catch (RpcException e) {
			logger.error("", e);
			throw new ObjectNotFoundException("User " + owner + " not found, due to internal server error");
		}
		if (o != null) {
			req.setAttribute(OWNER_ATTRIBUTE, o);
			return path.substring(slash + 1);
		}
		if (!path.startsWith(PATH_SEARCH))
			throw new ObjectNotFoundException("User " + owner + " not found");
		return path;
	}

	/**
	 * Retrieve the request context path with or without a trailing slash
	 * according to the provided argument.
	 *
	 * @param req the HTTP request
	 * @param withTrailingSlash a flag that denotes whether the path should
	 * 			end with a slash
	 * @return the context path
	 */
	protected String getContextPath(HttpServletRequest req, boolean withTrailingSlash) {
		String contextPath = req.getRequestURL().toString();
		if (withTrailingSlash)
			return contextPath.endsWith("/")? contextPath: contextPath + '/';
		return contextPath.endsWith("/")? contextPath.substring(0, contextPath.length()-1): contextPath;

	}

	/**
	 * @param req
	 * @param resp
	 * @param json
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	protected void sendJson(HttpServletRequest req, HttpServletResponse resp, String json) throws UnsupportedEncodingException, IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	OutputStreamWriter osWriter = new OutputStreamWriter(stream, "UTF8");
    	PrintWriter writer = new PrintWriter(osWriter);

    	writer.write(json);
    	writer.flush();

    	resp.setContentType("text/html;charset=UTF-8");
    	resp.setBufferSize(output);
		try {
			copy(null, new ByteArrayInputStream(stream.toByteArray()), resp.getOutputStream(), req, null);
		} catch (ObjectNotFoundException e) {
			// This should never happen with a null first parameter.
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		} catch (InsufficientPermissionsException e) {
			// This should never happen with a null first parameter.
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		} catch (RpcException e) {
			logger.error("", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}

	/**
	 * Retrieve the path to the requested resource after removing the user namespace
	 * part and the subsequent namespace part that differentiates resources like files,
	 * groups, trash, etc.
	 *
	 * @param req the HTTP request
	 * @param namespace the subnamespace
	 * @return the inner path
	 */
	protected String getInnerPath(HttpServletRequest req, String namespace) {
    	// Strip the username part
    	String path;
		try {
			path = getUserPath(req);
		} catch (ObjectNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
		// Chop the resource namespace part
    	path = path.substring(namespace.length());
		return path;
	}

	/**
	 * Confirms the validity of the request.
	 *
	 * @param request the incoming HTTP request
	 * @return true if the request is valid, false otherwise
	 */
	private boolean isRequestValid(HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			Enumeration headers = request.getHeaderNames();
			while (headers.hasMoreElements()) {
				String h = (String) headers.nextElement();
					logger.debug(h + ": " +	request.getHeader(h));
			}
		}
		// Fetch the timestamp used to guard against replay attacks.
		long timestamp = 0;
		boolean useGssDateHeader = true;
		try {
			timestamp = request.getDateHeader(GSS_DATE_HEADER);
			if (timestamp == -1) {
				useGssDateHeader = false;
				timestamp = request.getDateHeader(DATE_HEADER);
			}
		} catch (IllegalArgumentException e) {
			return false;
		}
		if (timestamp == -1)
			return false;
		Calendar cal = Calendar.getInstance();
		if (logger.isDebugEnabled())
			logger.debug("Time: server=" + cal.getTimeInMillis() + ", client=" + timestamp);
		// Ignore the request if the timestamp is too far off.
		if (Math.abs(timestamp - cal.getTimeInMillis()) > TIME_SKEW)
			return false;
		String auth = request.getHeader(AUTHORIZATION_HEADER);
		String[] authParts = auth.split(" ");
		if (authParts.length != 2)
			return false;
		String username = authParts[0];
		String signature = authParts[1];
		User user = null;
		try {
			user = getService().findUser(username);
		} catch (RpcException e) {
			return false;
		}
		if (user == null)
			return false;

		request.setAttribute(USER_ATTRIBUTE, user);
		String dateHeader = useGssDateHeader? request.getHeader(GSS_DATE_HEADER):
				request.getHeader(DATE_HEADER);
		String data;
		try {
			data = request.getMethod() + dateHeader + URLEncoder.encode(request.getPathInfo(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		if (logger.isDebugEnabled())
			logger.debug("server pre-signing data: "+data);
		String serverSignature = null;
		// If the authentication token is not valid, the user must get another one.
		if (user.getAuthToken() == null)
			return false;
		// Get an HMAC-SHA1 key from the authentication token.
		SecretKeySpec signingKey = new SecretKeySpec(user.getAuthToken(), HMAC_SHA1);
		try {
			// Get an HMAC-SHA1 Mac instance and initialize with the signing key.
			Mac mac = Mac.getInstance(HMAC_SHA1);
			mac.init(signingKey);
			// Compute the HMAC on input data bytes.
			byte[] rawHmac = mac.doFinal(data.getBytes());
			serverSignature = new String(Base64.encodeBase64(rawHmac), "US-ASCII");
		} catch (Exception e) {
			logger.error("Error while creating signature", e);
			return false;
		}

		if (logger.isDebugEnabled())
			logger.debug("Signature: client="+signature+", server="+serverSignature);
		if (!serverSignature.equals(signature))
			return false;

		return true;
	}

	protected boolean getAuthDeferred(HttpServletRequest req) {
		Boolean attr = (Boolean) req.getAttribute(AUTH_DEFERRED_ATTR);
		return attr == null? false: attr;
	}
}
