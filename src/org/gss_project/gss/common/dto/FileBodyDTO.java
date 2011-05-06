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

import com.google.gwt.i18n.client.NumberFormat;


/**
 * @author kman
 *
 */
public class FileBodyDTO implements Serializable{
	private Long id;
	private Long fileHeaderId;
	private int version;
	private String mimeType;

	private String originalFilename;
	private String originalFilenameEncoded;

	private long fileSize;
	private AuditInfoDTO auditInfo;

	/**
	 * Retrieve the auditInfo.
	 *
	 * @return the auditInfo
	 */
	public AuditInfoDTO getAuditInfo() {
		return auditInfo;
	}

	/**
	 * Modify the auditInfo.
	 *
	 * @param anAuditInfo the auditInfo to set
	 */
	public void setAuditInfo(AuditInfoDTO anAuditInfo) {
		auditInfo = anAuditInfo;
	}

	/**
	 * Retrieve the originalFilenameEncoded.
	 *
	 * @return the originalFilenameEncoded
	 */
	public String getOriginalFilenameEncoded() {
		return originalFilenameEncoded;
	}

	/**
	 * Modify the originalFilenameEncoded.
	 *
	 * @param anOriginalFilenameEncoded the originalFilenameEncoded to set
	 */
	public void setOriginalFilenameEncoded(String anOriginalFilenameEncoded) {
		originalFilenameEncoded = anOriginalFilenameEncoded;
	}

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
	 * Retrieve the fileHeaderId.
	 *
	 * @return the fileHeaderId
	 */
	public Long getFileHeaderId() {
		return fileHeaderId;
	}

	/**
	 * Modify the fileHeaderId.
	 *
	 * @param aFileHeaderId the fileHeaderId to set
	 */
	public void setFileHeaderId(Long aFileHeaderId) {
		fileHeaderId = aFileHeaderId;
	}

	/**
	 * Retrieve the version.
	 *
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Modify the version.
	 *
	 * @param aVersion the version to set
	 */
	public void setVersion(int aVersion) {
		version = aVersion;
	}

	/**
	 * Retrieve the mimeType.
	 *
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Modify the mimeType.
	 *
	 * @param aMimeType the mimeType to set
	 */
	public void setMimeType(String aMimeType) {
		mimeType = aMimeType;
	}

	/**
	 * Retrieve the originalFilename.
	 *
	 * @return the originalFilename
	 */
	public String getOriginalFilename() {
		return originalFilename;
	}

	/**
	 * Modify the originalFilename.
	 *
	 * @param anOriginalFilename the originalFilename to set
	 */
	public void setOriginalFilename(String anOriginalFilename) {
		originalFilename = anOriginalFilename;
	}

	/**
	 * Retrieve the fileSize.
	 *
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * Modify the fileSize.
	 *
	 * @param aFileSize the fileSize to set
	 */
	public void setFileSize(long aFileSize) {
		fileSize = aFileSize;
	}

	/**
	 * Return the file size in a humanly readable form, using SI units to denote
	 * size information, e.g. 1 KB = 1000 B (bytes).
	 *
	 * @return the fileSize
	 */
	public String getFileSizeAsString() {
		if (fileSize < 1024)
			return String.valueOf(fileSize) + " B";
		else if (fileSize <= 1024*1024)
			return getSize(fileSize, 1024D) + " KB";
		else if (fileSize <= 1024*1024*1024)
			return getSize(fileSize,(1024D*1024D)) + " MB";
		return getSize(fileSize , (1024D*1024D*1024D)) + " GB";
	}

	private String getSize(Long size, Double division) {
		Double res = Double.valueOf(size.toString())/division;
		NumberFormat nf = NumberFormat.getFormat("######.###");
		return nf.format(res);
	}

}
