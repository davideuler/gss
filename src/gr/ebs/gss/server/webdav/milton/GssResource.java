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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import gr.ebs.gss.common.exceptions.RpcException;
import gr.ebs.gss.server.domain.User;
import gr.ebs.gss.server.ejb.ExternalAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;


/**
 * @author kman
 *
 */
public abstract class GssResource implements Resource, MoveableResource, CopyableResource, LockableResource, DigestResource {
    private static final Logger log = LoggerFactory.getLogger(GssResource.class);
    String host;
    GSSResourceFactory factory;
    Object resource;
    User currentUser;
    
	/**
	 * 
	 */
	public GssResource(String host, GSSResourceFactory factory, Object resource) {
		this.host=host;
		this.factory=factory;
		this.resource=resource;
		
	}
	
	public Object authenticate(String user, String password) {
        return factory.getSecurityManager().authenticate(user, password);
    }

    public Object authenticate( DigestResponse digestRequest ) {
        return  factory.getSecurityManager().authenticate(digestRequest);
        
    }

    public boolean isDigestAllowed() {
        return true;
    }




    public boolean authorise(Request request, Method method, Auth auth) {
        return factory.getSecurityManager().authorise(request, method, auth, this);
    }

    public String getRealm() {
        return factory.getRealm(this.host);
    }
    
    public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException {
        return factory.getLockManager().lock(timeout, lockInfo, this);
    }

    public LockResult refreshLock(String token) throws NotAuthorizedException {
        return factory.getLockManager().refresh(token, this);
    }

    public void unlock(String tokenId) throws NotAuthorizedException {
        factory.getLockManager().unlock(tokenId, this);
    }

    public LockToken getCurrentLock() {
        if( factory.getLockManager() != null ) {
            return factory.getLockManager().getCurrentToken( this );
        } else {
            log.warn("getCurrentLock called, but no lock manager: file: " + resource);
            return null;
        }
    }
    
    
	/**
	 * Retrieve the currentUser.
	 *
	 * @return the currentUser
	 */
	public User getCurrentUser() {
		if(currentUser!=null)
			return currentUser;
		if(HttpManager.request().getAuthorization()!=null && HttpManager.request().getAuthorization().getTag()==null){
			String username = HttpManager.request().getAuthorization().getUser();
			//log.info("username is:"+username);
			if(username !=null)
				try {
					currentUser = getService().getUserByUserName(username);
				} catch (RpcException e) {
					// TODO Auto-generated catch block
					log.error("unable to access ejb service",e);
				}
		}
		else if(HttpManager.request().getAuthorization()!=null&&HttpManager.request().getAuthorization().getTag()!=null){
			//log.info(HttpManager.request().getAuthorization().getUser());
			currentUser =(User) HttpManager.request().getAuthorization().getTag();//getService().getUserByUserName("past@ebs.gr");
		}
		return currentUser;
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
	
	
	/**
	 * Retrieve the host.
	 *
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
}
