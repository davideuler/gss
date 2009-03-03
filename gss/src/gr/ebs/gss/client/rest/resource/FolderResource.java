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

import gr.ebs.gss.client.GSS;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * @author kman
 */
public class FolderResource extends RestResource {

	/**
	 * @param path
	 */
	public FolderResource(String path) {
		super(path);
	}

	String name;

	String owner;

	String createdBy;

	String modifiedBy;

	Date creationDate;

	Date modificationDate;

	List<String> filePaths = new LinkedList<String>();

	List<String> subfolderPaths = new LinkedList<String>();

	Set<PermissionHolder> permissions = new HashSet<PermissionHolder>();

	List<FolderResource> folders = new ArrayList<FolderResource>();

	List<FileResource> files = new ArrayList<FileResource>();

	String parentURI;

	boolean deleted = false;

	boolean needsExpanding = false;

	/**
	 * Retrieve the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Modify the name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieve the owner.
	 *
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Modify the owner.
	 *
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * Retrieve the createdBy.
	 *
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * Modify the createdBy.
	 *
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * Retrieve the modifiedBy.
	 *
	 * @return the modifiedBy
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * Modify the modifiedBy.
	 *
	 * @param modifiedBy the modifiedBy to set
	 */
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	/**
	 * Retrieve the creationDate.
	 *
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Modify the creationDate.
	 *
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Retrieve the modificationDate.
	 *
	 * @return the modificationDate
	 */
	public Date getModificationDate() {
		return modificationDate;
	}

	/**
	 * Modify the modificationDate.
	 *
	 * @param modificationDate the modificationDate to set
	 */
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	/**
	 * Retrieve the filePaths.
	 *
	 * @return the filePaths
	 */
	public List<String> getFilePaths() {
		return filePaths;
	}

	/**
	 * Modify the filePaths.
	 *
	 * @param filePaths the filePaths to set
	 */
	public void setFilePaths(List<String> filePaths) {
		this.filePaths = filePaths;
	}

	/**
	 * Retrieve the subfolderPaths.
	 *
	 * @return the subfolderPaths
	 */
	public List<String> getSubfolderPaths() {
		return subfolderPaths;
	}

	/**
	 * Modify the subfolderPaths.
	 *
	 * @param subfolderPaths the subfolderPaths to set
	 */
	public void setSubfolderPaths(List<String> subfolderPaths) {
		this.subfolderPaths = subfolderPaths;
	}

	/**
	 * Retrieve the permissions.
	 *
	 * @return the permissions
	 */
	public Set<PermissionHolder> getPermissions() {
		return permissions;
	}

	/**
	 * Modify the permissions.
	 *
	 * @param permissions the permissions to set
	 */
	public void setPermissions(Set<PermissionHolder> permissions) {
		this.permissions = permissions;
	}

	/**
	 * Retrieve the deleted.
	 *
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Modify the deleted.
	 *
	 * @param deleted the deleted to set
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public void removeSubfolderPath(String spath) {
		if (subfolderPaths.remove(spath))
			return;
		else if (subfolderPaths.remove(spath + "/"))
			return;
		else
			subfolderPaths.remove(spath.substring(0, spath.length() - 1));
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

	/**
	 * Retrieve the parentURI.
	 *
	 * @return the parentURI
	 */
	public String getParentURI() {
		return parentURI;
	}

	/**
	 * Modify the parentURI.
	 *
	 * @param parentURI the parentURI to set
	 */
	public void setParentURI(String parentURI) {
		this.parentURI = parentURI;
	}

	public void createFromJSON(String text) {

		JSONObject json = (JSONObject) JSONParser.parse(text);
		name = unmarshallString(json, "name");
		// name = URL.decodeComponent(name);
		owner = unmarshallString(json, "owner");
		parentURI = unmarshallString(json, "parent");
		deleted = unmarshallBoolean(json, "deleted");
		if (deleted)
			GWT.log("FOUND A DELETED FOLDER:" + name, null);

		if (json.get("permissions") != null) {
			JSONArray perm = json.get("permissions").isArray();
			if (perm != null)
				for (int i = 0; i < perm.size(); i++) {
					JSONObject obj = perm.get(i).isObject();
					if (obj != null) {
						PermissionHolder permission = new PermissionHolder();
						if (obj.get("user") != null)
							permission.setUser(unmarshallString(obj, "user"));
						if (obj.get("group") != null)
							permission.setGroup(unmarshallString(obj, "group"));
						permission.setRead(unmarshallBoolean(obj, "read"));
						permission.setWrite(unmarshallBoolean(obj, "write"));
						permission.setModifyACL(unmarshallBoolean(obj, "modifyACL"));
						permissions.add(permission);
					}

				}
		}
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

						Integer fversion = null;
						if (fo.get("version") != null)
							fversion = new Integer(fo.get("version").toString());
						boolean fdeleted = unmarshallBoolean(fo, "deleted");
						Date fcreationDate = null;
						if (fo.get("creationDate") != null)
							fcreationDate = new Date(new Long(fo.get("creationDate").toString()));
						String furi = unmarshallString(fo, "uri");
						Long fsize = 0L;
						if (fo.get("size") != null)
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
		if (json.get("creationDate") != null)
			creationDate = new Date(new Long(json.get("creationDate").toString()));
		if (json.get("modificationDate") != null)
			modificationDate = new Date(new Long(json.get("modificationDate").toString()));

	}

	public String getParent() {
		String base = GSS.GSS_REST_PATH;
		int length = base.length();
		return path.substring(length, path.length());
	}

	/**
	 * Retrieve the needsExpanding.
	 *
	 * @return the needsExpanding
	 */
	public boolean isNeedsExpanding() {
		return needsExpanding;
	}

	/**
	 * Modify the needsExpanding.
	 *
	 * @param needsExpanding the needsExpanding to set
	 */
	public void setNeedsExpanding(boolean needsExpanding) {
		this.needsExpanding = needsExpanding;
	}

}
