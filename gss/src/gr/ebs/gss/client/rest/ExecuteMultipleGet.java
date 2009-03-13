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
import gr.ebs.gss.client.rest.resource.OtherUserResource;
import gr.ebs.gss.client.rest.resource.OthersResource;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashResource;
import gr.ebs.gss.client.rest.resource.UserResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

/**
 * @author kman
 */
public abstract class ExecuteMultipleGet<T extends RestResource> extends AbstractRestCommand {

	Class<T> aclass;
	List<T> result = new ArrayList<T>();
	Map<String, Throwable> errors = new HashMap<String, Throwable>();
	String[] paths;

	public ExecuteMultipleGet(Class<T> aclass, String[] pathToGet){
		this(aclass, pathToGet, true);
	}

	public ExecuteMultipleGet(Class<T> aclass, String[] pathToGet, boolean showLoading){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator();
		this.aclass = aclass;
		this.paths = pathToGet;
		for (String pathg : pathToGet) {
			final String path = fixPath(pathg);
			RestRequestBuilder builder = new RestRequestBuilder("GET",  path);

			try {
				handleHeaders(builder, path);
				builder.sendRequest("", new RestCallback(path) {

					public Object deserialize(Response response) {
						return deserializeResponse(path, response);
					}

					public void handleError(Request request, Throwable exception) {

						errors.put(path, exception);
						//ExecuteMultipleGet.this.onError(exception);
					}

					public void handleSuccess(Object object) {
						if(object!= null)
							result.add((T)object);
						else
							errors.put(path, new ObjectNotFoundException("resource not found"));


					}

				});
			} catch (Exception ex) {
				errors.put(path, ex);
			}
		}
	}

	public boolean isComplete() {
		return result.size()+errors.size() == paths.length;
	}

	public List<T> getResult() {
		if(aclass.equals(FolderResource.class))
			Collections.sort(result, new Comparator(){
				public int compare(Object o1, Object o2) {
					return ((FolderResource)o1).getName().compareTo(((FolderResource)o2).getName());
				}

			});
		else if(aclass.equals(GroupResource.class))
			Collections.sort(result, new Comparator(){
				public int compare(Object o1, Object o2) {
					return ((GroupResource)o1).getName().compareTo(((GroupResource)o2).getName());
				}

			});
		else if(aclass.equals(GroupUserResource.class))
			Collections.sort(result, new Comparator(){
				public int compare(Object o1, Object o2) {
					return ((GroupUserResource)o1).getName().compareTo(((GroupUserResource)o2).getName());
				}

			});
		return result;
	}

	public boolean execute() {
		boolean com = isComplete();
		if (com) {
			if(isShowLoadingIndicator())
				GSS.get().hideLoadingIndicator();
			if(hasErrors())
				for(String p : errors.keySet())
					onError(p, errors.get(p));
			onComplete();
			return false;
		}
		return true;
	}

	/**
	 * @param p
	 * @param throwable
	 */
	public abstract void onError(String p, Throwable throwable);

	public Object deserializeResponse(String path, Response response) {
		RestResource result1 = null;
		if(aclass.equals(FolderResource.class)){
			result1 = new FolderResource(path);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(FileResource.class)){
			result1 = new FileResource(path);
			result1.createFromJSON(response.getHeader("X-GSS-Metadata"));
			((FileResource)result1).setContentType(response.getHeader("Content-Type"));
			((FileResource)result1).setContentLength(new Long(response.getHeader("Content-Length")));
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
		return result1;

	}

	public boolean hasErrors(){
		return errors.size() >0;
	}


	/**
	 * Retrieve the errors.
	 *
	 * @return the errors
	 */
	public Map<String, Throwable> getErrors() {
		return errors;
	}

	public void debug(){
		GWT.log("--->"+result.size(), null);
		GWT.log("-ERRORS-->"+getErrors().size(), null);
		for(String p : getErrors().keySet())
			GWT.log("error:"+p, getErrors().get(p));
	}
}
