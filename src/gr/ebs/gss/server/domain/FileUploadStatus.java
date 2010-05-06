/*
 * Copyright 2009 Electronic Business Systems Ltd.
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


/**
 * @author kman
 *
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "filename"}))
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class FileUploadStatus implements Serializable{

	/**
	 * The persistence ID of the object.
	 */
	@SuppressWarnings("unused")
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * The owner of this file.
	 */
	@ManyToOne(optional=false)
	@JoinColumn(name="owner_id", nullable=false)
	private User owner;

	@Column(name="filename", nullable=false)
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
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
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
	 * @param owner the owner to set
	 */
	public void setOwner(User owner) {
		this.owner = owner;
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
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
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
	 * @param bytesUploaded the bytesUploaded to set
	 */
	public void setBytesUploaded(Long bytesUploaded) {
		this.bytesUploaded = bytesUploaded;
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
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}



}
