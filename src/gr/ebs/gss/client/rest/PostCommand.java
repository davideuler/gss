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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;


/**
 * @author kman
 *
 */
public abstract class PostCommand extends RestCommand{
	boolean complete = false;
	String postBody=null;

	public PostCommand(final String path, String data, final int okStatusCode){
		this(path, data, okStatusCode, true);
	}
	public PostCommand(final String path, String data, final int okStatusCode, boolean showLoading){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator();

		RestRequestBuilder builder = new RestRequestBuilder("POST", path);

		try {
			handleHeaders(builder, path);
			builder.sendRequest(data, new RequestCallback() {


				public void onError(Request arg0, Throwable arg1) {
					complete = true;
					PostCommand.this.onError(arg1);
				}


				public void onResponseReceived(Request arg0, Response arg1) {
					complete=true;
					if(arg1.getStatusCode() == okStatusCode){
						postBody = arg1.getText();
						onComplete();
					}
					else if(arg1.getStatusCode() == 403)
						sessionExpired();
					else
						PostCommand.this.onError(new RestException(path, arg1.getStatusCode(), arg1.getStatusText(), arg1.getText()));
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
			if(isShowLoadingIndicator())
				GSS.get().hideLoadingIndicator();
			return false;
		}
		return true;
	}


	/**
	 * Retrieve the postBody.
	 *
	 * @return the postBody
	 */
	public String getPostBody() {
		return postBody;
	}


}