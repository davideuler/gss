/*
 * Copyright 2009 Electronic Business Systems Ltd.
 */
package gr.ebs.gss.client.rest;

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

/**
 * @author kman
 */
public abstract class ExecuteMultipleDelete extends AbstractRestCommand {


	Map<String, Throwable> errors = new HashMap<String, Throwable>();

	List<String> successPaths = new ArrayList<String>();

	String[] paths;

	public ExecuteMultipleDelete(String[] pathToDelete) {
		GSS.get().showLoadingIndicator();
		paths = pathToDelete;
		for (final String pathg : pathToDelete) {
			GWT.log("[DEL]"+pathg, null);
			RestRequestBuilder builder = new RestRequestBuilder("DELETE", pathg);

			try {
				handleHeaders(builder, pathg);
				builder.sendRequest("", new RequestCallback() {

					public void onError(Request arg0, Throwable arg1) {
						errors.put(pathg, arg1);
					}

					public void onResponseReceived(Request arg0, Response arg1) {
						if (arg1.getStatusCode() == 204)
							successPaths.add(pathg);
						else if (arg1.getStatusCode() == 405)
							errors.put(pathg, new InsufficientPermissionsException("You don't have permissions to delete this resource"));
						else
							errors.put(pathg, new RestException(pathg, arg1.getStatusCode(), arg1.getStatusText(), arg1.getText()));
					}

				});
			} catch (Exception ex) {
				errors.put(pathg, ex);
			}
		}
	}

	public boolean isComplete() {
		return errors.size() + successPaths.size() == paths.length;
	}

	public boolean execute() {
		boolean com = isComplete();
		if (com) {
			if(hasErrors())
				for(String p : errors.keySet())
					onError(p, errors.get(p));
			onComplete();
			GSS.get().hideLoadingIndicator();
			return false;
		}
		return true;
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
		GWT.log("-ERRORS-->"+getErrors().size(), null);
		for(String p : getErrors().keySet())
			GWT.log("error:"+p, getErrors().get(p));
	}

	public abstract void onError(String path, Throwable throwable);

}
