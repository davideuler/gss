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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;

/**
 * @author kman
 */
public class GroupResource extends RestResource {

	public GroupResource(String aUri) {
		super(aUri);
	}

	List<String> userPaths = new ArrayList<String>();

	/**
	 * Retrieve the userPaths.
	 *
	 * @return the userPaths
	 */
	public List<String> getUserPaths() {
		return userPaths;
	}

	/**
	 * Modify the userPaths.
	 *
	 * @param newUserPaths the userPaths to set
	 */
	public void setUserPaths(List<String> newUserPaths) {
		userPaths = newUserPaths;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray array = (JSONArray) JSONParser.parse(text);
		if (array != null)
			for (int i = 0; i < array.size(); i++)
				if(array.get(i).isString() != null)
					getUserPaths().add(array.get(i).isString().stringValue());
	}

	public String getName() {
		String[] names = uri.split("/");
		return URL.decodeComponent(names[names.length - 1]);
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}

}
