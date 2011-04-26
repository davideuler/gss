/*
 * Copyright 2007, 2008, 2009 Electronic Business Systems Ltd.
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
package org.gss_project.gss.common.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author chstath
 */
public class FileHeaderDTO implements Serializable {

	/**
	 * The path for the resource manipulation subsystem.
	 */
	public static final String PATH_FILES = "/files";

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The persistence ID of the object.
	 */
	private Long id;

	/**
	 * The file name.
	 */
	private String name;

	/**
	 * The file path.
	 */
	private String path;

	/**
	 * Is this a versioned file?
	 */
	private boolean versioned;

	/**
	 * The current version
	 */
	private int version = 0;

	/**
	 * The owner of this file.
	 */
	private UserDTO owner;

	/**
	 * The folder that contains this file.
	 */
	private FolderDTO folder;

	/**
	 * The size of the file
	 */
	private long fileSize;

	/**
	 * The MIME type of this file.
	 */
	private String mimeType;

	/**
	 * The original filename (with which file was uploaded).
	 */
	private String originalFilename;

	/**
	 * The original filename URL-encoded.
	 */
	private String originalFilenameEncoded;

	/**
	 * The audit information for this object.
	 */
	private AuditInfoDTO auditInfo;

	/**
	 * A list of tags (Strings).
	 *
	 *
	 */
	private List<String> tags;

	private Set<PermissionDTO> permissions;
	/**
	 * is this file deleted?
	 */
	private boolean deleted=false;
	/**
	 * Anyone can read this file?
	 */
	private boolean readForAll=false;
	
	
	private Boolean shared=false;
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param newId the id to set
	 */
	public void setId(final Long newId) {
		id = newId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param newName the name to set
	 */
	public void setName(final String newName) {
		name = newName;
	}

	/**
	 * @return the versioned
	 */
	public boolean isVersioned() {
		return versioned;
	}

	/**
	 * @param newVersioned the versioned to set
	 */
	public void setVersioned(final boolean newVersioned) {
		versioned = newVersioned;
	}

	/**
	 * @return the owner
	 */
	public UserDTO getOwner() {
		return owner;
	}

	/**
	 * @param newOwner the owner to set
	 */
	public void setOwner(final UserDTO newOwner) {
		owner = newOwner;
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param newVersion the version to set
	 */
	public void setVersion(final int newVersion) {
		version = newVersion;
	}

	/**
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * @param newFileSize the fileSize to set
	 */
	public void setFileSize(final long newFileSize) {
		fileSize = newFileSize;
	}

	/**
	 * Return the file size in a humanly readable form, using SI units to denote
	 * size information, e.g. 1 KB = 1000 B (bytes).
	 *
	 * @return the fileSize
	 */
	public String getFileSizeAsString() {
		if (fileSize < 1024)
			return String.valueOf(fileSize) + " B";
		else if (fileSize <= 1024*1024)
			return getSize(fileSize, 1024D) + " KB";
		else if (fileSize <= 1024*1024*1024)
			return getSize(fileSize,(1024D*1024D)) + " MB";
		return getSize(fileSize , (1024D*1024D*1024D)) + " GB";
	}

	private String getSize(Long size, Double division){
		Double res = Double.valueOf(size.toString())/division;
		NumberFormat nf = NumberFormat.getFormat("######.###");
		return nf.format(res);
	}

	/**
	 * Retrieve the auditInfo.
	 *
	 * @return the auditInfo
	 */
	public AuditInfoDTO getAuditInfo() {
		return auditInfo;
	}

	/**
	 * Modify the auditInfo.
	 *
	 * @param newAuditInfo the auditInfo to set
	 */
	public void setAuditInfo(final AuditInfoDTO newAuditInfo) {
		auditInfo = newAuditInfo;
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
	 * Retrieve the mimeType.
	 *
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Modify the mimeType.
	 *
	 * @param newMimeType the mimeType to set
	 */
	public void setMimeType(String newMimeType) {
		mimeType = newMimeType;
	}

	/**
	 * Retrieve the originalFilename.
	 *
	 * @return the originalFilename
	 */
	public String getOriginalFilename() {
		return originalFilename;
	}

	/**
	 * Modify the originalFilename.
	 *
	 * @param newOriginalFilename the originalFilename to set
	 */
	public void setOriginalFilename(String newOriginalFilename) {
		originalFilename = newOriginalFilename;
	}

	/**
	 * Retrieve the originalFilenameEncoded.
	 *
	 * @return the originalFilenameEncoded
	 */
	public String getOriginalFilenameEncoded() {
		return originalFilenameEncoded;
	}

	/**
	 * Modify the originalFilenameEncoded.
	 *
	 * @param newOriginalFilenameEncoded the originalFilenameEncoded to set
	 */
	public void setOriginalFilenameEncoded(String newOriginalFilenameEncoded) {
		originalFilenameEncoded = newOriginalFilenameEncoded;
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
	 * @param aReadForAll the readForAll to set
	 */
	public void setReadForAll(boolean aReadForAll) {
		readForAll = aReadForAll;
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
	 * Retrieve the URI for this resource, relative to the REST API root URI.
	 * This unique identifier can be used to refer to the resource from
	 * various GSS clients.
	 *
	 * @return the URI
	 */
	public String getURI() {
		return owner.getUsername() + PATH_FILES + getPath();
	}

	/**
	 * Retrieve the folder.
	 *
	 * @return the folder
	 */
	public FolderDTO getFolder() {
		return folder;
	}

	/**
	 * Modify the folder.
	 *
	 * @param aFolder the folder to set
	 */
	public void setFolder(FolderDTO aFolder) {
		folder = aFolder;
	}

	/**
	 * Modify the permissions.
	 *
	 * @param newPermissions the permissions to set
	 */
	public void setPermissions(Set<PermissionDTO> newPermissions) {
		permissions = newPermissions;
	}

	/**
	 * Retrieve the permissions.
	 *
	 * @return the permissions
	 */
	public Set<PermissionDTO> getPermissions() {
		return permissions;
	}
	
	/**
	 * Retrieve the shared.
	 *
	 * @return the shared
	 */
	public Boolean getShared() {
		return shared;
	}
	
	
	/**
	 * Modify the shared.
	 *
	 * @param shared the shared to set
	 */
	public void setShared(Boolean shared) {
		this.shared = shared;
	}
}
