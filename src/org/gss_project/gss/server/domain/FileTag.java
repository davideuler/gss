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
package org.gss_project.gss.server.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A file tag. This is basically a many-to-many relationship between User and
 * FileHeader, with an extra string which is the tag. For implementation
 * details, see Java Persistence With Hibernate, p. 303.
 *
 * @author droutsis
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class FileTag  implements Serializable{

	/**
	 * The composite ID of the object.
	 */
	@Embeddable
	public static class FileTagId implements Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The ID of the user.
		 */
		@Column(name = "userId")
		private Long userId;

		/**
		 * The ID of the file header.
		 */
		@Column(name = "fileId")
		private Long fileId;

		/**
		 * The actual tag.
		 */
		@Column(name = "tag")
		private String tag;

		@Override
		public boolean equals(final Object o) {
			if (o != null && o instanceof FileTagId) {
				final FileTagId that = (FileTagId) o;
				return userId.equals(that.userId) && fileId.equals(that.fileId) && tag.equals(that.tag);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return userId.hashCode() + fileId.hashCode() + tag.hashCode();
		}
	}

	/**
	 * Database id. Not to be set by user.
	 */
	@EmbeddedId
	private FileTagId id;

	/*
	 * No version field: immutable object.
	 */

	/**
	 * The audit information.
	 */
	@SuppressWarnings("unused")
	@Embedded
	private AuditInfo auditInfo;

	/**
	 * The user who added this tag.
	 */
	@ManyToOne
	@JoinColumn(name = "userId", nullable = false, insertable=false, updatable=false)
	private User user;

	/**
	 * The file to which this tag belongs.
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "fileId", nullable = false, insertable=false, updatable=false)
	private FileHeader file;

	/**
	 * The name of the tag.
	 */
	@Column(nullable=false, insertable=false, updatable=false)
	private String tag;

	/**
	 * A default constructor.
	 */
	public FileTag() {
		// Empty.
	}

	/**
	 * The proper constructor to call, which guarantees referential integrity.
	 *
	 * @param aUser The user who adds the tag
	 * @param aFile The file to which the tag is added
	 * @param aTag The tag itself
	 */
	public FileTag(final User aUser, final FileHeader aFile, final String aTag) {
		// Set fields
		user = aUser;
		file = aFile;
		tag = aTag;

		// Set identifier values
		id = new FileTagId();
		id.userId = aUser.getId();
		id.fileId = aFile.getId();
		id.tag = aTag;

		// Guarantee referential integrity
		aUser.getFileTags().add(this);
		aFile.getFileTags().add(this);

		final AuditInfo ai = new AuditInfo();
		ai.setCreatedBy(aUser);
		ai.setModifiedBy(aUser);
		final Date now = new Date();
		ai.setCreationDate(now);
		ai.setModificationDate(now);
		auditInfo = ai;
	}

	/**
	 * Retrieve the FileTagId.
	 *
	 * @return the ID
	 */
	public FileTagId getId() {
		return id;
	}

	/**
	 * Retrieve the user.
	 *
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Retrieve the file header.
	 *
	 * @return the fileheader
	 */
	public FileHeader getFile() {
		return file;
	}

	/**
	 * Retrieve the actual tag content.
	 *
	 * @return the tag
	 */
	public String getTag() {
		return tag;
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
	 * Modify the user.
	 *
	 * @param newUser the user to set
	 */
	public void setUser(User newUser) {
		user = newUser;
	}


	/**
	 * Modify the file.
	 *
	 * @param newFile the file to set
	 */
	public void setFile(FileHeader newFile) {
		file = newFile;
	}
}
