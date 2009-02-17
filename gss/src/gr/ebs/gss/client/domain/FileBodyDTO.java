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
package gr.ebs.gss.client.domain;

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
	 * @param auditInfo the auditInfo to set
	 */
	public void setAuditInfo(AuditInfoDTO auditInfo) {
		this.auditInfo = auditInfo;
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
	 * @param originalFilenameEncoded the originalFilenameEncoded to set
	 */
	public void setOriginalFilenameEncoded(String originalFilenameEncoded) {
		this.originalFilenameEncoded = originalFilenameEncoded;
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
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
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
	 * @param fileHeaderId the fileHeaderId to set
	 */
	public void setFileHeaderId(Long fileHeaderId) {
		this.fileHeaderId = fileHeaderId;
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
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
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
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
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
	 * @param originalFilename the originalFilename to set
	 */
	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
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
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
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

	private String getSize(Long size, Double division){
		Double res = Double.valueOf(size.toString())/division;
		NumberFormat nf = NumberFormat.getFormat("######.###");
		return nf.format(res);
	}

}
