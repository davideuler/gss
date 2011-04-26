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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.MiltonServlet;
import com.bradmcevoy.http.ServletHttpManager;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;

/**
 * @author kman
 *
 */
public class GssMiltonServlet extends MiltonServlet {
	private Logger log = LoggerFactory.getLogger( GssMiltonServlet.class );
	private ServletConfig config;
	
	public void init( ServletConfig config ) throws ServletException {
        try {
            this.config = config;
            GssLockManager lockManager = new GssLockManager();
            //SimpleMemoryNonceProvider nonce = new SimpleMemoryNonceProvider( 60*60*24 );
            GssNonceProvider nonce = new GssNonceProvider( 60*60*24 );
            GssSecurityManager securityManager = new GssSecurityManager("Pithos WebDAV");
            AuthenticationService authService = new AuthenticationService(nonce);
            authService.setDisableBasic(true);
            authService.setDisableDigest(false);
            DefaultWebDavResponseHandler responseHandler = new DefaultWebDavResponseHandler(authService);
            GssWebDavResponseHandler compressHandler = new GssWebDavResponseHandler(responseHandler);
            //CompressingResponseHandler compressHandler = new CompressingResponseHandler(responseHandler);
            GSSResourceFactory resourceFactory = new GSSResourceFactory();
            resourceFactory.setSecurityManager(securityManager);
            resourceFactory.setLockManager(lockManager);
            resourceFactory.setMaxAgeSeconds(3600l);
            resourceFactory.setContextPath("webdav");
            //PreAuthenticationFilter filter = new PreAuthenticationFilter(compressHandler, securityManager,nonce);
            /*ConsoleResourceFactory consoleResourceFactory = new ConsoleResourceFactory(resourceFactory, "/console", "/webdav", Arrays.asList(new com.ettrema.console.LsFactory(),
                        new com.ettrema.console.CdFactory(),
                        new com.ettrema.console.RmFactory(),
                        new com.ettrema.console.HelpFactory(),
                        new com.ettrema.console.CpFactory(),
                        new com.ettrema.console.MkFactory(),
                        new com.ettrema.console.MkdirFactory()), "webdav");
            */
            httpManager = new ServletHttpManager(resourceFactory,compressHandler,authService);
            
            
            	
            /*if(httpManager.getFilters()==null)
            	httpManager.setFilters(new ArrayList<Filter>());
            httpManager.getFilters().add(filter);*/
            //List<AuthenticationHandler> list = new ArrayList();
            //list.add(new DigestAuthenticationHandler(authService));
           // httpManager.addFilter(0, filter);
            
        }catch( Throwable ex ) {
            log.error( "Exception starting milton servlet", ex );
            throw new RuntimeException( ex );
        }
    }
}
