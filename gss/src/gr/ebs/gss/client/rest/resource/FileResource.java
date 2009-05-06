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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;

/**
 * @author kman
 */
public class FileResource extends RestResource {

	public FileResource(String aUri) {
		super(aUri);
	}

	String name;

	String owner;

	String createdBy;

	String modifiedBy;

	Date creationDate;

	Date modificationDate;

	String contentType;

	Long contentLength;

	boolean readForAll;

	boolean versioned;

	Integer version;

	String etag;

	boolean deleted = false;

	List<String> tags = new ArrayList<String>();

	Set<PermissionHolder> permissions = new HashSet<PermissionHolder>();

	String folderURI;

	String path;

	String folderName;

	/**
	 * Retrieve the folderName.
	 *
	 * @return the folderName
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * Modify the folderName.
	 *
	 * @param aFolderName the folderName to set
	 */
	public void setFolderName(String aFolderName) {
		folderName = aFolderName;
	}

	/**
	 * Retrieve the path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Modify the path.
	 *
	 * @param aPath the path to set
	 */
	public void setPath(String aPath) {
		path = aPath;
	}

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
	 * @param aName the name to set
	 */
	public void setName(String aName) {
		name = aName;
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
	 * @param newOwner the owner to set
	 */
	public void setOwner(String newOwner) {
		owner = newOwner;
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
	 * @param aCreatedBy the createdBy to set
	 */
	public void setCreatedBy(String aCreatedBy) {
		createdBy = aCreatedBy;
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
	 * @param aModifiedBy the modifiedBy to set
	 */
	public void setModifiedBy(String aModifiedBy) {
		modifiedBy = aModifiedBy;
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
	 * @param aCreationDate the creationDate to set
	 */
	public void setCreationDate(Date aCreationDate) {
		creationDate = aCreationDate;
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
	 * @param aModificationDate the modificationDate to set
	 */
	public void setModificationDate(Date aModificationDate) {
		modificationDate = aModificationDate;
	}

	/**
	 * Retrieve the contentType.
	 *
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Modify the contentType.
	 *
	 * @param newContentType the contentType to set
	 */
	public void setContentType(String newContentType) {
		contentType = newContentType;
	}

	/**
	 * Retrieve the contentLength.
	 *
	 * @return the contentLength
	 */
	public Long getContentLength() {
		return contentLength;
	}

	/**
	 * Modify the contentLength.
	 *
	 * @param newContentLength the contentLength to set
	 */
	public void setContentLength(Long newContentLength) {
		contentLength = newContentLength;
	}

	/**
	 * Retrieve the readForAll.
	 *
	 * @return the readForAll
	 */
	public boolean isReadForAll() {
		return readForAll;
	}

	/**
	 * Modify the readForAll.
	 *
	 * @param newReadForAll the readForAll to set
	 */
	public void setReadForAll(boolean newReadForAll) {
		readForAll = newReadForAll;
	}

	/**
	 * Retrieve the versioned.
	 *
	 * @return the versioned
	 */
	public boolean isVersioned() {
		return versioned;
	}

	/**
	 * Modify the versioned.
	 *
	 * @param newVersioned the versioned to set
	 */
	public void setVersioned(boolean newVersioned) {
		versioned = newVersioned;
	}

	/**
	 * Retrieve the version.
	 *
	 * @return the version
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * Modify the version.
	 *
	 * @param aVersion the version to set
	 */
	public void setVersion(Integer aVersion) {
		version = aVersion;
	}

	/**
	 * Retrieve the etag.
	 *
	 * @return the etag
	 */
	public String getEtag() {
		return etag;
	}

	/**
	 * Modify the etag.
	 *
	 * @param anEtag the etag to set
	 */
	public void setEtag(String anEtag) {
		etag = anEtag;
	}

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
	 * @param newPermissions the permissions to set
	 */
	public void setPermissions(Set<PermissionHolder> newPermissions) {
		permissions = newPermissions;
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
	 * @param newDeleted the deleted to set
	 */
	public void setDeleted(boolean newDeleted) {
		deleted = newDeleted;
	}

	/**
	 * Retrieve the folderURI.
	 *
	 * @return the folderURI
	 */
	public String getFolderURI() {
		return folderURI;
	}

	/**
	 * Modify the folderURI.
	 *
	 * @param aFolderURI the folderURI to set
	 */
	public void setFolderURI(String aFolderURI) {
		folderURI = aFolderURI;
	}

	@Override
	public void createFromJSON(String text) {
		JSONObject metadata = (JSONObject) JSONParser.parse(text);
		name = unmarshallString(metadata, "name");
		name = URL.decodeComponent(name);
		path = unmarshallString(metadata, "path");
		path = URL.decodeComponent(path);
		owner = unmarshallString(metadata, "owner");
		contentType = unmarshallString(metadata, "content");
		readForAll = unmarshallBoolean(metadata, "readForAll");
		versioned = unmarshallBoolean(metadata, "versioned");

		if (metadata.get("version") != null)
			version = new Integer(metadata.get("version").toString());

		deleted = unmarshallBoolean(metadata, "deleted");
		if (deleted)
			GWT.log("FOUND A DELETED FILE:" + name, null);

		if (metadata.get("folder") != null) {
			JSONObject folder = metadata.get("folder").isObject();
			folderURI = unmarshallString(folder, "uri");
			if (folderURI!=null)
				folderURI = URL.decodeComponent(folderURI);
			folderName = unmarshallString(folder, "name");
			if(folderName != null)
				folderName = URL.decodeComponent(folderName);
		}

		if (metadata.get("permissions") != null) {
			JSONArray perm = metadata.get("permissions").isArray();
			if (perm != null)
				for (int i = 0; i < perm.size(); i++) {
					JSONObject obj = perm.get(i).isObject();
					if (obj != null) {
						PermissionHolder permission = new PermissionHolder();
						if (obj.get("user") != null)
							permission.setUser(unmarshallString(obj, "user"));
						if (obj.get("group") != null) {
							String group = unmarshallString(obj, "group");
							group = URL.decodeComponent(group);
							permission.setGroup(group);
						}
						permission.setRead(unmarshallBoolean(obj, "read"));
						permission.setWrite(unmarshallBoolean(obj, "write"));
						permission.setModifyACL(unmarshallBoolean(obj, "modifyACL"));
						permissions.add(permission);
					}
				}

		}
		if (metadata.get("tags") != null) {
			JSONArray perm = metadata.get("tags").isArray();
			if (perm != null)
				for (int i = 0; i < perm.size(); i++) {
					JSONString obj = perm.get(i).isString();
					if(obj != null)
						tags.add(URL.decodeComponent(obj.stringValue()));
				}
		}
		if (metadata.get("creationDate") != null)
			creationDate = new Date(new Long(metadata.get("creationDate").toString()));
		if (metadata.get("modificationDate") != null)
			modificationDate = new Date(new Long(metadata.get("modificationDate").toString()));
		if (metadata.get("size") != null)
			contentLength = Long.parseLong(metadata.get("size").toString());
	}

	/**
	 * Return the file size in a humanly readable form, using SI units to denote
	 * size information, e.g. 1 KB = 1000 B (bytes).
	 *
	 * @return the fileSize
	 */
	public String getFileSizeAsString() {
		return getFileSizeAsString(contentLength);
	}

	/**
	 * Return the given size in a humanly readable form, using SI units to denote
	 * size information, e.g. 1 KB = 1000 B (bytes).
	 *
	 * @param size in bytes
	 * @return the size in human readable string
	 */
	public static String getFileSizeAsString(long size) {
		if (size < 1024)
			return String.valueOf(size) + " B";
		else if (size <= 1024 * 1024)
			return getSize(size, 1024D) + " KB";
		else if (size <= 1024 * 1024 * 1024)
			return getSize(size, (1024D * 1024D)) + " MB";
		return getSize(size, (1024D * 1024D * 1024D)) + " GB";
	}

	private static String getSize(Long size, Double division) {
		Double res = Double.valueOf(size.toString()) / division;
		NumberFormat nf = NumberFormat.getFormat("######.#");
		return nf.format(res);
	}

	public boolean isShared(){
		if (isReadForAll())
			return true;
		for(PermissionHolder perm : permissions){
			if(perm.getUser() != null && !owner.equals(perm.getUser()))
				return true;
			if(perm.getGroup() != null)
				return true;
		}
		return false;
	}
}
