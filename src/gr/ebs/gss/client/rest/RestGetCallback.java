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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author kman
 */
public abstract class RestGetCallback implements RequestCallback {

	private static final int HTTP_OK = 200;

	private AsyncCallback callback;
	private String path;
	private int okcode = -1;

	public RestGetCallback(String aPath, AsyncCallback aCallback) {
		callback = aCallback;
		path = aPath;
	}

	public RestGetCallback(String aPath, AsyncCallback aCallback, int okCode) {
		callback = aCallback;
		path = aPath;
		okcode = okCode;
	}

	@Override
	public void onError(Request request, Throwable exception) {
		callback.onFailure(exception);
	}

	@Override
	public void onResponseReceived(Request request, Response response) {
		try {
			if (okcode == -1 && response.getStatusCode() == HTTP_OK)
				callback.onSuccess(deserialize(response));
			//this one is only used for trash handling where empty trash has 201 status code
			else if(okcode !=-1 && (response.getStatusCode() == okcode || response.getStatusCode() == HTTP_OK))
				callback.onSuccess(deserialize(response));
			else
				callback.onFailure(new RestException(path, response.getStatusCode(), response.getStatusText(), response.getText()));
		} catch (Exception e) {
			callback.onFailure(e);
		}
	}

	public abstract Object deserialize(Response response);

}
