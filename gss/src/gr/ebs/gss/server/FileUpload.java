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

import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.GSSIOException;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.QuotaExceededException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The implementation of the file upload servlet.
 *
 * @author past
 */
public class FileUpload extends HttpServlet {

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	private static final int TRACK_PROGRESS_PERCENT = 5;

	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(FileUpload.class);

	/**
	 * A helper method that retrieves a reference to the ExternalApi bean and
	 * stores it for future use.
	 *
	 * @return an ExternalAPI instance
	 * @throws RpcException in case an error occurs
	 */
	private ExternalAPI getService() throws RpcException {
		try {
			Context ctx = new InitialContext();
			Object ref = ctx.lookup("gss/ExternalAPIBean/local");
			return (ExternalAPI) PortableRemoteObject.narrow(ref, ExternalAPI.class);
		} catch (NamingException e) {
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
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Check that we have a file upload request
		if (!ServletFileUpload.isMultipartContent(request)) {
			logger.warn("Received non-multipart request: " + request.getQueryString());
			return;
		}

		String folderId = null;
		String fileHeaderId = null;
		String userId = null;
		String fileName = null;
		String ownerId = null;
		FileItemIterator iter;
		File uploadedFile = null;
		try {
			// Create a new file upload handler.
			ServletFileUpload upload = new ServletFileUpload();
			StatusProgressListener progressListener = new StatusProgressListener(getService());
			upload.setProgressListener(progressListener);
			iter = upload.getItemIterator(request);
			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String name = item.getFieldName();
				InputStream stream = item.openStream();
				if (item.isFormField()) {
					final String value = Streams.asString(stream);
					if (name.equals("folderId"))
						folderId = value;
					else if (name.equals("fileHeaderId"))
						fileHeaderId = value;
					else if (name.equals("userId"))
						userId = value;
					else if (name.equals("ownerId"))
						ownerId = value;
				} else {

					fileName = getFilename(item.getName());
					progressListener.setUserId(userId);
					progressListener.setFilename(fileName);
					String contentType = item.getContentType();
					User user = getService().findUser(userId);

					String fpath = null;
					FolderDTO folder = null;
					if(ownerId != null){
						fpath = getInnerPath(ownerId, folderId);
						User owner = getService().findUser(ownerId);
						folder = (FolderDTO) getService().getResourceAtPath(owner.getId(), fpath, false);
					}
					else{
						fpath = getInnerPath(userId, folderId);
						folder  = (FolderDTO) getService().getResourceAtPath(user.getId(), fpath, false);
					}

					try {
						uploadedFile = getService().uploadFile(stream, user.getId());
					} catch (IOException ex) {
						throw new GSSIOException(ex, false);
					}
					if (fileHeaderId == null)
						getService().createFile(user.getId(), folder.getId(), fileName, contentType, uploadedFile);
					else{
						FileHeaderDTO file = (FileHeaderDTO) getService().getResourceAtPath(user.getId(), getInnerPath(userId,fileHeaderId), false);
						getService().updateFileContents(user.getId(), file.getId(), contentType, uploadedFile);
					}
				}
			}
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
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);

		} catch (InsufficientPermissionsException e) {
			if (uploadedFile != null && uploadedFile.exists())
				uploadedFile.delete();
			String error = "You don't have the necessary permissions";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);

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
			String error = "A specified object was not found "+e.getMessage();
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
		} catch (RpcException e) {
			if (uploadedFile != null && uploadedFile.exists())
				uploadedFile.delete();
			String error = "An error occurred while communicating with the service";
			logger.error(error, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
		}

	}

	/**
	 * Returns the file name from a potential full path argument. Apparently IE
	 * insists on sending the full path name of a file when uploading, forcing
	 * us to trim the extra path info. Since this is only observed on Windows we
	 * get to check for a single path separator value.
	 *
	 * @param name the potentially full path name of a file
	 * @return the file name without extra path information
	 */
	private String getFilename(String name) {
		int pathSepIndex = name.lastIndexOf("\\");
		if (pathSepIndex == -1)
			return name;
		return name.substring(pathSepIndex + 1);
	}

	public static class StatusProgressListener implements ProgressListener {
		private int percentLogged = 0;
		private long bytesTransferred = 0;

		private long fileSize = -100;

		private long tenKBRead = -1;

		private String userId;

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
		public void setUserId(String aUserId) {
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
						if (userId != null && filename != null){
							User user = service.findUser(userId);
							service.createFileUploadProgress(user.getId(), filename, bytesTransferred, fileSize);
						}
					} catch (ObjectNotFoundException e) {
						// Swallow the exception since it is going to be caught
						// by previously called methods
					}
				}
		}
	}

	protected String getInnerPath(String user, String path){
		String test = "/rest/"+user+"/files";
		int i = path.indexOf(test);
		return path.substring(i+test.length(), path.length());
	}
}
