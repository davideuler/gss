/*
 * Copyright 2009, 2010 Electronic Business Systems Ltd.
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

import java.io.Serializable;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;


/**
 * @author kman
 *
 */
@Entity
public class FileUploadStatus implements Serializable{

	/**
	 * The persistence ID of the object.
	 */
	@Id
	private Long id;

	/**
	 * The owner of this file.
	 */
//	@ManyToOne(optional=false)
//	@JoinColumn(name="owner_id", nullable=false)
	private User owner;

//	@Column(name="filename", nullable=false)
	private String filename;

	private Long bytesUploaded;

	private Long fileSize;

	/**
	 * Retrieve the id.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Modify the id.
	 *
	 * @param anId the id to set
	 */
	public void setId(Long anId) {
		id = anId;
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
	 * @param anOwner the owner to set
	 */
	public void setOwner(User anOwner) {
		owner = anOwner;
	}

	/**
	 * Retrieve the filename.
	 *
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Modify the filename.
	 *
	 * @param aFilename the filename to set
	 */
	public void setFilename(String aFilename) {
		filename = aFilename;
	}

	/**
	 * Retrieve the bytesUploaded.
	 *
	 * @return the bytesUploaded
	 */
	public Long getBytesUploaded() {
		return bytesUploaded;
	}

	/**
	 * Modify the bytesUploaded.
	 *
	 * @param theBytesUploaded the bytesUploaded to set
	 */
	public void setBytesUploaded(Long theBytesUploaded) {
		bytesUploaded = theBytesUploaded;
	}

	/**
	 * Retrieve the fileSize.
	 *
	 * @return the fileSize
	 */
	public Long getFileSize() {
		return fileSize;
	}

	/**
	 * Modify the fileSize.
	 *
	 * @param aFileSize the fileSize to set
	 */
	public void setFileSize(Long aFileSize) {
		fileSize = aFileSize;
	}
}
