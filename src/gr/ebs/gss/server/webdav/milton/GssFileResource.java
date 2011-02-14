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
package gr.ebs.gss.server.webdav.milton;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.ContentTypeUtils;
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
import com.bradmcevoy.io.StreamUtils;

import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.GSSIOException;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;
import gr.ebs.gss.server.ejb.TransactionHelper;


/**
 * @author kman
 *
 */
public class GssFileResource extends GssResource implements CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource, PropPatchableResource {
	FileHeaderDTO file;
	private static final Logger log = LoggerFactory.getLogger(GssFileResource.class);
	/**
	 * @param host
	 * @param factory
	 * @param resource
	 */
	public GssFileResource(String host, GSSResourceFactory factory, Object resource) {
		super(host, factory, resource);
		this.file = (FileHeaderDTO)resource;
		
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
						factory.getService().moveFile(getCurrentUser().getId(), file.getId(), newFsParent.folder.getId(), arg1);
						return null;
					}
					
				});
				GssFileResource.this.file = factory.getService().getFile(getCurrentUser().getId(), file.getId());
				
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
						factory.getService().copyFile(getCurrentUser().getId(), file.getId(), newFsParent.folder.getId(), arg1);
						return null;
					}
					
				});
				 GssFileResource.this.file = factory.getService().getFile(getCurrentUser().getId(), file.getId());
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
			factory.getService().deleteFile(getCurrentUser().getId(), file.getId());
		} catch (ObjectNotFoundException e) {
			throw new BadRequestException(this);			
		} catch (InsufficientPermissionsException e) {
			throw new NotAuthorizedException(this);
		} catch (RpcException e) {
			throw new BadRequestException(this);
		}
		
	}
	@Override
	public Long getContentLength() {
		return file.getFileSize();
	}
	@Override
	public String getContentType(String preferredList ) {
        return file.getMimeType();
	}
	@Override
	public Long getMaxAgeSeconds(Auth arg0) {
		return factory.maxAgeSeconds( this );
	}
	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        InputStream in = null;
        try {
            in = factory.getService().getFileContents(getCurrentUser().getId(), file.getId());
            /*if( range != null ) {
            	long start = range.getStart();
            	if( start > 0 ) in.skip(start);
            	long finish = range.getFinish();
            	if( finish > 0 ) {
            		StreamUtils.readTo(in, out);
            	}
            } else {
            */
            	int bytes = IOUtils.copy( in, out );
            	out.flush();
            //}
        } catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InsufficientPermissionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            IOUtils.closeQuietly( in );
        }
		
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
        	UserDTO user = (UserDTO) auth.getTag();
        	//check permission
        	try {
				factory.getService().getFile(user.getId(), file.getId());
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
