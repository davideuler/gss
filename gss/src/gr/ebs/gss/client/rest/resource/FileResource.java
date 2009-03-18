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

	/**
	 * @param path
	 */
	public FileResource(String path) {
		super(path);
		// TODO Auto-generated constructor stub
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
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
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
	 * @param contentLength the contentLength to set
	 */
	public void setContentLength(Long contentLength) {
		this.contentLength = contentLength;
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
	 * @param readForAll the readForAll to set
	 */
	public void setReadForAll(boolean readForAll) {
		this.readForAll = readForAll;
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
	 * @param versioned the versioned to set
	 */
	public void setVersioned(boolean versioned) {
		this.versioned = versioned;
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
	 * @param version the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
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
	 * @param etag the etag to set
	 */
	public void setEtag(String etag) {
		this.etag = etag;
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
	 * @param tags the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
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
	 * @param folderURI the folderURI to set
	 */
	public void setFolderURI(String folderURI) {
		this.folderURI = folderURI;
	}

	public void createFromJSON(String text) {
		JSONObject metadata = (JSONObject) JSONParser.parse(text);
		name = unmarshallString(metadata, "name");
		name = URL.decodeComponent(name);
		owner = unmarshallString(metadata, "owner");
		readForAll = unmarshallBoolean(metadata, "readForAll");
		versioned = unmarshallBoolean(metadata, "versioned");

		if (metadata.get("version") != null)
			version = new Integer(metadata.get("version").toString());

		folderURI = unmarshallString(metadata, "folder");
		deleted = unmarshallBoolean(metadata, "deleted");
		if (deleted)
			GWT.log("FOUND A DELETED FILE:" + name, null);

		if (metadata.get("permissions") != null) {
			JSONArray perm = metadata.get("permissions").isArray();
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
		if (metadata.get("tags") != null) {
			JSONArray perm = metadata.get("tags").isArray();
			if (perm != null)
				for (int i = 0; i < perm.size(); i++) {
					JSONString obj = perm.get(i).isString();
					if(obj != null)
						tags.add(obj.stringValue());
				}
		}
		if (metadata.get("creationDate") != null)
			creationDate = new Date(new Long(metadata.get("creationDate").toString()));
		if (metadata.get("modificationDate") != null)
			modificationDate = new Date(new Long(metadata.get("modificationDate").toString()));
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
		NumberFormat nf = NumberFormat.getFormat("######.###");
		return nf.format(res);
	}
}
