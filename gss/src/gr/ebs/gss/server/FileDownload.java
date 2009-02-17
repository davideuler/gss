/*
 * Copyright 2007, 2008, 2009 Electronic Business Systems Ltd.
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
package gr.ebs.gss.server;

import gr.ebs.gss.client.domain.FileBodyDTO;
import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.io.IOException;
import java.io.InputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The implementation of the file download servlet.
 *
 * @author past
 */
public class FileDownload extends HttpServlet {
	/**
	 * The size of the buffer that is used to temporarily store chunks of
	 * downloaded files, while pushing them out.
	 */
	private static final int DOWNLOAD_BUFFER_SIZE = 1024 * 4;

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(FileDownload.class);

	/**
	 * A helper method that retrieves a reference to the ExternalAPI bean and
	 * stores it for future use.
	 *
	 * @return an ExternalAPI instance
	 * @throws RpcException in case an error occurs
	 */
	private ExternalAPI getService() throws RpcException {
		try {
			final Context ctx = new InitialContext();
			final Object ref = ctx.lookup("gss/ExternalAPIBean/local");
			return (ExternalAPI) PortableRemoteObject.narrow(ref, ExternalAPI.class);
		} catch (final NamingException e) {
			logger.error("Unable to retrieve the ExternalAPI EJB", e);
			throw new RpcException("An error occurred while contacting the naming service");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		// Check that we have a file id in the request
		if (request.getParameter("fileId") == null) {
			logger.warn("No file header id in the request: " + request.getQueryString());
			return;
		}
		// Check that we have a user id in the request
		if (request.getParameter("userId") == null) {
			logger.warn("No user id in the request: " + request.getQueryString());
			return;
		}
		Long fileId = Long.valueOf(request.getParameter("fileId"));
		Long userId = Long.valueOf(request.getParameter("userId"));

		try {

			FileHeaderDTO file = getService().getFile(userId, fileId);
			InputStream contents = null;
			if(request.getParameter("bodyId") ==  null){
				contents = getService().getFileContents(userId, fileId);
				response.reset();
				response.setContentType(file.getMimeType());
				response.setHeader("Content-disposition", "inline; filename*=UTF-8''"
						+ file.getOriginalFilenameEncoded());
				response.setContentLength ((int) file.getFileSize());
			}
			else{
				Long bodyId = Long.valueOf(request.getParameter("bodyId"));
				FileBodyDTO body = getService().getFileBody(userId, fileId, bodyId);
				contents = getService().getFileContents(userId, fileId, bodyId);
				response.reset();
				response.setContentType(body.getMimeType());
				response.setHeader("Content-disposition", "inline; filename*=UTF-8''"
						+ body.getOriginalFilenameEncoded());
				response.setContentLength ((int) body.getFileSize());
			}

			ServletOutputStream out = response.getOutputStream();
			final byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
			int n = 0;
			try {
				while (-1 != (n = contents.read(buffer)))
					out.write(buffer, 0, n);
			} finally {
				contents.close();
				out.close();
			}
		} catch (ObjectNotFoundException e) {
			final String error = "A specified object was not found";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
		} catch (RpcException e) {
			final String error = "An error occurred while communicating with the service";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
		} catch (InsufficientPermissionsException e) {
			final String error = "Insufficient privileges for reading the specified file";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
		}
	}
}
