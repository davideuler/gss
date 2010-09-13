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

import gr.ebs.gss.server.domain.dto.AuditInfoDTO;

import java.io.Serializable;
import java.util.Date;

import com.google.code.morphia.annotations.Embedded;


/**
 * This class holds audit information pertaining to the associated domain
 * object. This information includes the user that created the object, the
 * creation time, the user that modified the object and the modification date.
 */
@Embedded
public class AuditInfo  implements Serializable{

	/**
	 * The date the associated object was created. We can never change it after
	 * creation.
	 */
//	@Temporal(TemporalType.TIMESTAMP)
//	@Column(updatable = false, nullable = false)
	private Date creationDate;

	/**
	 * The user that created the associated object. We can never change it after
	 * creation.
	 */
//	@ManyToOne
//	@JoinColumn(updatable = false)
	private User createdBy;

	/**
	 * The date the associated object was modified.
	 */
//	@Temporal(TemporalType.TIMESTAMP)
//	@Column(nullable = false)
	private Date modificationDate;

	/**
	 * The user that modified the associated object.
	 */
//	@ManyToOne
	private User modifiedBy;

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
	 * @param _creationDate the new date of creation
	 */
	public void setCreationDate(final Date _creationDate) {
		creationDate = _creationDate;
	}

	/**
	 * Retrieve the user that created the associated object.
	 *
	 * @return the user that created the associated object
	 */
	public User getCreatedBy() {
		return createdBy;
	}

	/**
	 * Modify the user that created the associated object.
	 *
	 * @param _createdBy the new user that created the associated object
	 */
	public void setCreatedBy(final User _createdBy) {
		createdBy = _createdBy;
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
	 * @param _modificationDate the new date of modification
	 */
	public void setModificationDate(final Date _modificationDate) {
		modificationDate = _modificationDate;
	}

	/**
	 * Retrieve the user that modified the associated object.
	 *
	 * @return the user that modified the associated object
	 */
	public User getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * Modify the user that modified the associated object.
	 *
	 * @param _modifiedBy the new user that modified the associated object
	 */
	public void setModifiedBy(final User _modifiedBy) {
		modifiedBy = _modifiedBy;
	}

	/**
	 * Retrieve the Data Transfer Object for this audit info, that pays
	 * particular attention to avoid any instances of java.sql.Timestamp in the
	 * creation and modification dates.
	 *
	 * @return a DTO object with audit info
	 */
	public AuditInfoDTO getDTO() {
		final AuditInfoDTO dto = new AuditInfoDTO();
		dto.setCreatedBy(createdBy.getDTO());
		dto.setCreationDate(new Date(creationDate.getTime()));
		dto.setModificationDate(new Date(modificationDate.getTime()));
		dto.setModifiedBy(modifiedBy.getDTO());
		return dto;
	}
}
