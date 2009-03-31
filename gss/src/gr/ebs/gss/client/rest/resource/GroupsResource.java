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

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * @author kman
 */
public class GroupsResource extends RestResource {

	public GroupsResource(String aPath) {
		super(aPath);
	}

	List<String> groupPaths = new ArrayList<String>();

	/**
	 * Retrieve the groupPaths.
	 *
	 * @return the groupPaths
	 */
	public List<String> getGroupPaths() {
		return groupPaths;
	}

	/**
	 * Modify the groupPaths.
	 *
	 * @param newGroupPaths the groupPaths to set
	 */
	public void setGroupPaths(List<String> newGroupPaths) {
		groupPaths = newGroupPaths;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray array = (JSONArray) JSONParser.parse(text);
		if (array != null)
			for (int i = 0; i < array.size(); i++) {
				JSONObject js = array.get(i).isObject();
				if(js != null){
					String uri = unmarshallString(js, "uri");
					if(uri != null)
						getGroupPaths().add(uri);
				}
			}
	}

}
