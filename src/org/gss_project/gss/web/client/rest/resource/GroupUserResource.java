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
package org.gss_project.gss.web.client.rest.resource;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;


/**
 * @author kman
 *
 */
public class GroupUserResource extends RestResource{

	public GroupUserResource(String aUri) {
		super(aUri);
	}

	String username;
	String name;
	String home;

	/**
	 * Retrieve the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Modify the username.
	 *
	 * @param aUsername the username to set
	 */
	public void setUsername(String aUsername) {
		username = aUsername;
	}

	/**
	 * Retrieve the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Modify the name.
	 *
	 * @param aName the name to set
	 */
	public void setName(String aName) {
		name = aName;
	}

	/**
	 * Retrieve the home.
	 *
	 * @return the home
	 */
	public String getHome() {
		return home;
	}

	/**
	 * Modify the home.
	 *
	 * @param aHome the home to set
	 */
	public void setHome(String aHome) {
		home = aHome;
	}

	@Override
	public void createFromJSON(String text) {
		JSONObject json = (JSONObject) JSONParser.parse(text);
		name = unmarshallString(json, "name");
		home = unmarshallString(json, "home");
		username = unmarshallString(json, "username");
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
}
