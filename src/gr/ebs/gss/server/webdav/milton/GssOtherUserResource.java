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

import gr.ebs.gss.client.rest.resource.OthersResource;
import gr.ebs.gss.server.domain.dto.UserDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;


/**
 * @author kman
 *
 */
public class GssOtherUserResource implements PropFindableResource,  GetableResource, DigestResource, CollectionResource{
	private static final Logger log = LoggerFactory.getLogger(GssOthersResource.class);
    String host;
    GSSResourceFactory factory;
    UserDTO currentUser;
    UserDTO resource;
	/**
	 * 
	 */
	public GssOtherUserResource(String host, GSSResourceFactory factory, UserDTO resource) {
		this.host=host;
		this.factory=factory;
		this.resource=resource;
		
	}
	
	@Override
	public Date getCreateDate() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public String checkRedirect(Request arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return resource.getName();
	}

	@Override
	public String getUniqueId() {
		return "user:"+resource.getId();
	}

	@Override
	public Long getContentLength() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentType(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getMaxAgeSeconds(Auth arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendContent(OutputStream arg0, Range arg1, Map<String, String> arg2, String arg3) throws IOException, NotAuthorizedException, BadRequestException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Object authenticate(String user, String password) {
        return factory.getSecurityManager().authenticate(user, password);
    }
	@Override
    public Object authenticate( DigestResponse digestRequest ) {
        return (UserDTO) factory.getSecurityManager().authenticate(digestRequest);
        
        
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return factory.getSecurityManager().authorise(request, method, auth, this);
    }
    @Override
    public String getRealm() {
        return factory.getRealm(this.host);
    }

	@Override
	public boolean isDigestAllowed() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Resource child(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends Resource> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

}
