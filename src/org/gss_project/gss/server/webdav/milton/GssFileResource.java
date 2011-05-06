/*
 * Copyright 2011 Electronic Business Systems Ltd.
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
package org.gss_project.gss.server.webdav.milton;

import org.gss_project.gss.common.exceptions.DuplicateNameException;
import org.gss_project.gss.common.exceptions.GSSIOException;
import org.gss_project.gss.common.exceptions.InsufficientPermissionsException;
import org.gss_project.gss.common.exceptions.ObjectNotFoundException;
import org.gss_project.gss.common.exceptions.RpcException;
import org.gss_project.gss.server.domain.FileHeader;
import org.gss_project.gss.server.domain.User;
import org.gss_project.gss.server.ejb.TransactionHelper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.jboss.remoting.transport.coyote.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;


/**
 * @author kman
 *
 */
public class GssFileResource extends GssResource implements CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource, PropPatchableResource {
	/**
	 * Size of file transfer buffer in bytes.
	 */
	private static final int BUFFER_SIZE = 4096;

	/**
	 * The output buffer size to use when serving resources.
	 */
	protected int output = 2048;

	/**
	 * The input buffer size to use when serving resources.
	 */
	private int input = 2048;
	
	FileHeader file;
	private static final Logger log = LoggerFactory.getLogger(GssFileResource.class);
	/**
	 * @param host
	 * @param factory
	 * @param resource
	 */
	public GssFileResource(String host, GSSResourceFactory factory, Object resource, User currentUser) {
		super(host, factory, resource);
		this.file = (FileHeader) resource;
		this.currentUser = currentUser;
		
	}
	@Override
	public String checkRedirect(Request arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Date getModifiedDate() {
		return file.getAuditInfo().getModificationDate();
	}
	@Override
	public String getName() {
		return file.getName();
	}
	@Override
	public String getUniqueId() {
		return "file:"+file.getId().toString();
	}
	@Override
	public void moveTo(final CollectionResource newParent, final String arg1) throws ConflictException, NotAuthorizedException, BadRequestException {
		if( newParent instanceof GssFolderResource ) {
			final GssFolderResource newFsParent = (GssFolderResource) newParent;
			try {
				 new TransactionHelper<Void>().tryExecute(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						if(newFsParent.folder.getId().equals(file.getFolder().getId())){
							getService().updateFile(getCurrentUser().getId(), file.getId(), arg1, null, new Date(), null, null, null);
						}
						else{
							getService().moveFile(getCurrentUser().getId(), file.getId(), newFsParent.folder.getId(), arg1);
						}
						return null;
					}
					
				});
				GssFileResource.this.file = getService().getFile(getCurrentUser().getId(), file.getId());
				
			} catch (InsufficientPermissionsException e) {
				throw new NotAuthorizedException(this);
			} catch (ObjectNotFoundException e) {
				throw new BadRequestException(this);
			} catch (DuplicateNameException e) {
				throw new ConflictException(this);
			} catch (RpcException e) {
				throw new RuntimeException("System error");
			} catch (GSSIOException e) {
				throw new RuntimeException("Unable to Move");
			} catch (Exception e) {
				throw new RuntimeException("Unable to Move");
			}
        } else {
            throw new RuntimeException("Destination is an unknown type. Must be a Folder, is a: " + newParent.getClass());
        }
		
	}
	@Override
	public void copyTo(final CollectionResource newParent, final String arg1) throws NotAuthorizedException, BadRequestException, ConflictException {
		if( newParent instanceof GssFolderResource ) {			
			final GssFolderResource newFsParent = (GssFolderResource) newParent;
			try {
				 new TransactionHelper<Void>().tryExecute(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						getService().copyFile(getCurrentUser().getId(), file.getId(), newFsParent.folder.getId(), arg1);
						return null;
					}
					
				});
				 GssFileResource.this.file = getService().getFile(getCurrentUser().getId(), file.getId());
			} catch (InsufficientPermissionsException e) {
				throw new NotAuthorizedException(this);
			} catch (ObjectNotFoundException e) {
				throw new BadRequestException(this);
			} catch (DuplicateNameException e) {
				throw new ConflictException(this);
			} catch (RpcException e) {
				throw new RuntimeException("System error");
			} catch (GSSIOException e) {
				throw new RuntimeException("Unable to Move");
			} catch (Exception e) {
				throw new RuntimeException("Unable to Move");
			}
        } else {
            throw new RuntimeException("Destination is an unknown type. Must be a FsDirectoryResource, is a: " + newParent.getClass());
        }
		
	}
	@Override
	public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
		try {
			new TransactionHelper<Void>().tryExecute(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					getService().deleteFile(getCurrentUser().getId(), file.getId());
					return null;
				}
			});
		} catch (ObjectNotFoundException e) {
			throw new BadRequestException(this);			
		} catch (InsufficientPermissionsException e) {
			throw new NotAuthorizedException(this);
		} catch (RpcException e) {
			throw new BadRequestException(this);
		}
		catch (Exception e) {			
			throw new BadRequestException(this);
		}
	}
	@Override
	public Long getContentLength() {
		return file.getCurrentBody().getFileSize();
	}
	@Override
	public String getContentType(String preferredList ) {
        return file.getCurrentBody().getMimeType();
	}
	@Override
	public Long getMaxAgeSeconds(Auth arg0) {
		return factory.maxAgeSeconds( this );
	}
	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        InputStream in = null;
        try {
            in = getService().getFileContents(getCurrentUser().getId(), file.getId());
            if( range != null ) {
            	copy(in, out, range);
            } else {
            	copyRange(in, out);
            }
            out.flush();
            IOUtils.closeQuietly( in );
        } catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (InsufficientPermissionsException e) {
			// TODO Auto-generated catch block
		} catch (RpcException e) {
			// TODO Auto-generated catch block
		} 
		catch(ClientAbortException ex){
			//do nothing
		}
		finally {
            IOUtils.closeQuietly( in );
            IOUtils.closeQuietly( out );
        }
		
	}
	
	protected void copy(InputStream resourceInputStream, OutputStream ostream, Range range) throws IOException {
		IOException exception = null;
		InputStream istream = new BufferedInputStream(resourceInputStream, input);
		exception = copyRange(istream, ostream, range.getStart(), range.getFinish());
		// Clean up the input stream
		istream.close();
		// Rethrow any exception that has occurred
		if (exception != null)
			throw exception;
	}
	protected void copy(InputStream resourceInputStream, OutputStream ostream) throws IOException{
		IOException exception = null;
		InputStream istream = new BufferedInputStream(resourceInputStream, input);
		// Copy the input stream to the output stream
		exception = copyRange(istream, ostream);
		// Clean up the input stream
		istream.close();
		// Rethrow any exception that has occurred
		if (exception != null)
			throw exception;
	}
	
	private IOException copyRange(InputStream istream, OutputStream ostream) {
		// Copy the input stream to the output stream
		IOException exception = null;
		byte buffer[] = new byte[input];
		int len = buffer.length;
		while (true)
			try {
				len = istream.read(buffer);
				if (len == -1)
					break;
				ostream.write(buffer, 0, len);
			} catch (IOException e) {
				exception = e;
				len = -1;
				break;
			}
		return exception;
	}
	
	private IOException copyRange(InputStream istream, OutputStream ostream, long start, long end) {
		log.debug("Serving bytes:" + start + "-" + end);
		try {
			istream.skip(start);
		} catch (IOException e) {
			return e;
		}
		IOException exception = null;
		long bytesToRead = end - start + 1;
		byte buffer[] = new byte[input];
		int len = buffer.length;
		while (bytesToRead > 0 && len >= buffer.length) {
			try {
				len = istream.read(buffer);
				if (bytesToRead >= len) {
					ostream.write(buffer, 0, len);
					bytesToRead -= len;
				} else {
					ostream.write(buffer, 0, (int) bytesToRead);
					bytesToRead = 0;
				}
			} catch (IOException e) {
				exception = e;
				len = -1;
			}
			if (len < buffer.length)
				break;
		}
		return exception;
	}
	@Override
	public Date getCreateDate() {
		// TODO Auto-generated method stub
		return file.getAuditInfo().getCreationDate();
	}
	@Override
	public void setProperties(Fields arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
        boolean result = factory.getSecurityManager().authorise(request, method, auth, this);
        if(result){
        	User user = getCurrentUser();
        	//check permission
        	try {
				getService().getFile(user.getId(), file.getId());
			} catch (ObjectNotFoundException e) {
				return false;
			} catch (InsufficientPermissionsException e) {
				return false;
			} catch (RpcException e) {
				return false;
			}
			return true;
        }
        return result;
    }
}
