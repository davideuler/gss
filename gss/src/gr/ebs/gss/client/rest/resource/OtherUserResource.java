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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;


/**
 * @author kman
 *
 */
public class OtherUserResource extends RestResource{
	/**
	 * @param path
	 */
	public OtherUserResource(String path) {
		super(path);
	}
	String username;


	List<String> filePaths = new LinkedList<String>();
	List<String> subfolderPaths = new LinkedList<String>();
	List<FolderResource> folders = new ArrayList<FolderResource>();

	List<FileResource> files = new ArrayList<FileResource>();




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
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

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
	 * @param files the files to set
	 */
	public void setFilePaths(List<String> files) {
		filePaths = files;
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
	public void setSubfolderPaths(List<String> subfolders) {
		subfolderPaths = subfolders;
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
			if (subs != null)
				for (int i = 0; i < subs.size(); i++) {
					JSONObject so = subs.get(i).isObject();
					if (so != null) {
						String subUri = unmarshallString(so, "uri");
						String subName = unmarshallString(so, "name");
						if (subUri != null && subName != null) {
							if (!subUri.endsWith("/"))
								subUri = subUri + "/";
							FolderResource sub = new FolderResource(subUri);
							sub.setName(subName);
							sub.setNeedsExpanding(true);
							folders.add(sub);
							subfolderPaths.add(subUri);
						}

					}
				}
		}
		if (json.get("files") != null) {
			JSONArray subs = json.get("files").isArray();
			if (subs != null)
				for (int i = 0; i < subs.size(); i++) {
					JSONObject fo = subs.get(i).isObject();
					if (fo != null) {
						String fname = unmarshallString(fo, "name");
						String fowner = unmarshallString(fo, "owner");
						String fvs = unmarshallString(fo, "version");
						Integer fversion = null;
						if (fo.get("version") != null)
							fversion = new Integer(fo.get("version").toString());
						boolean fdeleted = unmarshallBoolean(fo, "deleted");
						Date fcreationDate = null;
						if (fo.get("creationDate") != null)
							fcreationDate = new Date(new Long(fo.get("creationDate").toString()));
						String furi = unmarshallString(fo,"uri");
						Long fsize = 0L;
						if(fo.get("size") != null)
							fsize = new Long(fo.get("size").toString());
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
		}

	}

	public String getName(){
		String[] names = path.split("/");
		return names[names.length -1];
	}
}