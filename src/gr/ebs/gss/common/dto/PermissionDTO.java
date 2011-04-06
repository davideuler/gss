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
package gr.ebs.gss.common.dto;

import java.io.Serializable;

/**
 * Class representing a permission. A permission belongs to a Folder or
 * FileHeader on one side, and a User or GroupDefinition on the other. It is
 * actually a "group of permissions": <code>boolean</code>s represent each
 * individual permission.
 */
public class PermissionDTO implements Serializable {

	/**
	 * The serial version UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Database id. Not to be set by user.
	 */
	private Long id = null;

	/**
	 * The user to whom this permission belongs, if it is a user permission.
	 * null otherwise
	 */
	private UserDTO user;

	/**
	 * The group to which this permission belongs, if it is a group permission.
	 * null otherwise
	 */
	private GroupDTO group;

	/**
	 * Read permission.
	 */
	private boolean read;

	/**
	 * Write permission.
	 */
	private boolean write;

	/**
	 * Modify ACL permission.
	 */
	private boolean modifyACL;

	/**
	 * Resets all permissions to false
	 */
	public void reset() {
		read = false;
		write = false;
		modifyACL = false;
	}

	// ********************** Accessor Methods ********************** //

	/**
	 * @return Long The DB id of this access
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Set id.
	 *
	 * @param _id Long The id to set
	 */
	public void setId(final Long _id) {
		id = _id;
	}

	/**
	 * Get the user.
	 *
	 * @return User The user
	 */
	public UserDTO getUser() {
		return user;
	}

	/**
	 * Set the user.
	 *
	 * @param _user User The user to set
	 */
	public void setUser(final UserDTO _user) {
		user = _user;
	}

	/**
	 * Get the group.
	 *
	 * @return Group The group
	 */
	public GroupDTO getGroup() {
		return group;
	}

	/**
	 * Set the group.
	 *
	 * @param _group Group The group to set
	 */
	public void setGroup(final GroupDTO _group) {
		group = _group;
	}

	/**
	 * Get the Read permission.
	 *
	 * @return boolean The Read permission
	 */
	public boolean getRead() {
		return read;
	}

	/**
	 * Get the Read permission.
	 *
	 * @return boolean The Read permission
	 */
	public boolean hasRead() {
		return read;
	}

	/**
	 * Set the Read permission.
	 *
	 * @param _read boolean The Read permission.
	 */
	public void setRead(final boolean _read) {
		read = _read;
	}

	/**
	 * Get the Write permission.
	 *
	 * @return boolean The Write permission
	 */
	public boolean getWrite() {
		return write;
	}

	/**
	 * Get the Write permission.
	 *
	 * @return boolean The Write permission
	 */
	public boolean hasWrite() {
		return write;
	}

	/**
	 * Set the Write permission.
	 *
	 * @param newWrite the new write permission
	 */
	public void setWrite(final boolean newWrite) {
		write = newWrite;
	}

	/**
	 * Get the modify ACL permission.
	 *
	 * @return boolean The modify ACL permission
	 */
	public boolean getModifyACL() {
		return modifyACL;
	}

	/**
	 * Get the modify ACL permission.
	 *
	 * @return boolean The modify ACL permission
	 */
	public boolean hasModifyACL() {
		return modifyACL;
	}

	/**
	 * Set the modify ACL permission.
	 *
	 * @param newModifyACL the new modify ACL permission
	 */
	public void setModifyACL(final boolean newModifyACL) {
		modifyACL = newModifyACL;
	}

}
