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
import gr.ebs.gss.web.client.rest.MultipleGetCommand.Cached;
import gr.ebs.gss.web.client.rest.resource.FileResource;
import gr.ebs.gss.web.client.rest.resource.FolderResource;
import gr.ebs.gss.web.client.rest.resource.GroupResource;
import gr.ebs.gss.web.client.rest.resource.GroupUserResource;
import gr.ebs.gss.web.client.rest.resource.GroupsResource;
import gr.ebs.gss.web.client.rest.resource.RestResource;
import gr.ebs.gss.web.client.rest.resource.SharedResource;
import gr.ebs.gss.web.client.rest.resource.TrashResource;
import gr.ebs.gss.web.client.rest.resource.UserResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DeferredCommand;


/**
 * @author kman
 *
 */
public abstract class MultipleHeadCommand <T extends RestResource> extends RestCommand {
	String[] paths;
	Class<T> aclass;
	List<T> result = new ArrayList<T>();
	Map<String, Throwable> errors = new HashMap<String, Throwable>();
	private boolean requestSent=false;
	Cached[] cached;

	public MultipleHeadCommand(Class<T> theClass, String[] pathToGet, Cached[] theCached) {
		this(theClass, pathToGet, true, theCached);
	}

	public MultipleHeadCommand(Class<T> theClass, String[] pathToGet, boolean showLoading, Cached[] theCached) {
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator();
		paths = pathToGet;
		this.aclass = theClass;
		this.cached = theCached;
		//sendRequest();
	}

	private void sendRequest() {
		if(requestSent)
			return;
		requestSent=true;
		if(cached!=null)
			for (final Cached c : cached){
				final String path;
				if(aclass.equals(FileResource.class)){
					if(c.uri.indexOf("?") == -1)
						path=c.uri+"?"+Math.random();
					else
						path=c.uri;
				}
				else
					path = fixPath(c.uri);
				DeferredCommand.addCommand(new HeadCommand<T>(aclass,path,false, (T)c.cache) {

					@Override
					public void onComplete() {
						MultipleHeadCommand.this.result.add(getResult());
					}

					@Override
					public void onError(Throwable t) {
						errors.put(path, t);
					}

				});
			}
		else
			for (String pathg : paths) {
				final String path;
				if(aclass.equals(FileResource.class))
					path = pathg;
				else
					path = fixPath(pathg);
				DeferredCommand.addCommand(new HeadCommand<T>(aclass,path,false, null) {
					@Override
					public void onComplete() {
						MultipleHeadCommand.this.result.add(getResult());
					}

					@Override
					public void onError(Throwable t) {
						errors.put(path, t);
					}
				});
			}
	}
	public boolean isComplete() {
		return result.size()+errors.size() == paths.length;
	}

	public List<T> getResult() {
		return result;
	}

	@Override
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
		if (aclass.equals(FolderResource.class)) {
			result1 = new FolderResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(FileResource.class)) {
			result1 = new FileResource(path);
			result1.createFromJSON(response.getHeader("X-GSS-Metadata"));
		} else if (aclass.equals(GroupsResource.class)) {
			result1 = new GroupsResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(TrashResource.class)) {
			result1 = new TrashResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(SharedResource.class)) {
			result1 = new SharedResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(GroupResource.class)) {
			result1 = new GroupResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(GroupUserResource.class)) {
			result1 = new GroupUserResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(UserResource.class)) {
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
