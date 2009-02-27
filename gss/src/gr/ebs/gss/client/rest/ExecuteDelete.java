/*
 * Copyright 2009 Electronic Business Systems Ltd.
 */
package gr.ebs.gss.client.rest;

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;


/**
 * @author kman
 *
 */
public abstract class ExecuteDelete extends AbstractRestCommand{

	boolean complete = false;

	public ExecuteDelete(String pathToDelete){
		GSS.get().showLoadingIndicator();
		final String path;
		if(pathToDelete.endsWith("/"))
			path = pathToDelete;
		else
			path = pathToDelete+"/";
		RestRequestBuilder builder = new RestRequestBuilder("DELETE", path);

		try {
			handleHeaders(builder, path);
			builder.sendRequest("", new RequestCallback() {


				public void onError(Request arg0, Throwable arg1) {
					complete = true;
					ExecuteDelete.this.onError(arg1);
				}


				public void onResponseReceived(Request arg0, Response arg1) {
					complete=true;
					if(arg1.getStatusCode() == 204)
						onComplete();
					else if(arg1.getStatusCode() == 405)
						ExecuteDelete.this.onError(new InsufficientPermissionsException("You don't have permissions to delete this resource"));
					else
						ExecuteDelete.this.onError(new RestException(path, arg1.getStatusCode(), arg1.getStatusText(), arg1.getText()));
				}

			});
		} catch (Exception ex) {
			complete=true;
			onError(ex);
		}
	}

	public boolean isComplete() {
		return complete;
	}

	public boolean execute() {
		boolean com = isComplete();
		if(com){
			GSS.get().hideLoadingIndicator();
			return false;
		}
		return true;
	}

}
