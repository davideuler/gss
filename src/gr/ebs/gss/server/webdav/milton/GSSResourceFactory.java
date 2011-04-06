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

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.common.exceptions.ObjectNotFoundException;
import gr.ebs.gss.common.exceptions.RpcException;
import gr.ebs.gss.server.domain.FileHeader;
import gr.ebs.gss.server.domain.Folder;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.ejb.ExternalAPI;
import gr.ebs.gss.server.ejb.TransactionHelper;

import java.util.Date;
import java.util.concurrent.Callable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.SecurityManager;
import com.ettrema.http.fs.LockManager;


/**
 * @author kman
 *
 */
public class GSSResourceFactory implements ResourceFactory {
	private static final Logger log = LoggerFactory.getLogger(GSSResourceFactory.class);
	
	SecurityManager securityManager;
    LockManager lockManager;
    Long maxAgeSeconds;
    String contextPath;
    boolean allowDirectoryBrowsing;
    String defaultPage;
    HttpManager httpManager;
	@Override
	public Resource getResource(String host, String url) {
		


		log.debug("getResource: host: " + host + " - url:" + url);
        url = stripContext(url);
        if(url==null||url.trim().equals("")||url.equals("/")){
        	url="/";
        }
        /*//log.info("URL:"+url);
        if(url.equals("/OthersShared")||url.equals("/OthersShared/")){
        	//log.info("[returning others]");
        	return new GssOthersResource(host, this);
        }
        if(url.startsWith("/OthersShared")){
        	
        }*/
        try {
        	User user =null;
    		if(HttpManager.request().getAuthorization()!=null && HttpManager.request().getAuthorization().getTag()==null){
    			String username = HttpManager.request().getAuthorization().getUser();
    			if(username !=null)
    				user = getService().getUserByUserName(username);
    		}
    		else if(HttpManager.request().getAuthorization()!=null&&HttpManager.request().getAuthorization().getTag()!=null){
    			user =(User) HttpManager.request().getAuthorization().getTag();
    		}
    	
    		if(user==null){
    			//create a resource based on path if no resource exists at this path it will be handled by subsequent webdav method calls
				return new GssRootFolderResource(host, this, null,url);
    		}
    			
        	Object r = getResourceGss(url,user);
        	if(r==null){
        		
        		return null;
        	}
        	if(r instanceof Folder){
        		
        		return new GssFolderResource(host, this,r ,user);
        	}
        	else
        		return new GssFileResource(host, this,r,user);
		} catch (RpcException e) {
			log.error("unable to access ejb service",e);
		}
		return null;
    }
	public Long maxAgeSeconds(GssResource resource) {
        return maxAgeSeconds;
    }
	protected Object getResourceGss(String path, User user) throws RpcException{

		if(user ==null){
			if(HttpManager.request().getAuthorization()!=null && HttpManager.request().getAuthorization().getTag()==null){
				String username = HttpManager.request().getAuthorization().getUser();
				if(username !=null)
					user = getService().getUserByUserName(username);
			}
			else if(HttpManager.request().getAuthorization()!=null&&HttpManager.request().getAuthorization().getTag()!=null){
				user =(User) HttpManager.request().getAuthorization().getTag();
			}
		}
		
		if(user==null){
			return null;
		}
		boolean exists = true;
		Object resource = null;
		try {
			resource = getService().getResourceAtPath(user.getId(), path, true);
		} catch (ObjectNotFoundException e) {
			exists = false;
		} catch (RpcException e) {
			
			return null;
		}

		if (!exists) {
			
			return null;
		}
		if(resource instanceof Folder){
			try {
				resource = getService().expandFolder((Folder) resource);
			} catch (ObjectNotFoundException e) {
				// TODO Auto-generated catch block
				return null;
			}
		}
		else if(resource instanceof FileHeader){
			try {
				resource = getService().expandFile((FileHeader) resource);
			} catch (ObjectNotFoundException e) {
				// TODO Auto-generated catch block
				return null;
			}
		}
		return resource;
	}
	
	
	 private String stripContext( String url ) {
	        if( this.contextPath != null && contextPath.length() > 0 ) {
	            url = url.replaceFirst( '/' + contextPath, "");
	            log.debug( "stripped context: " + url);
	            return url;
	        } else {
	            return url;
	        }
	    }
	 
