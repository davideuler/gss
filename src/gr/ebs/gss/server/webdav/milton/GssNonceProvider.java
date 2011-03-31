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
import gr.ebs.gss.server.domain.WebDavNonce;
import gr.ebs.gss.server.ejb.ExternalAPI;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.ExpiredNonceRemover;
import com.bradmcevoy.http.http11.auth.Nonce;
import com.bradmcevoy.http.http11.auth.NonceProvider;
import com.bradmcevoy.http.http11.auth.SimpleMemoryNonceProvider;
import com.bradmcevoy.http.http11.auth.NonceProvider.NonceValidity;


/**
 * @author kman
 *
 */
public class GssNonceProvider implements NonceProvider {

    private static final Logger log = LoggerFactory.getLogger( GssNonceProvider.class );
    private final int nonceValiditySeconds;
    private boolean enableNonceCountChecking;
    public GssNonceProvider( int nonceValiditySeconds ) {
        this.nonceValiditySeconds = nonceValiditySeconds;
    }
    
	@Override
	public String createNonce(Resource resource, Request request ) {
		UUID id = UUID.randomUUID();
        Date now = new Date();
        Nonce n = new Nonce( id, now );
        createOrUpdateGssNonce(n);
        return n.getValue().toString();
	}

	@Override
	public NonceValidity getNonceValidity( String nonce, Long nc ) {
        log.trace( "getNonceValidity: " + nonce );
        UUID value = null;
        try {
            value = UUID.fromString( nonce );
        } catch( Exception e ) {
            log.warn( "couldnt parse nonce" );
            return NonceValidity.INVALID;
        }
        Nonce n = getNonce( nonce );
        if( n == null ) {
            log.debug( "not found");
            return NonceValidity.INVALID;
        } else {
            if( isExpired( n.getIssued() ) ) {
                log.debug( "nonce has expired" );
                return NonceValidity.EXPIRED;
            } else {
                if( nc == null ) {
                    log.trace( "nonce ok" );
                    return NonceValidity.OK;
                } else {
                    if( enableNonceCountChecking && nc <= n.getNonceCount() ) {
                        log.warn( "nonce-count was not greater then previous, possible replay attack. new: " + nc + " old:" + n.getNonceCount() );
                        return NonceValidity.INVALID;
                    } else {
                        log.trace( "nonce and nonce-count ok" );
                        Nonce newNonce = n.increaseNonceCount( nc );
                        createOrUpdateGssNonce(newNonce);
                        return NonceValidity.OK;
                    }
                }
            }
        }
    }

    private boolean isExpired( Date issued ) {
        long dif = ( System.currentTimeMillis() - issued.getTime() ) / 1000;
        return dif > nonceValiditySeconds;
    }
    
    private void createOrUpdateGssNonce(Nonce nonce){
    	try{
	    	WebDavNonce non = getService().getWebDavNonce(nonce.getValue().toString());
	    	if(non==null){
	    		non = new WebDavNonce();
	    		non.setId(nonce.getValue().toString());
	    	}
	    	non.setIssued(nonce.getIssued());
	    	non.setNonceCount(nonce.getNonceCount());
	    	getService().saveOrUpdateWebDavNonce(non);
    	}
    	catch(Exception ex){
    		throw new RuntimeException("Unable to save or update nonce",ex);
    	}
    }
    
    private Nonce getNonce(String id){
    	try{
	    	WebDavNonce non = getService().getWebDavNonce(id);
	    	if(non!=null){
	    		Nonce nonce = new Nonce(UUID.fromString(id), non.getIssued());
	    		nonce.increaseNonceCount(non.getNonceCount());
	    		return nonce;
	    	}
    	}
    	catch(Exception ex){
    		throw new RuntimeException("Unable to retrieve nonce",ex);
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
	/**
     * IE seems to send nc (nonce count) parameters out of order. To correctly
     * implement checking we need to record which nonces have been sent, and not
     * assume they will be sent in a monotonically increasing sequence.
     *
     * The quick fix here is to disable checking of the nc param, since other
     * common servers seem to do so to.
     *
     * Note that this will allow replay attacks.
     *
     * @return
     */
    public boolean isEnableNonceCountChecking() {
        return enableNonceCountChecking;
    }

   public void setEnableNonceCountChecking( boolean enableNonceCountChecking ) {
       this.enableNonceCountChecking = enableNonceCountChecking;
   }
}
