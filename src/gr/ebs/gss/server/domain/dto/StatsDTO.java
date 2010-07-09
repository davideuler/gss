/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
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
 * DTO Object holding stats for User: file count, size count and quota
 * @author kman
 *
 */
public class StatsDTO implements Serializable{

	private Long fileCount = 0L;
	private Long fileSize = 0L;
	private Long quotaLeftSize = 0L;
	private Long bandwithQuotaUsed=0L;

	public StatsDTO() {

	}

	public StatsDTO(Long aFileCount, Long aFileSize, Long aQuotaLeftSize) {
		fileCount = aFileCount;
		fileSize = aFileSize;
		quotaLeftSize = aQuotaLeftSize;
	}

	/**
	 * Retrieve the fileCount.
	 *
	 * @return the fileCount
	 */
	public Long getFileCount() {
		return fileCount;
	}

	/**
	 * Modify the fileCount.
	 *
	 * @param aFileCount the fileCount to set
	 */
	public void setFileCount(Long aFileCount) {
		fileCount = aFileCount;
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

	/**
	 * Retrieve the quotaLeftSize.
	 *
	 * @return the quotaLeftSize
	 */
	public Long getQuotaLeftSize() {
		return quotaLeftSize;
	}

	/**
	 * Modify the quotaLeftSize.
	 *
	 * @param aQuotaLeftSize the quotaLeftSize to set
	 */
	public void setQuotaLeftSize(Long aQuotaLeftSize) {
		quotaLeftSize = aQuotaLeftSize;
	}



	/**
	 * Retrieve the bandwithQuotaUsed.
	 *
	 * @return the bandwithQuotaUsed
	 */
	public Long getBandwithQuotaUsed() {
		return bandwithQuotaUsed;
	}


	/**
	 * Modify the bandwithQuotaUsed.
	 *
	 * @param aBandwithQuotaUsed the bandwithQuotaUsed to set
	 */
	public void setBandwithQuotaUsed(Long aBandwithQuotaUsed) {
		bandwithQuotaUsed = aBandwithQuotaUsed;
	}

	public String getFileSizeAsString() {
		if (fileSize < 1024)
			return String.valueOf(fileSize) + " B";
		else if (fileSize <= 1024*1024)
			return getSize(fileSize, 1024D) + " KB";
		else if (fileSize <= 1024*1024*1024)
			return getSize(fileSize,(1024D*1024D)) + " MB";
		return getSize(fileSize , (1024D*1024D*1024D)) + " GB";
	}

	public String getQuotaLeftAsString() {
		if (quotaLeftSize < 1024)
			return String.valueOf(quotaLeftSize) + " B";
		else if (quotaLeftSize <= 1024*1024)
			return getSize(quotaLeftSize, 1024D) + " KB";
		else if (quotaLeftSize <= 1024*1024*1024)
			return getSize(quotaLeftSize,(1024D*1024D)) + " MB";
		return getSize(quotaLeftSize , (1024D*1024D*1024D)) + " GB";
	}

	public String getBandwithQuotaUsedAsString() {
		if (bandwithQuotaUsed < 1024)
			return String.valueOf(bandwithQuotaUsed) + " B";
		else if (bandwithQuotaUsed <= 1024*1024)
			return getSize(bandwithQuotaUsed, 1024D) + " KB";
		else if (bandwithQuotaUsed <= 1024*1024*1024)
			return getSize(bandwithQuotaUsed,(1024D*1024D)) + " MB";
		return getSize(bandwithQuotaUsed , (1024D*1024D*1024D)) + " GB";
	}

	private String getSize(Long size, Double division){
		Double res = Double.valueOf(size.toString())/division;
		NumberFormat nf = NumberFormat.getFormat("######.#");
		return nf.format(res);
	}

	public long percentOfFreeSpace(){
		return quotaLeftSize*100/(fileSize+quotaLeftSize);
	}
}
