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
import gr.ebs.gss.server.domain.GssLock;
import gr.ebs.gss.server.ejb.ExternalAPI;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.http.fs.LockManager;


/**
 * @author kman
 *
 */
public class GssLockManager implements LockManager {

    private static final Logger log = LoggerFactory.getLogger( GssLockManager.class );
    /**
     * maps current locks by the file associated with the resource
     */
    
    public GssLockManager() {

    }

    public synchronized LockResult lock( LockTimeout timeout, LockInfo lockInfo, LockableResource r ) {
        GssResource resource = (GssResource) r;
        LockToken currentLock = currentLock( resource );
        if( currentLock != null ) {
            return LockResult.failed( LockResult.FailureReason.ALREADY_LOCKED );
        }

        LockToken newToken = new LockToken( UUID.randomUUID().toString(), lockInfo, timeout );
        GssLock newLock = new GssLock( resource.getUniqueId(), newToken);
        
        //locksByFile.put( resource.getUniqueId(), newLock );
        //locksByToken.put( newToken.tokenId, newLock );
        getService().saveOrUpdateLock(newLock);
        return LockResult.success( newToken );
    }

    public synchronized LockResult refresh( String tokenId, LockableResource resource ) {
        GssLock curLock = getService().getLockByToken(tokenId);
        if( curLock == null ) {
            log.debug( "can't refresh because no lock");
            return LockResult.failed( LockResult.FailureReason.PRECONDITION_FAILED );
        } else {
            curLock.setFrom( new Date() );
            return LockResult.success( curLock.toToken() );
        }
    }

    public synchronized void unlock( String tokenId, LockableResource r ) throws NotAuthorizedException {
    	GssResource resource = (GssResource) r;
        LockToken lockToken = currentLock( resource );
        if( lockToken == null ) {
            log.debug( "not locked" );
            return;
        }
        if( lockToken.tokenId.equals( tokenId ) ) {
            removeLock( lockToken );
        } else {
            throw new NotAuthorizedException( resource );
        }
    }

    private LockToken currentLock( GssResource resource ) {
        GssLock curLock = getService().getLockById(resource.getUniqueId());
        if( curLock == null ) return null;
        LockToken token = curLock.toToken();
        if( token.isExpired() ) {
            removeLock( token );
            return null;
        } else {
            return token;
        }
    }

    private void removeLock( LockToken token ) {
        log.debug( "removeLock: " + token.tokenId );
        GssLock currentLock = getService().getLockByToken(token.tokenId);
        if( currentLock != null ) {
            getService().removeLock(currentLock);
        } else {
            log.warn( "couldnt find lock: " + token.tokenId );
        }
    }

    public LockToken getCurrentToken( LockableResource r ) {
    	GssResource resource = (GssResource) r;
        GssLock lock = getService().getLockById( resource.getUniqueId() );
        if( lock == null ) return null;
        LockToken token = new LockToken();
        token.info = new LockInfo( LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lock.lockedByUser, LockInfo.LockDepth.ZERO );
        token.info.lockedByUser = lock.lockedByUser;
        token.timeout = lock.toToken().timeout;
        token.tokenId = lock.getTokenId();
        return token;
    }

    protected ExternalAPI getService() throws RuntimeException {
		try {
			final Context ctx = new InitialContext();
			final Object ref = ctx.lookup(getConfiguration().getString("externalApiPath"));
			return (ExternalAPI) PortableRemoteObject.narrow(ref, ExternalAPI.class);
		} catch (final NamingException e) {
			log.error("Unable to retrieve the ExternalAPI EJB", e);
			throw new RuntimeException("An error occurred while contacting the naming service");
		}
	}

}
