/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
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
	String path;


	/**
	 * @param path
	 */
	public RestResource(String path) {
		super();
		this.path = path;
	}




	/**
	 * Retrieve the path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}




	/**
	 * Modify the path.
	 *
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}



	public abstract void createFromJSON(String text);


	protected String unmarshallString(JSONObject obj, String varName){
		if(obj.get(varName) != null)
			if(obj.get(varName).isString() != null)
				return obj.get(varName).isString().stringValue();
		return null;
	}

	protected boolean unmarshallBoolean(JSONObject obj, String varName){
		if(obj.get(varName) != null)
			if(obj.get(varName).isBoolean() != null)
				return obj.get(varName).isBoolean().booleanValue();
		return false;
	}
}
