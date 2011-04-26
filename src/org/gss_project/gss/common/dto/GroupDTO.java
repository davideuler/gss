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
import java.util.HashSet;
import java.util.Set;

/**
 * @author chstath
 */
public class GroupDTO implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The persistence ID of the object.
	 */
	private Long id;

	/**
	 * The name of the group.
	 */
	private String name;

	/**
	 * The user that owns this group.
	 */
	private UserDTO owner;

	/**
	 * The set of users that belong to this group.
	 *
	 */
	private Set<UserDTO> members = new HashSet<UserDTO>();

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param _id the id to set
	 */
	public void setId(final Long _id) {
		id = _id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param _name the name to set
	 */
	public void setName(final String _name) {
		name = _name;
	}

	/**
	 * @return the owner
	 */
	public UserDTO getOwner() {
		return owner;
	}

	/**
	 * @param _owner the owner to set
	 */
	public void setOwner(final UserDTO _owner) {
		owner = _owner;
	}

	/**
	 * Retrieve the members.
	 *
	 * @return the members
	 */
	public Set<UserDTO> getMembers() {
		return members;
	}

	/**
	 * Modify the members.
	 *
	 * @param newMembers the members to set
	 */
	public void setMembers(final Set<UserDTO> newMembers) {
		members = newMembers;
	}
}
