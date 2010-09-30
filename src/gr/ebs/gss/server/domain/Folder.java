/*
 * Copyright 2007, 2008, 2009, 2010 Electronic Business Systems Ltd.
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

import gr.ebs.gss.server.domain.dto.FolderDTO;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;
import com.google.code.morphia.annotations.Version;


/**
 * The structure of a folder on the GSS service.
 */
@Entity
public class Folder  implements Serializable {
	/**
	 * The persistence ID of the object.
	 * XXX: we must generate unique ids ourselves, if type is not ObjectId,
	 * so we do it in the constructor
	 */
	@Id
	private Long id;

	/**
	 * XXX: The constructor is only necessary for enforcing unique ids. If/When
	 * id is converted to ObjectId this will no longer be necessary.
	 */
	public Folder() {
		id = new Random().nextLong();
	}

	/**
	 * Version field for optimistic locking.
	 */
	@SuppressWarnings("unused")
	@Version
	private long version;

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
	 * The folder name.
	 */
	private String name;

	/**
	 * The files in this folder. A List so we can keep order.
	 */
	@Reference
	private List<FileHeader> files = new ArrayList<FileHeader>();

	/**
	 * The subfolders in this folder. A List so we can keep order.
	 */
	@Reference
	private List<Folder> subfolders = new ArrayList<Folder>();

	/**
	 * The parent folder of this one.
	 */
	@Reference
	private Folder parent;

	/**
	 * The owner of this folder.
	 */
	@Reference
	private User owner;

	/**
	 * Set of Permission objects: The permissions (User and Group) for this
	 * Folder.
	 */
	@Embedded
	private Set<Permission> permissions = new HashSet<Permission>();

	/**
	 * Is this folder temporarily deleted?
	 */
	private boolean deleted = false;

	/**
	 * Can this folder be read by anyone?
	 */
	private boolean readForAll = false;

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
	 * Replace the icon.
	 *
	 * @param newIcon the new icon
	 */
	public void setIcon(String newIcon) {
		icon = newIcon;
	}

	/**
	 * Retrieve the folder name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Modify the folder name.
	 *
	 * @param newName the new name
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * Retrieve the list of files in the folder.
	 *
	 * @return a list of file header objects
	 */
	public List<FileHeader> getFiles() {
		return files;
	}

	/**
	 * Replace the list of files in the folder.
	 *
	 * @param newFiles the new list of files
	 */
	public void setFiles(List<FileHeader> newFiles) {
		files = newFiles;
	}

	/**
	 * Retrieve the list of subfolders.
	 *
	 * @return the subfolders
	 */
	public List<Folder> getSubfolders() {
		return subfolders;
	}

	/**
	 * Replace the list of subfolders.
	 *
	 * @param newSubfolders the new subfolders
	 */
	public void setSubfolders(List<Folder> newSubfolders) {
		subfolders = newSubfolders;
	}

	/**
	 * Retrieve the parent folder.
	 *
	 * @return the parent
	 */
	public Folder getParent() {
		return parent;
	}

