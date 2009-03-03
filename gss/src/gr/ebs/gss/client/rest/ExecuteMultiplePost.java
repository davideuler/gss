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
 *
 */
public abstract class ExecuteMultiplePost extends AbstractRestCommand {


	Map<String, Throwable> errors = new HashMap<String, Throwable>();

	List<String> successPaths = new ArrayList<String>();

	String[] paths;

	public ExecuteMultiplePost(String[] pathToDelete, final int okStatusCode) {
		GSS.get().showLoadingIndicator();
		paths = pathToDelete;
		for (final String pathg : pathToDelete) {
			GWT.log("[DEL]"+pathg, null);
			RestRequestBuilder builder = new RestRequestBuilder("POST", pathg);

			try {
				handleHeaders(builder, pathg);
				builder.sendRequest("", new RequestCallback() {

					public void onError(Request arg0, Throwable arg1) {
						errors.put(pathg, arg1);
					}

					public void onResponseReceived(Request arg0, Response arg1) {
						if (arg1.getStatusCode() == okStatusCode)
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


	/**
	 * @param p
	 * @param throwable
	 */
	public abstract void onError(String p, Throwable throwable);

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

	@Override
	public void onError(Throwable t) {


	}

}
