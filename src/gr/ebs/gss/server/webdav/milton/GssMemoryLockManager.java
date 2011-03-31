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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
public class GssMemoryLockManager implements LockManager {

    private static final Logger log = LoggerFactory.getLogger( GssMemoryLockManager.class );
    /**
     * maps current locks by the file associated with the resource
     */
    Map<String, CurrentLock> locksByFile;
    Map<String, CurrentLock> locksByToken;

    public GssMemoryLockManager() {
        locksByFile = new HashMap<String, CurrentLock>();
        locksByToken = new HashMap<String, CurrentLock>();
    }

    public synchronized LockResult lock( LockTimeout timeout, LockInfo lockInfo, LockableResource r ) {
        GssResource resource = (GssResource) r;
        LockToken currentLock = currentLock( resource );
        if( currentLock != null ) {
            return LockResult.failed( LockResult.FailureReason.ALREADY_LOCKED );
        }

        LockToken newToken = new LockToken( UUID.randomUUID().toString(), lockInfo, timeout );
        CurrentLock newLock = new CurrentLock( resource.getUniqueId(), newToken, lockInfo.lockedByUser );
        locksByFile.put( resource.getUniqueId(), newLock );
        locksByToken.put( newToken.tokenId, newLock );
        return LockResult.success( newToken );
    }

    public synchronized LockResult refresh( String tokenId, LockableResource resource ) {
        CurrentLock curLock = locksByToken.get( tokenId );
        if( curLock == null ) {
            log.debug( "can't refresh because no lock");
            return LockResult.failed( LockResult.FailureReason.PRECONDITION_FAILED );
        } else {
            curLock.token.setFrom( new Date() );
            return LockResult.success( curLock.token );
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
        CurrentLock curLock = locksByFile.get( resource.getUniqueId() );
        if( curLock == null ) return null;
        LockToken token = curLock.token;
        if( token.isExpired() ) {
            removeLock( token );
            return null;
        } else {
            return token;
        }
    }

    private void removeLock( LockToken token ) {
        log.debug( "removeLock: " + token.tokenId );
        CurrentLock currentLock = locksByToken.get( token.tokenId );
        if( currentLock != null ) {
            locksByFile.remove( currentLock.file );
            locksByToken.remove( currentLock.token.tokenId );
        } else {
            log.warn( "couldnt find lock: " + token.tokenId );
        }
    }

    public LockToken getCurrentToken( LockableResource r ) {
    	GssResource resource = (GssResource) r;
        CurrentLock lock = locksByFile.get( resource.getUniqueId() );
        if( lock == null ) return null;
        LockToken token = new LockToken();
        token.info = new LockInfo( LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lock.lockedByUser, LockInfo.LockDepth.ZERO );
        token.info.lockedByUser = lock.lockedByUser;
        token.timeout = lock.token.timeout;
        token.tokenId = lock.token.tokenId;
        return token;
    }

    class CurrentLock {

        final String file;
        final LockToken token;
        final String lockedByUser;

        public CurrentLock( String file, LockToken token, String lockedByUser ) {
            this.file = file;
            this.token = token;
            this.lockedByUser = lockedByUser;
        }
    }

}
