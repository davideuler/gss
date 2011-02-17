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
package gr.ebs.gss.client.rest.resource;


/**
 * @author kman
 *
 */
public class RestResourceWrapper extends RestResource{
	FolderResource resource;
	
	public RestResourceWrapper(FolderResource resource) {
		super(resource.getUri());
		this.resource=resource;
	}
	
	
	/**
	 * Modify the resource.
	 *
	 * @param resource the resource to set
	 */
	public void setResource(FolderResource resource) {
		this.resource = resource;
	}
	/**
	 * Retrieve the resource.
	 *
	 * @return the resource
	 */
	public FolderResource getResource() {
		return resource;
	}
	
	@Override
	public void createFromJSON(String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLastModifiedSince() {
		// TODO Auto-generated method stub
		return null;
	}

}
