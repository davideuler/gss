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
package gr.ebs.gss.server.domain;

import gr.ebs.gss.server.domain.dto.FileHeaderDTO;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * The immutable part of the structure of a file on the GSS service.
 */
@Entity
@Table(name="fileheader", uniqueConstraints=@UniqueConstraint(columnNames={"folder_id", "name"}))
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public final class FileHeader  implements Serializable{

	/**
	 * The persistence ID of the object.
	 */
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Version field for optimistic locking.
	 */
	@SuppressWarnings("unused")
	@Version
	private int version;

	/**
	 * The audit information.
	 */
	@Embedded
	private AuditInfo auditInfo;

	/**
	 * The icon filename.
	 */
	private String icon;

	/**
	 * The file name.
	 */
	@Column(name="name")
	private String name;

	/**
	 * The parent folder of this file.
	 */
	@ManyToOne(optional=false)
	@JoinColumn(name="folder_id", nullable=false)
	private Folder folder;

	/**
	 * Is this a versioned file?
	 */
	private boolean versioned = false;
	/**
	 * Is this file temporarily deleted?
	 * XXX: the columnDefinition is postgres specific, if deployment database is changed this shall be changed too
	 */
	@Column(columnDefinition=" boolean DEFAULT false")
	private boolean deleted=false;

	/**
	 * Can this file be read by anyone?
	 * XXX: the columnDefinition is postgres specific, if deployment database is changed this shall be changed too
	 */
	@Column(columnDefinition=" boolean DEFAULT false")
	private boolean readForAll=false;

	/**
	 * The owner of this file.
	 */
	@ManyToOne(optional=false)
	@JoinColumn(nullable=false)
	private User owner;

	/**
	 * The bodies of this file. (A single one if not versioned.) A List so we
	 * can keep order.
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "header")
	@OrderBy("version")
	private List<FileBody> bodies = new ArrayList<FileBody>();

	/**
	 * The current (most recent) body of this file. The single one if not
	 * versioned.
	 */
	@ManyToOne
	private FileBody currentBody;

	/**
	 * The list of all tags this file has specified from all Users.
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "file")
	@OrderBy("tag")
	private List<FileTag> fileTags = new ArrayList<FileTag>();

	/**
	 * Set of Permission objects: The permissions (User and Group) for this
	 * FileHeader.
	 */
	@OneToMany(cascade = CascadeType.ALL)
	private Set<Permission> permissions = new HashSet<Permission>();
	
	@Column(columnDefinition=" boolean DEFAULT false")
	private Boolean shared;
	/**
	 * Retrieve the ID.
	 *
	 * @return the ID
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Retrieve the icon.
	 *
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Modify the icon.
	 *
	 * @param newIcon the new icon
	 */
	public void setIcon(final String newIcon) {
		icon = newIcon;
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
	 * @param newName the new name
	 */
	public void setName(final String newName) {
		name = newName;
	}

	/**
	 * Retrieve the folder.
	 *
	 * @return the folder object
	 */
	public Folder getFolder() {
		return folder;
	}

	/**
	 * Modify the folder.
	 *
	 * @param newFolder the new folder
	 */
	public void setFolder(final Folder newFolder) {
		folder = newFolder;
	}

	/**
	 * Determine whether this file is versioned or not.
	 *
	 * @return true if this file is versioned
	 */
	public boolean isVersioned() {
		return versioned;
	}

	/**
	 * Modify the versioning status of this file.
	 *
	 * @param newStatus the new versioning status
	 */
	public void setVersioned(final boolean newStatus) {
		versioned = newStatus;
	}


	/**
	 * Is this file deleted or not?.
	 *
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}


	/**
	 * Set whether this file is deleted .
	 *
	 * @param newDeleted the deletedFlag to set
	 */
	public void setDeleted(boolean newDeleted) {
		deleted = newDeleted;
	}

	/**
	 * Retrieve the owner.
	 *
	 * @return the owner
	 */
	public User getOwner() {
		return owner;
	}

	/**
	 *Modify the owner.
	 *
	 * @param newOwner the new owner
	 */
	public void setOwner(final User newOwner) {
		owner = newOwner;
	}

	/**
	 * Retrieve the list of bodies.
	 *
	 * @return the list of bodies
	 */
	public List<FileBody> getBodies() {
		return bodies;
	}

	/**
	 * Replace the list of bodies.
	 *
	 * @param newBodies the new list of bodies
	 */
	public void setBodies(final List<FileBody> newBodies) {
		bodies = newBodies;
	}

	/**
	 * Retrieve the current body.
	 *
	 * @return the current body
	 */
	public FileBody getCurrentBody() {
		return currentBody;
	}

	/**
	 * Set another body as the current one.
	 *
	 * @param newCurrentBody the new current body
	 */
	public void setCurrentBody(final FileBody newCurrentBody) {
		currentBody = newCurrentBody;
	}

	/**
	 * Retrieve the audit info.
	 *
	 * @return the audit info object
	 */
	public AuditInfo getAuditInfo() {
		return auditInfo;
	}

	/**
	 * Modify the audit info.
	 *
	 * @param newAuditInfo the new audit info
	 */
	public void setAuditInfo(final AuditInfo newAuditInfo) {
		auditInfo = newAuditInfo;
	}

	/**
	 * Retrieve the file tags.
	 *
	 * @return the list of file tags
	 */
	public List<FileTag> getFileTags() {
		return fileTags;
	}

	/**
	 * Replace the list of file tags.
	 *
	 * @param newFileTags the new file tags list
	 */
	public void setFileTags(final List<FileTag> newFileTags) {
		fileTags = newFileTags;
	}

	/**
	 * Retrieve the set of permissions.
	 *
	 * @return the permission set
	 */
	public Set<Permission> getPermissions() {
		return permissions;
	}

	/**
	 * Replace the permission set.
	 *
	 * @param newPermissions the new permission set
	 */
	public void setPermissions(final Set<Permission> newPermissions) {
		permissions = newPermissions;
	}

	/**
	 * Add a body to list of bodies.
	 *
	 * @param body InfoItemBody The body to add.
	 */
	public void addBody(final FileBody body) {
		if (body == null)
			throw new IllegalArgumentException("Can't add a null FileBody.");
		// Remove from old header
		if (body.getHeader() != null)
			throw new IllegalArgumentException("Trying to add a FileBody that already belongs to a FileHeader.");

		// Set child in parent
		getBodies().add(body);
		// Set parent in child
		body.setHeader(this);

		// Update version number
		if (currentBody == null)
			body.setVersion(1);
		else
			body.setVersion(currentBody.getVersion() + 1);

		currentBody = body;
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
	 * Returns current body version number formatted for display. Returns "-"
	 * for non-versioned file.
	 *
	 * @return the current version
	 */
	public String getCurrentVersionString() {
		if (isVersioned())
			return String.valueOf(currentBody.getVersion());
		return "-";
	}

	/**
	 * Adds a permission to this FileHeader.
	 *
	 * @param permission Permission to add
	 * @throws IllegalArgumentException if permission is null
	 */
	public void addPermission(final Permission permission) {
		if (permission == null)
			throw new IllegalArgumentException("Can't add a null Permission.");
		getPermissions().add(permission);
	}

	/**
	 * Constructs and returns a DTO for this instance for use by remote clients
	 *
	 * @return FileHeaderDTO
	 */
	public FileHeaderDTO getDTO() {
		final FileHeaderDTO f = new FileHeaderDTO();
		f.setId(id);
		f.setName(name);
		f.setPath(getPath());
		f.setFolder(folder.getDTO());
		f.setVersioned(versioned);
		f.setVersion(currentBody.getVersion());
		f.setOwner(owner.getDTO());
		f.setFileSize(currentBody.getFileSize());
		f.setOriginalFilename(currentBody.getOriginalFilename());
		f.setOriginalFilenameEncoded(currentBody.getOriginalFilenameEncoded());
		f.setMimeType(currentBody.getMimeType());
		f.setDeleted(deleted);
		f.setReadForAll(readForAll);
		f.setShared(getShared());
		List<String> tags = new ArrayList<String>();
		for (FileTag tag : fileTags)
			tags.add(tag.getTag());
		f.setTags(tags);
		f.setAuditInfo(auditInfo.getDTO());
		return f;
	}

	/**
	 * Checks if the specified user has permission to delete this file, by
	 * checking if the user has write permission to this object.
	 *
	 * @param user the specified User
	 * @return true if the user has permission to delete the file, false
	 *         otherwise
	 */
	public boolean hasDeletePermission(final User user) {
		if (hasWritePermission(user))
			return true;
		return false;
	}

	/**
	 * Checks if the specified user has permission to modify this file.
	 *
	 * @param user the specified User
	 * @return true if the user has permission to modify the file, false
	 *         otherwise
	 */
	public boolean hasWritePermission(final User user) {
		for (final Permission p : permissions)
			if (p.getUser() != null) {
				if (p.getUser().equals(user) && p.getWrite())
					return true;
			} else if (p.getGroup().contains(user) && p.getWrite())
				return true;
		return false;
	}

	/**
	 * Checks if the specified user has permission to read this file.
	 *
	 * @param user the specified User
	 * @return true if the user has permission to read the file, false
	 *         otherwise
	 */
	public boolean hasReadPermission(final User user) {
		if(readForAll)
			return true;
		for (final Permission p : permissions)
			if (p.getUser() != null) {
				if (p.getUser().equals(user) && p.getRead())
					return true;
			} else if (p.getGroup().contains(user) && p.getRead())
				return true;
		return false;
	}

	/**
	 * Checks if the specified user has permission to modify the ACL of this file.
	 *
	 * @param user the specified User
	 * @return true if the user has permission to modify the ACL of the file, false
	 *         otherwise
	 */
	public boolean hasModifyACLPermission(final User user) {
		for (final Permission p : permissions)
			if (p.getUser() != null) {
				if (p.getUser().equals(user) && p.getModifyACL())
					return true;
			} else if (p.getGroup().contains(user) && p.getModifyACL())
				return true;
		return false;
	}

	/**
	 * Retrieve the full path of the file, URL-encoded in the form:
	 * /parent1/parent2/parent3/name
	 *
	 * @return the full path from the root of the files namespace
	 */
	public String getPath() {
		try {
			return folder.getPath() + URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return the total space occupied by this file in storage
	 * (i.e. the total size of all bodies)
	 * @return long
	 */
	public long getTotalSize() {
		long total = 0;
		for (FileBody body: getBodies())
			total += body.getFileSize();
		return total;
	}
	
	/**
	 * Retrieve the shared.
	 *
	 * @return the shared
	 */
	public Boolean getShared() {
		if(shared==null)
			return false;
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
	
	@PrePersist
	@PreUpdate
	private void fixSharedFlag(){
		if(isReadForAll()||getPermissions().size()>1)
			shared=true;
		else
			shared=false;
	}
}

