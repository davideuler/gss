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
package gr.ebs.gss.client.rest.resource;

import java.io.Serializable;

import com.google.gwt.json.client.JSONObject;


/**
 * @author kman
 *
 */
public abstract class RestResource implements Serializable{
	String uri;

	public RestResource(String aUri) {
		super();
		setUri(aUri);
	}

	/**
	 * Retrieve the uri.
	 *
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Modify the uri.
	 *
	 * @param aUri the path to set
	 */
	public void setUri(String aUri) {
		uri = aUri;
		// Remove any parameter part
		int qm = uri.indexOf('?');
		if (qm>=0) uri = uri.substring(0, qm);
	}

	public abstract void createFromJSON(String text);

	protected String unmarshallString(JSONObject obj, String key){
		if(obj.get(key) != null)
			if(obj.get(key).isString() != null)
				return obj.get(key).isString().stringValue();
		return null;
	}

	protected boolean unmarshallBoolean(JSONObject obj, String key){
		if(obj.get(key) != null)
			if(obj.get(key).isBoolean() != null)
				return obj.get(key).isBoolean().booleanValue();
		return false;
	}
}
