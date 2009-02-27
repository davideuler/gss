/*
 * Copyright 2009 Electronic Business Systems Ltd.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;


/**
 * @author kman
 *
 */
public abstract class ExecuteMultipleHead <T extends RestResource> extends AbstractRestCommand {
	String[] paths;
	Class<T> aclass;
	List<T> result = new ArrayList<T>();
	Map<String, Throwable> errors = new HashMap<String, Throwable>();

	public ExecuteMultipleHead(Class<T> aclass, String[] pathToGet) {
		GSS.get().showLoadingIndicator();
		paths = pathToGet;
		this.aclass = aclass;
		for (String pathg : pathToGet) {
			final String path;
			if(aclass.equals(FileResource.class))
				path = pathg;
			else
				path = fixPath(pathg);
			RestRequestBuilder builder = new RestRequestBuilder("HEAD", path);

			try {
				handleHeaders(builder, path);
				builder.sendRequest("", new RestCallback(path) {

					public Object deserialize(Response response) {
						return deserializeResponse(path, response);
					}

					public void handleError(Request request, Throwable exception) {
						errors.put(path, exception);
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
		return result;
	}

	public boolean execute() {
		boolean com = isComplete();
		if (com) {
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
			((FileResource) result1).setContentType(response.getHeader("Content-Type"));
			((FileResource) result1).setContentLength(new Long(response.getHeader("Content-Length")));
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