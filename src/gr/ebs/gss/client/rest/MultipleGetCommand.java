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
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DeferredCommand;

/**
 * @author kman
 */
public abstract class MultipleGetCommand<T extends RestResource> extends RestCommand {

	Class<T> aclass;
	List<T> result = new ArrayList<T>();
	Map<String, Throwable> errors = new HashMap<String, Throwable>();
	Cached[] cached;
	String[] paths;
	private boolean requestSent=false;

	public MultipleGetCommand(Class<T> aNewClass, String[] pathToGet, Cached[] cached ){
		this(aNewClass, pathToGet, true, cached);
	}

	public MultipleGetCommand(Class<T> aNewClass, String[] pathToGet, boolean showLoading, Cached[] cached){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator();
		aclass = aNewClass;
		paths = pathToGet;
		this.cached = cached;
		//sendRequest();
	}

	private void sendRequest(){
		if(requestSent)
			return;
		requestSent=true;
		if(cached!=null)
			for (final Cached pathg : cached)
				DeferredCommand.addCommand(new GetCommand<T>(aclass,pathg.uri,false,(T)pathg.cache) {


					@Override
					public void onComplete() {
						MultipleGetCommand.this.result.add(getResult());
					}

					@Override
					public void onError(Throwable t) {
						errors.put(pathg.uri, t);

					}

				});
		else
			for (final String pathg : paths)
				DeferredCommand.addCommand(new GetCommand<T>(aclass,pathg,false,null) {


					@Override
					public void onComplete() {
						MultipleGetCommand.this.result.add(getResult());
					}

					@Override
					public void onError(Throwable t) {
						errors.put(pathg, t);

					}

				});
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
		if(!requestSent)
			sendRequest();
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

	protected void debug(){
		GWT.log("--->"+result.size(), null);
		GWT.log("-ERRORS-->"+getErrors().size(), null);
		for(String p : getErrors().keySet())
			GWT.log("error:"+p, getErrors().get(p));
	}


	public static class Cached {
		public String uri;
		public RestResource cache;
	}
}
