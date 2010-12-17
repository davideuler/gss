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

import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * @author kman
 */
public class SearchResource extends RestResource {

	public SearchResource(String aUri) {
		super(aUri);
	}

	List<FileResource> files = new ArrayList<FileResource>();
	List<String> filePaths = new LinkedList<String>();

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
	 * @param newFilePaths the files to set
	 */
	public void setFilePaths(List<String> newFilePaths) {
		filePaths = newFilePaths;
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
	 * @param newFiles the files to set
	 */
	public void setFiles(List<FileResource> newFiles) {
		files = newFiles;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray subs = JSONParser.parse(text).isArray();
		if (subs != null)
			for (int i = 0; i < subs.size(); i++) {
				JSONObject fo = subs.get(i).isObject();
				if (fo != null) {
					String fname = unmarshallString(fo, "name");
					String fowner = unmarshallString(fo, "owner");
					String fcontent = unmarshallString(fo, "content");
					String fpath = unmarshallString(fo, "path");
					Boolean fshared = unmarshallBoolean(fo,"shared");
					boolean fversioned = unmarshallBoolean(fo,"versioned");
					fpath = URL.decodeComponent(fpath);
					Integer fversion = null;
					if (fo.get("version") != null)
						fversion = new Integer(fo.get("version").toString());
					boolean fdeleted = unmarshallBoolean(fo, "deleted");
					Date fcreationDate = null;
					if (fo.get("creationDate") != null)
						fcreationDate = new Date(new Long(fo.get("creationDate").toString()));
					Date fmodificationDate = null;
					if (fo.get("modificationDate") != null)
						fmodificationDate = new Date(new Long(fo.get("modificationDate").toString()));
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
					fs.setModificationDate(fmodificationDate);
					fs.setContentType(fcontent);
					fs.setPath(fpath);
					fs.setShared(fshared);
					fs.setVersioned(fversioned);
					files.add(fs);
				}
			}
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
}
