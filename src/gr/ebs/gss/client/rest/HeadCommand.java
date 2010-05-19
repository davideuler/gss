/*
 * Copyright 2009 Electronic Business Systems Ltd.
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
package gr.ebs.gss.client.rest;

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.exceptions.ObjectNotFoundException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.GroupUserResource;
import gr.ebs.gss.client.rest.resource.GroupsResource;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashResource;
import gr.ebs.gss.client.rest.resource.UserResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;


/**
 * @author kman
 *
 */
public  abstract class HeadCommand <T extends RestResource> extends RestCommand{

	boolean complete = false;
	T result = null;
	Class<T> aclass;
	private boolean requestSent = false;
	T cached;
	final String path;
	public HeadCommand(Class<T> aclass, String pathToGet, T cached){
		this(aclass, pathToGet, true, cached);
	}
	public HeadCommand(Class<T> aclass, String pathToGet, boolean showLoading, T cached){
		setShowLoadingIndicator(showLoading);
		this.aclass = aclass;
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator();

		if(aclass.equals(FileResource.class))
			path = pathToGet;
		else
			path = fixPath(pathToGet);
		this.cached = cached;

	}

	private void sendRequest(){
		if(requestSent)
			return;
		requestSent=true;
		RestRequestBuilder builder = new RestRequestBuilder("HEAD", path);
		if(cached!=null && cached.getLastModifiedSince() != null){
			GWT.log("ADDING IF MODIFIED HEADERS", null);
			builder.setHeader("If-Modified-Since", cached.getLastModifiedSince());
		}
		try {
			handleHeaders(builder, path);
			builder.sendRequest("", new RestCallback(path) {

				public Object deserialize(Response response) {
					return deserializeResponse(path, response);
				}

				public void handleError(Request request, Throwable exception) {
					if(exception instanceof RestException)
						if(((RestException)exception).getHttpStatusCode() == 304 && cached != null){
							GWT.log("Using cache:"+cached.getUri(), null);
							handleSuccess(cached);
							return;
						}
					complete = true;
					HeadCommand.this.onError(exception);
				}

				public void handleSuccess(Object object) {
					result = (T) object;
					complete = true;
				}

			});
		} catch (Exception ex) {
			complete = true;
			onError(ex);
		}
	}


	public boolean isComplete() {
		return complete;
	}

	public T getResult(){
		return result;
	}

	public boolean execute() {
		if(!requestSent)
			sendRequest();
		boolean com = isComplete();
		if(com){
			if(isShowLoadingIndicator())
				GSS.get().hideLoadingIndicator();
			if(getResult() != null)
				onComplete();
			else
				onError(new ObjectNotFoundException("Resource Not Found"));
			return false;
		}
		return true;
	}

	public  Object deserializeResponse(String path, Response response){
		RestResource result1 = null;
		if(aclass.equals(FolderResource.class)){
			result1 = new FolderResource(path);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(FileResource.class)){
			result1 = new FileResource(path);
			result1.createFromJSON(response.getHeader("X-GSS-Metadata"));
		}
		else if(aclass.equals(GroupsResource.class)){
			result1 = new GroupsResource(path);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(TrashResource.class)){
			result1 = new TrashResource(path);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(SharedResource.class)){
			result1 = new SharedResource(path);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(GroupResource.class)){
			result1 = new GroupResource(path);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(GroupUserResource.class)){
			result1 = new GroupUserResource(path);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(UserResource.class)){
			result1 = new UserResource(path);
			result1.createFromJSON(response.getText());

		}
		return result1;

	}

}
