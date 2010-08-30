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

import gr.ebs.gss.server.domain.dto.PermissionDTO;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Class representing a permission. A permission belongs to a Folder or
 * FileHeader on one side, and a User or GroupDefinition on the other. It is
 * actually a "group of permissions": <code>boolean</code>s represent each
 * individual permission.
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Permission implements Serializable {

	/**
	 * Database id. Not to be set by user.
	 */
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Database record version. Used by hibernate, not to be used by user.
	 */
	@SuppressWarnings("unused")
	@Version
	private int version;

	/**
	 * The user to whom this permission belongs, if it is a user permission.
	 * null otherwise
	 */
	@ManyToOne
	private User user;

	/**
	 * The group to which this permission belongs, if it is a group permission.
	 * null otherwise
	 */
	@ManyToOne
	private Group group;

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
	 * Get the user.
	 *
	 * @return User The user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Set the user.
	 *
	 * @param _user User The user to set
	 */
	public void setUser(final User _user) {
		user = _user;
	}

	/**
	 * Get the group.
	 *
	 * @return Group The group
	 */
	public Group getGroup() {
		return group;
	}

	/**
	 * Set the group.
	 *
	 * @param _group Group The group to set
	 */
	public void setGroup(final Group _group) {
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

	// ********************** Business Methods ********************** //

	/**
	 * Merge an initial Permission object with a collection of individual
	 * permissions and produce an aggregate Permission object. Aggregate object
	 * has a permission if at least one of the individual objects has said
	 * permission.
	 *
	 * @param initialPermission Permission The initial Permission object
	 * @param permissions Collection Permissions with which to merge
	 * @return Permission Aggregate permission object
	 */
	private Permission mergePermissions(final Permission initialPermission, final Collection<Permission> permissions) {
		if (permissions != null)
			for (final Permission perm : permissions) {
				initialPermission.setRead(initialPermission.hasRead() || perm.hasRead());
				initialPermission.setWrite(initialPermission.hasWrite() || perm.hasWrite());
				initialPermission.setModifyACL(initialPermission.hasModifyACL() || perm.hasModifyACL());
			}
		return initialPermission;
	}

	/**
	 * Merge with a collection of individual permissions and produce an
	 * aggregate Permission object. Aggregate object has a permission if at
	 * least one of the individual objects has said permission.
	 *
	 * @param permissions Collection Permissions with which to merge
	 * @return Permission Aggregate permission object
	 */
	public Permission mergePermissions(final Collection<Permission> permissions) {
		return mergePermissions(this, permissions);
	}

	/**
	 * Return a new Data Transfer Object for this object.
	 *
	 * @return a new DTO with the same contents as this object
	 */
	public PermissionDTO getDTO() {
		final PermissionDTO p = new PermissionDTO();
		p.setId(id);
		if(group != null)
			p.setGroup(group.getDTO());
		if(user != null)
			p.setUser(user.getDTO());
		p.setRead(read);
		p.setWrite(write);
		p.setModifyACL(modifyACL);
		return p;
	}

}
