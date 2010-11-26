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
package gr.ebs.gss.client;

import java.util.List;

import gr.ebs.gss.client.rest.RestCommand;

import com.google.gwt.core.client.GWT;
import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.httprequest.HttpRequest;
import com.google.gwt.gears.client.httprequest.ProgressEvent;
import com.google.gwt.gears.client.httprequest.ProgressHandler;
import com.google.gwt.gears.client.httprequest.RequestCallback;

/**
 * The 'File upload' dialog box implementation with Google Gears support
 * for IE.
 */
public class FileUploadGearsIEDialog extends FileUploadGearsDialog implements Updateable {

	/**
	 * Perform the HTTP request to upload the specified file.
	 */	
	@Override
	protected void doSend(final List<File> filesRemaining) {
		final GSS app = GSS.get();
		HttpRequest request = factory.createHttpRequest();
		requests.add(request);
		String method = "POST";

		String path;
		final String filename = getFilename(filesRemaining.get(0).getName());
		path = folder.getUri();
		if (!path.endsWith("/"))
			path = path + "/";
		path = path + encode(filename);

		String token = app.getToken();
		String resource = path.substring(app.getApiPath().length()-1, path.length());
		String date = RestCommand.getDate();
		String sig = RestCommand.calculateSig(method, date, resource, RestCommand.base64decode(token));
		request.open(method, path);
		request.setRequestHeader("X-GSS-Date", date);
		request.setRequestHeader("Authorization", app.getCurrentUserResource().getUsername() + " " + sig);
		request.setRequestHeader("Accept", "application/json; charset=utf-8");
		request.setCallback(new RequestCallback() {
			@Override
			public void onResponseReceived(HttpRequest req) {
				// XXX: No error checking, since IE throws an Internal Error
				// when accessing req.getStatus().
				filesRemaining.remove(0);
				if(filesRemaining.isEmpty()){
					selectedFiles = filesRemaining;
					finish();					
				}
				selectedFiles = filesRemaining;
				doSend(filesRemaining);	
			}
		});
		request.getUpload().setProgressHandler(new ProgressHandler() {
			@Override
			public void onProgress(ProgressEvent event) {
				double pcnt = (double) event.getLoaded() / event.getTotal();
				progressBars.get(0).setProgress((int) Math.floor(pcnt * 100));
				if(pcnt*100 == 100)
					progressBars.remove(0);
			}
		});
		request.send(filesRemaining.get(0).getBlob());
	}

}
