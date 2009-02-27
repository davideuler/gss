/*
 * Copyright 2009 Electronic Business Systems Ltd.
 */
package gr.ebs.gss.client.rest;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;


/**
 * @author kman
 *
 */
public abstract class RestCallback  implements RequestCallback {

	private static final int HTTP_OK = 200;
	private String path;
	private int okcode = -1;

	public RestCallback(String path) {
		this.path = path;
	}

	public RestCallback(String path, int okCode) {
		this.path = path;
		okcode = okCode;
	}

	public void onError(Request request, Throwable exception) {
		handleError(request, exception);
	}

	public void onResponseReceived(Request request, Response response) {
		try {
			if (okcode == -1 && response.getStatusCode() == HTTP_OK)
				handleSuccess(deserialize(response));
			//this one is only used for trash handling where empty trash has 201 status code
			else if(okcode !=-1 && (response.getStatusCode() == okcode || response.getStatusCode() == HTTP_OK))
				handleSuccess(deserialize(response));
			else
				handleError(request, new RestException(path, response.getStatusCode(), response.getStatusText(), response.getText()));
		} catch (Exception e) {
			handleError(request,e);
		}
	}

	public abstract void handleSuccess(Object object);

	public abstract void handleError(Request request, Throwable exception);

	public abstract Object deserialize(Response response);

}
