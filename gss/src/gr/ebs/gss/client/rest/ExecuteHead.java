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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;


/**
 * @author kman
 *
 */
public  abstract class ExecuteHead <T extends RestResource> extends AbstractRestCommand{

	boolean complete = false;
	T result = null;
	Class<T> aclass;

	public ExecuteHead(Class<T> aclass, String pathToGet){
		this.aclass = aclass;
		GSS.get().showLoadingIndicator();
		final String path;
		if(aclass.equals(FileResource.class))
			path = pathToGet;
		else
			path = fixPath(pathToGet);
		RestRequestBuilder builder = new RestRequestBuilder("HEAD", path);

		try {
			handleHeaders(builder, path);
			builder.sendRequest("", new RestCallback(path) {

				public Object deserialize(Response response) {
					return deserializeResponse(path, response);
				}

				public void handleError(Request request, Throwable exception) {
					complete = true;
					ExecuteHead.this.onError(exception);
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
		boolean com = isComplete();
		if(com){
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
