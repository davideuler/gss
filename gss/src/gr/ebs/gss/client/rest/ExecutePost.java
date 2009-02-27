/*
 * Copyright 2009 Electronic Business Systems Ltd.
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
public abstract class ExecutePost extends AbstractRestCommand{
	boolean complete = false;
	String postBody=null;

	public ExecutePost(final String path, String data, final int okStatusCode){
		GSS.get().showLoadingIndicator();

		RestRequestBuilder builder = new RestRequestBuilder("POST", path);

		try {
			handleHeaders(builder, path);
			builder.sendRequest(data, new RequestCallback() {


				public void onError(Request arg0, Throwable arg1) {
					complete = true;
					ExecutePost.this.onError(arg1);
				}


				public void onResponseReceived(Request arg0, Response arg1) {
					complete=true;
					if(arg1.getStatusCode() == okStatusCode){
						postBody = arg1.getText();
						onComplete();
					}
					else
						ExecutePost.this.onError(new RestException(path, arg1.getStatusCode(), arg1.getStatusText(), arg1.getText()));
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


	/**
	 * Retrieve the postBody.
	 *
	 * @return the postBody
	 */
	public String getPostBody() {
		return postBody;
	}


}
