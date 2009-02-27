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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;


/**
 * @author kman
 *
 */
public class TrashResource extends RestResource{

	/**
	 * @param path
	 */
	public TrashResource(String path) {
		super(path);
	}

	List<String> filePaths = new LinkedList<String>();
	List<String> subfolderPaths = new LinkedList<String>();
	List<FolderResource> folders = new ArrayList<FolderResource>();
	List<FileResource> files = new ArrayList<FileResource>();


	/**
	 * Retrieve the files.
	 *
	 * @return the files
	 */
	public List<String> getFilePaths() {
		return filePaths;
	}

	/**
	 * Modify the files.
	 *
	 * @param filePaths the files to set
	 */
	public void setFilePaths(List<String> filePaths) {
		filePaths = filePaths;
	}

	/**
	 * Retrieve the subfolders.
	 *
	 * @return the subfolders
	 */
	public List<String> getSubfolderPaths() {
		return subfolderPaths;
	}

	/**
	 * Modify the subfolders.
	 *
	 * @param subfolders the subfolders to set
	 */
	public void setSubfolderPaths(List<String> subfolderPaths) {
		subfolderPaths = subfolderPaths;
	}


	/**
	 * Retrieve the folders.
	 *
	 * @return the folders
	 */
	public List<FolderResource> getFolders() {
		return folders;
	}


	/**
	 * Modify the folders.
	 *
	 * @param folders the folders to set
	 */
	public void setFolders(List<FolderResource> folders) {
		this.folders = folders;
	}


	/**
	 * Retrieve the files.
	 *
	 * @return the files
	 */
	public List<FileResource> getFiles() {
		return files;
	}


	/**
	 * Modify the files.
	 *
	 * @param files the files to set
	 */
	public void setFiles(List<FileResource> files) {
		this.files = files;
	}

	public void createFromJSON(String text) {
		JSONObject json = (JSONObject) JSONParser.parse(text);
		if (json.get("folders") != null) {
			JSONArray subs = json.get("folders").isArray();
			for (int i = 0; i < subs.size(); i++){
				JSONObject so = subs.get(i).isObject();
				String subUri = so.get("uri").isString().stringValue();
				String subName = so.get("name").isString().stringValue();
				FolderResource sub = new FolderResource(subUri);
				sub.setName(subName);
				folders.add(sub);
				subfolderPaths.add(subUri);
			}
		}
		if (json.get("files") != null) {
			JSONArray subs = json.get("files").isArray();
			for (int i = 0; i < subs.size(); i++) {
				JSONObject fo = subs.get(i).isObject();
				String fname = fo.get("name").isString().stringValue();
				String fowner = fo.get("owner").isString().stringValue();
				Integer fversion = new Integer(fo.get("version").toString());
				boolean fdeleted = fo.get("deleted").isBoolean().booleanValue();
				Date fcreationDate = null;
				if(fo.get("creationDate") != null)
					fcreationDate = new Date(new Long(fo.get("creationDate").toString()));
				String furi = fo.get("uri").isString().stringValue();
				Long fsize = new Long(fo.get("size").toString());
				filePaths.add(furi);
				FileResource fs = new FileResource(furi);
				fs.setName(fname);
				fs.setOwner(fowner);
				fs.setVersion(fversion);
				fs.setContentLength(fsize);
				fs.setDeleted(fdeleted);
				fs.setCreationDate(fcreationDate);
				files.add(fs);
			}
		}
		//for(String s : subfolders)
			//GWT.log("Shared:"+s,null);
	}

	public List<FolderResource> getTrashedFolders(){
		List<FolderResource> res = new ArrayList<FolderResource>();
		for(String s : subfolderPaths){
			String[] pathElements =  s.split("/");
			FolderResource tr = new FolderResource(s);
			tr.setName(URL.decodeComponent(pathElements[pathElements.length-1]));
			res.add(tr);
		}
		return res;
	}
}
