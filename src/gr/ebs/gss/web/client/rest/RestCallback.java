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

	public RestCallback(String aPath) {
		path = aPath;
	}

	public RestCallback(String aPath, int okCode) {
		path = aPath;
		okcode = okCode;
	}

	@Override
	public void onError(Request request, Throwable exception) {
		handleError(request, exception);
	}

	@Override
	public void onResponseReceived(Request request, Response response) {
		try {
			if (okcode == -1 && response.getStatusCode() == HTTP_OK)
				handleSuccess(deserialize(response));
			//this one is only used for trash handling where empty trash has 201 status code
			else if(okcode !=-1 && (response.getStatusCode() == okcode || response.getStatusCode() == HTTP_OK))
				handleSuccess(deserialize(response));
			else if(response.getStatusCode() == 403)
				RestCommand.sessionExpired();
			else {
				String statusText = "";
				String text = "";
				// Ignore JavaScript errors caused by non-existent text.
				try {
					statusText = response.getStatusText();
				} catch (Exception e) {	}
				try {
					text = response.getText();
				} catch (Exception e) {	}
				handleError(request, new RestException(path, response.getStatusCode(), statusText, text));
			}
		} catch (Exception e) {
			handleError(request,e);
		}
	}

	public abstract void handleSuccess(Object object);

	public abstract void handleError(Request request, Throwable exception);

	public abstract Object deserialize(Response response);

}
