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

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.CompressingResponseHandler;
import com.bradmcevoy.http.MiltonServlet;
import com.bradmcevoy.http.ServletHttpManager;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;
import com.ettrema.console.ConsoleResourceFactory;


/**
 * @author kman
 *
 */
public class GssMiltonServlet extends MiltonServlet{
	private Logger log = LoggerFactory.getLogger( GssMiltonServlet.class );
	private ServletConfig config;
	
	public void init( ServletConfig config ) throws ServletException {
        try {
            this.config = config;
            GssLockManager lockManager = new GssLockManager();
            GssSecurityManager securityManager = new GssSecurityManager("Pithos WebDAV");
            AuthenticationService authService = new GssAuthenticationService();
            authService.setDisableBasic(true);
            authService.setDisableDigest(false);
            DefaultWebDavResponseHandler responseHandler = new DefaultWebDavResponseHandler(authService);
            CompressingResponseHandler compressHandler = new CompressingResponseHandler(responseHandler);
            GSSResourceFactory resourceFactory = new GSSResourceFactory();
            resourceFactory.setSecurityManager(securityManager);
            resourceFactory.setLockManager(lockManager);
            resourceFactory.setMaxAgeSeconds(3600l);
            resourceFactory.setContextPath("webdav");
            
            ConsoleResourceFactory consoleResourceFactory = new ConsoleResourceFactory(resourceFactory, "/console", "/webdav", Arrays.asList(new com.ettrema.console.LsFactory(),
                        new com.ettrema.console.CdFactory(),
                        new com.ettrema.console.RmFactory(),
                        new com.ettrema.console.HelpFactory(),
                        new com.ettrema.console.CpFactory(),
                        new com.ettrema.console.MkFactory(),
                        new com.ettrema.console.MkdirFactory()), "webdav");
            httpManager = new ServletHttpManager(consoleResourceFactory,compressHandler,authService);
        }catch( Throwable ex ) {
            log.error( "Exception starting milton servlet", ex );
            throw new RuntimeException( ex );
        }
    }
}
