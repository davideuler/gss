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

import static org.gss_project.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import org.gss_project.gss.common.exceptions.RpcException;
import org.gss_project.gss.server.domain.User;
import org.gss_project.gss.server.ejb.ExternalAPI;
import org.gss_project.gss.server.ejb.TransactionHelper;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.FailedLoginException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestGenerator;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.http.fs.SimpleSecurityManager;


/**
 * @author kman
 *
 */
public class GssSecurityManager  implements com.bradmcevoy.http.SecurityManager{

	private static final Logger log = LoggerFactory.getLogger(SimpleSecurityManager.class);

    private String realm;
    private DigestGenerator digestGenerator;

    public GssSecurityManager() {
        digestGenerator = new DigestGenerator();
    }

    public GssSecurityManager( DigestGenerator digestGenerator ) {
        this.digestGenerator = digestGenerator;
    }

   
    public GssSecurityManager( String realm) {
        this.realm = realm;
        this.digestGenerator = new DigestGenerator();
    }
    /*
    public Object getUserByName( String name ) {
        String actualPassword = nameAndPasswords.get( name );
        if( actualPassword != null ) return name;
        return null;
    }*/



    public Object authenticate( String user, String password ) {
        //log.info( "authenticate: " + user + " - " + password);
        // user name will include domain when coming form ftp. we just strip it off
        if( user.contains( "@")) {
            user = user.substring( 0, user.indexOf( "@"));
        }
        String actualPassword=null;
		try {
			actualPassword = getUsersPassword( user );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        if( actualPassword == null ) {
            log.debug( "user not found: " + user);
            return null;
        } else {
            boolean ok;
            if( actualPassword == null ) {
                ok = password == null || password.length()==0;
            } else {
                ok = actualPassword.equals( password);
            }
            return ok ? user : null;
        }
    }

    public Object authenticate( DigestResponse digestRequest ) {
    	//log.info( "DIGEST authenticate: " + digestRequest);
        String actualPassword=null;
		try {
			actualPassword = getUsersPassword( digestRequest.getUser() );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null;
		}
		
        String serverResponse = digestGenerator.generateDigest( digestRequest, actualPassword );
        String clientResponse = digestRequest.getResponseDigest();

        if( serverResponse.equals( clientResponse ) ) {
            try {
				return getService().getUserByUserName(digestRequest.getUser());
			} catch (RpcException e) {
				// TODO Auto-generated catch block
				return null;
			}
        } else {
            return null;
        }
    }



    public boolean authorise( Request request, Method method, Auth auth, Resource resource ) {
        return auth != null && auth.getTag() != null;
    }

    public String getRealm(String host) {
        return realm;
    }

    /**
     * @param realm the realm to set
     */
    public void setRealm( String realm ) {
        this.realm = realm;
    }
    
    private ExternalAPI getService() throws RpcException {
		try {
			final Context ctx = new InitialContext();
			final Object ref = ctx.lookup(getConfiguration().getString("externalApiPath"));
			return (ExternalAPI) PortableRemoteObject.narrow(ref, ExternalAPI.class);
		} catch (final NamingException e) {
			log.error("Unable to retrieve the ExternalAPI EJB", e);
			throw new RpcException("An error occurred while contacting the naming service");
		}
	}

	
	protected String getUsersPassword(String username) throws Exception {
		
		try {
			final User user = getService().findUser(username);
			if (user == null) throw new FailedLoginException("User '" + username + "' not found.");
			if (!user.isActive()) throw new FailedLoginException("User '" + username + "' is disabled.");
			if (user.getWebDAVPassword() != null && user.getWebDAVPassword().length() > 0)
				return user.getWebDAVPassword();
			// If no password has ever been generated, use token instead
			String tokenEncoded = new String(Base64.encodeBase64(user.getAuthToken()), "US-ASCII");
			user.setWebDAVPassword(tokenEncoded);
			new TransactionHelper<Void>().tryExecute(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					getService().updateUser(user);
					return null;
				}
			});
			return tokenEncoded;
		} catch (RpcException e) {
			String error = "An error occurred while communicating with the service";
			log.error(error, e);
			throw new Exception(e.getMessage());
		} catch (UnsupportedEncodingException e) {
            log.error("", e);
            throw new Exception(e.getMessage());
		} catch (Exception e) {
            log.error("", e);
			throw new Exception(e.getMessage());
		}
	}

    


}