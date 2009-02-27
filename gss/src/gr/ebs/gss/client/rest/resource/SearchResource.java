/*
 * Copyright 2009 Electronic Business Systems Ltd.
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

		JSONArray subs =  JSONParser.parse(text).isArray();
		for (int i = 0; i < subs.size(); i++)
			files.add(subs.get(i).isString().stringValue());

	}

}
