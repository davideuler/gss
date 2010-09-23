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

import gr.ebs.gss.server.domain.dto.FileBodyDTO;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Reference;

/**
 * The mutable part of the structure of a file on the GSS service.
 */
@Embedded
public class FileBody implements Serializable{
	/**
	 * The logger.
	 */
	private static Log logger = LogFactory.getLog(FileBody.class);

 	/**
	 * The persistence ID of the object.
	 * XXX: this is redundant and should be removed and all call sites should
	 * be modified to use the version field instead.
	 */
	private Long id;

	/**
	 * XXX: The constructor is only necessary for enforcing unique ids. If/When
	 * id is converted to ObjectId this will no longer be necessary.
	 */
	public FileBody() {
		id = new Random().nextLong();
	}

	/**
	 * The audit information.
	 */
	@Embedded
	private AuditInfo auditInfo;

	/**
	 * The version of the file, not the optimistic locking version field!
	 */
	private int version;

	/**
	 * The header of the file.
	 * XXX: this is superfluous if FileBody is embedded into FileHeader.
	 */
	@Reference
	private FileHeader header;

	/**
	 * The MIME type of this file.
	 */
	private String mimeType;

	/**
	 * The original name (with which file was uploaded)
	 */
	private String originalName;

	/**
	 * The full file path (path+filename) under which file is currently stored
	 * in local file system
	 */
	private String storedPath;

	/**
	 * The file size in bytes
	 */
	private long size;

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
	public void setVersion(int newVersion) {
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
	public void setHeader(FileHeader newHeader) {
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
	public void setMimeType(String newMimeType) {
		mimeType = newMimeType;
	}

	/**
	 * Retrieve the original name.
	 *
	 * @return the original name
	 */
	public String getOriginalName() {
		return originalName;
	}

	/**
	 * Modify the original name.
	 *
	 * @param newOriginalName the new original name
	 */
	public void setOriginalName(String newOriginalName) {
		originalName = newOriginalName;
	}

	/**
	 * Retrieve the stored file path.
	 *
	 * @return the file path
	 */
	public String getStoredPath() {
		return storedPath;
	}

	/**
	 * Modify the stored file path.
	 *
	 * @param newStoredPath the new file path
	 */
	public void setStoredPath(String newStoredPath) {
		storedPath = newStoredPath;
	}

	/**
	 * Retrieve the file size.
	 *
	 * @return the file size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Modify the file size.
	 *
	 * @param newSize the new file size
	 */
	public void setSize(long newSize) {
		size = newSize;
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
	 * Return the original name URL-encoded.
	 *
	 * @return the original name URL-encoded.
	 */
	public String getOriginalNameEncoded() {
		try {
			return URLEncoder.encode(getOriginalName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
			return getOriginalName();
		}
	}

	public FileBodyDTO getDTO(){
		FileBodyDTO dto = new FileBodyDTO();
		dto.setId(id);
		dto.setVersion(version);
		dto.setHeaderId(getHeader().getId());
		dto.setSize(size);
		dto.setMimeType(mimeType);
		dto.setOriginalName(originalName);
		dto.setOriginalNameEncoded(getOriginalNameEncoded());
		dto.setAuditInfo(auditInfo.getDTO());
		return dto;

	}
}
