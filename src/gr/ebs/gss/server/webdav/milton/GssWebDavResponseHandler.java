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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.AbstractWrappingResponseHandler;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.DefaultHttp11ResponseHandler;
import com.bradmcevoy.http.http11.DefaultHttp11ResponseHandler.BUFFERING;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;
import com.bradmcevoy.http.webdav.WebDavResponseHandler;


/**
 * @author kman
 *
 */
public class GssWebDavResponseHandler extends AbstractWrappingResponseHandler {

    private static final Logger log = LoggerFactory.getLogger( GssWebDavResponseHandler.class );
    
    

    public GssWebDavResponseHandler() {
    }

    public GssWebDavResponseHandler( WebDavResponseHandler wrapped ) {
        super( wrapped );
        if(wrapped instanceof DefaultWebDavResponseHandler){
        	((DefaultWebDavResponseHandler)wrapped).setBuffering(BUFFERING.always);
        }
    }

   

    @Override
    public void respondContent( Resource resource, Response response, Request request, Map<String, String> params ) throws NotAuthorizedException, BadRequestException {
        if( resource instanceof GetableResource ) {
            GetableResource r = (GetableResource) resource;
            log.trace( "respondContent: not compressable" );
            response.setVaryHeader( "Accept-Encoding" );
            wrapped.respondContent( resource, response, request, params );
            
        } else {
            throw new RuntimeException( "Cant generate content for non-Getable resource: " + resource.getClass() );
        }
    }

    protected void setRespondContentCommonHeaders( Response response, Resource resource, Response.Status status, Auth auth ) {
        response.setStatus( status );
        response.setDateHeader( new Date() );
        String etag = wrapped.generateEtag( resource );
        if( etag != null ) {
            response.setEtag( etag );
        }
        DefaultHttp11ResponseHandler.setModifiedDate( response, resource, auth );
    }

    private boolean canCompress( GetableResource r, String contentType, String acceptableEncodings ) {
        log.trace( "canCompress: contentType: " + contentType + " acceptable-encodings: " + acceptableEncodings );
        if( contentType != null ) {
            contentType = contentType.toLowerCase();
            boolean contentIsCompressable = contentType.contains( "text" ) || contentType.contains( "css" ) || contentType.contains( "js" ) || contentType.contains( "javascript" );
            if( contentIsCompressable ) {
                boolean supportsGzip = ( acceptableEncodings != null && acceptableEncodings.toLowerCase().indexOf( "gzip" ) > -1 );
                log.trace( "supports gzip: " + supportsGzip );
                return supportsGzip;
            }
        }
        return false;
    }

    

}
