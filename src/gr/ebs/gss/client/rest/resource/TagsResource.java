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
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.TreeItem;


/**
 * @author kman
 *
 */
public class TagsResource extends RestResource{

	public TagsResource(String aUri) {
		super(aUri);
	}

	List<String> tags = new ArrayList<String>();

	/**
	 * Retrieve the tags.
	 *
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * Modify the tags.
	 *
	 * @param newTags the tags to set
	 */
	public void setTags(List<String> newTags) {
		tags = newTags;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray array = (JSONArray) JSONParser.parse(text);
		if(array != null)
			for (int i = 0; i < array.size(); i++)
				getTags().add(array.get(i).isString().stringValue());
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
	/**
	 * Bugzilla – Bug 371, Add history support for folder navigation
	 */
	public void updateHistoryAbs(TreeItem item, String path){
		throw new UnsupportedOperationException();
	}
}
