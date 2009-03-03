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

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;

/**
 * @author kman
 */
public class SearchResource extends RestResource {

	/**
	 * @param path
	 */
	public SearchResource(String path) {
		super(path);
	}

	List<String> files = new LinkedList<String>();

	/**
	 * Retrieve the files.
	 *
	 * @return the files
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * Modify the files.
	 *
	 * @param files the files to set
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}

	public void createFromJSON(String text) {

		JSONArray subs = JSONParser.parse(text).isArray();
		if (subs != null)
			for (int i = 0; i < subs.size(); i++)
				files.add(subs.get(i).isString().stringValue());

	}

}
