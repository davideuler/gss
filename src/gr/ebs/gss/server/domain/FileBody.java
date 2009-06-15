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

import gr.ebs.gss.server.domain.dto.FileBodyDTO;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The mutable part of the structure of a file on the GSS service.
 */
@Entity
public final class FileBody  implements Serializable{
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(FileBody.class);

	/**
	 * The persistence ID of the object.
	 */
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Version field for optimistic locking. Renamed to avoid conflict with file
	 * body version.
	 */
	@SuppressWarnings("unused")
	@Version
	private int dbVersion;

	/**
	 * The audit information.
	 */
	@Embedded
	private AuditInfo auditInfo;

	/**
	 * The version of the file, not the JPA version field!
	 */
	private int version;

	/**
	 * The header of the file.
	 */
	@ManyToOne
	private FileHeader header;

	/**
	 * The MIME type of this file.
	 */
	private String mimeType;

	/**
	 * The original filename (with which file was uploaded)
	 */
	private String originalFilename;

	/**
	 * The full file path (path+filename) under which file is currently stored
	 * in local file system
	 */
	private String storedFilePath;

	/**
	 * The file size in bytes
	 */
	private long fileSize;

	/**
	 * Returns the version
	 *
	 * @return int
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Sets a new version
	 *
	 * @param newVersion
	 */
	public void setVersion(final int newVersion) {
		version = newVersion;
	}

	/**
	 * Retrieve the file header.
	 *
	 * @return the file header
	 */
	public FileHeader getHeader() {
		return header;
	}

	/**
	 * Modify the file header.
	 *
	 * @param newHeader the new header
	 */
	public void setHeader(final FileHeader newHeader) {
		header = newHeader;
	}

	/**
	 * Retrieve the MIME type.
	 *
	 * @return the MIME type
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Modify the MIME type.
	 *
	 * @param newMimeType the new MIME type
	 */
	public void setMimeType(final String newMimeType) {
		mimeType = newMimeType;
	}

	/**
	 * Retrieve the original filename.
	 *
	 * @return the original filename
	 */
	public String getOriginalFilename() {
		return originalFilename;
	}

	/**
	 * Modify the original filename.
	 *
	 * @param newOriginalFilename the new original filename
	 */
	public void setOriginalFilename(final String newOriginalFilename) {
		originalFilename = newOriginalFilename;
	}

	/**
	 * Retrieve the stored file path.
	 *
	 * @return the file path
	 */
	public String getStoredFilePath() {
		return storedFilePath;
	}

	/**
	 * Modify the stored file path.
	 *
	 * @param newStoredFilePath the new file path
	 */
	public void setStoredFilePath(final String newStoredFilePath) {
		storedFilePath = newStoredFilePath;
	}

	/**
	 * Retrieve the file size.
	 *
	 * @return the file size
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * Modify the file size.
	 *
	 * @param newFileSize the new file size
	 */
	public void setFileSize(final long newFileSize) {
		fileSize = newFileSize;
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
	 * Return the original filename URL-encoded.
	 *
	 * @return the original filename URL-encoded.
	 */
	public String getOriginalFilenameEncoded() {
		try {
			return URLEncoder.encode(getOriginalFilename(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
			return getOriginalFilename();
		}
	}

	public FileBodyDTO getDTO(){
		FileBodyDTO dto = new FileBodyDTO();
		dto.setId(id);
		dto.setVersion(version);
		dto.setFileHeaderId(getHeader().getId());
		dto.setFileSize(fileSize);
		dto.setMimeType(mimeType);
		dto.setOriginalFilename(originalFilename);
		dto.setOriginalFilenameEncoded(getOriginalFilenameEncoded());
		dto.setAuditInfo(auditInfo.getDTO());
		return dto;

	}
}