	/**
	 * Replace the parent folder.
	 *
	 * @param newParent the new parent
	 */
	public void setParent(Folder newParent) {
		parent = newParent;
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
	 * Modify the owner.
	 *
	 * @param newOwner the new owner
	 */
	public void setOwner(User newOwner) {
		owner = newOwner;
	}

	/**
	 * Retrieve the audit info.
	 *
	 * @return the audit info
	 */
	public AuditInfo getAuditInfo() {
		return auditInfo;
	}

	/**
	 * Modify the audit info.
	 *
	 * @param newAuditInfo the new audit info
	 */
	public void setAuditInfo(AuditInfo newAuditInfo) {
		auditInfo = newAuditInfo;
	}

	/**
	 * Retrieve the permission set.
	 *
	 * @return the permissions
	 */
	public Set<Permission> getPermissions() {
		return permissions;
	}

	/**
	 * Replace the permission set.
	 *
	 * @param newPermissions the new permissions
	 */
	public void setPermissions(Set<Permission> newPermissions) {
		permissions = newPermissions;
	}

	/**
	 * Adds a subfolder to this folder. If the child already belongs to another
	 * parent folder, it is first removed from it.
	 *
	 * @param subfolder Folder to add
	 * @throws IllegalArgumentException if folder is null
	 */
	public void addSubfolder(Folder subfolder) {
		if (subfolder == null)
			throw new IllegalArgumentException("Can't add a null subfolder as a child.");
		// Remove from old parent folder
		if (subfolder.getParent() != null)
			subfolder.getParent().removeSubfolder(subfolder);
		// Set parent in child
		subfolder.setParent(this);
		// Set child in parent
		getSubfolders().add(subfolder);
	}

	/**
	 * Removes a subfolder from this folder.
	 *
	 * @param subfolder Folder to remove
	 * @throws IllegalArgumentException if subfolder is null
	 */
	public void removeSubfolder(Folder subfolder) {
		if (subfolder == null)
			throw new IllegalArgumentException("Can't remove a null subfolder.");
		getSubfolders().remove(subfolder);
		subfolder.setParent(null);
	}

	/**
	 * Adds a file to this folder. If the file already belongs to another parent
	 * folder, it is first removed from it.
	 *
	 * @param file FileHeader to add
	 * @throws IllegalArgumentException if file is null
	 */
	public void addFile(FileHeader file) {
		if (file == null)
			throw new IllegalArgumentException("Can't add a null file.");
		// Remove from old parent folder
		if (file.getFolder() != null)
			file.getFolder().removeFile(file);

		getFiles().add(file);
		file.setFolder(this);
	}

	/**
	 * Removes a file from this folder.
	 *
	 * @param file FileHeader to remove
	 * @throws IllegalArgumentException if file is null
	 */
	public void removeFile(FileHeader file) {
		if (file == null)
			throw new IllegalArgumentException("Can't remove a null file.");
		getFiles().remove(file);
		file.setFolder(null);
	}

	/**
	 * Adds a permission to this Folder.
	 *
	 * @param permission Permission to add
	 * @throws IllegalArgumentException if permission is null
	 */
	public void addPermission(Permission permission) {
		if (permission == null)
			throw new IllegalArgumentException("Can't add a null Permission.");
		getPermissions().add(permission);
	}

	/**
	 * Return the FolderDTO for this Folder object. The object graph that is
	 * constructed has maximum depth 2. This method is mainly intended for use
	 * by the web application interface.
	 *
	 * @return the FolderDTO that corresponds to this Folder
	 */
	public FolderDTO getDTO() {
		return getDTO(2);
	}

	/**
	 * Return the FolderDTO for this Folder object. The object graph that is
	 * constructed has the specified maximum depth and contains marked as deleted folders
	 *
	 * @param depth the maximum depth of the returned folder tree
	 * @return the FolderDTO that corresponds to this Folder
	 */
	private FolderDTO getDTO(int depth) {
		FolderDTO f = new FolderDTO();
		f.setId(id);
		f.setName(name);
		f.setPath(getPath());
		f.setOwner(owner.getDTO());
		f.setAuditInfo(auditInfo.getDTO());
		f.setDeleted(deleted);
		f.setReadForAll(readForAll);
		if (parent != null)
			f.setParent(parent.getDTO(0));
		for (Folder subfolder : subfolders)
			if (depth > 0)
				f.getSubfolders().add(subfolder.getDTO(depth - 1));
		return f;
	}

	/**
	 * Checks if the specified user has permission to delete this folder, by
	 * checking if the user has write permission to the parent folder.
	 *
	 * @param user the specified User
	 * @return true if the user has permission to delete the folder, false
	 *         otherwise
	 */
	public boolean hasDeletePermission(User user) {
		if (parent.hasWritePermission(user))
			return true;
		return false;
	}

	/**
	 * Checks if the specified user has permission to modify this folder.
	 *
	 * @param user the specified User
	 * @return true if the user has permission to modify the folder, false
	 *         otherwise
	 */
	public boolean hasWritePermission(User user) {
		for (Permission p : permissions)
			if (p.getUser() != null) {
				if (p.getUser().equals(user) && p.getWrite())
					return true;
			} else if (p.getGroup().contains(user) && p.getWrite())
				return true;
		return false;
	}

	/**
	 * Checks if the specified user has permission to read this folder.
	 *
	 * @param user the specified User
	 * @return true if the user has permission to read the folder, false
	 *         otherwise
	 */
	public boolean hasReadPermission(User user) {
		for (Permission p : permissions)
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
	public boolean hasModifyACLPermission(User user) {
		for (Permission p : permissions)
			if (p.getUser() != null) {
				if (p.getUser().equals(user) && p.getModifyACL())
					return true;
			} else if (p.getGroup().contains(user) && p.getModifyACL())
				return true;
		return false;
	}

	/**
	 * Checks if the specified folder is shared (if it has permissions for other users and groups)
	 *
	 * @param user the specified User
	 * @return true if the user has permission to read the folder, false
	 *         otherwise
	 */
	public boolean isShared(User user) {
		for (Permission p : permissions)
			if (p.getUser() != null) {
				if (!p.getUser().equals(user))
					return true;
			} else if (!p.getGroup().contains(user))
				return true;
		return false;
	}

	public boolean isSharedForOtherUser(User user) {
		for (Permission p : permissions)
			if (p.getUser() != null) {
				if (p.getUser().equals(user))
					return true;
			} else if (p.getGroup().contains(user))
				return true;
		return false;
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
	 * Retrieve the full path of the folder, URL-encoded in the form:
	 * /parent1/parent2/parent3/name
	 *
	 * @return the full path from the root of the files namespace
	 */
	public String getPath() {
		if (parent == null)
			return "/";
		try {
			return parent.getPath() + URLEncoder.encode(name, "UTF-8") + '/';
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
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
	 * Retrieve the readForAll.
	 *
	 * @return the readForAll
	 */
	public boolean isReadForAll() {
		return readForAll;
	}

	@Override
	public String toString() {
		return "Folder [name=" + name + "]";
	}
}
