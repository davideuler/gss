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
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.AuthenticationHandler;
import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.AuthenticationService.AuthStatus;


/**
 * @author kman
 *
 */
public class GssAuthenticationService extends com.bradmcevoy.http.AuthenticationService{
	private static final Logger log = LoggerFactory.getLogger( GssAuthenticationService.class );
	
	public GssAuthenticationService( ) {
        super(new ArrayList<AuthenticationHandler>());
    }
	
	public AuthStatus authenticate( Resource resource, Request request ) {
        Auth auth = request.getAuthorization();
        boolean preAuthenticated = ( auth != null && auth.getTag() != null );
        if( preAuthenticated ) {
            return new AuthStatus( auth, false );
        }
        String username = request.getHeaders().get("authorization");
    	if(username!=null){
    		username=GSSResourceFactory.getUsernameFromAuthHeader(username);
    		try {
				Object user = getService().getUserByUserName(username);
				if( auth == null ) { // some authentication handlers do not require an Auth object
                    auth = new Auth( Auth.Scheme.FORM, username ,null);
                    request.setAuthorization( auth );
                }
                auth.setTag( user );
            
            return new AuthStatus( auth, false );
			} catch (RpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
    	}
        
        return null;
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
}