	 /**
		 * For a provided path, remove the last element and return the rest, that is
		 * the path of the parent folder.
		 *
		 * @param path the specified path
		 * @return the path of the parent folder
		 * @throws ObjectNotFoundException if the provided string contains no path
		 *             delimiters
		 */
		protected String getParentPath(String path) throws ObjectNotFoundException {
			int lastDelimiter = path.lastIndexOf('/');
			if (lastDelimiter == 0)
				return "/";
			if (lastDelimiter == -1)
				// No path found.
				throw new ObjectNotFoundException("There is no parent in the path: " + path);
			else if (lastDelimiter < path.length() - 1)
				// Return the part before the delimiter.
				return path.substring(0, lastDelimiter);
			else {
				// Remove the trailing delimiter and then recurse.
				String strippedTrail = path.substring(0, lastDelimiter);
				return getParentPath(strippedTrail);
			}
		}

		/**
		 * A helper method that retrieves a reference to the ExternalAPI bean and
		 * stores it for future use.
		 *
		 * @return an ExternalAPI instance
		 * @throws RpcException in case an error occurs
		 */
		protected ExternalAPI getService() throws RpcException {
			try {
				final Context ctx = new InitialContext();
				final Object ref = ctx.lookup(getConfiguration().getString("externalApiPath"));
				return (ExternalAPI) PortableRemoteObject.narrow(ref, ExternalAPI.class);
				
			} catch (final NamingException e) {
				log.error("Unable to retrieve the ExternalAPI EJB", e);
				throw new RpcException("An error occurred while contacting the naming service");
			}
		}

		private void updateAccounting(final User user, final Date date, final long bandwidthDiff) {
			try {
				new TransactionHelper<Void>().tryExecute(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						getService().updateAccounting(user, date, bandwidthDiff);
						return null;
					}
				});
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				// updateAccounting() doesn't throw any checked exceptions
				assert false;
			}
		}

		
		/**
		 * Retrieve the securityManager.
		 *
		 * @return the securityManager
		 */
		public SecurityManager getSecurityManager() {
			return securityManager;
		}

		
		/**
		 * Retrieve the lockManager.
		 *
		 * @return the lockManager
		 */
		public LockManager getLockManager() {
			return lockManager;
		}

		
		/**
		 * Retrieve the maxAgeSeconds.
		 *
		 * @return the maxAgeSeconds
		 */
		public Long getMaxAgeSeconds() {
			return maxAgeSeconds;
		}

		
		/**
		 * Retrieve the contextPath.
		 *
		 * @return the contextPath
		 */
		public String getContextPath() {
			return contextPath;
		}

		
		/**
		 * Retrieve the allowDirectoryBrowsing.
		 *
		 * @return the allowDirectoryBrowsing
		 */
		public boolean isAllowDirectoryBrowsing() {
			return allowDirectoryBrowsing;
		}

		
		/**
		 * Retrieve the defaultPage.
		 *
		 * @return the defaultPage
		 */
		public String getDefaultPage() {
			return defaultPage;
		}
		
		public String getRealm(String host) {
	        return securityManager.getRealm(host);
	    }

		
		/**
		 * Modify the securityManager.
		 *
		 * @param securityManager the securityManager to set
		 */
		public void setSecurityManager(SecurityManager securityManager) {
			this.securityManager = securityManager;
		}

		
		/**
		 * Modify the lockManager.
		 *
		 * @param lockManager the lockManager to set
		 */
		public void setLockManager(LockManager lockManager) {
			this.lockManager = lockManager;
		}

		
		/**
		 * Modify the maxAgeSeconds.
		 *
		 * @param maxAgeSeconds the maxAgeSeconds to set
		 */
		public void setMaxAgeSeconds(Long maxAgeSeconds) {
			this.maxAgeSeconds = maxAgeSeconds;
		}

		
		/**
		 * Modify the contextPath.
		 *
		 * @param contextPath the contextPath to set
		 */
		public void setContextPath(String contextPath) {
			this.contextPath = contextPath;
		}

		
		/**
		 * Modify the defaultPage.
		 *
		 * @param defaultPage the defaultPage to set
		 */
		public void setDefaultPage(String defaultPage) {
			this.defaultPage = defaultPage;
		}
		
		
		/**
		 * Retrieve the httpManager.
		 *
		 * @return the httpManager
		 */
		public HttpManager getHttpManager() {
			return httpManager;
		}
		
		
		/**
		 * Modify the httpManager.
		 *
		 * @param httpManager the httpManager to set
		 */
		public void setHttpManager(HttpManager httpManager) {
			this.httpManager = httpManager;
		}
		
		
	
		public static String getUsernameFromAuthHeader(String header) {
			String first = header.split(",")[0];
			int indx = first.indexOf("\"");
			return first.substring(indx+1,first.length()-1);
		}
}
