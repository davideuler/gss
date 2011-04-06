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
import gr.ebs.gss.common.exceptions.RpcException;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.BasicAuthHandler;
import com.bradmcevoy.http.http11.auth.DigestAuthenticationHandler;


/**
 * @author kman
 *
 */
public class GssAuthenticationService extends com.bradmcevoy.http.AuthenticationService{
	private static final Logger log = LoggerFactory.getLogger( AuthenticationService.class );
    private List<AuthenticationHandler> authenticationHandlers;
    private List<AuthenticationHandler> extraHandlers;
    private List<AuthenticationHandler> allHandlers;
    private boolean disableBasic;
    private boolean disableDigest;

    /**
     * Creates a AuthenticationService using the given handlers. Use this if
     * you don't want the default of a BasicAuthHandler and a DigestAuthenticationHandler
     *
     * @param authenticationHandlers
     */
    public GssAuthenticationService( List<AuthenticationHandler> authenticationHandlers ) {
        this.authenticationHandlers = authenticationHandlers;
        setAllHandlers();
    }


    /**
     * Creates with Basic and Digest handlers
     *
     */
    public GssAuthenticationService() {
        AuthenticationHandler digest = new com.bradmcevoy.http.http11.auth.DigestAuthenticationHandler();
        AuthenticationHandler basic = new BasicAuthHandler();
        authenticationHandlers = new ArrayList<AuthenticationHandler>();
        authenticationHandlers.add( basic );
        authenticationHandlers.add( digest );
        setAllHandlers();
    }

    public void setDisableBasic( boolean b ) {
        if( b ) {
            Iterator<AuthenticationHandler> it = this.authenticationHandlers.iterator();
            while( it.hasNext() ) {
                AuthenticationHandler hnd = it.next();
                if( hnd instanceof BasicAuthHandler ) {
                    it.remove();
                }
            }
        }
        disableBasic = b;
        setAllHandlers();
    }

    public boolean isDisableBasic() {
        return disableBasic;
    }

    public void setDisableDigest( boolean b ) {
        if( b ) {
            Iterator<AuthenticationHandler> it = this.authenticationHandlers.iterator();
            while( it.hasNext() ) {
                AuthenticationHandler hnd = it.next();
                if( hnd instanceof DigestAuthenticationHandler ) {
                    it.remove();
                }
            }
        }
        disableDigest = b;
        setAllHandlers();
    }

    public boolean isDisableDigest() {
        return disableDigest;
    }

   
    /**
     * Generates a list of http authentication challenges, one for each
     * supported authentication method, to be sent to the client.
     *
     * @param resource - the resoruce being requested
     * @param request - the current request
     * @return - a list of http challenges
     */
    public List<String> getChallenges( Resource resource, Request request ) {
        List<String> challenges = new ArrayList<String>();
        for( AuthenticationHandler h : allHandlers ) {
            if( h.isCompatible( resource ) ) {
                log.debug( "challenge for auth: " + h.getClass() );
                String ch = h.getChallenge( resource, request );
                challenges.add( ch );
            } else {
                log.debug( "not challenging for auth: " + h.getClass() + " for resource type: " + resource.getClass() );
            }
        }
        return challenges;
    }

    public List<AuthenticationHandler> getAuthenticationHandlers() {
        return allHandlers;
    }

    public List<AuthenticationHandler> getExtraHandlers() {
        return extraHandlers;
    }

    public void setExtraHandlers( List<AuthenticationHandler> extraHandlers ) {
        this.extraHandlers = extraHandlers;
        setAllHandlers();
    }

    /**
     * Merge standard and extra handlers into single list
     */
    private void setAllHandlers() {
        List<AuthenticationHandler> handlers = new ArrayList<AuthenticationHandler>();
        if( authenticationHandlers != null ) {
            handlers.addAll( authenticationHandlers );
        }
        if( extraHandlers != null ) {
            handlers.addAll( extraHandlers );
        }
        this.allHandlers = Collections.unmodifiableList( handlers );
    }

    
	public AuthStatus authenticate( Resource resource, Request request ) {
        log.trace( "authenticate" );
        Auth auth = request.getAuthorization();
        boolean preAuthenticated = ( auth != null && auth.getTag() != null );
        if( preAuthenticated ) {
            log.trace( "request is pre-authenticated" );
            return new AuthStatus( auth, false );
        }
        for( AuthenticationHandler h : getAuthenticationHandlers() ) {
            if( h.supports( resource, request ) ) {
                Object loginToken = h.authenticate( resource, request );
                if( loginToken == null ) {
                    log.warn( "authentication failed by AuthenticationHandler:" + h.getClass() );
                    return new AuthStatus( auth, true );
                } else {
                    if( log.isTraceEnabled() ) {
                        log.trace( "authentication passed by: " + h.getClass() );
                    }
                    if( auth == null ) { // some authentication handlers do not require an Auth object
                        auth = new Auth( Auth.Scheme.FORM, null, loginToken );
                        request.setAuthorization( auth );
                    }
                    auth.setTag( loginToken );
                }
                return new AuthStatus( auth, false );
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
