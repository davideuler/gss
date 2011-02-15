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

import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.exceptions.RpcException;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;
import gr.ebs.gss.server.domain.dto.FolderDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;


/**
 * @author kman
 *
 */
public class GssRootFolderResource extends GssFolderResource{

	/**
	 * @param host
	 * @param factory
	 * @param resource
	 */
	public GssRootFolderResource(String host, GSSResourceFactory factory, Object resource) {
		super(host, factory, resource);
		
	}
	
	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		// TODO Auto-generated method stub
		boolean result = factory.getSecurityManager().authorise(request, method, auth, this);
        if(result){
        	UserDTO user = getCurrentUser();
        	try {
				this.resource = factory.getResourceGss("/");//getService().getFolder(user.getId(), folder.getId());
			} catch (RpcException e) {
				return false;
			}
			return true;
        }
        return result;
    }
	
	
	@Override
	public Object authenticate(DigestResponse digestRequest) {
		// TODO Auto-generated method stub
		return super.authenticate(digestRequest);
	}
	
	@Override
	public String getName() {
		return "/";
	}
	@Override
	public String getUniqueId() {
		return "folder:/";
	}
	@Override
	public Resource child(String name) {
		try {
			this.folder = (FolderDTO) factory.getResourceGss("/");
		} catch (RpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.child(name);
	}
	@Override
	public List<? extends Resource> getChildren() {
		try {
			this.folder = (FolderDTO) factory.getResourceGss("/");
		} catch (RpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Resource> result = new ArrayList<Resource>();
		for(FolderDTO f : folder.getSubfolders())
			if(!f.isDeleted())
				result.add(new GssFolderResource(host, factory, f));
		try {
			for(FileHeaderDTO f : factory.getService().getFiles(getCurrentUser().getId(), folder.getId(), true))
				result.add(new GssFileResource(host, factory, f));
		} catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InsufficientPermissionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//result.add(new GssOthersResource(host, factory));
		return result;
	}

}
