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
import gr.ebs.gss.client.exceptions.InsufficientPermissionsException;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;


/**
 * @author kman
 *
 */
public abstract class DeleteCommand extends RestCommand{

	boolean complete = false;

	public DeleteCommand(String pathToDelete){
		this(pathToDelete, true);
	}


	public DeleteCommand(String pathToDelete, boolean showLoading){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator();
		final String path;
		if(pathToDelete.endsWith("/"))
			path = pathToDelete;
		else
			path = pathToDelete+"/";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, path);

		try {
			handleHeaders(builder, path);
			builder.sendRequest("", new RequestCallback() {

				@Override
				public void onError(Request arg0, Throwable arg1) {
					complete = true;
					DeleteCommand.this.onError(arg1);
				}

				@Override
				public void onResponseReceived(Request arg0, Response arg1) {
					complete=true;
					if(arg1.getStatusCode() == 204)
						onComplete();
					else if(arg1.getStatusCode() == 403)
						sessionExpired();
					else if(arg1.getStatusCode() == 405)
						DeleteCommand.this.onError(new InsufficientPermissionsException("You don't have permissions to delete this resource"));
					else
						DeleteCommand.this.onError(new RestException(path, arg1.getStatusCode(), arg1.getStatusText(), arg1.getText()));
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

	@Override
	public boolean execute() {
		boolean com = isComplete();
		if(com){
			if(isShowLoadingIndicator())
				GSS.get().hideLoadingIndicator();
			return false;
		}
		return true;
	}

}
