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

import java.util.Date;

/**
 * The Data Transfer Object version of the AuditInfo class.
 *
 * @author past
 */
public class AuditInfoDTO implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The date the associated object was created.
	 */
	private Date creationDate;

	/**
	 * The user that created the associated object.
	 */
	private UserDTO createdBy;

	/**
	 * The date the associated object was modified.
	 */
	private Date modificationDate;

	/**
	 * The user that modified the associated object.
	 */
	private UserDTO modifiedBy;

	/**
	 * A default constructor for serialization.
	 */
	public AuditInfoDTO() {
		// Do nothing.
	}

	/**
	 * Retrieve the creation date for the associated object.
	 *
	 * @return the date of creation
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Modify the creation date for the associated object.
	 *
	 * @param newCreationDate the new date of creation
	 */
	public void setCreationDate(final Date newCreationDate) {
		creationDate = newCreationDate;
	}

	/**
	 * Retrieve the user that created the associated object.
	 *
	 * @return the user that created the associated object
	 */
	public UserDTO getCreatedBy() {
		return createdBy;
	}

	/**
	 * Modify the user that created the associated object.
	 *
	 * @param newCreatedBy the new user that created the associated object
	 */
	public void setCreatedBy(final UserDTO newCreatedBy) {
		createdBy = newCreatedBy;
	}

	/**
	 * Retrieve the modification date for the associated object.
	 *
	 * @return the date of modification
	 */
	public Date getModificationDate() {
		return modificationDate;
	}

	/**
	 * Modify the modification date for the associated object.
	 *
	 * @param newModificationDate the new date of modification
	 */
	public void setModificationDate(final Date newModificationDate) {
		modificationDate = newModificationDate;
	}

	/**
	 * Retrieve the user that modified the associated object.
	 *
	 * @return the user that modified the associated object
	 */
	public UserDTO getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * Modify the user that modified the associated object.
	 *
	 * @param newModifiedBy the new user that modified the associated object
	 */
	public void setModifiedBy(final UserDTO newModifiedBy) {
		modifiedBy = newModifiedBy;
	}
}
