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

import gr.ebs.gss.client.exceptions.DuplicateNameException;
import gr.ebs.gss.client.exceptions.GSSIOException;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.QuotaExceededException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;
import gr.ebs.gss.server.domain.dto.FolderDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;
import gr.ebs.gss.server.ejb.TransactionHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.QuotaResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;


/**
 * @author kman
 *
 */
public class GssFolderResource extends GssResource implements MakeCollectionableResource, PutableResource, CopyableResource, DeletableResource, MoveableResource, PropFindableResource, LockingCollectionResource, GetableResource, QuotaResource{
	 private static final Logger log = LoggerFactory.getLogger(GssFolderResource.class);
	FolderDTO folder;
	
	/**
	 * @param host
	 * @param factory
	 * @param resource
	 */
	public GssFolderResource(String host, GSSResourceFactory factory, Object resource) {
		super(host, factory, resource);
		folder=(FolderDTO) resource;
	}
	@Override
	public String checkRedirect(Request request) {
		if( factory.getDefaultPage() != null ) {
            return request.getAbsoluteUrl() + "/" + factory.getDefaultPage();
        } else {
            return null;
        }
	}
	@Override
	public Date getModifiedDate() {
		return folder.getAuditInfo().getModificationDate();
	}
	@Override
	public String getName() {
		return folder.getName();
	}
	@Override
	public String getUniqueId() {
		return "folder:"+folder.getId().toString();
	}
	@Override
	public void moveTo(final CollectionResource newParent, final String arg1) throws ConflictException, NotAuthorizedException, BadRequestException {
		if( newParent instanceof GssFolderResource ) {
			final GssFolderResource newFsParent = (GssFolderResource) newParent;
			try {
				if(newFsParent.folder.getName().equals(folder.getParent().getName())){
					if(!folder.getName().equals(arg1))
						new TransactionHelper<Void>().tryExecute(new Callable<Void>() {
	
							@Override
							public Void call() throws Exception {
								factory.getService().updateFolder(getCurrentUser().getId(), folder.getId(), arg1, null, null);
								return null;
							}
							
						});
				}
				else new TransactionHelper<Void>().tryExecute(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						factory.getService().moveFolder(getCurrentUser().getId(), folder.getId(), newFsParent.folder.getId(), arg1);
						log.info("MOVING OK:"+arg1);
						
						return null;
					}
					
				});
				GssFolderResource.this.folder = factory.getService().getFolder(getCurrentUser().getId(), folder.getId());
				log.info("MOVING:"+folder.getName());
				
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
						factory.getService().copyFolder(getCurrentUser().getId(), folder.getId(), newFsParent.folder.getId(), arg1);
						return null;
					}
					
				});
				GssFolderResource.this.folder = factory.getService().getFolder(getCurrentUser().getId(), folder.getId());
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
	public CollectionResource createCollection(final String name) throws NotAuthorizedException, ConflictException, BadRequestException {
		try {
			final FolderDTO folderParent = folder;
			FolderDTO created = new TransactionHelper<FolderDTO>().tryExecute(new Callable<FolderDTO>() {
				@Override
				public FolderDTO call() throws Exception {
					FolderDTO f = factory.getService().createFolder(getCurrentUser().getId(), folder.getId(), name);
					return f;
				}
			});
			return new GssFolderResource(host, factory, created);
		} catch (DuplicateNameException e) {
			e.printStackTrace();
			// XXX If the existing name is a folder we should be returning
			// SC_METHOD_NOT_ALLOWED, or even better, just do the createFolder
			// without checking first and then deal with the exceptions.
			throw new ConflictException(this);
		} catch (InsufficientPermissionsException e) {
			e.printStackTrace();
			throw new NotAuthorizedException(this);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (RpcException e) {
			e.printStackTrace();
			throw new RuntimeException("System Error");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("System Error");
		}
	}
	@Override
	public Resource child(String name) {
		for(FolderDTO f : folder.getSubfolders())
			if(f.getName().equals(name))
				return new GssFolderResource(host, factory, f);
		
			try {
				for(FileHeaderDTO f : factory.getService().getFiles(folder.getOwner().getId(), folder.getId(), true))
					if(f.getName().equals(name))
						return new GssFileResource(host, factory, f);
			} catch (ObjectNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InsufficientPermissionsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return null;
	}
	@Override
	public List<? extends Resource> getChildren() {
		try {
			this.folder = factory.getService().getFolder(getCurrentUser().getId(), folder.getId());
		} catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InsufficientPermissionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<GssResource> result = new ArrayList<GssResource>();
		for(FolderDTO f : folder.getSubfolders())
			if(!f.isDeleted())
				result.add(new GssFolderResource(host, factory, f));
		try {
			for(FileHeaderDTO f : factory.getService().getFiles(getCurrentUser().getId(), folder.getId(), true))
				result.add(new GssFileResource(host, factory, f));
		} catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InsufficientPermissionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	@Override
	public Resource createNew(final String name, InputStream in, Long length, final String contentType ) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
		
    	File uploadedFile = null;
    	try {
			uploadedFile = factory.getService().uploadFile(in, getCurrentUser().getId());
		} catch (IOException ex) {
			throw new IOException(ex);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
			throw new BadRequestException(this);
		} catch (RpcException e) {
			throw new RuntimeException("Unable to upload file");			
		}
		final File uf = uploadedFile;
		try {
			String pathFolder = folder.getPath();
			if(!pathFolder.endsWith("/"))
				pathFolder=pathFolder+"/";
			String fname = pathFolder+name;
			log.info("fname:"+fname+" "+URLDecoder.decode(fname));
			Object ff2;
			try{
				ff2 = factory.getService().getResourceAtPath(folder.getOwner().getId(), URLDecoder.decode(fname), true);
			}
			catch(ObjectNotFoundException ex){
				ff2=null;
			}
			final Object ff = ff2;
			FileHeaderDTO kmfileDTO=null;
			if(ff!=null && ff instanceof FileHeaderDTO){
				kmfileDTO = new TransactionHelper<FileHeaderDTO>().tryExecute(new Callable<FileHeaderDTO>() {
					@Override
					public FileHeaderDTO call()  throws Exception{
						return factory.getService().updateFileContents(getCurrentUser().getId(), ((FileHeaderDTO)ff).getId(),  contentType, uf.length(), uf.getAbsolutePath());
					}
				});
			}
			else
				kmfileDTO = new TransactionHelper<FileHeaderDTO>().tryExecute(new Callable<FileHeaderDTO>() {
					@Override
					public FileHeaderDTO call() throws Exception{
						return factory.getService().createFile(getCurrentUser().getId(), folder.getId(), name, contentType, uf.length(), uf.getAbsolutePath());
					}
				});
			return new GssFileResource(host, factory, kmfileDTO);
		} catch (ObjectNotFoundException e) {
			throw new BadRequestException(this);
		} catch (InsufficientPermissionsException e) {
			throw new NotAuthorizedException(this);
		}
		catch (DuplicateNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ConflictException(this);
		}
		catch(QuotaExceededException e){
			throw new ConflictException(this);
		}
		catch(Exception e){
			throw new RuntimeException("System Error");
		}
	}
	@Override
	public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
		try {
			factory.getService().deleteFolder(getCurrentUser().getId(), folder.getId());
		} catch (InsufficientPermissionsException e) {
			e.printStackTrace();
			throw new NotAuthorizedException(this);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
			throw new BadRequestException(this);
		} catch (RpcException e) {
			e.printStackTrace();
			throw new BadRequestException(this);
		}
		
	}
	@Override
	public Date getCreateDate() {
		if(folder!=null && folder.getAuditInfo()!=null)
			return folder.getAuditInfo().getCreationDate();
		return null;
	}
	@Override
	public LockToken createAndLock(final String name, LockTimeout timeout, LockInfo lockInfo ) throws NotAuthorizedException {
		FileHeaderDTO kmfileDTO=null;
		try {
			kmfileDTO = new TransactionHelper<FileHeaderDTO>().tryExecute(new Callable<FileHeaderDTO>() {
				@Override
				public FileHeaderDTO call() throws Exception {
					return factory.getService().createEmptyFile(getCurrentUser().getId(), folder.getId(), name);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("CREATE AND LOCK:"+kmfileDTO.getId());
		//File dest = new File( this.getFile(), name );
	        //createEmptyFile(  );
	        GssFileResource newRes = new GssFileResource( host, factory, kmfileDTO );
	        LockResult res = newRes.lock( timeout, lockInfo );
	        return res.getLockToken();
		
	}
	@Override
	public Long getContentLength() {
		return null;
	}
	@Override
	public String getContentType(String arg0) {
		 return "text/html";
	}
	@Override
	public Long getMaxAgeSeconds(Auth arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
    * Will generate a listing of the contents of this directory, unless
    * the factory's allowDirectoryBrowsing has been set to false.
    *
    * If so it will just output a message saying that access has been disabled.
    *
    * @param out
    * @param range
    * @param params
    * @param contentType
    * @throws IOException
    * @throws NotAuthorizedException
    */
   public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
       String subpath = folder.getPath();//getFile().getCanonicalPath().substring( factory.getRoot().getCanonicalPath().length() ).replace( '\\', '/' );
       String uri = "/" + factory.getContextPath() + subpath;
       XmlWriter w = new XmlWriter( out );
       w.open( "html" );
       w.open( "body" );
       w.begin( "h1" ).open().writeText( this.getName() ).close();
       w.open( "table" );
       for( Resource r : getChildren() ) {
           w.open( "tr" );

           w.open( "td" );
           w.begin( "a" ).writeAtt( "href", uri + "/" + r.getName() ).open().writeText( r.getName() ).close();
           w.close( "td" );

           w.begin( "td" ).open().writeText( r.getModifiedDate() + "" ).close();
           w.close( "tr" );
       }
       w.close( "table" );
       w.close( "body" );
       w.close( "html" );
       w.flush();
   }
	@Override
	public Long getQuotaAvailable() {
		if(getCurrentUser()!=null)
			try {
				return factory.getService().getUserStatistics(getCurrentUser().getId()).getQuotaLeftSize();
			} catch (ObjectNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	@Override
	public Long getQuotaUsed() {
		if(getCurrentUser()!=null)
			try {
				return factory.getService().getUserStatistics(getCurrentUser().getId()).getFileSize();
			} catch (ObjectNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
        boolean result = factory.getSecurityManager().authorise(request, method, auth, this);
        if(result){
        	UserDTO user = (UserDTO) auth.getTag();
        	//check permission
        	try {
				factory.getService().getFolder(user.getId(), folder.getId());
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
