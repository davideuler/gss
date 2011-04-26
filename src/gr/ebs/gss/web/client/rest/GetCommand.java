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
package gr.ebs.gss.web.client.rest;

import gr.ebs.gss.web.client.GSS;
import gr.ebs.gss.web.client.rest.resource.FileResource;
import gr.ebs.gss.web.client.rest.resource.FolderResource;
import gr.ebs.gss.web.client.rest.resource.GroupResource;
import gr.ebs.gss.web.client.rest.resource.GroupUserResource;
import gr.ebs.gss.web.client.rest.resource.GroupsResource;
import gr.ebs.gss.web.client.rest.resource.OtherUserResource;
import gr.ebs.gss.web.client.rest.resource.OthersResource;
import gr.ebs.gss.web.client.rest.resource.RestResource;
import gr.ebs.gss.web.client.rest.resource.SearchResource;
import gr.ebs.gss.web.client.rest.resource.SharedResource;
import gr.ebs.gss.web.client.rest.resource.TagsResource;
import gr.ebs.gss.web.client.rest.resource.TrashResource;
import gr.ebs.gss.web.client.rest.resource.UploadStatusResource;
import gr.ebs.gss.web.client.rest.resource.UserResource;
import gr.ebs.gss.web.client.rest.resource.UserSearchResource;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;

/**
 * @author kman
 */
public abstract class GetCommand<T extends RestResource> extends RestCommand{

	boolean complete = false;
	T result = null;
	Throwable exception = null;
	Class<T> aclass;
	private final String path;
	private String username;
	private boolean requestSent = false;
	T cached;
	
	private static final long MAX_CACHE_AGE = 1000;
	
	private static class RequestData {
		public String path;
		public String username;
		
		public RequestData(String _path, String _username) {
			path = _path;
			username = _username;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result + ((username == null) ? 0 : username.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RequestData other = (RequestData) obj;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			if (username == null) {
				if (other.username != null)
					return false;
			} else if (!username.equals(other.username))
				return false;
			return true;
		}
	}
	
	private static class ResponseData {
		public long timestamp;
		public Object result;
		public ResponseData(long _timestamp, Object _result) {
			timestamp = _timestamp;
			result = _result;
		}
	}
	
	private static Map<RequestData, ResponseData> cache = new HashMap<RequestData, ResponseData>();
	

	public GetCommand(Class<T> theclass, String pathToGet, T theCached){
		this(theclass, pathToGet, true, theCached);
	}

	public GetCommand(Class<T> theclass, String pathToGet, boolean showLoading, T theCached){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator("Getting ",pathToGet);
		this.aclass = theclass;
		if(pathToGet.indexOf("?") != -1)
			path = pathToGet;
		else
			path =fixPath(pathToGet);
		this.cached = theCached;
	}

	public GetCommand(Class<T> theclass, String aUsername , String pathToGet, T theCached){
		this(theclass, aUsername, pathToGet, true, theCached);
	}

	public GetCommand(Class<T> theclass, String aUsername , String pathToGet, boolean showLoading, T theCached){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator("Getting ",pathToGet);
		this.aclass = theclass;
		path = fixPath(pathToGet);
		this.username = aUsername;
		this.cached = theCached;
	}

	private void sendRequest(){
		if(requestSent)
			return;
		requestSent=true;
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, path);
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
				public void handleError(Request request, Throwable _exception) {
					result = null;
					complete = true;
					exception = _exception;
					if(_exception instanceof RestException)
						if(((RestException)_exception).getHttpStatusCode() == 304 && cached != null){
							GWT.log("Using cache:"+cached.getUri(), null);
							handleSuccess(cached);
							return;
						}
					
				}

				@Override
				public void handleSuccess(Object object) {
					result = (T) object;
					complete = true;
				}

			});
		} catch (Exception ex) {
			complete = true;
			exception = ex;
		}
	}
	public boolean isComplete() {
		return complete;
	}

	public T getResult(){
		return result;
	}

	@Override
	public boolean execute() {
		boolean com = isComplete();
		RequestData key = new RequestData(path, username);
		if (!com) {
			if (cache.containsKey(key)) {
				ResponseData resp = cache.get(key);
				if (resp==null) {
					return true;
				}
				
				// Cache hit
				if (System.currentTimeMillis()-resp.timestamp>MAX_CACHE_AGE) {
					// Cache stale, remove
					cache.put(key,null);
				}
				else {
					// Use cache data
					if(isShowLoadingIndicator())
						GSS.get().hideLoadingIndicator();
					if (resp.result instanceof Throwable) {
						// Error to be handled
						Throwable ex = (Throwable) resp.result;
						onError(ex);
						return false;
					}
					result = (T) resp.result;
					if (result != null) {
						onComplete();
					}
					complete = true;
					return false;
				}
			}
		
			if(!requestSent) {
				cache.put(key,null);
				sendRequest();
			}
		}
		
		if(com){
			if(isShowLoadingIndicator())
				GSS.get().hideLoadingIndicator();
			if(getResult() != null) {
				// Add to cache
				cache.put(key, new ResponseData(System.currentTimeMillis(), getResult()));
				onComplete();
			}
			else {
				cache.put(key, new ResponseData(System.currentTimeMillis(), exception));
				onError(exception);
			}
			return false;
		}
		return true;
	}

	public Object deserializeResponse(String aPath, Response response) {
		RestResource result1 = null;
		if(aclass.equals(FolderResource.class)){
			result1 = new FolderResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(FileResource.class)){
			result1 = new FileResource(aPath);
			result1.createFromJSON(response.getHeader("X-GSS-Metadata"));
		}
		else if(aclass.equals(GroupsResource.class)){
			result1 = new GroupsResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(TrashResource.class)){
			result1 = new TrashResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(SharedResource.class)){
			result1 = new SharedResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(OthersResource.class)){
			result1 = new OthersResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(OtherUserResource.class)){
			result1 = new OtherUserResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(GroupResource.class)){
			result1 = new GroupResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(GroupUserResource.class)){
			result1 = new GroupUserResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(UserResource.class)){
			result1 = new UserResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(TagsResource.class)){
			result1 = new TagsResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(SearchResource.class)){
			result1 = new SearchResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(UserSearchResource.class)){
			result1 = new UserSearchResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(UploadStatusResource.class)){
			result1 = new UploadStatusResource(aPath);
			result1.createFromJSON(response.getText());
		}
		return result1;
	}

	public T getCached() {
		return cached;
	}

	public void setCached(T theCached) {
		this.cached = theCached;
	}
	
	public boolean usedCachedVersion(){
		if(exception !=null && exception instanceof RestException)
			if(((RestException)exception).getHttpStatusCode() == 304){
				return true;
			}
		return false;
	}
}
