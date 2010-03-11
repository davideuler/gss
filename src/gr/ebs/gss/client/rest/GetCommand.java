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
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.GroupUserResource;
import gr.ebs.gss.client.rest.resource.GroupsResource;
import gr.ebs.gss.client.rest.resource.OtherUserResource;
import gr.ebs.gss.client.rest.resource.OthersResource;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.SearchResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TagsResource;
import gr.ebs.gss.client.rest.resource.TrashResource;
import gr.ebs.gss.client.rest.resource.UploadStatusResource;
import gr.ebs.gss.client.rest.resource.UserResource;
import gr.ebs.gss.client.rest.resource.UserSearchResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

/**
 * @author kman
 */
public abstract class GetCommand<T extends RestResource> extends RestCommand{

	boolean complete = false;
	T result = null;
	Class<T> aclass;
	private final String path;
	private String username;
	private boolean requestSent = false;
	T cached;

	public GetCommand(Class<T> theclass, String pathToGet, T cached){
		this(theclass,pathToGet,true,cached);
	}

	public GetCommand(Class<T> theclass, String pathToGet, boolean showLoading, T cached){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator();
		this.aclass = theclass;
		if(pathToGet.indexOf("?") != -1)
			path = pathToGet;
		else
			path =fixPath(pathToGet);
		this.cached = cached;

	}

	public GetCommand(Class<T> theclass, String username , String pathToGet, T cached){
		this(theclass,username, pathToGet, true, cached);
	}

	public GetCommand(Class<T> theclass, String username , String pathToGet, boolean showLoading, T cached){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator();
		this.aclass = theclass;
		path = fixPath(pathToGet);
		this.username = username;
		this.cached = cached;
	}

	private void sendRequest(){
		if(requestSent)
			return;
		requestSent=true;
		RestRequestBuilder builder = new RestRequestBuilder("GET", path);
		if(cached!=null && cached.getLastModifiedSince() != null)
			builder.setHeader("If-Modified-Since", cached.getLastModifiedSince());
		try {
			if(username == null)
				handleHeaders(builder, path);
			else
				handleHeaders(username, builder, path);
			builder.sendRequest("", new RestCallback(path) {

				@Override
				public Object deserialize(Response response) {
					return deserializeResponse(path, response);
				}

				@Override
				public void handleError(Request request, Throwable exception) {
					if(exception instanceof RestException)
						if(((RestException)exception).getHttpStatusCode() == 304 && cached != null){
							GWT.log("Using cache:"+cached.getUri(), null);
							handleSuccess(cached);
							return;
						}
					complete = true;
					GetCommand.this.onError(exception);
				}

				@Override
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
			return false;
		}
		return true;
	}

	public Object deserializeResponse(String path, Response response) {
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
		else if(aclass.equals(OthersResource.class)){
			result1 = new OthersResource(path);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(OtherUserResource.class)){
			result1 = new OtherUserResource(path);
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
		else if(aclass.equals(TagsResource.class)){
			result1 = new TagsResource(path);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(SearchResource.class)){
			result1 = new SearchResource(path);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(UserSearchResource.class)){
			result1 = new UserSearchResource(path);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(UploadStatusResource.class)){
			result1 = new UploadStatusResource(path);
			result1.createFromJSON(response.getText());
		}
		return result1;
	}


	public T getCached() {
		return cached;
	}


	public void setCached(T cached) {
		this.cached = cached;
	}



}
