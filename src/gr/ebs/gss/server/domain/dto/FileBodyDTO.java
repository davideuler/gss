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
package gr.ebs.gss.server.domain.dto;

import java.io.Serializable;

import com.google.gwt.i18n.client.NumberFormat;


/**
 * @author kman
 *
 */
public class FileBodyDTO implements Serializable {
	private Long id;
	private Long headerId;
	private int version;
	private String mimeType;

	private String originalName;
	private String originalNameEncoded;

	private long size;
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
	 * Retrieve the originalNameEncoded.
	 *
	 * @return the originalNameEncoded
	 */
	public String getOriginalNameEncoded() {
		return originalNameEncoded;
	}

	/**
	 * Modify the originalNameEncoded.
	 *
	 * @param anOriginalNameEncoded the originalNameEncoded to set
	 */
	public void setOriginalNameEncoded(String anOriginalNameEncoded) {
		originalNameEncoded = anOriginalNameEncoded;
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
	 * Retrieve the headerId.
	 *
	 * @return the headerId
	 */
	public Long getHeaderId() {
		return headerId;
	}

	/**
	 * Modify the headerId.
	 *
	 * @param aHeaderId the headerId to set
	 */
	public void setHeaderId(Long aHeaderId) {
		headerId = aHeaderId;
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
	 * Retrieve the originalName.
	 *
	 * @return the originalName
	 */
	public String getOriginalName() {
		return originalName;
	}

	/**
	 * Modify the originalName.
	 *
	 * @param anOriginalName the originalName to set
	 */
	public void setOriginalName(String anOriginalName) {
		originalName = anOriginalName;
	}

	/**
	 * Retrieve the size.
	 *
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Modify the size.
	 *
	 * @param aSize the size to set
	 */
	public void setSize(long aSize) {
		size = aSize;
	}

	/**
	 * Return the file size in a humanly readable form, using SI units to denote
	 * size information, e.g. 1 KB = 1000 B (bytes).
	 */
	public String getSizeAsString() {
		if (size < 1024)
			return String.valueOf(size) + " B";
		else if (size <= 1024*1024)
			return getSize(size, 1024D) + " KB";
		else if (size <= 1024*1024*1024)
			return getSize(size,(1024D*1024D)) + " MB";
		return getSize(size , (1024D*1024D*1024D)) + " GB";
	}

	private String getSize(Long aSize, Double division) {
		Double res = Double.valueOf(aSize.toString())/division;
		NumberFormat nf = NumberFormat.getFormat("######.###");
		return nf.format(res);
	}

}
