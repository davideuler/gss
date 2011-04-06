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
package gr.ebs.gss.web.client.rest.resource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * A container for the results of a search query for users.
 *
 * @author past
 */
public class UserSearchResource extends RestResource {

	public UserSearchResource(String aUri) {
		super(aUri);
	}

	List<UserResource> users = new ArrayList<UserResource>();

	@Override
	public void createFromJSON(String text) {
		JSONArray json = JSONParser.parse(text).isArray();
		if (json != null)
			for (int i = 0; i < json.size(); i++) {
				JSONObject j = json.get(i).isObject();
				if (j != null) {
					String username = unmarshallString(j, "username");
					String name = unmarshallString(j, "name");
					String home = unmarshallString(j, "home");
					UserResource user = new UserResource(home);
					user.setName(name);
					user.setUsername(username);
					users.add(user);
				}
			}
	}

	/**
	 * Retrieve the users.
	 *
	 * @return the users
	 */
	public List<UserResource> getUsers() {
		return users;
	}

	/**
	 * Modify the users.
	 *
	 * @param newUsers the users to set
	 */
	public void setUsers(List<UserResource> newUsers) {
		users = newUsers;
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
}
